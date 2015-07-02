package net.dougharris.dns;

import net.dougharris.utility.P;
import net.dougharris.utility.PacketInputStream;
import java.net.InetAddress;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.BindException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.lang.SecurityException;
import java.net.UnknownHostException;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.util.StringTokenizer;

public class RFC1034{
// serverHost serverPort recordType recordName
  
  public static void usage(){
    System.out.println("Usage: DNSQuery serverHostName recordType domainName");
  }

  public static void main(String[] args) throws Exception{
    boolean recursion = true;
    if (args.length==0){
      usage();
      System.exit(0);
    }

    if (args.length!=3){
      usage();
      System.exit(1);
    }
    String serverHostName=(args[0]);
    String recordType = args[1];
    String domainName = args[2];

    InetAddress serverHostAddress=null;
    // Clearly serverPort could be an argument
    int serverPort = 53;
    byte[] bs=null; // will carry the query
    byte[] br=new byte[512];
    DatagramPacket ps=null;
    DatagramPacket pr=null;
    DatagramSocket s=null;
    StringTokenizer k = null;

    ByteArrayOutputStream ob=new ByteArrayOutputStream();
    DataOutputStream o=new DataOutputStream(ob);
    try{
    /*
      Getting the socket ready
    */
      s=new DatagramSocket(0,InetAddress.getLocalHost());
    /*
      Finding out where the packet should go
    */
      serverHostAddress=InetAddress.getByName(serverHostName);
    /* 
      Getting the packet ready
    */
      bs = RFC1035.createQueryPacket(domainName, recordType, recursion);
    /*
      Getting the DatagramPacket ready for the server filled with the packet
    */
      ps=new DatagramPacket(bs,bs.length,serverHostAddress,serverPort);

      s.send(ps);
      System.out.print(s.getLocalAddress()+":"+s.getLocalPort());
      System.out.print(" querying "+serverHostAddress.getHostName()+":"+serverPort);
      System.out.println(":"+ps.getLength());
      pr=new DatagramPacket(br, br.length);
      s.receive(pr);
      /*now to take pr apart*/
      byte[] bq;
      //JDH need to be sure the byte length is correct
      //JDH getData may still have the original length
      bq=pr.getData();
      System.out.print("Received a packet of length "+pr.getLength());
      System.out.println(" in a buffer of length "+bq.length);
      RFC1035 p = new RFC1035(pr.getData(), 0, pr.getLength());
      
      P.rintln(p.parse().toString());
      
    } catch(SecurityException x){
      System.err.println(x.getMessage());
      System.err.println(x.getClass().getName());
      System.exit(1);
    } catch(NoRouteToHostException x){
      System.err.println(x.getClass().getName());
      System.exit(2);
    } catch(BindException x){
      System.err.println(x.getClass().getName());
      System.exit(2);
    } catch(ConnectException x){
      System.err.println(x.getClass().getName());
      System.exit(2);
    } catch(SocketException x){
      System.err.println(x.getClass().getName());
      System.exit(2);
    } catch(UnknownHostException x){
      System.err.println(x.getClass().getName());
      System.exit(2);
    } catch(IOException x){
      System.err.println(x.getClass().getName());
      System.exit(3);
    }
  }
}
