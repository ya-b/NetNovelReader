function upload(id,remote) {
    var token = localStorage.getItem("Authorization");
    $.ajax({
        beforeSend:function(request){
            request.setRequestHeader("Authorization", token);
        },
        type: "POST",
        dataType: "json",
        url: remote,
        data: $(id).serialize(),
        success: function (result) {
            console.log(result);
            alert("success");
        },
        error: function () {
            console.log("error!!");
            alert("failed");
        }
    });
}