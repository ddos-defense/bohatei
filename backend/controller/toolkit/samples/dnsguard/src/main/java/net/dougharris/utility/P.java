package net.dougharris.utility;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

public class P extends java.util.HashMap{

  public static void rint(String s){
    System.out.print(s);
  }

  public static void rint(long n){
    System.out.print(""+n);
  }

  public static void rintln(String s){
    System.out.println(s);
  }

  public static void rintln(long n){
    System.out.println(""+n);
  }

  public static void error(String s){
    System.err.println(s);
  }

  public static void exception(Exception x){
    error(x.getClass().getName()+ " says " + x.getMessage());
  }

  public static void exception(String s, Exception x){
    error(s+": "+x.getClass().getName()+ " says " + x.getMessage());
  }

  public static void exception(Exception x, int code){
    P.exception(x);
    System.exit(code);
  }

  public static void exception(String s,Exception x, int code){
    P.exception(s, x);
    P.exit(code);
  }

  public static void exit(){
    System.exit(0);
  }
  public static void exit(int code){
    System.exit(code);
  }

  public static void exit(Exception x){
    exit(x, 1);
  }

  public static void exit(Exception x, int code){
    if (null!=x){
      exception(x);
    }
    exit(code);
  }

  public static void ause(long d){
    try{
      Thread.currentThread().sleep(d);
    } catch(java.lang.InterruptedException x){
    }
  }

  public static void usage(String usage){
    System.out.println(usage);
    System.exit(1);
  }

  public static void usage(String error, String usage){
    System.out.println(error);
    System.out.println("Usage: "+usage);
    System.exit(1);
  }

  public boolean getFlag(String flag){
    return null != this.get(flag);
  }

  public String getProperty(String flag){
    return (String)this.get(flag);
  }

  public String[] getParams(){
    return (String[])this.get(" ");
  }

  public String getErrors(){
    return (String)this.get("*");
  }

  public static P arseArgs(String options, String[] args){
    return (new P()).parse(options, args);
  }

  public static P arseArgs(String options, String[] args, String usage){
    P p = (new P()).parse(options, args);
    if (0!= p.getErrors().length()){
      P.rintln(p.getErrors());
      P.usage(usage);
    }
    return p;
  }

  protected P parse(String options, String[] args){
    // a- means optional flag
    // a+ means required flag
    // a; means optional option, must be followed by a value
    // a: means required option, must be followed by a value
    HashMap cmdFlags = new HashMap();
    String flag;
    String nextFlag=null;
    StringBuffer errors=new StringBuffer();
    /**
      First go through options to see what should be in args
    */
    for(int which=0;which<options.length();which++){
      flag = "-"+options.substring(which,which+1);
      if(which+1<options.length()){
        nextFlag=options.substring(which+1,which+2);
        if (nextFlag.equals("-")){
          cmdFlags.put(flag,nextFlag);
        } else
        if (nextFlag.equals("+")){
          cmdFlags.put(flag,nextFlag);
          /*
            mark that it is required
            if found this will be overwritten by -
          */
          this.put(flag,nextFlag);
        } else
        //JDH changed to "_" from ";" because too many cmdlines mess up ;
        if (nextFlag.equals("_")){
          cmdFlags.put(flag,nextFlag);
        } else
        //JDH changed to "." from ":" because too many cmdlines mess up ;
        if (nextFlag.equals(".")){
          cmdFlags.put(flag," "); //JDH changed this from ":"
          /*
            mark that it is required
            if found this will be overwritten by value
            JDH should use " " so it cannot be the same as a value
          */
          this.put(flag," "); // mark that it is required
        } else {
          System.out.println("Bad symbol "+nextFlag+"in option string");
        }
        which++;
      } else {
        System.out.println("Missing symbol in option string at "+which);
      }
    }

    int arg=0;
    for(;arg<args.length;arg++){
      if (!args[arg].startsWith("-")){
        break;
      }
      flag = args[arg];
      /*
        This should tell it to quit looking for flags or options
      */
      if (flag.equals("--")){
        arg++;
        break;
      }
      if (!(cmdFlags.containsKey(flag))){
        errors.append("\nbad flag "+flag);
        continue;
      }
      if (((String)cmdFlags.get(flag)).equals("-")){
      this.put(flag,"-");
        continue;
      }
      if (((String)cmdFlags.get(flag)).equals("+")){
      this.put(flag,"-");// turns off the + because it was found
        continue;
      }
      if (!(arg+1<args.length)){
        errors.append("\nMissing value for "+flag);
        continue;
      }
      arg++;
      this.put(flag,args[arg]);
    }
    String[] params=null;
    params = new String[args.length - arg];

    int n=0;
    // reverse these so they come back in the right order!
    for(;arg<args.length;arg++){
      params[n++] = args[arg];
    }
    Iterator k = null;
    Map.Entry e = null;
    if (this.containsValue("+")){
      // can iterate through to see which ones
      k = this.entrySet().iterator();
      while (k.hasNext()){
        if ("+".equals((String)(e=(Map.Entry)k.next()).getValue())){
          errors.append("\nThe required flag "+(String)e.getKey()+" was not supplied.");
        };
      }
    } 
    /*
      Should change this to " " in accordance with remark above
    */
      //JDH changed to " " from ":" in both spots below
    if (this.containsValue(" ")){
      // can iterate through to see which ones
      k = this.entrySet().iterator();
      while (k.hasNext()){
        if (" ".equals((String)(e=(Map.Entry)k.next()).getValue())){
          errors.append("\nThe required option "+(String)e.getKey()+" was not supplied.");
        }
      }
    }
    this.put(" ",params);
    this.put("*",errors.toString());
    return this;
  }
}
