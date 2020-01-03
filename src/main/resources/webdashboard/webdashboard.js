//js code to run the main page of the webdashboard,
//which contains the sub-pages

//The websocket which connects to the java server in the robot code
let socket;

//Tracks whether the websocket has recently received a keepalive message from the server
let keepaliveSent = false;

//Clears the localStorage if it was configured for a previous version of the webdashboard
if (localStorage['pageName'] !== undefined) {
    localStorage.clear();
}

//Open tab based on localStorage so that the webdashboard will open to the page that was last open
openPage(localStorage['page_name'] || "values", document.getElementById((localStorage['page_name'] || "values") + "Tab"));

//Open the page corresponding to a tab when a tab is pressed
function openPage(pageName, element) {
    //Set the current tab in localStorage
    localStorage['page_name'] = pageName;

    //Hide all elements with class="tabcontent" by default
    let tabcontents = document.getElementsByClassName("tabcontent");
    for (let tabcontent of tabcontents) {
        tabcontent.classList.remove('selectedtabcontent')
    }

    //Remove the background color of all tablinks/buttons
    let tablinks = document.getElementsByClassName("tablink");
    for (let tablink of tablinks) {
        tablink.classList.remove('selectedtablink')
        tablink.classList.add('unselectedtablink')
    }

    //Show the specific tab content
    document.getElementById(pageName).classList.add('selectedtabcontent');

    //Add the specific color to the tablink/button used to open the tab content
    if (element !== null) {
        element.classList.remove('unselectedtablink');
        element.classList.add('selectedtablink');
    }
}

//Called when the auto button is pressed
function chooseAuto() {

    //Opens the auto chooser menu
    setVisibility(document.getElementById("autoPopup"), true);
}

//Called when the auto chooser menu is closed
//"send data" is whether auto data should be sent to the robot
function closeChooseAuto(sendData) {
    if (sendData) {
        //If it should send data to the robot

        //Create a new UrlFormData
        socket.send(new UrlFormData()
            //Set the request type to setting auto data
            .append("request", "set_auto_data")
            //Set auto auto_origin, auto_destination, and auto_action based on corresponding selectors in menu
            .append("auto_origin", document.getElementById("autoOriginSelector").value)
            .append("auto_destination", document.getElementById("autoDestinationSelector").value)
            .append("auto_action", document.getElementById("autoActionSelector").value)
            //Turn data into a string to send to the robot code
            .toString());
    }

    //Hide the auto chooser menu
    setVisibility(document.getElementById("autoPopup"), false);
}

//Used to show and hide an element
//"element" is the element
//"visible" is whether the element should be displayed
function setVisibility(element, visible) {
    //Show or hide the element using the visibility property based on the visible variable
    if (visible) {
        element.style.visibility = "visible";
    } else {
        element.style.visibility = "hidden";
    }

    //Get children of the element
    let children = element.children;

    //Loop through and show or hide the element's children using the visibility property based on the visible variable
    for (let c = 0; c < children.length; c++) {
        if (visible) {
            children[c].style.visibility = "visible";
        } else {
            children[c].style.visibility = "hidden";
        }
    }
}

//Code to create, maintain, and reopen a connection with the server in the robot code
function connect() {

    //Create a new websocket with the same host as the page and the page port plus 1 and path "/webdashboard"
    //to connect with the server in the robot code
    socket = new WebSocket("ws://" + window.location.hostname + ":" + (parseInt(window.location.port) + 1) + "/webdashboard");

    //Called when the connection opens
    socket.onopen = function () {

    };

    //Called when the connection closes
    socket.onclose = function () {
        //Set the connection status indicator to red
        //Loop trough each bar of the connection status indicator
        for (let e of document.getElementsByClassName('connectionbar')) {
            //Set bar background color to the --red color specified in style.css
            e.style.background = getComputedStyle(document.body).getPropertyValue('--red');
        }

        //Try to reopen the connection after 1 second
        setTimeout(function () {
            connect();
        }, 1000);
    };

    //Called when a message is received over the connection
    socket.onmessage = function (message) {
        if (message.data === "keepalive") {
            //If message is keepalive set keepaliveSent to false,
            //to tell connection checking code that websocket is connected
            keepaliveSent = false;

            //Set left half of the connection status indicator to green
            //Loop trough each bar of the connection status indicator
            let elements = document.getElementsByClassName('connectionbar');
            for (let e in elements) {
                if (e <= 1) {
                    //If this is one of the first two bars,
                    //set bar background color to the --green color specified in style.css
                    elements[e].style.background = getComputedStyle(document.body).getPropertyValue('--green');
                }
            }
            return;
        }

        //Turn message data into UrlFormData
        let messageData = new UrlFormData(message.data);

        //Case statement on the message response type
        switch (messageData.get("response")) {
            case "auto_data":
                //If message response type is auto_data set the choices in the the selectors of the auto chooser
                //to the choices in the message data

                //Get the list of auto origin choices from the UrlFormData
                let autoOriginList = messageData.get("auto_origin_list").split("~");

                //Clear all choices from the auto origin chooser
                document.getElementById("autoOriginSelector").innerHTML = "";

                //Loop through each auto origin choice
                for (let origin of autoOriginList) {

                    //Add auto origin choice to the auto origin chooser
                    document.getElementById("autoOriginSelector").innerHTML += "<option value=\"" + origin + "\">" + origin + "</option>";
                }

                //Get the list of auto destination choices from the UrlFormData
                let autoDestinationList = messageData.get("auto_destination_list").split("~");

                //Clear all choices from the auto destination chooser
                document.getElementById("autoDestinationSelector").innerHTML = "";

                //Loop through each auto destination choice
                for (let destination of autoDestinationList) {

                    //Add auto destination choice to the auto destination chooser
                    document.getElementById("autoDestinationSelector").innerHTML += "<option value=\"" + destination + "\">" + destination + "</option>";
                }

                //Get the list of auto action choices from the UrlFormData
                let autoActionList = messageData.get("auto_action_list").split("~");

                //Clear all choices from the auto action chooser
                document.getElementById("autoActionSelector").innerHTML = "";

                //Loop through each auto action choice
                for (let action of autoActionList) {

                    //Add auto action choice to the auto action chooser
                    document.getElementById("autoActionSelector").innerHTML += "<option value=\"" + action + "\">" + action + "</option>";
                }
                break;

            case "connected":
                //If message response type is connected,
                //this means that the connection status with the server in the robot code has changed

                if (messageData.get("connected") === 'true') {
                    //Connection with the server in the robot code is working properly

                    //Set the connection status indicator to green
                    //Loop trough each bar of the connection status indicator
                    for (let e of document.getElementsByClassName('connectionbar')) {
                        //Set bar background color to the --green color specified in style.css
                        e.style.background = getComputedStyle(document.body).getPropertyValue('--green');
                    }
                } else {
                    //Set the right half of the connection status indicator to red
                    //Loop trough each bar of the connection status indicator
                    let elements = document.getElementsByClassName('connectionbar');
                    for (let e in elements) {
                        if (e > 1) {
                            //If this is one of the last two bars,
                            //set bar background color to the --red color specified in style.css
                            elements[e].style.background = getComputedStyle(document.body).getPropertyValue('--red');
                        }
                    }
                }

                //Set the selected auto to undefined in sessionStorage so that it will be updated by the values page
                sessionStorage["si_selected_auto"] = "undefined";
                break;
        }
    };
}

//Call connect to initiate a connection with the server in the robot code
connect();

//Check every 5 seconds to confirm the websocket is still connected with the robot code
setInterval(function () {
    if (keepaliveSent === true) {
        //If a keepalive hasn't been received from the robot in the past 5 seconds,
        //assume the connection is dead and close the socket
        socket.onclose();
    }
    if (socket !== undefined) {
        //If socket exists then send a keepalive to make sure the socket is still connected
        socket.send("keepalive");

        //Set keepaliveSent to true so that it can be checked
        //on the next cycle to make sure the socket is still connected
        keepaliveSent = true;
    }
}, 5000);

//Set the selected auto in sessionStorage to undefined to start, will be updated by the values page later
sessionStorage["si_selected_auto"] = "undefined";

//Update the selected auto text in the auto button every 500 milliseconds
setInterval(function () {
    if (sessionStorage["si_selected_auto"] !== "undefined") {
        //If sessionStorage contains the selected auto, use it to set the auto text in the auto button
        document.getElementById("autoButton").innerText = sessionStorage["si_selected_auto"];
    } else {
        //If sessionStorage doesn't contain the selected auto, set the auto text in the auto button to "No Auto"
        document.getElementById("autoButton").innerText = "No Auto";
    }
}, 500);