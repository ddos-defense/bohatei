package net.dougharris.utility;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

 /**
 * Used for reading Internet primitives from a byte array
 * @author  Douglas Harris, doug@kisadvice.com
 * @version 0.1, 07/25/01
 * @see     java.io.DataInputStream
 * @see     java.io.DataInput
 * @see     java.io.ByteArrayInputStream
 */
public class PacketInputStream extends ByteArrayInputStream
  implements DataInput{
  protected DataInputStream iData;
  protected long cap=0;
  protected byte[] byteArray;

  public PacketInputStream(byte[] b){
    this(b, 0, b.length);
  }

  /**
     Because of the way constructors work,
     it cannot extend DataInputStream,
     although that would be a little cleaner for the method calls.
     That constructor must wrap a stream, so my constructor
     would have to have the ByteArrayInputStream available when called!
     @param b    the byte array
     @param off  offset into <code>b</code>
     @param len  length from the offset
  */
  public PacketInputStream(byte[] b, int off, int len){
    super(b, off, len);
    this.iData=new DataInputStream(this);
    /* just in case somebody changes BAIS!*/
    this.cap=(long)super.available();
    this.byteArray=b;
  }

  public byte[] getByteArray(){
    return this.byteArray;
  }

  /**
    @param n where the position should be, an offset from the beginning
    @exception throws IOException when the extended stream does
  */
  public void setPosition(long n) throws IOException{
    if (n>cap){
      throw new IOException("setPosition to "+n+" beyond end of stream");
    }
    iData.reset();
    skip(n);
  }

  /**
    Nothing "should" go wrong
  */
  public long getPosition(){
    long result = cap-available();
    if (result <0){
      // impossible?
    }
    return result;
  }

  /** 
    Just delegates to the constructed DataInputStream
    @exception
  */
  public final boolean readBoolean() throws IOException { 
    return iData.readBoolean();
  }

  /** 
    Just delegates to the constructed DataInputStream
    @exception
  */
  public final byte readByte() throws IOException { 
    return iData.readByte();
  }

  /** 
    Just delegates to the constructed DataInputStream
    @exception
  */
  public final int readUnsignedByte() throws IOException { 
    return iData.readUnsignedByte();
  }

  /** 
    Just delegates to the constructed DataInputStream
    @exception
  */
  public final char readChar() throws IOException { 
    return iData.readChar();
  }

  /** 
    Just delegates to the constructed DataInputStream
    @exception
  */
  public final float readFloat() throws IOException { 
    return iData.readFloat();
  }

  /** 
    Just delegates to the constructed DataInputStream
    @exception
  */
  public final double readDouble() throws IOException { 
    return iData.readDouble();
  }

  /** 
    Just delegates to the constructed DataInputStream
    @exception
  */
  public final short readShort() throws IOException { 
    return iData.readShort();
  }

  /** 
    Just delegates to the constructed DataInputStream
    @exception
  */
  public final int readUnsignedShort() throws IOException { 
    return iData.readUnsignedShort();
  }

  /** 
    Just delegates to the constructed DataInputStream
    @exception
  */
  public final int readInt() throws IOException { 
    return iData.readInt();
  }

  /** 
    Gets it as two UnsignedShorts glued together
    @exception
  */
  public final long readUnsignedInt() throws IOException { 
    long result;
    long l3= (long)iData.read();
    long l2= (long)iData.read();
    long l1= (long)iData.read();
    long l0= (long)iData.read();
    result = (l3<<24)+(l2<<16)+(l1<<8)+l0;
    return result;
  }

  /** 
    Just delegates to the constructed DataInputStream
    @exception
  */
  public final long readLong() throws IOException { 
    return iData.readLong();
  }

  /** 
    Just delegates to the constructed DataInputStream
    @param b
    @exception
  */
  public final void readFully(byte[] b) throws IOException { 
    iData.readFully(b);
  }

  /** 
    Just delegates to the constructed DataInputStream
    @param b
    @param off
    @param len
    @exception
  */
  public final void readFully(byte[] b, int off, int len) throws IOException { 
    iData.readFully(b,off,len);
  }

  /** 
    Just delegates to the constructed DataInputStream
    @exception
  */
  public final String readLine() throws IOException { 
    return iData.readLine();
  }

  /** 
    Just delegates to the constructed DataInputStream
    @exception
  */
  public final String readUTF() throws IOException { 
    return iData.readUTF();
  }

  /** 
    Just delegates to the constructed DataInputStream
    @param in
    @exception
  */
  public final String readUTF(DataInput in) throws IOException { 
    return iData.readUTF(in);
  }

  /** 
    Just delegates to the constructed DataInputStream
    @param n
    @exception
  */
  public final int skipBytes(int n) throws IOException { 
    return iData.skipBytes(n);     
  }
}
