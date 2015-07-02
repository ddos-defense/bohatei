#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="java.net.URL"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>App</title>
    <meta name="description" content="">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <!-- style -->
    <!-- <link rel="stylesheet" href="http://yui.yahooapis.com/pure/0.4.2/pure-min.css"/> -->
    <link rel="stylesheet" href="/css/ext/pure/pure.css"/>
    <link rel="stylesheet" href="/css/global.css"/>
    <link rel="stylesheet" href="/css/header.css"/>
    <link rel="stylesheet" href="/css/menu.css"/>
    <link rel="stylesheet" href="/css/core.css"/>

    <!-- scripts -->
    <script data-main="/app/js/main" src="/js/ext/requirejs/require.js"></script>
    <!-- 
    <script src="http://code.jquery.com/jquery-2.1.0.min.js"></script>
    <script src="/js/core.js"></script>
    -->
  </head>
  <body>
    <h3>App</h3>
    <div id="main"></div>
  </body>
</html>
