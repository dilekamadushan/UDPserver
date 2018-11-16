package datagram.singleThreaded;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Created by dileka on 9/27/18.
 */
public class Server {
    public static void main(String[] args) throws IOException
    {
        // Step 1 : Create a socket to listen at port 1234 
        DatagramSocket datagramSocket = new DatagramSocket(1234);
        byte[] receive = new byte[65535];
        
        DatagramPacket datagramPacket = null;
        while (true)
        {
            
            // Step 2 : create a DatgramPacket to receive the data. 
            datagramPacket = new DatagramPacket(receive, receive.length);
            
            // Step 3 : revieve the data in byte buffer. 
            datagramSocket.receive(datagramPacket);
            
            System.out.println("Successful:-" + data(receive));
            
            // Exit the server if the client sends "bye" 
            if (data(receive).toString().equals("bye"))
            {
                System.out.println("RegisterAndJoinMessenger sent bye.....EXITING");
                break;
            }
            
            // Clear the buffer after every message. 
            receive = new byte[65535];
        }
    }
    
    // A utility method to convert the byte array 
    // data into a string representation. 
    public static StringBuilder data(byte[] a)
    {
        if (a == null)
            return null;
        StringBuilder ret = new StringBuilder();
        int i = 0;
        while (a[i] != 0)
        {
            ret.append((char) a[i]);
            i++;
        }
        return ret;
    }
    
}
