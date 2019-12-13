let socket;

let tableValues = [];

let valueUpdateType = "";
let valueUpdateName = "";
let valueUpdateValue = "";
let valueChange = false;
let dpadButton = false;
let leftstickButton = false;
let rightstickButton = false;

let leftstickX = 0;
let leftstickY = 0;
let rightstickX = 0;
let rightstickY = 0;

let driverButtons = null;
let driverAxis = null;
let operatorButtons = null;
let operatorAxis = null;

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
let macNumericIdMap = {
    0: 'left_x',
    1: 'left_y',
    2: 'left_trigger',
    3: 'right_x',
    4: 'right_y',
    5: 'right_trigger'
};
let windowsNumericIdMap = {
    0: 'left_x',
    1: 'left_y',
    2: 'right_x',
    3: 'right_y',
};

let popoutMap = new Map();

function openMenu() {
    setVisibility(document.getElementById("menuPopup"), true);

    document.getElementById("driverSelector").innerHTML = "";
    document.getElementById("operatorSelector").innerHTML = "";

    let gamepads = navigator.getGamepads();

    document.getElementById("driverSelector").innerHTML += "<option value=\"" + -1 + "\">0 - Nothing</option>";
    document.getElementById("driverSelector").innerHTML += "<option value=\"" + -2 + "\">1 - Keyboard</option>";
    document.getElementById("operatorSelector").innerHTML += "<option value=\"" + -1 + "\">0 - Nothing</option>";
    document.getElementById("operatorSelector").innerHTML += "<option value=\"" + -2 + "\">1 - Keyboard</option>";

    let num = 2;

    for (let g = 0; g < gamepads.length; g++) {
        try {
            let gamepad = gamepads[g];
            if (gamepad === null) continue;
            if (gamepad.id.toLowerCase().includes('x')) {
                document.getElementById("driverSelector").innerHTML += "<option value=\"" + g + "\">" + num + " - " + gamepad.id + "</option>";
                document.getElementById("operatorSelector").innerHTML += "<option value=\"" + g + "\">" + num + " - " + gamepad.id + "</option>";
                num++;
            }
        } catch (e) {
        }
    }
}

function closeMenu() {
    setVisibility(document.getElementById("menuPopup"), false);

    socket.send(new UrlFormData().append("request", "set_fms_mode").append("mode", document.getElementById("fmsSelector").value).toString());
}

function openUpdateValue(type, name, value) {
    valueUpdateType = type;
    valueUpdateName = name;
    valueUpdateValue = value;

    document.getElementById('valueDisplay').innerText = name;

    if (type === "output") {
        document.getElementById('valueInput').style.display = "none";
        document.getElementById('vectorInputSelector').style.display = "none";
    } else if (type === "vector") {
        let values = value.split("\n");
        values.splice(values.length - 1, 1);

        document.getElementById("vectorInputSelector").innerHTML = "";

        for (let v of values) {
            let val = v.split(": ")[0];
            document.getElementById("vectorInputSelector").innerHTML += "<option value=\"" + val + "\">" + val + "</option>";
        }

        document.getElementById('valueInput').style.display = "block";
        document.getElementById('vectorInputSelector').style.display = "block";
    } else {
        document.getElementById('valueInput').style.display = "block";
        document.getElementById('vectorInputSelector').style.display = "none";
    }

    if (type === "boolean") document.getElementById('valueInput').value = (value === "false");
    else if (type === "numeric" || type === "vector") document.getElementById('valueInput').value = "0.0";
    else document.getElementById('valueInput').value = "";

    onValueChange();
    setVisibility(document.getElementById("valuePopup"), true);

    document.getElementById('valueInput').focus();
    document.getElementById('valueInput').select();
}

function closeUpdateValue(b) {
    valueChange = false;
    if (b) {
        if (valueUpdateType !== 'output') sendUpdateValue(valueUpdateType, valueUpdateName, document.getElementById("valueInput").value);
    }

    setVisibility(document.getElementById("valuePopup"), false)
}

function onValueChange() {
    valueChange = true;
    if ((valueUpdateType === 'numeric' || valueUpdateType === 'vector') && (!parseFloat(document.getElementById("valueInput").value) && document.getElementById("valueInput").value != 0)) {
        document.getElementById('updateButton').style.background = 'var(--grey)';
    } else if (valueUpdateType === 'boolean' && !(document.getElementById("valueInput").value === 'true' || document.getElementById("valueInput").value === 'false')) {
        document.getElementById('updateButton').style.background = 'var(--grey)';
    } else {
        document.getElementById('updateButton').style.background = 'var(--green)';
    }
}

function toggleGraph() {
    if (valueUpdateType === 'vector') {
        let graphValues = JSON.parse(sessionStorage['graph_values']);
        if (graphValues[valueUpdateName] === undefined) {
            openVectorGraphingMenu();
        } else {
            graphValues[valueUpdateName] = undefined;
            sessionStorage['graph_values'] = JSON.stringify(graphValues);
        }
    } else if (valueUpdateType !== 'string') {
        let graphValues = JSON.parse(sessionStorage['graph_values']);
        if (graphValues[valueUpdateName] === undefined) {
            graphValues[valueUpdateName] = {value: null, updated: false};
            socket.send(new UrlFormData().append("request", "all_values").toString());
        } else {
            graphValues[valueUpdateName] = undefined;
        }

        sessionStorage['graph_values'] = JSON.stringify(graphValues);
    }
}

function openVectorGraphingMenu() {
    document.getElementById("vectorDisplay").innerText = valueUpdateName;
    document.getElementById("vectorGraphingX").innerHTML = "";
    document.getElementById("vectorGraphingY").innerHTML = "";
    let values = valueUpdateValue.split("\n");
    values.splice(values.length - 1, 1);
    document.getElementById("vectorGraphingX").innerHTML = "";
    document.getElementById("vectorGraphingY").innerHTML = "";
    for (let v of values) {
        let value = v.split(": ")[0];
        document.getElementById("vectorGraphingX").innerHTML += "<option value=\"" + value + "\">" + value + "</option>";
        document.getElementById("vectorGraphingY").innerHTML += "<option value=\"" + value + "\">" + value + "</option>";
    }
    setVisibility(document.getElementById("vectorGraphingPopup"), true);
}

function closeVectorGraphingMenu(b) {
    setVisibility(document.getElementById("vectorGraphingPopup"), false);
    if (b) {
        let graphValues = JSON.parse(sessionStorage['graph_values']);

        graphValues[valueUpdateName] = {
            value: null,
            updated: false,
            x: document.getElementById("vectorGraphingX").value,
            y: document.getElementById("vectorGraphingY").value
        };

        sessionStorage['graph_values'] = JSON.stringify(graphValues);

        socket.send(new UrlFormData().append("request", "all_values").toString());
    }
}

function setVisibility(x, b) {

    if (b) x.style.visibility = "visible";
    else x.style.visibility = "hidden";

    let children = x.children;

    for (let c = 0; c < children.length; c++) {
        if (b) children[c].style.visibility = "visible";
        else children[c].style.visibility = "hidden";
    }
}

function createDraggableViewer(key, left, top) {
    let div = document.createElement("DIV");
    div.id = key;
    div.classList.add("draggableViewer");
    div.classList.add("unselectable");
    div.ondblclick = function () {
        document.getElementById("dragContainer").removeChild(div);
        popoutMap.delete(key);
        document.getElementById("menuPopup").style.display = 'none';
        setTimeout(function () {
            closeMenu();
            document.getElementById("menuPopup").style.display = 'block';
            storePopouts()
        }, 1);
    };

    document.getElementById("dragContainer").appendChild(div);

    setDraggable(div);

    popoutMap.set(key, div);

    for (let row of document.getElementById("valueTableBody").rows) {
        if (row.cells[0].innerText === key) {
            popoutMap.get(row.cells[0].innerText).innerText = row.cells[0].innerText + ":\n" + valueUpdateValue;
        }
    }

    if (left !== undefined) div.style.left = left;
    if (top !== undefined) div.style.top = top;

    storePopouts();
}

function storePopouts() {
    let popouts = "";

    for (let popout of popoutMap.entries()) {
        popouts += popout[0] + "," + (popout[1].style.left || document.body.clientWidth / 2 + "px") + "," + (popout[1].style.top || "0px") + "*";
    }

    if (popouts.length > 0) popouts = popouts.substring(0, popouts.length - 1);

    localStorage["popouts"] = popouts;
}

function setDraggable(element) {
    element.onmousedown = function (event) {
        element.style.position = 'absolute';
        //element.style.zIndex = document.getElementById("valuePopup").style.zIndex - 1;

        moveAt(event.pageX, event.pageY);

        function moveAt(pageX, pageY) {
            if (pageX < (document.getElementById("valueTable").offsetWidth + element.offsetWidth / 2) + 20) pageX = (document.getElementById("valueTable").clientWidth + element.offsetWidth / 2) + 20;

            element.style.left = pageX - element.offsetWidth / 2 + 'px';
            element.style.top = pageY - element.offsetHeight / 2 + 'px';

            storePopouts();
        }

        function onMouseMove(event) {
            moveAt(event.pageX, event.pageY);
        }

        document.addEventListener('mousemove', onMouseMove);

        document.addEventListener('mouseup', onMouseUp);
        window.parent.document.addEventListener('mouseup', onMouseUp);

        function onMouseUp() {
            document.removeEventListener('mousemove', onMouseMove);

            document.removeEventListener('mouseup', onMouseUp);
            window.parent.document.removeEventListener('mouseup', onMouseUp);
        }

        return false;
    };
}

window.addEventListener("gamepadconnected", function (e) {

});

document.getElementById("valueInput").addEventListener("keyup", function (event) {
    if (event.keyCode === 13) {
        event.preventDefault();
        document.getElementById("updateButton").click();
    }
});

document.addEventListener('keydown', function (event) {
    if (!valueChange) {
        keyEvent(event.key, true);
    }
});

document.addEventListener('keyup', function (event) {
    if (!valueChange) {
        keyEvent(event.key, false);
    }
});

function keyEvent(key, value) {
    let name = null;
    let type = "boolean";

    switch (key) {
        case "a":
        case "b":
        case "x":
        case "y":
            name = key;
            break;
        case "1":
            name = "left_trigger";
            break;
        case "0":
            name = "right_trigger";
            break;
        case "2":
            name = "left_bumper";
            break;
        case "9":
            name = "right_bumper";
            break;
        case "5":
            dpadButton = value;
            break;
        case "7":
            name = "start";
            break;
        case "3":
            leftstickButton = value;
            break;
        case "8":
            rightstickButton = value;
            break;
        case "4":
            name = "back";
            break;
        case "6":
            if (leftstickButton) {
                name = "left_stick_button"
            }
            if (rightstickButton) {
                name = "right_stick_button"
            }
            break;
        case "ArrowUp":
            if (dpadButton) {
                name = "dpad_up"
            } else if (leftstickButton) {
                type = "numeric";
                leftstickY -= 0.1;
                if (leftstickY < -1) leftstickY = -1;
                value = leftstickY;
                name = "left_y"
            } else if (rightstickButton) {
                type = "numeric";
                rightstickY -= 0.1;
                if (rightstickY < -1) rightstickY = -1;
                value = rightstickY;
                name = "right_y"
            }
            break;
        case "ArrowDown":
            if (dpadButton) {
                name = "dpad_down"
            } else if (leftstickButton) {
                type = "numeric";
                leftstickY += 0.1;
                if (leftstickY > 1) leftstickY = 1;
                value = leftstickY;
                name = "left_y"
            } else if (rightstickButton) {
                type = "numeric";
                rightstickY += 0.1;
                if (rightstickY > 1) rightstickY = 1;
                value = rightstickY;
                name = "right_y"
            }
            break;
        case "ArrowLeft":
            if (dpadButton) {
                name = "dpad_left"
            } else if (leftstickButton) {
                type = "numeric";
                leftstickX -= 0.1;
                if (leftstickX < -1) leftstickX = -1;
                value = leftstickX;
                name = "left_x"
            } else if (rightstickButton) {
                type = "numeric";
                rightstickX -= 0.1;
                if (rightstickX < -1) rightstickX = -1;
                value = rightstickX;
                name = "right_x"
            }
            break;
        case "ArrowRight":
            if (dpadButton) {
                name = "dpad_right"
            } else if (leftstickButton) {
                type = "numeric";
                leftstickX += 0.1;
                if (leftstickX > 1) leftstickX = 1;
                value = leftstickX;
                name = "left_x"
            } else if (rightstickButton) {
                type = "numeric";
                rightstickX += 0.1;
                if (rightstickX > 1) rightstickX = 1;
                value = rightstickX;
                name = "right_x"
            }
            break;
    }

    if (name === null) return;

    if (document.getElementById('driverSelector').value == "-2") {
        sendUpdateValue(type, (type === "boolean" ? "bi_driver_" : "ni_driver_") + name, value);
    }
    if (document.getElementById('operatorSelector').value == "-2") {
        sendUpdateValue(type, (type === "boolean" ? "bi_operator_" : "ni_operator_") + name, value);
    }
}

function sendUpdateValue(type, name, value) {
    if (type === "vector") {
        socket.send(new UrlFormData().append("request", "change_value").append("type", type).append("name", name).append("value", parseFloat(value)).append("selected", document.getElementById("vectorInputSelector").value).toString());
    } else {
        socket.send(new UrlFormData().append("request", "change_value").append("type", type).append("name", name).append("value", type === 'numeric' ? parseFloat(value) : value).toString());
    }
}

function connect() {
    socket = new WebSocket("ws://" + window.location.hostname + ":" + (parseInt(window.location.port) + 1) + "/values");

    socket.onopen = function () {

    };

    socket.onclose = function () {
        setTimeout(function () {
            connect();
        }, 1000);
    };

    socket.onmessage = function (message) {
        let r = new UrlFormData(message.data);
        switch (r.get("response")) {
            case "values":
                let children = document.getElementById("valueTableBody").children;

                let values = r.get("values").split("~");

                let graphValues = JSON.parse(sessionStorage['graph_values']);

                loop: for (let v of values) {
                    let parts = v.split("*");

                    let value = "";
                    let p = [];
                    p = p.concat(parts);
                    p.shift();
                    p.shift();
                    p.sort();
                    for (let i = 0; i < p.length; i++) {
                        value += p[i] + "\n";
                    }

                    if (parts[1] === "si_selected_auto") {
                        sessionStorage[parts[1]] = parts[2];
                    }
                    if (parts[0] === "vector" && parts[1].startsWith("gr_")) {

                        let vectorGraphValues = [];

                        let values = value.split("\n");
                        values.splice(values.length - 1, 1);
                        for (let v = 0; v < values.length; v += 2) {
                            vectorGraphValues.push({x: values[v].split(": ")[1], y: values[v + 1].split(": ")[1]});
                        }

                        graphValues[parts[1]] = {
                            value: vectorGraphValues,
                            updated: true
                        };

                        continue loop;
                    }

                    if (graphValues[parts[1]] !== undefined) {
                        if (parts[0] === "vector") {
                            let vectorGraphValues = {};

                            let vectorValues = value.split("\n");
                            vectorValues.splice(vectorValues.length - 1, 1);
                            for (let v of vectorValues) {
                                let vectorValueParts = v.split(": ");
                                vectorGraphValues[vectorValueParts[0]] = vectorValueParts[1];
                            }

                            graphValues[parts[1]].value = vectorGraphValues[graphValues[parts[1]].x] + "," + vectorGraphValues[graphValues[parts[1]].y];
                        } else if (parts[0] === "boolean") {
                            graphValues[parts[1]].value = (parts[2] === "true" ? "1" : "0");
                        } else if (parts[0] === "output") {
                            if (parts[2] === "false" || parts[2] === "true") {
                                graphValues[parts[1]].value = (parts[2] === "true" ? "1" : "0");
                            } else {
                                graphValues[parts[1]].value = parts[2];
                            }
                        } else {
                            graphValues[parts[1]].value = parts[2];
                        }

                        graphValues[parts[1]].updated = true;
                    }

                    if (popoutMap.has(parts[1])) {
                        popoutMap.get(parts[1]).innerText = parts[1] + ":\n" + value;
                    }

                    for (let child of children) {
                        if (child.cells[0].innerText === parts[1]) {
                            if (child.cells[1].innerText !== value) child.cells[1].innerText = value;
                            child.cells[0].onclick = function () {
                                openUpdateValue(parts[0], parts[1], value);
                            };
                            continue loop;
                        }
                    }

                    tableValues.push(parts[1]);
                    tableValues.sort();

                    let r = document.getElementById("valueTableBody").insertRow(tableValues.indexOf(parts[1]));
                    let cell = r.insertCell(0);
                    cell.classList.add('unselectable');
                    cell.innerHTML = parts[1];
                    cell.onclick = function () {
                        openUpdateValue(parts[0], parts[1], value);
                    };

                    let valueCell = r.insertCell(1);
                    valueCell.style.maxWidth = "10vw";
                    valueCell.innerHTML = value.substring(0, value.length - 1);
                }

                sessionStorage['graph_values'] = JSON.stringify(graphValues);

                document.getElementById('nameHeader').style.width = document.getElementById("valueTableBody").rows[0].cells[0].clientWidth + "px";
                break;
        }
    };
}

connect();

setInterval(function () {
    if (socket !== undefined) {
        socket.send("keepalive");
    }
}, 1000);

sessionStorage['graph_values'] = JSON.stringify({});

if (localStorage['popouts'] !== undefined && localStorage['popouts'] !== "") {
    for (let popout of localStorage['popouts'].split("*")) {
        createDraggableViewer(popout.split(",")[0], popout.split(",")[1], popout.split(",")[2]);
    }
}

setInterval(function () {
    let driverGamepad = parseInt(document.getElementById('driverSelector').value);

    let gamepad = navigator.getGamepads()[driverGamepad];

    if (gamepad !== null && gamepad !== undefined) {

        let buttons = gamepad.buttons;
        if (driverButtons === null) {
            driverButtons = buttons;
            for (let button of buttons) {
                updateGamepadBooleanEvent(gamepad, "driver", buttons.indexOf(button), button.pressed);
            }
        }
        for (let button of buttons) {
            if (driverButtons[buttons.indexOf(button)].pressed !== button.pressed) {
                updateGamepadBooleanEvent(gamepad, "driver", buttons.indexOf(button), button.pressed);
            }
        }
        driverButtons = buttons;

        let axis = gamepad.axes;
        if (driverAxis === null) {
            driverAxis = axis;
            for (let a of axis) {
                updateGamepadNumericEvent(gamepad, "driver", axis.indexOf(a), a);
            }
        }
        for (let a = 0; a < axis.length; a++) {
            if (driverAxis[a] !== axis[a]) {
                updateGamepadNumericEvent(gamepad, "driver", a, axis[a]);
            }
        }
        driverAxis = axis;
    }

    let operatorGamepad = parseInt(document.getElementById('operatorSelector').value);

    gamepad = navigator.getGamepads()[operatorGamepad];

    if (gamepad !== null && gamepad !== undefined) {

        let buttons = gamepad.buttons;
        if (operatorButtons === null) {
            operatorButtons = buttons;
            for (let button of buttons) {
                updateGamepadBooleanEvent(gamepad, "operator", buttons.indexOf(button), button.pressed);
            }
        }
        for (let button of buttons) {
            if (operatorButtons[buttons.indexOf(button)].pressed !== button.pressed) {
                updateGamepadBooleanEvent(gamepad, "operator", buttons.indexOf(button), button.pressed);
            }
        }
        operatorButtons = buttons;

        let axis = gamepad.axes;
        if (operatorAxis === null) {
            operatorAxis = axis;
            for (let a of axis) {
                updateGamepadNumericEvent(gamepad, "operator", axis.indexOf(a), a);
            }
        }
        for (let a = 0; a < axis.length; a++) {
            if (operatorAxis[a] !== axis[a]) {
                updateGamepadNumericEvent(gamepad, "operator", a, axis[a]);
            }
        }
        operatorAxis = axis;
    }
}, 200);

function updateGamepadBooleanEvent(gamepad, type, id, value) {
    let name = gamepad.id.includes('XInput') ? windowsButtonIdMap[id] : macButtonIdMap[id];

    if (name === undefined) return;

    name = "bi_" + type + "_" + name;

    sendUpdateValue('boolean', name, value);
}

function updateGamepadNumericEvent(gamepad, type, id, value) {
    let name = gamepad.id.includes('XInput') ? windowsNumericIdMap[id] : macNumericIdMap[id];

    if (name === undefined) return;

    name = "ni_" + type + "_" + name;

    if (name.includes('trigger')) {
        value = (value + 1) / 2;
        sendUpdateValue('boolean', name.replace('ni_', 'bi_'), value > 0.5);
    }

    sendUpdateValue('numeric', name, value);
}
