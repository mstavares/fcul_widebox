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


function startClient(){
	document.getElementById("theaterInfo").style.display = 'none';
	document.getElementById("theaterList").style.display = 'block';
	
    var clientId = Math.floor(Math.random() * 99999) + 1;
    
    document.getElementById("buttons").innerHTML = 
    	'<input class="btn" type="submit" value="Confirm Reservation" onclick="confirmReservation('
    	+ clientId + 
    	'); return false;"></input><input class="btn" type="submit" value="Cancel Reservation" onclick="cancelReservation(' 
    	+ clientId + '); return false;"></input>';
    
    ajax.onreadystatechange = function () {
        var html = ajax.responseText;
        if (ajax.readyState == 4 && ajax.status == 200) {
        	
            var content = JSON.parse(html);
            var str = 'Choose a theater:';
            
            for (var key in content) {
                str += '<br /><a href="#" onclick="getTheaterInfo(' + content[key] + ', ' + clientId + ')">' + key + '</a>';
            }
            
            document.getElementById("theaterList").innerHTML = str;
        }
    };
    
    ajax.open('GET', "getTheaters?clientId=" + clientId, true);
    ajax.send();
}


function getTheaterInfo(theaterId, clientId){
    document.getElementById("theaterList").style.display = 'none';
    document.getElementById("theaterInfo").style.display = 'block';
    
    ajax.onreadystatechange = function () {
        var html = ajax.responseText;
        if (ajax.readyState == 4 && ajax.status == 200) {
            var content = JSON.parse(html);

            var str = '<table id="theater">';
            
            //str += content.items[0].ID;

            for (var i = 0; i < content.length; i++){
                str += '<tr>';
                for (var j = 0; j < content[i].length; j++){
                    if (content[i][j]['seat'] === 'SELF')
                        str += '<td class="self">█</td>';
                    else if (content[i][j]['seat'] === 'RESERVED')
                        str += '<td class="reserved">█</td>';
                    else if (content[i][j]['seat'] === 'OCCUPIED')
                        str += '<td class="occupied">█</td>';
                    else
                        str += '<td class="free"><a href="#" onclick="reserveSeat(' 
                        + clientId + ', '  + theaterId + ', ' + i + ', ' + j + ')">█</a></td>';
                }
                str += '</tr>';
            }

            str += '</table>';
            
            document.getElementById("theaterTable").innerHTML = str;
        }
    };
    
    ajax.open('GET', "getTheaterInfo?clientId=" + clientId + "&theaterId=" + theaterId, true);
    ajax.send();
}


function reserveSeat(clientId, theaterId, row, column){
    ajax.onreadystatechange = function () {
        var html = ajax.responseText;
        if (ajax.readyState == 4 && ajax.status == 200) {
            if (html === "true")
                getTheaterInfo(theaterId, clientId);
        }
    };
    
    ajax.open('GET', "reserveSeat?clientId=" + clientId + "&theaterId=" + theaterId + "&row=" + row + "&column=" + column, true);
    ajax.send();
}


function confirmReservation(clientId){
    ajax.onreadystatechange = function () {
        var html = ajax.responseText;
        if (ajax.readyState == 4 && ajax.status == 200) {
            if (html === "true"){
            	alert("You reservation was confirmed.")
            	document.getElementById("theaterInfo").style.display = 'none';
            }
        }
    };
    
    ajax.open('GET', "acceptReservedSeat?clientId=" + clientId, true);
    ajax.send();
}


function cancelReservation(clientId){
    ajax.onreadystatechange = function () {
        var html = ajax.responseText;
        if (ajax.readyState == 4 && ajax.status == 200) {
            if (html === "true"){
            	alert("You reservation was cancelled.")
            	document.getElementById("theaterInfo").style.display = 'none';
            }
        }
    };
    
    ajax.open('GET', "cancelReservation?clientId=" + clientId, true);
    ajax.send();
}