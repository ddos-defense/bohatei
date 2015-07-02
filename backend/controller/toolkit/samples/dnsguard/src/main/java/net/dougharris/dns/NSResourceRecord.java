package net.dougharris.dns;

public class NSResourceRecord extends ResourceRecord{
  private String name;

  public void setNSName(String name){
    this.name = name;
  }
  
  public String getNSName(){
    return name;
  }

  public String dataToString(){
    return getNSName();
  }
}
