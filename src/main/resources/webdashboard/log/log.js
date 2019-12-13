/*let socket;

function connect() {
    socket = new WebSocket("ws://" + window.location.hostname + ":" + (parseInt(window.location.port) + 1) + "/log");

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
            case "log":
                document.getElementById("textArea").innerHTML += r.get("text");
                break;
        }
    };
}

connect();*/
