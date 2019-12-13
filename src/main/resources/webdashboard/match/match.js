let socket;

let blinking = false;
let connected = false;
let repeat = 0;

function chooseAuto() {
    if (window.location === window.parent.location) {
        setVisibility(document.getElementById("autoPopup"), true);
    }
}

function closeChooseAuto(x) {
    if (x) {
        socket.send(new UrlFormData()
            .append("request", "set_auto_data")
            .append("auto_origin", document.getElementById("autoOriginSelector").value)
            .append("auto_destination", document.getElementById("autoDestinationSelector").value)
            .append("auto_action", document.getElementById("autoActionSelector").value)
            .toString());
    }
    setVisibility(document.getElementById("autoPopup"), false);
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

function setValue(name, value) {
    let id = "value__" + name.split(" ").join("__");
    if ($("#" + id)[0] === undefined) {
        let valueBox = document.createElement("DIV");
        valueBox.classList.add("value");
        valueBox.id = id;
        document.getElementById("valuesContainer").appendChild(valueBox);
    }
    $("#" + id)[0].innerText = name + ": " + value;
}

function setBooleanBox(name, value) {
    let id = "boolean__" + name.split(" ").join("__");
    if ($("#" + id)[0] === undefined) {
        let booleanBox = document.createElement("DIV");
        booleanBox.classList.add("booleanBox");
        booleanBox.innerText = name;
        booleanBox.id = id;
        document.getElementById("valuesContainer").appendChild(booleanBox);
    }

    if (value == "true") {
        $("#" + id)[0].style.background = "var(--green)";
    } else if (value == "false") {
        $("#" + id)[0].style.background = "var(--red)";
    } else {
        $("#" + id)[0].style.background = "var(--grey)";
    }
}

function setDial(name, min, max, value) {
    let id = "dial__" + name.split(" ").join("__");
    if ($("#" + id)[0] === undefined) {
        let container = document.createElement("DIV");
        container.classList.add("dialContainer");
        let label = document.createElement("DIV");
        label.classList.add("dialLabel");
        label.innerText = name;
        container.appendChild(label);
        let dial = document.createElement("INPUT");
        dial.classList.add("dial");
        dial.classList.add("unselectable");
        dial.id = id;
        container.appendChild(dial);
        document.getElementById("valuesContainer").appendChild(container);
        $("#" + id).knob({
            displayInput: true,
            fontWeight: 2,
            width: 100,
            height: 50,
            angleOffset: -90,
            angleArc: 180,
            readOnly: true,
            step: 0.1,
            min: min,
            max: max,
            fgColor: getComputedStyle(document.documentElement).getPropertyValue('--foreground'),
            inputColor: getComputedStyle(document.documentElement).getPropertyValue('--white')
        });
    }
    $("#" + id).val(value).trigger('change');
    $("#" + id)[0].style.marginTop = "0%";
}

function blink() {
    if (connected) {
        repeat = 0;
        document.body.style.background = "var(--background)";
        document.getElementById("valuesContainer").style.background = "var(--background)";
        return;
    }
    if (repeat > 15) {
        repeat = 0;
        document.body.style.background = "var(--red)";
        document.getElementById("valuesContainer").style.background = "var(--red)";
        return;
    }

    blinking = true;

    if (repeat % 2 === 0) {
        document.body.style.background = "var(--red)";
        document.getElementById("valuesContainer").style.background = "var(--red)";
    } else {
        document.body.style.background = "var(--background)";
        document.getElementById("valuesContainer").style.background = "var(--background)";
    }

    repeat++;

    setTimeout(blink, 100);
}

function connect() {
    socket = new WebSocket("ws://" + window.location.hostname + ":" + (parseInt(window.location.port) + 1) + "/match");

    socket.onopen = function () {
        if (window.location === window.parent.location) {
            connected = true;
            blinking = false;
            document.body.style.background = "var(--background)";
            document.getElementById("valuesContainer").style.background = "var(--background)";
        }
    };

    socket.onclose = function () {
        if (window.location === window.parent.location) {
            connected = false;
            if (!blinking) {
                blink();
            }
        }

        setTimeout(function () {
            connect();
        }, 1000);
    };

    socket.onmessage = function (message) {
        let r = new UrlFormData(message.data);
        switch (r.get("response")) {
            case "match_values":
                let values = r.get("values").split("~");

                loop: for (let v of values) {
                    let parts = v.split("*");

                    if (parts[0] === "value") {
                        setValue(parts[1], parts[2])
                    } else if (parts[0] === "boolean") {
                        setBooleanBox(parts[1], parts[2]);
                    } else if (parts[0] === "dial") {
                        setDial(parts[1], parseFloat(parts[3]), parseFloat(parts[4]), parseFloat(parts[2]));
                    }
                }
                break;
            case "auto_data":
                let autoOriginList = r.get("auto_origin_list").split("~");
                document.getElementById("autoOriginSelector").innerHTML = "";
                for (let p of autoOriginList) {
                    document.getElementById("autoOriginSelector").innerHTML += "<option value=\"" + p + "\">" + p + "</option>";
                }

                let autoDestinationList = r.get("auto_destination_list").split("~");
                document.getElementById("autoDestinationSelector").innerHTML = "";
                for (let p of autoDestinationList) {
                    document.getElementById("autoDestinationSelector").innerHTML += "<option value=\"" + p + "\">" + p + "</option>";
                }

                let autoActionList = r.get("auto_action_list").split("~");
                document.getElementById("autoActionSelector").innerHTML = "";
                for (let a of autoActionList) {
                    document.getElementById("autoActionSelector").innerHTML += "<option value=\"" + a + "\">" + a + "</option>";
                }
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

if (window.location === window.parent.location) {
    connected = false;
    blink();
}

setInterval(function () {
    $("#mainCamera")[0].style.backgroundImage = "http://10.16.19.2:1181/?action=stream";
    $("#secondaryCamera1")[0].style.backgroundImage = "http://10.16.19.101:5800";
    $("#secondaryCamera2")[0].style.backgroundImage = "http://10.16.19.102:5800";
}, 1000);