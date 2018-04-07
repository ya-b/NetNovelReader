<%@ page pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>rule editor</title>
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
    <h3>-------------import---------------------</h3>
    <form action="ImportSitePreference" method="post" enctype="multipart/form-data">
        <input type="file" name="fileupload"/><br />
        <input type="submit" value="import"/>
    </form>
    <h3>-------------query---------------------</h3>
    <form action="GetSitePreference" accept-charset="utf-8">
        hostname:<input type="text" name="hostname"><br>
        <input type="submit">
    </form>
    <h3>-------------add---------------------</h3>
    <form action="AddSitePreference" accept-charset="utf-8">
        hostname:<input type="text" name="hostname"><br>
        catalogSelector:<input type="text" name="catalog_selector"><br>
        chapterSelector:<input type="text" name="chapter_selector"><br>
        catalogFilter:<input type="text" name="catalog_filter"><br>
        chapterFilter:<input type="text" name="chapter_filter"><br>
        searchUrl:<input type="text" name="search_url"><br>
        redirectFileld:<input type="text" name="redirect_fileld"><br>
        redirectUrl:<input type="text" name="redirect_selector"><br>
        noRedirectUrl:<input type="text" name="no_redirect_selector"><br>
        redirectName:<input type="text" name="redirect_name"><br>
        noRedirectName:<input type="text" name="no_redirect_name"><br>
        redirectImage:<input type="text" name="redirect_image"><br>
        noRedirectImage:<input type="text" name="no_redirect_image"><br>
        charset:<input type="text" name="charset"><br>
        <input type="submit">
    </form>
    <h3>-------------delete---------------------</h3>
    <form action="DeleteSitePreference" accept-charset="utf-8">
        hostname:<input type="text" name="hostname"><br>
        <input type="submit">
    </form>
</body>
</html>