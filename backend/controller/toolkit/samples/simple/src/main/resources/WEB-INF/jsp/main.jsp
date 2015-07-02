<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>App</title>
    <meta name="description" content="">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <!-- style -->
    <link rel="stylesheet" href="/css/ext/pure.css"/>
    <link rel="stylesheet" href="/css/phoenix.css"/>

    <!-- style app -->
    <link rel="stylesheet" href="/simple/web/css/simple.css"/>

    <!-- scripts -->
    <script data-main="/simple/web/js/main" src="/js/ext/require.js"></script>
  </head>
  <body>
    <h1>FOO</h1>
    <h1>Simple OpenDaylight App</h1>
    <form class="pure-form pure-form-stacked" onsubmit="return false;">
      <legend>Simple Form</legend>
      <input type="text" placeholder="Foo" id="foo"/>
      <input type="text" placeholder="Bar" id="bar"/>
      <button type="submit" class="pure-button pure-button-primary">Submit</button>
    </form>

    <table class="pure-table">
      <thead>
        <tr>
          <th>UUID</th>
          <th>Foo</th>
          <th>Bar</th>
        </tr>
      </thead>
      <tbody>
      </tbody>
    </table>
  </body>
</html>
