<%@ page pageEncoding="UTF-8"%>
<html>
<head>
    <meta charset="UTF-8">
    <script type="text/javascript">
        var time = 4;
        function returnUrlByTime() {
            window.setTimeout('returnUrlByTime()', 1000);
            time = time - 1;
            document.getElementById("time").innerHTML = "[ " + time + " ] 秒后跳转";
        }
    </script>
</head>
<body onload="returnUrlByTime()">
    <%
        String redirectUrl = (String) session.getAttribute("redirect");
        String result = (String) session.getAttribute("result");
        response.setHeader("Refresh", "3;URL=" + redirectUrl);
    %>
    <%=result != null ? result : "" %>
    <br/>
    <br/>
    <br/>
    <br/>
    <div id=time ></div>
</body>
</html>
