const colors = ['--red', '--blue', '--green', '--purple', '--yellow', '--pink', '--orange'];

const originalOnClick = Chart.defaults.global.legend.onClick;
let lastLegendItemClickTime = 0;
let lastLegendItem;
Chart.defaults.global.legend.onClick = function (e, legendItem) {
    lastLegendItemClickTime = Date.now();
    lastLegendItem = legendItem;
    originalOnClick.call(this, e, legendItem);
};

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

function openMenu() {
    if (Date.now() - lastLegendItemClickTime < 500) {
        let graphValues = JSON.parse(sessionStorage['graph_values']);
        graphValues[lastLegendItem.text] = undefined;
        sessionStorage['graph_values'] = JSON.stringify(graphValues);

        setTimeout(function () {
            chart.data.datasets.splice(lastLegendItem.datasetIndex, 1);
            chart.update();
        }, 50);
    } else {
        setVisibility(document.getElementById("menuPopup"), true);
    }
}

function closeMenu() {
    setVisibility(document.getElementById("menuPopup"), false);
}

function update() {
    let graphValues = JSON.parse(sessionStorage['graph_values']);
    for (let dataset of Object.getOwnPropertyNames(graphValues)) {
        if (graphValues[dataset].updated) {
            if (dataset.startsWith("gr_")) {
                addData(dataset, graphValues[dataset].value);
                graphValues[dataset] = undefined;
                sessionStorage['graph_values'] = JSON.stringify(graphValues);
            } else {
                if ((typeof graphValues[dataset].value) === "string" && graphValues[dataset].value.includes(",")) {
                    addData(dataset, [{
                        x: graphValues[dataset].value.split(",")[0],
                        y: graphValues[dataset].value.split(",")[1]
                    }]);
                } else {
                    addData(dataset, [{
                        x: new Date().getTime() % 1000000,
                        y: graphValues[dataset].value
                    }]);
                }
            }
        }
    }
}

function updateSize() {
    chartCanvas.style.width = document.body.clientWidth + 'px';
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

    let color = colors[chart.data.datasets.length % 7];
    let dataset = {
        label: label,
        labelColor: getComputedStyle(document.documentElement).getPropertyValue('--foreground'),
        borderColor: getComputedStyle(document.documentElement).getPropertyValue(color),
        backgroundColor: getComputedStyle(document.documentElement).getPropertyValue(color),
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

function setVisibility(x, b) {

    if (b) x.style.visibility = "visible";
    else x.style.visibility = "hidden";

    let children = x.children;

    for (let c = 0; c < children.length; c++) {
        if (b) children[c].style.visibility = "visible";
        else children[c].style.visibility = "hidden";
    }
}

setInterval(updateSize, 250);

setInterval(update, 1000 / 60);