//js to configure the values page of the webdashboard,
//which has a table for viewing values in the robot code's input and output service,
//an area for the user to put values they want to see,
//allows the user to change values in sim mode,
//and handles gamepad input in sim mode

//The websocket which connects to the java server in the robot code
let socket;

//Array of values in the display table
let tableValues = [];

//The type of the value currently being updated
let valueUpdateType = "";

//The name of the value currently being updated
let valueUpdateName = "";

//The value of the value currently being updated
let valueUpdateValue = "";

//A boolean tracking whether a value update menu is open
let valueMenuOpen = false;

//A map of value names to value popouts
let popoutMap = new Map();

//Opens the main menu, called when a double click occurs anywhere on the page
function openMenu() {
    if (valueMenuOpen) {
        //If the value editing menu is already open return so this menu won't open on top of the values editing menu
        return;
    }

    //Get the driverSelector element from the html page
    let driverSelector = document.getElementById("driverSelector");

    //Get the operatorSelector element from the html page
    let operatorSelector = document.getElementById("operatorSelector");

    //Clear the driverSelector
    driverSelector.innerHTML = "";

    //Clear the operatorSelector
    operatorSelector.innerHTML = "";

    //Add the "Nothing" and "Keyboard" options to the driverSelector
    driverSelector.innerHTML += "<option value=\"" + -1 + "\">0 - Nothing</option>";
    driverSelector.innerHTML += "<option value=\"" + -2 + "\">1 - Keyboard</option>";

    //Add the "Nothing" and "Keyboard" options to the operatorSelector
    operatorSelector.innerHTML += "<option value=\"" + -1 + "\">0 - Nothing</option>";
    operatorSelector.innerHTML += "<option value=\"" + -2 + "\">1 - Keyboard</option>";

    //Set the gamepad number to 2, accounting for the "Nothing" and "Keyboard" options
    let gamepadNumber = 2;

    //Get the gamepad objects from the js gamepad api
    let gamepads = navigator.getGamepads();

    //Loop through each of the gamepads
    for (let g = 0; g < gamepads.length; g++) {
        try {

            //Get the gamepad object from the array
            let gamepad = gamepads[g];

            if (gamepad === null) {
                //If the gamepad is null move onto the next iteration immediately of the loop,
                //so that it isn't added to the selector
                continue;
            }

            if (gamepad.id.toLowerCase().includes('x')) {
                //If the gamepad name includes an x as in xbox then add it to the selector

                //Add the gamepad number and name to the driverSelector
                driverSelector.innerHTML += "<option value=\"" + g + "\">" + gamepadNumber + " - " + gamepad.id + "</option>";

                //Add the gamepad number and name to the driverSelector
                operatorSelector.innerHTML += "<option value=\"" + g + "\">" + gamepadNumber + " - " + gamepad.id + "</option>";

                //Increment the gamepad number
                gamepadNumber++;
            }
        } catch (e) {
            //If something weird happened with a gamepad skip it and keep going
        }
    }

    //Set the menu html element to be visible on the page
    setVisibility(document.getElementById("menuPopup"), true);
}

//Closes the main menu, called when the close button on the menu is pressed
function closeMenu() {
    //Hide the menu element on the html page
    setVisibility(document.getElementById("menuPopup"), false);

    //Send FMS mode data to the robot code
    socket.send(new UrlFormData().append("request", "set_fms_mode").append("mode", document.getElementById("fmsSelector").value).toString());
}

//Opens the value update menu, called when a click occurs on a value name
function openUpdateValue(type, name, value) {

    //Set valueMenuOpen to true so the main menu won't open over it
    valueMenuOpen = true;

    //Set the global update value type to the type of the value
    valueUpdateType = type;

    //Set the global update value name to the name of the value
    valueUpdateName = name;

    //Set the global update value to the value of the value
    valueUpdateValue = value;

    //Set the menu title to the name of the value
    document.getElementById('valueDisplay').innerText = name;

    if (type === "output") {
        //If the value is an output

        //Hide the input field because the user can't update an output
        document.getElementById('valueInput').style.display = "none";

        //Hide the vector selector because this isn't a vector
        document.getElementById('vectorInputSelector').style.display = "none";
    } else if (type === "vector") {
        //If the value is an vector

        //Get the parts of the vector
        let values = value.split("\n");

        //Clear the vector selector
        document.getElementById("vectorInputSelector").innerHTML = "";

        //Loop through the parts of the vector
        for (let v of values) {

            //Get the value to put into the vector selector
            let val = v.split(": ")[0];

            //Put the value into the vector selector
            document.getElementById("vectorInputSelector").innerHTML += "<option value=\"" + val + "\">" + val + "</option>";
        }

        //Show the input field so the user can enter a new value
        document.getElementById('valueInput').style.display = "block";

        //Show the vector selector because this is a vector
        document.getElementById('vectorInputSelector').style.display = "block";
    } else {
        //If the value is an a numeric or boolean

        //Show the input field so the user can enter a new value
        document.getElementById('valueInput').style.display = "block";

        //Hide the vector selector because this isn't a vector
        document.getElementById('vectorInputSelector').style.display = "none";
    }

    if (type === "boolean") {
        //If the value is a boolean autofill the input with the opposite value
        document.getElementById('valueInput').value = (value.trim() === "false");
    } else if (type === "numeric" || type === "vector") {
        //If the value is a numeric or vector autofill the input with 0.0
        document.getElementById('valueInput').value = "0.0";
    } else {
        //If the value is an output clear the input
        document.getElementById('valueInput').value = "";
    }

    //Call on value change so the submit button will have the correct formatting to start
    onValueChange();

    //Display the value update menu
    setVisibility(document.getElementById("valuePopup"), true);

    //Select the value input automatically
    document.getElementById('valueInput').focus();
    document.getElementById('valueInput').select();
}

//Closes the value update menu, called when the close button on the value menu is pressed
function closeUpdateValue(sendData) {

    //Set valueMenuOpen to false so the main menu can open
    valueMenuOpen = false;

    if (sendData && valueUpdateType !== 'output') {
        //If the data should be send and the value isn't an output,
        //send the data to the robot code with the new value from the input in the value update menu
        sendUpdateValue(valueUpdateType, valueUpdateName, document.getElementById("valueInput").value);
    }

    //Hide the value update menu
    setVisibility(document.getElementById("valuePopup"), false)
}

//Called whenever the value in the value update menu changes,
//determines whether the input is valid and disables the submit button if it isn't
function onValueChange() {

    if ((valueUpdateType === 'numeric' || valueUpdateType === 'vector') &&
        (!parseFloat(document.getElementById("valueInput").value) &&
            document.getElementById("valueInput").value != 0)) {
        //If the value is a numeric or vector and the value is not a valid double

        //Set the background of the updateButton to the --grey color specified in style.css
        document.getElementById('updateButton').style.background = 'var(--grey)';

        //Set the updateButton to be disabled so the value can't be submitted
        document.getElementById('updateButton').disabled = true;

    } else if (valueUpdateType === 'boolean' &&
        !(document.getElementById("valueInput").value.toLowerCase() === 'true' ||
            document.getElementById("valueInput").value.toLowerCase() === 'false')) {
        //If the value is a boolean and the value is not a valid boolean,

        //Set the background of the updateButton to the --grey color specified in style.css
        document.getElementById('updateButton').style.background = 'var(--grey)';

        //Set the updateButton to be disabled so the value can't be submitted
        document.getElementById('updateButton').disabled = true;

    } else if (valueUpdateType === 'output') {
        //If the value is an output the value should never be updated

        //Set the background of the updateButton to the --grey color specified in style.css
        document.getElementById('updateButton').style.background = 'var(--grey)';

        //Set the updateButton to be disabled so the value can't be submitted
        document.getElementById('updateButton').disabled = true;
    } else {
        //The value is good

        //Set the background of the updateButton to the --green color specified in style.css
        document.getElementById('updateButton').style.background = 'var(--green)';

        //Set the updateButton to be enabled so the value can be submitted
        document.getElementById('updateButton').disabled = false;
    }
}

//Calls toggleGraph on the value value and closes the value menu,
//called when the toggleGraph button on the value menu is pressed
function onToggleGraph() {

    //Toggle if the value is being displayed on the graph
    toggleGraph();

    //Close the value menu
    closeUpdateValue(false);
}

//Toggles a value on the graph
function toggleGraph() {
    if (valueUpdateType === 'vector') {
        //If the value is a vector

        //Get the current graphValues data from sessionStorage
        let graphValues = JSON.parse(sessionStorage['graph_values']);

        if (graphValues[valueUpdateName] === undefined) {
            //If the vector isn't in graphValues open a menu so the user can select what they what to use for x and y
            openVectorGraphingMenu();
        } else {
            //If the vector is already in graphValues remove it

            //Removed the vector from graphValues so it will no longer be graphed
            graphValues[valueUpdateName] = undefined;

            //Write the new graph values into sessionStorage so the graph will show the changes
            sessionStorage['graph_values'] = JSON.stringify(graphValues);
        }
    } else if (valueUpdateType !== 'string') {
        //If the value is a type that can be graphed over time (numeric or boolean)

        //Get the current graphValues data from sessionStorage
        let graphValues = JSON.parse(sessionStorage['graph_values']);

        if (graphValues[valueUpdateName] === undefined) {
            //If the value isn't in graphValues add it

            //Set the graphValues data for the value
            graphValues[valueUpdateName] = {

                //No data yet will be updated when new values are sent from the robot code
                value: null,

                //Set updated to false so the graph page won't graph the value until data has come from the robot code
                updated: false
            };

            //Sends a request for all robot code values so the value is graphed immediately even if it doesn't change
            socket.send(new UrlFormData().append("request", "all_values").toString());
        } else {
            //If the value is already in graphValues remove it

            //Removed the value from graphValues so it will no longer be graphed
            graphValues[valueUpdateName] = undefined;
        }

        //Write the new graph values into sessionStorage so the graph will show the changes
        sessionStorage['graph_values'] = JSON.stringify(graphValues);
    }
}

//Opens a menu so the user can select what parts of a vector they what to use for x and y when graphing
function openVectorGraphingMenu() {

    //Set the menu title to the name of the vector
    document.getElementById("vectorDisplay").innerText = valueUpdateName;

    //Get all the parts of the vectors value from
    let values = valueUpdateValue.split("\n");

    //Clears all selections from the x and y selectors
    document.getElementById("vectorGraphingX").innerHTML = "";
    document.getElementById("vectorGraphingY").innerHTML = "";

    //Loop through each of the values
    for (let v of values) {

        //Get the name of the value
        let value = v.split(": ")[0];

        //Add the value's name to the x and y selectors
        document.getElementById("vectorGraphingX").innerHTML += "<option value=\"" + value + "\">" + value + "</option>";
        document.getElementById("vectorGraphingY").innerHTML += "<option value=\"" + value + "\">" + value + "</option>";
    }

    //Show the menu on the html page
    setVisibility(document.getElementById("vectorGraphingPopup"), true);
}

//Closes the vector graphing menu
//"send data" is whether the vector should be graphed
function closeVectorGraphingMenu(graphVector) {

    //Hide the vector graphing menu
    setVisibility(document.getElementById("vectorGraphingPopup"), false);
    if (graphVector) {
        //If the vector should be graphed

        //Get the graphValues from sessionStorage
        let graphValues = JSON.parse(sessionStorage['graph_values']);

        //Set the graphValues data for the vector
        graphValues[valueUpdateName] = {
            //No data yet will be updated when new values are sent from the robot code
            value: null,

            //Set updated to false so the graph page won't graph the value until data has come from the robot code
            updated: false,

            //The name of the x dataset,
            //so the code that updates the values knows which vector value one to use for the x dataset
            x: document.getElementById("vectorGraphingX").value,

            //The name of the y dataset,
            //so the code that updates the values knows which vector value one to use for the y dataset
            y: document.getElementById("vectorGraphingY").value
        };

        //Write the new graph values into sessionStorage so the graph will show the changes
        sessionStorage['graph_values'] = JSON.stringify(graphValues);

        //Sends a request for all robot code values so the value is graphed immediately even if it doesn't change
        socket.send(new UrlFormData().append("request", "all_values").toString());
    }
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

//Called when the user presses the popout button on the value menu
function onCreateDraggableViewer() {

    //Create a new draggable viewer with the name of the value
    createDraggableViewer(document.getElementById('valueDisplay').innerText);

    //Close the value menu
    closeUpdateValue(false);
}

function createDraggableViewer(name, left, top) {

    //Create the viewer element
    let div = document.createElement("DIV");

    //Set the id as the name of the value so that it can be identified later
    div.id = name;

    //Set the text of the viewer to the name of the value
    div.innerHTML = name + ": ";

    //Add the draggableViewer css class
    div.classList.add("draggableViewer");

    //Add the unselectable css class so that the text won't highlight when you are trying to drag the element
    div.classList.add("unselectable");

    div.ondblclick = function () {
        //Add handler to remove viewer on double click

        //Set the menu popup to be invisible so it won't appear on double click
        document.getElementById("menuPopup").style.display = 'none';

        //Remove the element from the html page
        document.getElementById("dragContainer").removeChild(div);

        //Remove the element from the popout map
        popoutMap.delete(name);

        //Store the popout values in localstorage
        storePopouts();

        setTimeout(function () {
            //Wait a millisecond to make sure the open menu function is done running

            //Close the menu which opened on double click
            closeMenu();

            //Set the menu popup to be visible so next time it is open it will appear
            document.getElementById("menuPopup").style.display = 'block';
        }, 1);
    };

    //Add the viewer to the draggable viewer container
    document.getElementById("dragContainer").appendChild(div);

    //Make the viewer draggable
    makeDraggable(div);

    //Add the viewer to the popout map so it can be accessed later
    popoutMap.set(name, div);

    //Loop through the rows in the values table
    for (let row of document.getElementById("valueTableBody").rows) {
        if (row.cells[0].innerText === name) {
            //If the text of the name cell matches the name of the value,
            //then set the popout value to the name and value of the row
            popoutMap.get(row.cells[0].innerText).innerText = row.cells[0].innerText + ":\n" + valueUpdateValue;
            break;
        }
    }

    if (left !== undefined) {
        //If a left edge position value was passed in then set the viewer's left edge position
        div.style.left = left;
    }

    if (top !== undefined) {
        //If a top edge position value was passed in then set the viewer's top edge position
        div.style.top = top;
    }

    //Store popouts in localStorage so if the page is refreshed they will persist
    storePopouts();
}

//Stores popout values in localStorage so the persist through page load
function storePopouts() {

    //Create an array where the data will be put before being be put into localStorage
    let popouts = [];

    //Loop through the popout values
    for (let popout of popoutMap.entries()) {

        //Get the name from the popout key
        let name = popout[0];

        //Get the x and y from the popout value
        let xPosition = popout[1].style.left || document.body.clientWidth / 2 + "px";
        let yPosition = popout[1].style.top || "0px";

        //Add the name xPosition and yPosition to the data string
        popouts.push(name + "," + xPosition + "," + yPosition);
    }

    //Write the popout string into localStorage
    localStorage["popouts"] = popouts.join("*");
}

//Makes an html element draggable
//"element" the element to make draggable
function makeDraggable(element) {
    element.onmousedown = function (event) {
        //When the mouse presses on the element

        //Prevent what the mouse press would otherwise do
        event.preventDefault();

        //Set the element's positioning mode to absolute so that it can be move to specific coordinates
        element.style.position = 'absolute';

        //Moves the center of the element to a specific locations
        //"xPosition" where the horizontal center of the element should be
        //"yPosition" where the vertical center of the element should be
        function moveElementTo(xPosition, yPosition) {

            if (xPosition < (document.getElementById("valueTable").offsetWidth + element.offsetWidth / 2) + 20) {
                //If the mouse moves over the values table keep the elements x within the popout container
                xPosition = (document.getElementById("valueTable").clientWidth + element.offsetWidth / 2) + 20;
            }

            //Set the left side of the element to the x position minus the element width divided by 2,
            //so the horizontal center of the element ends at the x position
            element.style.left = xPosition - element.offsetWidth / 2 + 'px';

            //Set the left side of the element to the y position minus the element height divided by 2,
            //so the vertical center of the element ends at the y position
            element.style.top = yPosition - element.offsetHeight / 2 + 'px';

            //Store popouts in localStorage so if the page is refreshed they will persist
            storePopouts();
        }

        //Moves the element with the cursor, called when the mose is released
        function onMouseMove(event) {

            //Move the center of the element to the cursor location so it follows the cursor
            moveElementTo(event.pageX, event.pageY);
        }

        //Removes all the event handles for dragging from the page, called when the mose is released
        function onMouseUp() {
            //Remove the mousemove listener from the page
            document.removeEventListener('mousemove', onMouseMove);

            //Remove the mouseup listener from the page and the webdashboard container page
            document.removeEventListener('mouseup', onMouseUp);
            window.parent.document.removeEventListener('mouseup', onMouseUp);
        }

        //Call moveElementTo so that the center of the element will move to the cursor when the mouse presses the element
        moveElementTo(event.pageX, event.pageY);

        //When the mouse is moved call onMouseMove
        document.addEventListener('mousemove', onMouseMove);

        //When the mouse is released on this page or the webdashboard container page call onMouseUp
        document.addEventListener('mouseup', onMouseUp);
        window.parent.document.addEventListener('mouseup', onMouseUp);
    };
}

//Add key listener to the valueInput so that if enter is pressed it updates the value
document.getElementById("valueInput").addEventListener("keyup", function (event) {
    if (event.key === "Enter") {
        //If the key press event occurred on the Enter key

        //Prevent what the key press would otherwise do
        event.preventDefault();

        //Press the updateButton automatically to update the value and close the menu
        document.getElementById("updateButton").click();
    }
});

//Send a value update event to the robot code
//"type" the updated value's type
//"name" the updated value's name
//"value" the updated value's new value
function sendUpdateValue(type, name, value) {
    if (type === "vector") {
        //If the updated value is a vector

        //Call parse float on the value so it is formatted properly
        value = parseFloat(value);

        //Get the selected part of the vector from the vectorInputSelector on the value menu
        let selected = document.getElementById("vectorInputSelector").value;

        //Send the update to the robot code
        socket.send(new UrlFormData().append("request", "change_value").append("type", type).append("name", name).append("value", value).append("selected", selected).toString());
    } else if (type === "boolean") {
        //If the updated value is a boolean

        //Call toString and toLowerCase on the value so it is formatted properly
        value = value.toString().toLowerCase();

        //Send the update to the robot code
        socket.send(new UrlFormData().append("request", "change_value").append("type", type).append("name", name).append("value", value).toString());
    } else {
        //If the updated value is a numeric

        //Call parse float on the value so it is formatted properly
        value = parseFloat(value);

        //Send the update to the robot code
        socket.send(new UrlFormData().append("request", "change_value").append("type", type).append("name", name).append("value", value).toString());
    }
}

//Code to create, maintain, and reopen a connection with the server in the robot code
function connect() {

    //Create a new websocket with the same host as the page and the page port plus 1 and path "/log"
    //to connect with the server in the robot code
    socket = new WebSocket("ws://" + window.location.hostname + ":" + (parseInt(window.location.port) + 1) + "/values");

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
            case "values":
                //If message response type is values, update the values in the value table

                //Get the children of the value table body
                let children = document.getElementById("valueTableBody").children;

                //Get an array of the values data
                let values = messageData.get("values").split("~");

                //Get the
                let graphValues = JSON.parse(sessionStorage['graph_values']);

                loop: for (let v of values) {

                    //Get the parts of the value this usually will be one unless the value is a vector
                    let parts = v.split("*");

                    //Create a value to be displayed,
                    //this allows from vectors to be displayed with each value on a new line.
                    //Add all the parts except the the first 2 as they are the type and name not the value,
                    //sort the parts alphabetically, then join them together with a new line between each part
                    let value = parts.slice(2, parts.length).sort().join("\n");

                    if (parts[1] === "si_selected_auto") {
                        //If the name of the value is si_selected_auto then add it to sessionStorage,
                        //so the auto button can display the auto selected in the robot code
                        sessionStorage[parts[1]] = parts[2];
                    }

                    if (parts[0] === "vector" && parts[1].startsWith("gr_")) {
                        //If the value is a vector and it starts with the graph dataset tag "gr_",
                        //then extract the data and send it to the graph page and don't add it to the values table

                        //Create an array for the new graph dataset
                        let vectorGraphValues = [];

                        //Split the value into each data point
                        let valueParts = value.split("\n");

                        //Loop through each of the values in the array
                        for (let v = 0; v < valueParts.length; v += 2) {

                            //Add each of the x,y points to the graph dataset
                            vectorGraphValues.push({x: valueParts[v].split(": ")[1], y: valueParts[v + 1].split(": ")[1]});
                        }

                        //Set the data in graphValues corresponding to this value
                        graphValues[parts[1]] = {
                            //Set the value to the dataset
                            value: vectorGraphValues,

                            //Set updated to true so the graphing code knows this is new data and should be graphed
                            updated: true
                        };

                        //Move onto the next value so the graph dataset isn't added to the values table
                        continue;
                    }

                    if (graphValues[parts[1]] !== undefined) {
                        //If the value is being graph update the value

                        if (parts[0] === "vector") {
                            //If the value is a vector

                            //Create a new map for the data point
                            let vectorGraphValues = {};

                            //Get the parts of the vector
                            let vectorValues = value.split("\n");

                            //Loop through the parts vector
                            for (let v of vectorValues) {

                                //Split each part of the vector into a name and value
                                let vectorValueParts = v.split(": ");

                                //Add the name and value to the map
                                vectorGraphValues[vectorValueParts[0]] = vectorValueParts[1];
                            }

                            //Get the x and y dataset names from the graph values
                            let xName = graphValues[parts[1]].x;
                            let yName = graphValues[parts[1]].y;

                            //Get x and y values from the map using the dataset names
                            let xValue = vectorGraphValues[xName];
                            let yValue = vectorGraphValues[yName];

                            //Set the graph values value to x,y so the point will be graphed
                            graphValues[parts[1]].value = xValue + "," + yValue;
                        } else if (parts[0] === "boolean") {
                            //If the value is a boolean, set its value in graphValues 0 for false 1 for true
                            graphValues[parts[1]].value = (parts[2] === "true" ? "1" : "0");
                        } else if (parts[0] === "output") {
                            //If the value is an output

                            if (parts[2] === "false" || parts[2] === "true") {
                                //If the output is a boolean, set its value in graphValues 0 for false 1 for true
                                graphValues[parts[1]].value = (parts[2] === "true" ? "1" : "0");
                            } else {
                                //If the output is a boolean, set its value in graphValues to the value
                                graphValues[parts[1]].value = parts[2];
                            }
                        } else {
                            //If the output is a numeric, set its value in graphValues to the value
                            graphValues[parts[1]].value = parts[2];
                        }

                        //Set updated to true so the graph page knows it is a new value and should be graphed
                        graphValues[parts[1]].updated = true;
                    }

                    if (popoutMap.has(parts[1])) {
                        //If the value has a popout update the value in the popout's display
                        popoutMap.get(parts[1]).innerText = parts[1] + ":\n" + value;
                    }

                    //Loop through each of the rows in the values table
                    for (let child of children) {

                        if (child.cells[0].innerText === parts[1]) {
                            //If name cell matches the name of the value

                            if (child.cells[1].innerText !== value) {
                                //If the value has changed

                                //Update the value in the value cell
                                child.cells[1].innerText = value;

                                child.cells[0].onclick = function () {
                                    //Update the onclick handler for the new value
                                    openUpdateValue(parts[0], parts[1], value);
                                };
                            }

                            //Since this value has been updated move onto the next value
                            continue loop;
                        }
                    }

                    //The value isn't yet in the display table

                    //Add the name to the array of table values
                    tableValues.push(parts[1]);

                    //Sort the array of table values in alphabetical order
                    tableValues.sort();

                    //Create a new row at the correct location in the table so it is in alphabetical order
                    let r = document.getElementById("valueTableBody").insertRow(tableValues.indexOf(parts[1]));

                    //Create the name cell
                    let cell = r.insertCell(0);

                    //Set the text of the cell to the name of the value
                    cell.innerHTML = parts[1];

                    cell.onclick = function () {
                        //Add an onclick handler so the value menu opens when the cell is clicked
                        openUpdateValue(parts[0], parts[1], value);
                    };

                    //Create the value cell
                    let valueCell = r.insertCell(1);

                    //Set the max width so text will wrap if it is too large
                    valueCell.style.maxWidth = "10vw";

                    //Set the text of the cell to the value
                    valueCell.innerHTML = value;
                }

                //Update the graph_values in sessionStorage so the graph will update with the new data
                sessionStorage['graph_values'] = JSON.stringify(graphValues);

                //Update the values table header so it matches the the table body
                document.getElementById('nameHeader').style.width = document.getElementById("valueTableBody").rows[0].cells[0].clientWidth + "px";
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

//Added a new empty json object to to the sessionStorage graph_values so that it can be filled later
sessionStorage['graph_values'] = JSON.stringify({});

if (localStorage['popouts'] !== undefined && localStorage['popouts'] !== "") {
    //If localStorage contains popout values add them to the page

    //Loop through each of the popout values in localStorage
    for (let popout of localStorage['popouts'].split("*")) {

        //Create a new draggable viewer for the popout value
        createDraggableViewer(popout.split(",")[0], popout.split(",")[1], popout.split(",")[2]);
    }
}

//Code for handling gamepads for sim mode

//A boolean tracking whether the dpad button for the keyboard gamepad is pressed
let dpadButtonPressed = false;

//A boolean tracking whether the leftstick button for the keyboard gamepad is pressed
let leftstickButtonPressed = false;

//A boolean tracking whether the rightstick button for the keyboard gamepad is pressed
let rightstickButtonPressed = false;

//The value of the leftstick x on the keyboard gamepad
let leftstickX = 0;

//The value of the leftstick y on the keyboard gamepad
let leftstickY = 0;

//The value of the rightstick x on the keyboard gamepad
let rightstickX = 0;

//The value of the rightstick y on the keyboard gamepad
let rightstickY = 0;

//The previous values of the buttons on the driver gamepad
let lastDriverButtonValues = null;

//The previous values of the axis on the driver gamepad
let lastDriverAxisValues = null;

//The previous values of the buttons on the operator gamepad
let lastOperatorButtonValues = null;

//The previous values of the axis on the operator gamepad
let lastOperatorAxisValues = null;

//A map for osx from gamepad button ids to robot code names
let macButtonIdMap = {
    0: 'a',
    1: 'b',
    2: 'x',
    3: 'y',
    4: 'left_bumper',
    5: 'right_bumper',
    6: 'left_stick_button',
    7: 'right_stick_button',
    8: 'start',
    9: 'back',
    11: 'dpad_up',
    12: 'dpad_down',
    13: 'dpad_left',
    14: 'dpad_right'
};

//A map for windows from gamepad button ids to robot code names
let windowsButtonIdMap = {
    0: 'a',
    1: 'b',
    2: 'x',
    3: 'y',
    4: 'left_bumper',
    5: 'right_bumper',
    6: 'left_trigger',
    7: 'right_trigger',
    8: 'back',
    9: 'start',
    10: 'left_stick_button',
    11: 'right_stick_button',
    12: 'dpad_up',
    13: 'dpad_down',
    14: 'dpad_left',
    15: 'dpad_right'
};

//A map for osx from gamepad numeric input ids to robot code names
let macNumericIdMap = {
    0: 'left_x',
    1: 'left_y',
    2: 'left_trigger',
    3: 'right_x',
    4: 'right_y',
    5: 'right_trigger'
};

//A map for windows from gamepad numeric input ids to robot code names
let windowsNumericIdMap = {
    0: 'left_x',
    1: 'left_y',
    2: 'right_x',
    3: 'right_y',
};

//Updates the sim gamepad states in the robot code
function updateGamepads() {
    let driverGamepad = parseInt(document.getElementById('driverSelector').value);

    let gamepad = navigator.getGamepads()[driverGamepad];

    if (gamepad !== null && gamepad !== undefined) {

        let buttons = gamepad.buttons;
        if (lastDriverButtonValues === null) {
            lastDriverButtonValues = buttons;
            for (let button of buttons) {
                updateGamepadBooleanEvent(gamepad, "driver", buttons.indexOf(button), button.pressed);
            }
        }
        for (let button of buttons) {
            if (lastDriverButtonValues[buttons.indexOf(button)].pressed !== button.pressed) {
                updateGamepadBooleanEvent(gamepad, "driver", buttons.indexOf(button), button.pressed);
            }
        }
        lastDriverButtonValues = buttons;

        let axis = gamepad.axes;
        if (lastDriverAxisValues === null) {
            lastDriverAxisValues = axis;
            for (let a of axis) {
                updateGamepadNumericEvent(gamepad, "driver", axis.indexOf(a), a);
            }
        }
        for (let a = 0; a < axis.length; a++) {
            if (lastDriverAxisValues[a] !== axis[a]) {
                updateGamepadNumericEvent(gamepad, "driver", a, axis[a]);
            }
        }
        lastDriverAxisValues = axis;
    }

    let operatorGamepad = parseInt(document.getElementById('operatorSelector').value);

    gamepad = navigator.getGamepads()[operatorGamepad];

    if (gamepad !== null && gamepad !== undefined) {

        let buttons = gamepad.buttons;
        if (lastOperatorButtonValues === null) {
            lastOperatorButtonValues = buttons;
            for (let button of buttons) {
                updateGamepadBooleanEvent(gamepad, "operator", buttons.indexOf(button), button.pressed);
            }
        }
        for (let button of buttons) {
            if (lastOperatorButtonValues[buttons.indexOf(button)].pressed !== button.pressed) {
                updateGamepadBooleanEvent(gamepad, "operator", buttons.indexOf(button), button.pressed);
            }
        }
        lastOperatorButtonValues = buttons;

        let axis = gamepad.axes;
        if (lastOperatorAxisValues === null) {
            lastOperatorAxisValues = axis;
            for (let a of axis) {
                updateGamepadNumericEvent(gamepad, "operator", axis.indexOf(a), a);
            }
        }
        for (let a = 0; a < axis.length; a++) {
            if (lastOperatorAxisValues[a] !== axis[a]) {
                updateGamepadNumericEvent(gamepad, "operator", a, axis[a]);
            }
        }
        lastOperatorAxisValues = axis;
    }
}

//Updates the keyboard gamepad state, called whenever a key is pressed or released
//"key" is the key's name
//"value" is true if the key was pressed, false if the key was released
function keyEvent(key, value) {
    //Create a gamepad button or axis name to be set according to the key pressed
    let name = null;

    //Create a type value which has a default value of boolean
    let type = "boolean";

    //A case statement based on the key pressed
    switch (key) {
        case "a":
        case "b":
        case "x":
        case "y":
            //If a letter key changed Set the gamepad value name to the key
            name = key;
            break;
        case "1":
            //If the 1 changed Set the gamepad value name to left_trigger
            name = "left_trigger";
            break;
        case "0":
            //If the 0 key changed Set the gamepad value name to right_trigger
            name = "right_trigger";
            break;
        case "2":
            //If the 2 key changed Set the gamepad value name to left_bumper
            name = "left_bumper";
            break;
        case "9":
            //If the 9 key changed Set the gamepad value name to right_bumper
            name = "right_bumper";
            break;
        case "5":
            //If the 5 key changed set the dpadButtonPressed value to whether the key is pressed
            dpadButtonPressed = value;
            break;
        case "3":
            //If the 3 key changed set the leftstickButtonPressed value to whether the key is pressed
            leftstickButtonPressed = value;
            break;
        case "8":
            //If the 8 key changed set the rightstickButtonPressed value to whether the key is pressed
            rightstickButtonPressed = value;
            break;
        case "7":
            //If the 7 key changed Set the gamepad value name to start
            name = "start";
            break;
        case "4":
            //If the 1 key changed Set the gamepad value name to back
            name = "back";
            break;
        case "6":
            //If the 6 key changed
            if (leftstickButtonPressed) {
                //If the left stick modifier button is pressed, set the gamepad value name to left_stick_button
                name = "left_stick_button"
            }
            if (rightstickButtonPressed) {
                //If the right stick modifier button is pressed, set the gamepad value name to right_stick_button
                name = "right_stick_button"
            }
            break;
        case "ArrowUp":
            //If the up arrow key changed
            if (dpadButtonPressed) {
                //If the dpad modifier button is pressed, set the gamepad value name to dpad_up
                name = "dpad_up"
            } else if (leftstickButtonPressed) {
                //If the left stick modifier button is pressed 

                //Set the gamepad value type to numeric
                type = "numeric";

                //Set the gamepad value name to left_y
                name = "left_y";

                //Decrement leftstickY by 0.1
                leftstickY -= 0.1;

                //Range leftstickY
                if (leftstickY < -1) {
                    leftstickY = -1;
                }

                //Set the gamepad value
                value = leftstickY;
            } else if (rightstickButtonPressed) {
                //If the right stick modifier button is pressed

                //Set the gamepad value type to numeric
                type = "numeric";

                //Set the gamepad value name to right_y
                name = "right_y";

                //Decrement rightstickY by 0.1
                rightstickY -= 0.1;

                //Range rightstickY
                if (rightstickY < -1) {
                    rightstickY = -1;
                }

                //Set the gamepad value
                value = rightstickY;
            }
            break;
        case "ArrowDown":
            //If the down arrow key changed
            if (dpadButtonPressed) {
                //If the dpad modifier button is pressed, Set the gamepad value name to the dpad_down
                name = "dpad_down"
            } else if (leftstickButtonPressed) {
                //If the left stick modifier button is pressed

                //Set the gamepad value type to numeric
                type = "numeric";

                //Set the gamepad value name to left_y
                name = "left_y";

                //Increment leftstickY by 0.1
                leftstickY += 0.1;

                //Range leftstickY
                if (leftstickY > 1) {
                    leftstickY = 1;
                }

                //Set the gamepad value
                value = leftstickY;
            } else if (rightstickButtonPressed) {
                //If the right stick modifier button is pressed

                //Set the gamepad value type to numeric
                type = "numeric";

                //Set the gamepad value name to right_y
                name = "right_y";

                //Increment rightstickY by 0.1
                rightstickY += 0.1;

                //Range rightstickY
                if (rightstickY > 1) {
                    rightstickY = 1;
                }

                //Set the gamepad value
                value = rightstickY;
            }
            break;
        case "ArrowLeft":
            //If the left arrow key changed
            if (dpadButtonPressed) {
                //If the dpad modifier button is pressed, Set the gamepad value name to the dpad_left
                name = "dpad_left"
            } else if (leftstickButtonPressed) {
                //If the left stick modifier button is pressed

                //Set the gamepad value type to numeric
                type = "numeric";

                //Set the gamepad value name to left_x
                name = "left_x";

                //Decrement leftstickX by 0.1
                leftstickX -= 0.1;

                //Range leftstickX
                if (leftstickX < -1) {
                    leftstickX = -1;
                }

                //Set the gamepad value
                value = leftstickX;
            } else if (rightstickButtonPressed) {
                //If the right stick modifier button is pressed

                //Set the gamepad value type to numeric
                type = "numeric";

                //Set the gamepad value name to right_x
                name = "right_x";

                //Decrement rightstickX by 0.1
                rightstickX -= 0.1;

                //Range rightstickX
                if (rightstickX < -1) {
                    rightstickX = -1;
                }

                //Set the gamepad value
                value = rightstickX;
            }
            break;
        case "ArrowRight":
            //If the right arrow key changed
            if (dpadButtonPressed) {
                //If the dpad modifier button is pressed, Set the gamepad value name to the dpad_right
                name = "dpad_right"
            } else if (leftstickButtonPressed) {
                //If the left stick modifier button is pressed

                //Set the gamepad value type to numeric
                type = "numeric";

                //Set the gamepad value name to left_x
                name = "left_x";

                //Increment leftstickX by 0.1
                leftstickX += 0.1;

                //Range leftstickX
                if (leftstickX > 1) {
                    leftstickX = 1;
                }

                //Set the gamepad value
                value = leftstickX;
            } else if (rightstickButtonPressed) {
                //If the right stick modifier button is pressed

                //Set the gamepad value type to numeric right_x
                type = "numeric";

                //Set the gamepad value name to right_x
                name = "right_x";

                //Increment rightstickX by 0.1
                rightstickX += 0.1;

                //Range rightstickX
                if (rightstickX > 1) {
                    rightstickX = 1;
                }

                //Set the gamepad value
                value = rightstickX;
            }
            break;
    }

    if (name === null) {
        //If no name was set from the key that was pressed return
        return;
    }

    if (document.getElementById('driverSelector').value === "-2") {
        //If the keyboard gamepad is selected for the driver,
        //use the value updating code to send the value to the robot code,
        //with the type, name, and value of the gamepad update event
        sendUpdateValue(type, (type === "boolean" ? "bi_driver_" : "ni_driver_") + name, value);
    }

    if (document.getElementById('operatorSelector').value === "-2") {
        //If the keyboard gamepad is selected for the operator,
        //use the value updating code to send the value to the robot code,
        //with the type, name, and value of the gamepad update event
        sendUpdateValue(type, (type === "boolean" ? "bi_operator_" : "ni_operator_") + name, value);
    }
}

//Sends a boolean gamepad value event to the robot code
//"gamepad" is the gamepad object from which the event originated
//"user" is either "driver" or "operator" based on where the event originated
//"id" is the id of the event
//"value" is the value of the event
function updateGamepadBooleanEvent(gamepad, user, id, value) {

    //Determine robot code name for the event using the button id maps and the gamepad value name
    let name = gamepad.id.includes('XInput') ? windowsButtonIdMap[id] : macButtonIdMap[id];

    if (name === undefined) {
        //If no event name was found return
        return;
    }

    //Add the extra formatting for the type and controller
    name = "bi_" + user + "_" + name;

    //Use the value updating code to send the value to the robot code,
    //with the type, name, and value of the gamepad update event
    sendUpdateValue('boolean', name, value);
}

//Sends a numeric gamepad value event to the robot code
//"gamepad" is the gamepad object from which the event originated
//"user" is either "driver" or "operator" based on where the event originated
//"id" is the id of the event
//"value" is the value of the event
function updateGamepadNumericEvent(gamepad, user, id, value) {

    //Determine robot code name for the event using the numeric id maps and the gamepad value name
    let name = gamepad.id.includes('XInput') ? windowsNumericIdMap[id] : macNumericIdMap[id];

    if (name === undefined) {
        //If no event name was found return
        return;
    }

    //Add the extra formatting for the type and controller
    name = "ni_" + user + "_" + name;

    if (name.includes('trigger')) {
        //If the axis is a trigger

        //Scale the value
        value = (value + 1) / 2;

        //Turn the numeric value into a boolean and use the value updating code to send the value to the robot code,
        //with the type, name, and value of the gamepad update event
        sendUpdateValue('boolean', name.replace('ni_', 'bi_'), value > 0.5);
    }

    //Use the value updating code to send the value to the robot code.
    //with the type, name, and value of the gamepad update event
    sendUpdateValue('numeric', name, value);
}

//Called whenever a key is pressed, updates the keyboard gamepad
document.addEventListener('keydown', function (event) {
    if (!valueMenuOpen) {
        //If the valueChangeMenu isn't open then update the keyboard gamepad
        //with the key name and true because the key was pressed
        keyEvent(event.key, true);
    }
});

//Called whenever a key is released, updates the keyboard gamepad
document.addEventListener('keyup', function (event) {
    if (!valueMenuOpen) {
        //If the valueChangeMenu isn't open then update the keyboard gamepad
        //with the key name and false because the key was released
        keyEvent(event.key, false);
    }
});

//Update the gamepads every 200 milliseconds
setInterval(updateGamepads, 200);
