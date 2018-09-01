function getUserName(id) {
    var key = "username";
    var value ="";
    var allcookies = document.cookie.split("; ");
    for(var i = 0; i < allcookies.length; i++){
        var arr = allcookies[i].split("=");
        if(arr[0] == key){
            value = arr[1]
        }
    }
    if (value == ""){
        value = "登陆";
    }
    console.log(allcookies);
    document.getElementById(id).innerHTML = value;
}