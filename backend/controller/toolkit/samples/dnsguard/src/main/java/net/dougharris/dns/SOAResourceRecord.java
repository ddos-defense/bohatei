package net.dougharris.dns;


public class SOAResourceRecord extends ResourceRecord{
  private long serial; //somehow this is unsigned 32 bit
  private long refresh;
  private long retry;
  private long expire;
  private long minimum;
  private String mname;
  private String rname;

  public void setMname(String mname){
    this.mname = mname;
  }

  public String getMname(){
    return mname;
  }

/**
This pair of setMname/getRname does not quite give the identity.
I decided to convert the email name to an email name, rather
than leaving in the first dot.
Of course you could set it with an @ to begin with.
*/
  public void setRname(String rname){
    this.rname = rname;
  }

  public String getRname(){
    StringBuffer b= new StringBuffer(rname);
    int j = rname.indexOf(".");
    b.replace(j,j+1,"@"); // replaces the first . with @
    b.setLength(b.length()-1); // drops the last .
    return b.toString();
  }

  public void setSerial(long serial){
    this.serial=serial;
  }

  public long getSerial(){
    return serial;
  }

  public void setRefresh(long refresh){
    this.refresh=refresh;
  }

  public long getRefresh(){
    return refresh;
  }

  public void setRetry(long retry){
    this.retry=retry;
  }

  public long getRetry(){
    return retry;
  }

  public void setExpire(long expire){
    this.expire=expire;
  }

  public long getExpire(){
    return expire;
  }

  public void setMinimum(long minimum){
    this.minimum=minimum;
  }

  public long getMinimum(){
    return minimum;
  }

  public String dataToString(){
    StringBuffer b = new StringBuffer();
      b.append("RName: ");
      b.append(getRname());
      b.append('\n');
      b.append("MName: ");
      b.append(getMname());
      b.append('\n');
      b.append("serial: ");
      b.append(""+getSerial());
      b.append('\n');
      b.append("refresh: ");
      b.append(""+getRefresh());
      b.append('\n');
      b.append("retry: ");
      b.append(""+getRetry());
      b.append('\n');
      b.append("expire: ");
      b.append(""+getExpire());
      b.append('\n');
      b.append("minimum: ");
      b.append(""+getMinimum());
      b.append('\n');
    return b.toString();
  }
}
