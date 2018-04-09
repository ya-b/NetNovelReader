<%@ page pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Upload File</title>
</head>
<body>
    <form action="upload" method="post" enctype="multipart/form-data">
        <input type="file" name="fileupload"/><br />
        <input type="submit" value="upload file"/>
    </form>
</body>
</html>