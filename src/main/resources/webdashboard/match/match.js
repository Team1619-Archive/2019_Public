//js code to run the log match of the webdashboard,
//which has camera views and displays values sent from the robot code

//The websocket which connects to the java server in the robot code
let socket;

const independent = window.location === window.parent.location;

//Whether the page is currently connected to the robot
let connected = false;

//The number of times the page should blink when the page disconnects
const numBlinks = 15;

//Whether the page background is currently flashing to indicate disconnecting from the robot code
let blinking = false;

//The current repeat count for the page blinking to indicate disconnecting from the robot code
let repeat = 0;

//Call when a double click occurs any where on the page
function chooseAuto() {

    if (independent) {
        //If page is independent from the rest of the webdashboard show the auto chooser menu
        setVisibility(document.getElementById("autoPopup"), true);
    }
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

//Sets a display value on html page
//"name" is the name that will be displayed
//"value" is the value that will be displayed
function setValue(name, value) {

    //Create a unique id based on the name so that the value can be accessed and updated
    let id = "value__" + name.split(" ").join("__");


    if ($("#" + id)[0] === undefined) {
        //If an element with the id of this value doesn't exist create a new one

        //Create a div to display the name and value
        let valueBox = document.createElement("DIV");

        //Add the value css class to the div
        valueBox.classList.add("value");

        //Set the id of the div so the value can be accessed and updated
        valueBox.id = id;

        //Add the value display to the valuesContainer
        document.getElementById("valuesContainer").appendChild(valueBox);
    }

    //Set the text of the element with the id matching this value display's with the name and value so they are displayed
    $("#" + id)[0].innerText = name + ": " + value;
}

//Sets a booleanBox on html page
//"name" is the name that will be displayed
//"value" is the value of the boolean that will be displayed
function setBooleanBox(name, value) {

    //Create a unique id based on the name so that the value can be accessed and updated
    let id = "boolean__" + name.split(" ").join("__");

    if ($("#" + id)[0] === undefined) {
        //If an element with the id of this value doesn't exist create a new one

        //Create a booleanBox to display the name and boolean value
        let booleanBox = document.createElement("DIV");

        //Add the booleanBox css class to the booleanBox
        booleanBox.classList.add("booleanBox");

        //Set the text in the booleanBox to the booleans name
        booleanBox.innerText = name;

        //Set the id of the booleanBox so the boolean can be accessed and updated
        booleanBox.id = id;

        //Add the booleanBox display to the valuesContainer
        document.getElementById("valuesContainer").appendChild(booleanBox);
    }

    if (value == "true") {
        //If the boolean is true, set the booleanBox background to the --green color specified in style.css
        $("#" + id)[0].style.background = "var(--green)";
    } else if (value == "false") {
        //If the boolean is false, set the booleanBox background to the --red color specified in style.css
        $("#" + id)[0].style.background = "var(--red)";
    } else {
        //If the boolean has no value, set the booleanBox background to the --grey color specified in style.css
        $("#" + id)[0].style.background = "var(--grey)";
    }
}

//Sets a dial on html page
//"name" is the name that will be displayed
//"min" is the minimum value the double can be
//"max" is the maximum value the double can be
//"value" is the value of the double that will be displayed
function setDial(name, min, max, value) {

    //Create a unique id based on the name so that the value can be accessed and updated
    let id = "dial__" + name.split(" ").join("__");

    if ($("#" + id)[0] === undefined) {
        //If an element with the id of this value doesn't exist create a new one

        //Create a div container to hold the dial
        let container = document.createElement("DIV");

        //Add the dialContainer css class the container
        container.classList.add("dialContainer");

        //Create a label div to display the name of the dial
        let label = document.createElement("DIV");

        //Add the dialLabel css class to the label
        label.classList.add("dialLabel");

        //Set the text of the label to the name of the dial
        label.innerText = name;

        //Add the label to the dial container
        container.appendChild(label);

        //Create the dial
        let dial = document.createElement("INPUT");

        //Add the dial and unselectable css classes to the dial
        dial.classList.add("dial");
        dial.classList.add("unselectable");

        //Set the id of the dial so the value can be accessed and updated
        dial.id = id;

        //Add the dial to the dial container
        container.appendChild(dial);

        //Add the dial container to the page
        document.getElementById("valuesContainer").appendChild(container);

        //Configure the dial
        $("#" + id).knob({
            //Show the dial's value as a number below the dial
            displayInput: true,

            //Set the font weight of the dial's value
            fontWeight: 2,

            //Set the width of the dial
            width: 100,

            //Set the height the dial
            height: 50,

            //Set the offset of the dial so that the dial goes from 180 to 0 on the unit circle
            angleOffset: -90,

            //Set the dial to have a 180 degree range
            angleArc: 180,

            //Don't allow the use to change the dial's value
            readOnly: true,

            //Set the step size of the dial to 0.1, this is the amount of decimal places that will be displayed
            step: 0.1,

            //The min and max allow for the dial to have the correct scale for the value being displayed
            //Set the min value of the dial
            min: min,

            //Set the min value of the dial
            max: max,

            //Set the shaded color of the dial to the --foreground color specified in style.css
            fgColor: getComputedStyle(document.documentElement).getPropertyValue('--foreground'),

            //Set the non-shaded color of the dial to the --white color specified in style.css
            inputColor: getComputedStyle(document.documentElement).getPropertyValue('--white')
        });
    }

    //Set the value of the dial and trigger a change event so the dial display updates
    $("#" + id).val(value).trigger('change');

    //Due to an oddity in the dial code, set the top margin of the dial so that it fits correctly in the valuesContainer
    $("#" + id)[0].style.marginTop = "0%";
}

//Recursive which blinks the page red to indicate disconnecting from the robot code
//Only runs if the page is independent from the rest of the webdashboard
function blink() {
    if (connected) {
        //If the socket is connected to the robot code

        //Set the repeat count to 0 for the next blink cycle
        repeat = 0;

        //Set the background of the page to the standard --background color specified in style.css
        document.body.style.background = "var(--background)";

        //Set the background of the valuesContainer to the standard --background color specified in style.css
        document.getElementById("valuesContainer").style.background = "var(--background)";
        return;
    }

    if (repeat > numBlinks) {
        //If the page has blinked the correct amount of times for this blink cycle stop the blinking

        //Set the repeat count to 0 for the next blink cycle
        repeat = 0;

        //Set the background of the page to the --red color specified in style.css
        document.body.style.background = "var(--red)";

        //Set the background of the valuesContainer to the --red color specified in style.css
        document.getElementById("valuesContainer").style.background = "var(--red)";
        return;
    }

    //The page is currently blinking to prevent it from running two blink cycles at the same time
    blinking = true;

    //Alternate colors each run to create a blinking effect
    if (repeat % 2 === 0) {
        //If repeat is even this run

        //Set the background of the page to the --red color specified in style.css
        document.body.style.background = "var(--red)";

        //Set the background of the valuesContainer to the --red color specified in style.css
        document.getElementById("valuesContainer").style.background = "var(--red)";
    } else {
        //If repeat is odd this run

        //Set the background of the page to the standard --background color specified in style.css
        document.body.style.background = "var(--background)";

        //Set the background of the valuesContainer to the standard --background color specified in style.css
        document.getElementById("valuesContainer").style.background = "var(--background)";
    }

    //Increment the repeat counter for the next run
    repeat++;

    //Do the next run in 100 milliseconds
    setTimeout(blink, 100);
}

//Code to create, maintain, and reopen a connection with the server in the robot code
function connect() {

    //Create a new websocket with the same host as the page and the page port plus 1 and path "/log"
    //to connect with the server in the robot code
    socket = new WebSocket("ws://" + window.location.hostname + ":" + (parseInt(window.location.port) + 1) + "/match");

    //Called when the connection opens
    socket.onopen = function () {

        if (independent) {
            //If page is independent handle the blinking state

            //The page in now connected
            connected = true;

            //Stop disconnect blinking because the page is now connected
            blinking = false;

            //Set the document background to the --background color specified in style.css
            document.body.style.background = "var(--background)";

            //Set the valuesContainer background to the --background color specified in style.css
            document.getElementById("valuesContainer").style.background = "var(--background)";
        }
    };

    //Called when the connection closes
    socket.onclose = function () {
        if (independent) {
            //If page is independent handle the blinking state

            //The page is now disconnected
            connected = false;

            if (!blinking) {
                //If the page isn't current blinking start it blinking to tell the user the page disconnected
                blink();
            }
        }

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
            case "match_values":
                //If the message type is match_values, display the values on the html page

                //Get the values string from the message data
                let values = messageData.get("values").split("~");

                //Loop through each value in values
                for (let v of values) {

                    //Split up the value string into its parts
                    let parts = v.split("*");

                    if (parts[0] === "value") {
                        //If the value type is a display value then call setValue with the data
                        setValue(parts[1], parts[2])
                    } else if (parts[0] === "boolean") {
                        //If the value type is a boolean box then call setBooleanBox with the data
                        setBooleanBox(parts[1], parts[2]);
                    } else if (parts[0] === "dial") {
                        //If the value type is a dial then call setDial with the data
                        setDial(parts[1], parseFloat(parts[3]), parseFloat(parts[4]), parseFloat(parts[2]));
                    }
                }
                break;

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
        }
    };
}

//Call connect to initiate a connection with the server in the robot code
connect();

//Check every second to confirm the websocket is still connected with the robot code
setInterval(function () {
    if (socket !== undefined) {
        //If socket exists then send a keepalive to make sure the socket is still connected
        socket.send("keepalive");
    }
}, 1000);

if (independent) {
    //If the page is independent setup blinking

    //Page is currently not connected because it just loaded
    connected = false;

    //Blink the page so the user knows the page is not connected
    blink();
}

//Update camera connections every seconds
setInterval(function () {
    $("#mainCamera")[0].style.backgroundImage = "http://10.16.19.2:1181/?action=stream";
    $("#secondaryCamera1")[0].style.backgroundImage = "http://10.16.19.101:5800";
    $("#secondaryCamera2")[0].style.backgroundImage = "http://10.16.19.102:5800";
}, 1000);