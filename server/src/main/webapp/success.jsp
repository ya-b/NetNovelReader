<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<html>
<head>
    <meta charset="UTF-8">
</head>
<body>
    <h2>success!</h2>
    <%
        String str = (String)session.getAttribute("result");
    %>
    Query Result :<%=str != null ? str : "result is null"%>
</body>
</html>
