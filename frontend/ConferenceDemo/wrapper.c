  #include <stdlib.h>
  #include <stdio.h>
  #include <sys/types.h>
  #include <unistd.h>

  int
  main (int argc, char *argv[])
  {
     setuid (0);

     /* WARNING: Only use an absolute path to the script to execute,
      *          a malicious user might fool the binary and execute
      *          arbitary commands if not.
      * */
     if (strcmp(argv[1], "syn_flood") == 0)
         system ("/bin/sh /var/www/ConferenceDemo/configure.sh syn");
     else if (strcmp(argv[1], "udp_flood") == 0)
         system ("/bin/sh /var/www/ConferenceDemo/configure.sh udp");
     else if (strcmp(argv[1], "dns_amp") == 0)
         system ("/bin/sh /var/www/ConferenceDemo/configure.sh dns");
     else if (strcmp(argv[1], "ele_flow") == 0)
         system ("/bin/sh /var/www/ConferenceDemo/configure.sh ele");
     return 0;
   }
