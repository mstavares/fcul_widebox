var ajax = createObject();

function createObject(){

    var xmlHttp;

    if (window.XMLHttpRequest){

        try{
            xmlHttp = new XMLHttpRequest();
        }catch(e){
            xmlHttp = false;
        }
        
    }else if (window.ActiveXObject){
        
        try{
            xmlHttp = new ActiveXObject("Microsoft.XMLHTTP");
        }catch(e){
            xmlHttp = false;
        }

    }
    
    if (!xmlHttp)
        alert ("Something went wrong.")
    else
        return xmlHttp;  

}

var generate = true;
var second = 0;

function startGenerator(){
	var numClients = document.getElementsByName("nClients")[0].value;
	var numTheaters = document.getElementsByName("nTeathers")[0].value;
	var confirm = document.getElementsByName("confirm")[0].value;
	
	document.getElementById("startButton").disabled = true;
	document.getElementById("stopButton").disabled = false;
	
    ajax.onreadystatechange = function () {
        var html = ajax.responseText;
        if (ajax.readyState == 4 && ajax.status == 200) {
            if (html != "error"){
                second++;
                var content = JSON.parse(html);

                config.data.labels.push(second);
                
                config.data.datasets[0].data.push(content.requestsCompleted.length);
                config.data.datasets[1].data.push(content.previousRequests.length);
                
                window.myLine.update();
                
                for (var time in content.requestsCompleted) {
                    barChart.data.datasets[0].data[ getBin(time) ]++;
                }

                for (var time in content.previousRequests) {
                    barChart.data.datasets[0].data[ getBin(time) ]++;
                }

                window.myBar.update();
            }
            
            if (generate)
            	startGenerator();
        }
    };
    
    generate = true;
    ajax.open('GET', "generateClients?numClients=" + numClients + "&numTeathers=" + numTheaters + "&confirm=" + confirm, true);
    ajax.send();
}


window.chartColors = {
	red: 'rgb(255, 99, 132)',
	orange: 'rgb(255, 159, 64)',
	yellow: 'rgb(255, 205, 86)',
	green: 'rgb(75, 192, 192)',
	blue: 'rgb(54, 162, 235)',
	purple: 'rgb(153, 102, 255)',
	grey: 'rgb(201, 203, 207)'
};

var config = {
    type: 'line',
    data: {
        datasets: [{
            label: "Clients finished within the second",
            backgroundColor: window.chartColors.red,
            borderColor: window.chartColors.red,
            fill: false
        }, {
            label: "Previous clients finished",
            fill: false,
            backgroundColor: window.chartColors.blue,
            borderColor: window.chartColors.blue
        }]
    },
    options: {
        responsive: true,
        title:{
            display:true,
            text:'Client Requests'
        },
        tooltips: {
            mode: 'index',
            intersect: false
        },
        hover: {
            mode: 'nearest',
            intersect: true
        },
        scales: {
            xAxes: [{
                display: true,
                scaleLabel: {
                    display: true,
                    labelString: 'Time (seconds)'
                }
            }],
            yAxes: [{
                display: true,
                scaleLabel: {
                    display: true,
                    labelString: 'Number of Clients'
                }
            }]
        }
    }
};

window.onload = function() {
    var ctx = document.getElementById("canvas").getContext("2d");
    window.myLine = new Chart(ctx, config);

    var bar = document.getElementById("canvas2").getContext("2d");
    window.myBar = new Chart(bar, barChart);
};



function stopGenerator() {
	generate = false;
	document.getElementById("startButton").disabled = false;
	document.getElementById("stopButton").disabled = true;
}


function getBin(time){
    var result = Math.ceil(time/100) - 1;
    if (result > 20)
        return 20;
    else
        return result;
}



var barChart = {
    type: 'bar',
    data: {
        labels: ["]0-0.1]", "]0.1-0.2]", "]0.2-0.3]", "]0.3-0.4]", "]0.4-0.5]", "]0.5-0.6]", "]0.6-0.7]", "]0.7-0.8]", "]0.8-0.9]", "]0.9-1.0]", "]1.0-1.1]", "]1.1-1.2]", "]1.2-1.3]", "]1.3-1.4]", "]1.4-1.5]", "]1.5-1.6]", "]1.6-1.7]", "]1.7-1.8]", "]1.8-1.9]", "]1.9-2.0]", "2.0+"],
        datasets: [{
            label: 'Time to serve Clients',
            backgroundColor: window.chartColors.yellow,
            borderColor: window.chartColors.red,
            borderWidth: 1,
            data: [
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
            ]
        }]

    },
    options: {
        responsive: true,
        legend: {
            position: 'top',
        },
        scales: {
            xAxes: [{
                barPercentage: 1.0,
                categoryPercentage: 1.0,
                scaleLabel: {
                    display: true,
                    labelString: 'Time (seconds)'
                }
            }],
            yAxes: [{
                display: true,
                scaleLabel: {
                    display: true,
                    labelString: 'Number of clients'
                }
            }]
        },
        title: {
            display: false,
            text: 'Time to Serve Client'
        }
    }
};