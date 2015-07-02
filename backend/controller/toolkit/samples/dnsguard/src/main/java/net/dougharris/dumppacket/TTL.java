package net.dougharris.dumppacket;

public class TTL{
  long seconds;
  long minutes;
  long hours;
  long days;
  long weeks;
  long years;

  public TTL(long time){
    seconds=time%60;
    minutes=time/60;
    hours=minutes/60;
    minutes=minutes-60*hours;
    days=hours/24;
    hours=hours-24*days;
    weeks=days/7;
    days=days-weeks*7;
    years=weeks/52;
    weeks=weeks-years*52;
  }

  public String toString(){
    return(years+"y-"+weeks+"w-"+days+"d-"+hours+"h-"+minutes+"m-"+seconds+"s");
  }

  public static void main(String[] args){
    for (int j=0;j<args.length;j++){
      System.out.println(new TTL(Long.parseLong(args[j])));
    }
  }
}
/*
y=31449600
w=604800
d=86400
h=3600
m=60
*/
