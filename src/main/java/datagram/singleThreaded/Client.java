package datagram.singleThreaded;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

/**
 * Created by dileka on 9/27/18.
 */
public class Client {
    public static void main(String args[]) throws IOException
    {
        System.out.println("Client started");
        Scanner sc = new Scanner(System.in);
        
        // Step 1:Create the socket object for 
        // carrying the data. 
        DatagramSocket ds = new DatagramSocket(1112);
        
        //InetAddress ip =InetAddress.getByAddress(new byte[] { (byte) 10, (byte) 10, (byte) 14, (byte) 214 });
        InetAddress ip =InetAddress.getByName("localhost");
    
        byte buf[] = null;
        
        // loop while user not enters "bye" 
        while (true)
        {
            String inp = sc.nextLine();
            
            // convert the String input into the byte array. 
            buf = inp.getBytes();
            
            // Step 2 : Create the datagramPacket for sending 
            // the data. 
            DatagramPacket DpSend =
                    new DatagramPacket(buf, buf.length, ip, 4469);
            
            // Step 3 : invoke the send call to actually send 
            // the data. 
            ds.send(DpSend);
            
            // break the loop if user enters "bye" 
            if (inp.equals("bye"))
                break;
        }
    }
}
