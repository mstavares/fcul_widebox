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
    document.getElementById("theaterList").innerHTML = "started";
    //TODO ajax call and fill the theater list
}


function confirmReservation(){
    document.getElementById("theaterList").innerHTML = "confirmed";
    //TODO ajax call to confirm
}


function cancelReservation(){
    document.getElementById("theaterList").innerHTML = "canceled";
    //TODO ajax call to cancel
}