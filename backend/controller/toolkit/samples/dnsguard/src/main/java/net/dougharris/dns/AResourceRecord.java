package net.dougharris.dns;

import net.dougharris.utility.DumpHex;

public class AResourceRecord extends ResourceRecord{
  private byte[] b;

  public String dataToString(){
    return DumpHex.dottedDecimalPrint(getData());
  }

  public void setData(byte[] b){
    this.b=b;
  }

  public byte[] getData(){
    return b;
  }
}
