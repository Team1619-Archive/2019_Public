let socket;

let keepalive = false;

if (localStorage['pageName'] !== undefined) localStorage.clear();

openPage(localStorage['page_name'] || "values", document.getElementById((localStorage['page_name'] || "values") + "Tab"));

function openPage(pageName, elmnt) {
    localStorage['page_name'] = pageName;

    // Hide all elements with class="tabcontent" by default
    let i, tabcontent, tablinks;
    tabcontent = document.getElementsByClassName("tabcontent");
    for (i = 0; i < tabcontent.length; i++) {
        tabcontent[i].classList.remove('selectedtabcontent')
    }

    // Remove the background color of all tablinks/buttons
    tablinks = document.getElementsByClassName("tablink");
    for (i = 0; i < tablinks.length; i++) {
        tablinks[i].classList.remove('selectedtablink')
        tablinks[i].classList.add('unselectedtablink')
    }

    // Show the specific tab content
    document.getElementById(pageName).classList.add('selectedtabcontent');

    // Add the specific color to the button used to open the tab content
    if (elmnt !== null) {
        elmnt.classList.remove('unselectedtablink');
        elmnt.classList.add('selectedtablink');
    }
}

function chooseAuto() {
    setVisibility(document.getElementById("autoPopup"), true);
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

function connect() {
    socket = new WebSocket("ws://" + window.location.hostname + ":" + (parseInt(window.location.port) + 1) + "/webdashboard");

    socket.onopen = function () {

    };

    socket.onclose = function () {
        for (let e of document.getElementsByClassName('connectedbar')) {
            e.style.background = getComputedStyle(document.body).getPropertyValue('--red');
        }

        setTimeout(function () {
            connect();
        }, 1000);
    };

    socket.onmessage = function (message) {
        if (message.data === "keepalive") {
            keepalive = false;

            let elements = document.getElementsByClassName('connectedbar');
            for (let e in elements) {
                if (e <= 1) elements[e].style.background = getComputedStyle(document.body).getPropertyValue('--green');
            }
            return;
        }
        let r = new UrlFormData(message.data);
        switch (r.get("response")) {
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
            case "connected":
                if (r.get("connected") === 'true') {
                    for (let e of document.getElementsByClassName('connectedbar')) {
                        e.style.background = getComputedStyle(document.body).getPropertyValue('--green');
                    }
                } else {
                    let elements = document.getElementsByClassName('connectedbar');
                    for (let e in elements) {
                        if (e > 1) elements[e].style.background = getComputedStyle(document.body).getPropertyValue('--red');
                    }
                }
                sessionStorage["si_selected_auto"] = "undefined";
                break;
        }
    };
}

setInterval(function () {
    if (keepalive === true) {
        socket.onclose();
    }
    if (socket !== undefined) {
        socket.send("keepalive");
        keepalive = true;
    }
}, 5000);

connect();

sessionStorage["si_selected_auto"] = "undefined";

setInterval(function () {
    if (sessionStorage["si_selected_auto"] !== "undefined") {
        document.getElementById("autoButton").innerText = sessionStorage["si_selected_auto"];
    } else {
        document.getElementById("autoButton").innerText = "No Auto";
    }
}, 500);