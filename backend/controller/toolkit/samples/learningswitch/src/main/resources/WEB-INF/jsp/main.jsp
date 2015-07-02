<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <title>App</title>
        <meta name="description" content="">
        <meta name="viewport" content="width=device-width, initial-scale=1">

        <!-- style -->
        <link rel="stylesheet" href="/css/ext/pure/pure.css"/>
        <link rel="stylesheet" href="/css/phoenix.css"/>

        <!-- style app -->
        <link rel="stylesheet" href="/learningswitch/web/css/simple.css"/>

        <!-- scripts -->
        <script data-main="/learningswitch/web/js/main" src="/js/ext/requirejs/require.js"></script>

    </head>

    <body>


        <h2> Learning Switch </h2>

        <h4> Currently functioning as <div id="mode"></div> </h4>
        <button id="togglemode" class="pure-button
            button-success">Toggle</button>

        <h3> Select node </h3>
        <select id="nodeselect"> </select>
        <button id="refreshNodeList" class="pure-button
            button-success">Refresh Node List</button>

        <!-- full-width outer box  -->

        <h3> Switch Mac Table </h3>

        <div class="pure-g">
            <div class="pure-u-1 pure-u-md-1-1"> 
                <!-- split-width inner box for heaer  -->
                <table class="pure-table" id="mactable" style="">
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>MAC</th>
                            <th>Nodeconnector</th>
                        </tr>
                    </thead>
                    <tbody>
                    </tbody>
                </table>

            </div> 
        </div> <!-- outer box ends -->
        <p />
        <button id="refreshMacTable" class="pure-button button-success">Refresh</button>
        <button id="clearMacTable" class="pure-button button-error">Clear</button>


        <p /><p />



        <h3>Flow tables</h3>
        <div class="pure-g">
            <div class="pure-u-1 pure-u-md-1-1"> 
                <table class="pure-table" id="flowtable" style="">
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>Flow Details</th>
                        </tr>
                    </thead>
                    <tbody>
                    </tbody>
                </table>
            </div>
        </div>
        <p />
        <button id="refreshFlowTable" class="pure-button button-success">Refresh</button>




    </body>
</html>
