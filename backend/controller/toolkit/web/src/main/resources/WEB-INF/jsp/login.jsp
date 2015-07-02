<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%>
<%@ page import="java.net.URL"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>OpenDaylight Toolkit - Login</title>
    <meta name="description" content="">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    
    <!-- style -->
    <link rel="icon" href="/img/favicon.ico"/>
    <link rel="stylesheet" href="http://yui.yahooapis.com/pure/0.4.2/pure-min.css"/>
    <link rel="stylesheet" href="/css/login.css"/>
    
    <!-- scripts -->
    <script src="http://code.jquery.com/jquery-2.1.0.min.js"></script>
  </head>
  <body>
    <div id="header"></div>
    <div id="container">
      <form action="<c:url value='j_security_check' />" id="form" method="post" class="pure-form">
        <fieldset>
          <input type="text" name="j_username" placeholder="Username">
          <input type="password" name="j_password" placeholder="Password">
          <button class="pure-button pure-button-primary" type="submit">Log In</button>
        </fieldset>
      </form>
    </div>
    <script type="text/javascript">
      $('input').first().focus();
    </script>
  </body>
</html>
