//js code to run the log page of the webdashboard,
//which has a console with messages from the logger in the robot code

//The websocket which connects to the java server in the robot code
let socket;

//The index of the current highlight, changed by using up and down arrows
let selectedHighlightIndex = 0;

//The total number of highlights
let numHighlights = 0;

//Called when the clear log button is pressed,
//clears all text from the textArea
function clearLog() {

    //Sets the innerHTML of the textArea to "" which removes everything from it
    document.getElementById("textArea").innerHTML = "";
}

//Searches through the textArea to find matches to the value in the searchField
function searchUpdate() {
    //Creates an highlight id counter so each highlight will have its own id
    let highlightID = -1;

    //Gets the searchField value from the html page
    let search = document.getElementById("searchField").value;

    //Encodes the search value so that the find and replace will work with special characters
    search = search.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');

    //Get the textArea
    let textArea = document.getElementById("textArea");

    if (search !== "") {
        //If the search value is not empty

        //Create a regex variable for the find and replace
        let searchRegex;

        if (document.getElementById("matchCaseCheckbox").checked) {
            //If the matchCase checkbox is checked create a case dependent regex
            searchRegex = new RegExp(search, "g");
        } else {
            //If the matchCase checkbox is checked create a case independent regex
            searchRegex = new RegExp(search, "gi");
        }

        //Get the showMatching value from the showMatching checkbox
        let showMatching = document.getElementById("showMatchingCheckbox").checked;

        //Loop through each line in the textArea
        for (let div of textArea.children) {

            //Get the plain message text from the div id
            let message = div.id;

            //Create a boolean to track whether the message has a match
            let hasMatch = false;

            //Replace each instance
            div.innerHTML = message.replace(searchRegex, function (matchingText) {
                //This function is only if the line has matches so set hasMatch to true
                hasMatch = true;

                //Increment highlightID so the highlights have ascending IDs
                highlightID++;

                //Put a highlighting span around the matching text,
                //with the css class highlight, and the id "highlight" plus a number
                return "<span class='highlight' id='highlight" + highlightID + "'>" + matchingText + "</span>";
            });

            if (showMatching && !hasMatch) {
                //If showMatching is true and the line doesn't have any matches hide the line
                div.classList.add("hidden");
            } else {
                //If showMatching is false or the line has matches display the line
                div.classList.remove("hidden");
            }
        }
    } else {
        //If the search value is empty

        //Loop through each line in the textArea
        for (let div of textArea.children) {
            //Set the line text to the id,
            //Because the id is just the plain message text this will remove all highlighting
            div.innerHTML = div.id;

            //Since there is no search, nothing should be hidden, so make sure the text visible
            div.classList.remove("hidden");
        }
    }

    //Set the total number of highlights
    numHighlights = highlightID + 1;

    if (numHighlights > 0) {
        //If there are one or more highlights then update the selected highlight

        if (selectedHighlightIndex < 0) {
            //If the selected highlight is less than 0,
            // cycle back to the bottom/last highlight
            selectedHighlightIndex = numHighlights - 1;
        }
        if (selectedHighlightIndex >= numHighlights) {
            //If the selected highlight is greater than the total number of highlights,
            //cycle back to the top/first highlight
            selectedHighlightIndex = 0;
        }

        //Get the selected highlight
        let highlight = document.getElementById("highlight" + selectedHighlightIndex);

        //Set the background of the selected highlight to the --red color specified in style.css
        highlight.style.background = "var(--red)";

        //Scroll the selected highlight into the center of the textArea so it is easy to find
        highlight.scrollIntoView({
            behavior: 'auto',
            block: 'center',
            inline: 'center'
        });
    }
}

//Called anytime a checkbox changes,
//to store the checkbox values into localStorage so they will persist from load to load
function checkboxUpdate() {
    //Sets the autoscrollCheckbox in localStorage based on the autoscroll checkbox value
    localStorage['autoscrollCheckbox'] = document.getElementById("autoscrollCheckbox").checked;

    //Sets the matchCaseCheckbox in localStorage based on the matchCase checkbox value
    localStorage['matchCaseCheckbox'] = document.getElementById("matchCaseCheckbox").checked;

    //Sets the showMatchingCheckbox in localStorage based on the showMatching checkbox value
    localStorage['showMatchingCheckbox'] = document.getElementById("showMatchingCheckbox").checked;

    //Updates search based on new checkbox values
    searchUpdate();
}

//Save is called when the saveButton is pressed,
//it uses the download method to download the textArea content as a text file
function save() {
    //Create an empty data string which all the messages will be added to
    let data = "";

    //Get the textArea from the html page
    let textArea = document.getElementById("textArea");

    //Loop through each div in the textArea
    for (let div of textArea.children) {
        //Add the message in the div to the data string
        data += div.id + "\n";
    }

    //Get the current datetime
    let currentDatetime = new Date();

    //Make a readable datetime string
    let datetimeString = currentDatetime.getDate() + "-"
        + (currentDatetime.getMonth() + 1) + "-"
        + currentDatetime.getFullYear() + " "
        + currentDatetime.getHours() + "-"
        + currentDatetime.getMinutes() + "-"
        + currentDatetime.getSeconds();

    //Download the file with the file data, name, and type
    download(data, "log[" + datetimeString + "]", "text/plain")
}

//Downloads a file onto the computers file system
//"data" is the data to be put into the file
//"filename" is the name of the file to be downloaded
//"type" is the type of the file to be downloaded
function download(data, filename, type) {

    //Create the file to be downloaded with data and type
    let file = new Blob([data], {type: type});

    if (window.navigator.msSaveOrOpenBlob) {
        //If browser is Internet Explorer download the file using msSaveOrOpenBlob
        window.navigator.msSaveOrOpenBlob(file, filename);
    } else {
        //If browser isn't Internet Explorer

        //Create a link
        let link = document.createElement("a");

        //Create a url that points to the file to allow it to be downloaded
        let url = URL.createObjectURL(file);

        //Set the url to the link so when it is pressed it will download the file
        link.href = url;

        //Set the download name
        link.download = filename;

        //Add the link to the page
        document.body.appendChild(link);

        //Automatically click the link to download the file
        link.click();

        setTimeout(function () {
            //Create a new thread to remove the link from the page
            document.body.removeChild(link);

            //Clear the url
            window.URL.revokeObjectURL(url);
        }, 0);
    }
}

//Called on page load to configure the page using values from localStorage
function setup() {
    if (localStorage['autoscrollCheckbox'] !== undefined) {
        //If the autoscrollCheckbox is not undefined, use it to set whether the autoscroll checkbox will start checked
        document.getElementById("autoscrollCheckbox").checked = (localStorage['autoscrollCheckbox'] === "true");
    }
    if (localStorage['matchCaseCheckbox'] !== undefined) {
        //If the matchCaseCheckbox is not undefined, use it to set whether the matchCase checkbox will start checked
        document.getElementById("matchCaseCheckbox").checked = (localStorage['matchCaseCheckbox'] === "true");
    }
    if (localStorage['showMatchingCheckbox'] !== undefined) {
        //If the showMatchingCheckbox is not undefined, use it to set whether the showMatching checkbox will start checked
        document.getElementById("showMatchingCheckbox").checked = (localStorage['showMatchingCheckbox'] === "true");
    }
}

//Code to create, maintain, and reopen a connection with the server in the robot code
function connect() {

    //Create a new websocket with the same host as the page and the page port plus 1 and path "/log"
    //to connect with the server in the robot code
    socket = new WebSocket("ws://" + window.location.hostname + ":" + (parseInt(window.location.port) + 1) + "/log");

    //Called when the connection opens
    socket.onopen = function () {

    };

    //Called when the connection closes
    socket.onclose = function () {

        //Try to reopen the connection after 1 second
        setTimeout(function () {
            connect();
        }, 1000);
    };

    //Called when a message is received over the connection
    socket.onmessage = function (message) {

        //Turn message data into UrlFormData
        let messageData = new UrlFormData(message.data);

        //Case statement on the message response type
        switch (messageData.get("response")) {
            case "log":
                //If message response type is log, put the line send into the textArea

                //Get log message text from the UrlFormData
                let message = messageData.get("message");

                //Get the textArea from the html page
                let textArea = document.getElementById("textArea");

                //Create a dive for the log message
                let div = document.createElement("DIV");

                //Set the id of the div to the message,
                //so that an unmodified version of the message can be accessed for searching later
                div.id = message;

                //Add the textLine css class to the div so it is styled correctly
                div.classList.add("textLine");

                if (messageData.get("type") === "ERROR") {
                    //If the message is an error message,
                    //add the errorLine css class to the div so it is styled correctly
                    div.classList.add("errorLine");
                }

                //Set the innerText of the div to the message, so that it is displayed on the page
                div.innerText = message;

                //Add the div with the message to the textArea
                textArea.appendChild(div);

                if (document.getElementById("autoscrollCheckbox").checked) {
                    //If the autoscroll checkbox is checked scroll the textArea to the bottom
                    textArea.scrollTop = textArea.scrollHeight;
                }

                //Update the search with the new message
                searchUpdate();
                break;
        }
    };
}

//Call the setup function when the page loads
onload = setup;

//Add a keydown lister so that the up and down arrows can move the selected highlight
document.addEventListener('keydown', function (event) {
    if (event.key === "ArrowUp") {
        //If the up arrow was pressed decrement the currentHighlightIndex to move the selected highlight up one
        selectedHighlightIndex--;

        //Update the search with the new highlight index
        searchUpdate();
    } else if (event.key === "ArrowDown") {
        //If the up arrow was pressed increment the currentHighlightIndex to move the selected highlight down one
        selectedHighlightIndex++;

        //Update the search with the new highlight index
        searchUpdate();
    }
});

//Call connect to initiate a connection with the server in the robot code
connect();

//Check every second to confirm the websocket is still connected with the robot code
setInterval(function () {
    if (socket !== undefined) {
        //If socket exists then send a keepalive to make sure the socket is still connected
        socket.send("keepalive");
    }
}, 1000);
