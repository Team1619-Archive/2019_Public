let chartWrapper = document.getElementById("chartWrapper");
let chartCanvas = document.getElementById("chart");
let chart = new Chart(chartCanvas, {
    responsive: true,
    maintainAspectRatio: false,
    type: 'scatter',
    data: {
        datasets: []
    },
    options: {
        legend: {
            labels: {
                fontColor: getComputedStyle(document.documentElement).getPropertyValue('--white'),
                fontSize: 18
            }
        },
        scales: {
            yAxes: [{
                ticks: {
                    fontColor: getComputedStyle(document.documentElement).getPropertyValue('--graph')
                }
            }],
            xAxes: [{
                ticks: {
                    fontColor: getComputedStyle(document.documentElement).getPropertyValue('--graph')
                }
            }]
        },
        animation: {
            duration: 0
        }
    }
});
let wayPoints = [];
let points = [];
let spacing = 0.5;
let t = 0.001;
let SMOOTHING = 0.8;

function openMenu() {
    setVisibility(document.getElementById("menuPopup"), true);
}

function closeMenu() {
    setVisibility(document.getElementById("menuPopup"), false);
}

function copyToClipboard() {
    let el = document.createElement('textarea');

    let path = "";

    for (let point of wayPoints) {
        path += "[" + point.x + ", " + point.y + "], ";
    }

    if (path.length > 0) {
        path = path.trim().substring(0, path.length - 2);
    }

    path = "[" + path + "]";

    el.value = path;
    el.setAttribute('readonly', '');
    el.display = 'none';
    document.body.appendChild(el);
    el.select();
    document.execCommand('copy');
    document.body.removeChild(el);
}

function openPaste() {
    setVisibility(document.getElementById("pastePopup"), true);

    document.getElementById('pathInput').focus();
    document.getElementById('pathInput').select();
}

function closePaste(b) {
    if (b) {
        let text = document.getElementById("pathInput").value;

        text = text.replace(/(\r\n|\n|\r|\s)/gm, "");
        text = text.substring(1, text.length - 1);
        text = text.split("[");
        text.splice(0, 1);
        clearPath();
        for (let p in text) {
            let point = text[p].replace("],", "").replace("]", "");
            let row = document.getElementById("pointTableBody").rows[p];
            row.cells[0].innerText = point.split(",")[0];
            row.cells[1].innerText = point.split(",")[1];
        }
        update();
        closeMenu();
    }
    setVisibility(document.getElementById("pastePopup"), false);
}

function updateSize() {
    chartCanvas.style.height = (document.body.clientHeight - 10) + 'px';
}

function addData(label, data) {
    for (let dataset of chart.data.datasets) {
        if (dataset.label === label) {
            for (let point of data) {
                dataset.data.push(point);
            }
            chart.update();
            return;
        }
    }

    let dataset = {
        label: label,
        labelColor: getComputedStyle(document.documentElement).getPropertyValue('--graph'),
        borderColor: getComputedStyle(document.documentElement).getPropertyValue('--graph'),
        backgroundColor: getComputedStyle(document.documentElement).getPropertyValue('--graph'),
        pointRadius: 5,
        data: data
    };
    chart.data.datasets.push(dataset);
    chart.update();
}

function removeData() {
    chart.data.datasets = [];
    chart.update();
}

function clearPath() {
    localStorage['way_points'] = "";

    for (let row of document.getElementById("pointTableBody").rows) {
        row.cells[0].innerText = "";
        row.cells[1].innerText = "";
    }

    removeData();

}

function build() {
    wayPoints = [];

    for (let row of document.getElementById("pointTableBody").rows) {
        if ((parseFloat(row.cells[0].innerText) || row.cells[0].innerText === '0') && (parseFloat(row.cells[1].innerText) || row.cells[1].innerText === '0')) wayPoints.push(new Point(parseFloat(row.cells[0].innerText), parseFloat(row.cells[1].innerText)));
    }

    localStorage['way_points'] = wayPoints.join("*");

    fill();

    smooth();

    return true;
}

function fill() {

    let newPoints = [];

    for (let s = 1; s < wayPoints.length; s++) {
        let vector = new Vector(wayPoints[s - 1], wayPoints[s]);

        let numPointsFit = Math.round(Math.ceil(vector.magnitude() / spacing));

        vector = vector.normalize().scale(spacing);

        for (let i = 0; i < numPointsFit; i++) {
            newPoints.push(wayPoints[s - 1].add(vector.scale(i)));
        }
    }

    newPoints.push(wayPoints[wayPoints.length - 1]);

    points = newPoints;
}

function smooth() {
    let newPoints = [...points];

    let change = t;
    while (change >= t) {
        change = 0.0;
        for (let i = 1; i < points.length - 1; i++) {
            let point = newPoints[i];
            newPoints[i] = newPoints[i].add(new Point((1 - SMOOTHING) * (points[i].x - newPoints[i].x) + SMOOTHING * (newPoints[i - 1].x + newPoints[i + 1].x - (2.0 * newPoints[i].x)), (1 - SMOOTHING) * (points[i].y - newPoints[i].y) + SMOOTHING * (newPoints[i - 1].y + newPoints[i + 1].y - (2.0 * newPoints[i].y))));
            change += point.distance(newPoints[i]);
        }
    }

    points = newPoints;
}

function update() {
    build();

    removeData();

    let data = [];

    for (let point of points) {
        if (point !== undefined) data.push({x: point.x, y: point.y});
    }

    addData("Path", data);
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

function keyListener(event) {
    if (event.keyCode === 13) {
        event.preventDefault();

        let rows = document.getElementById("pointTable").rows;

        for (let r = 0; r < rows.length; r++) {
            let row = rows[r];
            if (row.cells[0] === event.target) {
                row.cells[1].focus();
            }
            if (row.cells[1] === event.target) {
                rows[r + 1].cells[0].focus();
            }
        }
    } else if (event.keyCode === 38) {
        event.preventDefault();

        let rows = document.getElementById("pointTable").rows;

        for (let r = 0; r < rows.length; r++) {
            let row = rows[r];
            if (row.cells[0] === event.target) {
                rows[r - 1].cells[0].focus();
            }
            if (row.cells[1] === event.target) {
                rows[r - 1].cells[1].focus();
            }
        }
    } else if (event.keyCode === 40) {
        event.preventDefault();

        let rows = document.getElementById("pointTable").rows;

        for (let r = 0; r < rows.length; r++) {
            let row = rows[r];
            if (row.cells[0] === event.target) {
                rows[r + 1].cells[0].focus();
            }
            if (row.cells[1] === event.target) {
                rows[r + 1].cells[1].focus();
            }
        }
    } else if (event.keyCode === 37) {
        event.preventDefault();

        let rows = document.getElementById("pointTable").rows;

        for (let r = 0; r < rows.length; r++) {
            let row = rows[r];
            if (row.cells[1] === event.target) {
                row.cells[0].focus();
            }
        }
    } else if (event.keyCode === 39) {
        event.preventDefault();

        let rows = document.getElementById("pointTable").rows;

        for (let r = 0; r < rows.length; r++) {
            let row = rows[r];
            if (row.cells[0] === event.target) {
                row.cells[1].focus();
            }
        }
    }

    setTimeout(update, 100);
}

for (let i = 0; i < 100; i++) {
    let r = document.getElementById("pointTableBody").insertRow();
    let cell = r.insertCell(0);
    cell.contentEditable = 'true';
    cell.addEventListener("keydown", keyListener);
    cell = r.insertCell(1);
    cell.contentEditable = 'true';
    cell.addEventListener("keydown", keyListener);
}

if (localStorage['way_points'] !== undefined && localStorage['way_points'] !== "") {
    let points = localStorage['way_points'].split("*");
    for (let p in points) {
        let point = points[p];
        let row = document.getElementById("pointTableBody").rows[p];
        row.cells[0].innerText = point.split(",")[0];
        row.cells[1].innerText = point.split(",")[1];
    }
    update();
}

setInterval(updateSize, 250);