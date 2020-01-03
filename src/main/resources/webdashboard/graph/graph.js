//js code to run the graph page of the webdashboard,
//which graphs data from the robot code using the values page

//Array of color strings from style.css used sequentially for each dataset put on the graph
const colors = ['--red', '--blue', '--green', '--purple', '--yellow', '--pink', '--orange'];

//Code used to remove a dataset from the graph on double click of the legend entry
//Overrides the original legend onClick
//Needed because chart.js doesn't support native double click events

//Keeps track of the last time a legend click event occurred
//If a double click on the body occurs rapidly afterward the event can be connected to the legend
let lastLegendItemClickTime = 0;

//The last legend element that was clicked
let lastLegendItem;

//Keeps track of the original legend onClick so that it can still be called
//The original onClick toggles if the dataset is show on the graph
const originalLegendOnClick = Chart.defaults.global.legend.onClick;

//Overwrites the default legend onClick event
Chart.defaults.global.legend.onClick = function (e, legendItem) {
    //Records the time when the click occurs
    lastLegendItemClickTime = Date.now();

    //Tracks the element on which the click occurred
    lastLegendItem = legendItem;

    //Calls the original legend onClick event allowing for the default show/hide dataset behavior to be maintained
    originalLegendOnClick.call(this, e, legendItem);
};

//Gets the chart canvas to be used for the graph from the html page
let graphCanvas = document.getElementById("graph");

//Create the graph using chart.js
let graph = new Chart(graphCanvas, {
    //Configures graph to give hover text over points
    responsive: true,
    //Configures graph to allow for access ratio changes
    maintainAspectRatio: false,
    //Configures graph type to scatter plot
    type: 'scatter',
    data: {
        //Sets the datasets to an empty array to be filled later
        datasets: []
    },
    options: {
        legend: {
            labels: {
                //Set the graph legend text to the --white color specified in style.css
                fontColor: getComputedStyle(document.documentElement).getPropertyValue('--white'),
                //Set the font size of the labels in the legend to 18px
                fontSize: 18
            }
        },
        scales: {
            yAxes: [{
                ticks: {
                    //Sets the graph yAxes tick color to the --graph color specified in style.css
                    fontColor: getComputedStyle(document.documentElement).getPropertyValue('--graph')
                }
            }],
            xAxes: [{
                ticks: {
                    //Sets the graph xAxes tick color to the --graph color specified in style.css
                    fontColor: getComputedStyle(document.documentElement).getPropertyValue('--graph')
                }
            }]
        },
        animation: {
            //Removes graph dataset change animations
            duration: 0
        }
    }
});

//Called when a double click occurs on the page body
function doubleClick() {
    if (Date.now() - lastLegendItemClickTime < 500) {
        //If a click on the legend occurred less than 500 milliseconds ago trigger legend event

        //Get the graph values put into sessionStorage by the values pages
        let graphValues = JSON.parse(sessionStorage['graph_values']);

        //Set the dataset on which the double click occurred to undefined to tell the values page it has been removed
        graphValues[lastLegendItem.text] = undefined;

        //Write the values new data into sessionStorage
        sessionStorage['graph_values'] = JSON.stringify(graphValues);

        setTimeout(function () {
            //Wait 50 milliseconds to ensure sessionStorage update complete the update the graph

            //Remove the dataset on which the double click occurred from the graph
            graph.data.datasets.splice(lastLegendItem.datasetIndex, 1);

            //Update the graph to show changes
            graph.update();
        }, 50);
    } else {
        //If doubleClick not related to the legend open menu
        setVisibility(document.getElementById("menuPopup"), true);
    }
}

//Closes the menu, called when the close button is pressed
function closeMenu() {

    //Hide the menu
    setVisibility(document.getElementById("menuPopup"), false);
}

//Called periodically to add another data point to each dataset on the graph
//Uses data send to the values page of the webdashboard
function update() {
    //Get the graph values put into sessionStorage by the values pages
    let graphValues = JSON.parse(sessionStorage['graph_values']);

    //Loop through each dataset in graphValues
    for (let dataset of Object.getOwnPropertyNames(graphValues)) {

        //If the dataset has been updated then add it to the graph
        if (graphValues[dataset].updated) {
            if (dataset.startsWith("gr_")) {
                //If the value is a full graph dataset then add the dataset to the graph as is

                //Adding the dataset to the graph with its name and values
                addData(dataset, graphValues[dataset].value);

                //Clear dataset from sessionStorage
                //Prevents the dataset from being updated again until the values page adds it again
                graphValues[dataset] = undefined;
                sessionStorage['graph_values'] = JSON.stringify(graphValues);
            } else {
                if ((typeof graphValues[dataset].value) === "string" && graphValues[dataset].value.includes(",")) {
                    //If the dataset includes point with x and y values then add a point with those values

                    //Adding the dataset to the graph with its name creating x and v values
                    addData(dataset, [{

                        //Sets x value to the first value of the point
                        x: graphValues[dataset].value.split(",")[0],

                        //Sets y value to the second value of the point
                        y: graphValues[dataset].value.split(",")[1]
                    }]);
                } else {
                    //If the dataset only includes a y-value use the time for the x-value
                    //This allows the user to see how the variable changes over time

                    //Adding the dataset to the graph with its name creating x and v values
                    addData(dataset, [{

                        //Set the x value to the current time, using modulo to prevent the value from getting to huge
                        x: new Date().getTime() % 1000000,

                        //Set the y to the data value
                        y: graphValues[dataset].value
                    }]);
                }
            }
        }
    }
}

//Adds data to a dataset, creating a new dataset if needed
//"label" is the label of the dataset
//"data" is the data to be added to the dataset
function addData(label, data) {

    //Loop through all of the datasets on the graph to try and find a dataset with a matching label
    for (let dataset of graph.data.datasets) {

        if (dataset.label === label) {
            //If the labels match add the new data to the dataset

            //Loop through each point and add it to the dataset
            for (let point of data) {
                dataset.data.push(point);
            }

            //Update the graph to show changes
            graph.update();
            return;
        }
    }

    //If no dataset is found matching the label create a new dataset

    //Pick the color for the dataset based upon the current number of datasets
    //This ensures each dataset will have a different color from the colors array
    //The colors will wrap around if there are 7 or more datasets
    let color = colors[graph.data.datasets.length % 7];

    //Create the dataset
    let dataset = {

        //Set the dataset label the the label passed in
        label: label,

        //Sets the label to the --foreground color specified in style.css
        labelColor: getComputedStyle(document.documentElement).getPropertyValue('--foreground'),

        //Sets the point border and background colors to the color picked from the colors array
        borderColor: getComputedStyle(document.documentElement).getPropertyValue(color),
        backgroundColor: getComputedStyle(document.documentElement).getPropertyValue(color),

        //Sets the radius of the points to 5px
        pointRadius: 5,

        //Sets the data in the dataset to the data passed in
        data: data
    };

    //Add the dataset to the graph
    graph.data.datasets.push(dataset);

    //Update the graph to show changes
    graph.update();
}

//Due to some oddities in chart.js, this function correctly sizes the graph for the page
function updateSize() {
    //Sets the graph width to the page body width
    graphCanvas.style.width = document.body.clientWidth + 'px';

    //Sets the graph height to the page body height minus the height of the tab bar
    graphCanvas.style.height = (document.body.clientHeight - 10) + 'px';
}

//Removes all datasets from the graph
function removeData() {
    //Clears all datasets
    graph.data.datasets = [];

    //Updates the chart to display to changes
    graph.update();
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

//Due to some oddities in chart.js, this makes sure the graph is sized correctly every 250 milliseconds
setInterval(updateSize, 250);

//Updates each dataset on the graph every 16 milliseconds
//Similar to the speed of robot updates to avoid missing data while not using too much processing power
setInterval(update, 1000 / 60);