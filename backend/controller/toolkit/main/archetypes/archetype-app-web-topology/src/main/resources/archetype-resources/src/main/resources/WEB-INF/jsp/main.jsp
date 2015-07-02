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
    <title>OpenDaylight Phoenix - App</title>
    <meta name="description" content="">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <!-- style -->
    <link rel="stylesheet" href="http://yui.yahooapis.com/pure/0.4.2/pure-min.css"/>
    <style type="text/css">
      .link {
        stroke: #999;
        stroke-opacity: 0.6;
      }
    </style>

    <!-- scripts -->
    <script src="http://code.jquery.com/jquery-2.1.0.min.js"></script>
    <script src="http://d3js.org/d3.v3.min.js" charset="utf-8"></script>
    <script src="/js/assets/phoenix-topology.js"></script>
    <script src="/${artifactId}/js/sample.js"></script>
  </head>
  <body>
    <p>${artifactId}</p>
    <div id="topology"></div>
  </body>
</html>
