<%@ page pageEncoding="UTF-8"%>
<html>
<head>
    <meta charset="UTF-8">
    <title>NetNovelReader</title>
    <script type="text/javascript">
        function getUserName(){
            var name = "";
            var cookie_name="name"
            var allcookies = document.cookie;
            var name_start = allcookies.indexOf(cookie_name);
            if(name_start != -1){
                name_start += cookie_name.length + 1;
                var name_end = allcookies.indexOf("=|=");
                name = allcookies.substring(name_start, name_end);
            }
            if (name == ""){
                name = "登陆";
            }
            document.getElementById("username").innerHTML = name;
        }
    </script>
</head>
<body onload="getUserName()">
<a href="login.jsp"><div id=username></div></a><a href="logout">退出</a><br/>
<a href="upload.jsp">保存阅读记录</a><br />
<a href="download">同步阅读记录</a><br />
<h3>-------------searchBook---------------------</h3>
    <form action="search" accept-charset="utf-8">
        书名:<input type="text" name="bookname"><br>
        <input type="submit">
    </form>
</body>
</html>
