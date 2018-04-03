<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<html>
<head>
    <meta charset="UTF-8">
</head>
<body>
<%
    Cookie cookies[]=request.getCookies();
    String username = new com.netnovelreader.service.UserAuthorityService().getUserName(cookies);
%>
<a href="login.html"><%= username == null ? "登陆" : username %></a> <a href="logout">退出</a><br/>
<h3>-------------searchBook---------------------</h3>
<form action="search" accept-charset="utf-8">
    书名:<input type="text" name="bookname"><br>
    <input type="submit">
</form>
</body>
</html>
