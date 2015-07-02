<?php
   $output = (htmlspecialchars($_POST["attack"]));
   shell_exec("./wrapper {$output}");
?>

