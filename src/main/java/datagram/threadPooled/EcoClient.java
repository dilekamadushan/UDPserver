package datagram.threadPooled;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

/**
 * Created by dileka on 9/29/18.
 */
public class EcoClient {
    
    private static DatagramSocket socket;
    
    private static InetAddress address;
    
    private static Scanner sc = new Scanner(System.in);
    
    private static byte[] buf;
    
    public static void main(String[] args) throws IOException {
        socket = new DatagramSocket();
        address = InetAddress.getByName("localhost");
        
        // loop while user not enters "bye" 
        while (true) {
            String inp = sc.nextLine();
            sendEcho(inp);
            if (inp.equals("bye"))
                break;
        }
    }
    
    public static String sendEcho(String msg) throws IOException {
        System.out.println("in send eco method " + msg);
        buf = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 4474);
        socket.send(packet);
        System.out.println("sent " + msg);
        packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        String received = new String(packet.getData(), 0, packet.getLength());
        System.out.println("received string " + received);
        return received;
    }
    
    public void close() {
        socket.close();
    }
    
}
