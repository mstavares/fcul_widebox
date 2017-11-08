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


function loadServers(){

    ajax.onreadystatechange = function () {
        var html = ajax.responseText;
        if (ajax.readyState == 4 && ajax.status == 200) {
        	
            var content = JSON.parse(html);
            var str = '';
            var count = 0;
            
            for (var serverType in content) {
                str += '<div class="server-type">' + serverType;
                for (var i = 0; i < content[serverType].length; i++){
                	count++;
                    str += '<br /><div class="server">';
                    str += 'Ip: ' + content[serverType][i].ip;
                    str += '<br />Port: ' + content[serverType][i].port;
                    str += '<br />Status: <div id="serverId' + count + '"></div>';
                    autoRefresh(count, content[serverType][i].ip, content[serverType][i].port);
                    str += '<br /><input class="btn" type="submit" value="Start" onclick="startServer(\'' + content[serverType][i].ip + '\','  + content[serverType][i].port + '); return false;"></input>';
                    str += '<input class="btn" type="submit" value="Stop" onclick="stopServer(\'' + content[serverType][i].ip + '\','  + content[serverType][i].port + '); return false;"></input>';
                    str += '</div>';
                }
                str += '</div>';
            }
            
            document.getElementById("server-table").innerHTML = str;
        }
    };
    
    ajax.open('GET', "getServers", true);
    ajax.send();
}


function startServer(ip, port){
    ajax.onreadystatechange = function () {
        var html = ajax.responseText;
        if (ajax.readyState == 4 && ajax.status == 200) {
            if (html === "true")
            	alert("Server started.");
        }
    };
    
    ajax.open('GET', "startServer?ip=" + ip + "&port=" + port, true);
    ajax.send();
}


function stopServer(ip, port){
    ajax.onreadystatechange = function () {
        var html = ajax.responseText;
        if (ajax.readyState == 4 && ajax.status == 200) {
            if (html === "true")
            	alert("Server stopped.");
            //TODO fix this
        }
    };
    
    ajax.open('GET', "stopServer?ip=" + ip + "&port=" + port, true);
    ajax.send();
}


function isOnline(serverId, ip, port){
    ajax.onreadystatechange = function () {
        var html = ajax.responseText;
        if (ajax.readyState == 4 && ajax.status == 200) {
            if (html === "true"){
            	document.getElementById("serverId" + serverId).className = "online";
            	document.getElementById("serverId" + serverId).innerHTML = "online";
            }else{
            	document.getElementById("serverId" + serverId).className = "offline";
            	document.getElementById("serverId" + serverId).innerHTML = "offline";
            }
            	
        }
    };
    
    ajax.open('GET', "isServerOnline?ip=" + ip + "&port=" + port, true);
    ajax.send();
}


async function autoRefresh(serverId, ip, port){
    await sleep(serverId * 1000);
    console.log("Checking status of serverId: " + serverId);
    isOnline(serverId, ip, port);
    await sleep(10000);
    autoRefresh(serverId, ip, port);
}


function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}