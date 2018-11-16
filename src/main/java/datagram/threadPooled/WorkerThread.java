package datagram.threadPooled;

import datagram.threadPooled.domain.Node;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * Created by dileka on 9/27/18.
 */
public class WorkerThread implements Runnable {
    
    private byte[] dataBuffer = null;
    
    private DatagramPacket threadDatagramPacket = null;
    
    private DatagramSocket threadDatagramSocket = null;
    private ArrayList<Node> nodes;
    
    public WorkerThread(DatagramPacket datagramPacket, DatagramSocket socket, ArrayList<Node> nodes) {
        
        this.dataBuffer = datagramPacket.getData();
        this.threadDatagramPacket = datagramPacket;
        this.threadDatagramSocket = socket;
        this.nodes = nodes;
    }
    
    // A utility method to convert the byte array 
    // data into a string representation. 
    public static StringBuilder data(byte[] a) {
        if (a == null)
            return null;
        StringBuilder ret = new StringBuilder();
        int i = 0;
        while (a[i] != 0) {
            ret.append((char) a[i]);
            i++;
        }
        return ret;
    }
    
    public void run() {
        System.out.println("1111111WorkerThread:-" + Thread.currentThread().getId() + " " + data(this.dataBuffer));
        
        // Exit the server if the client sends "bye" 
        if (data(this.dataBuffer).toString().equals("bye")) {
            System.out.println("RegisterAndJoinMessenger sent bye.....EXITING");
        }
        InetAddress address = threadDatagramPacket.getAddress();
        int port = threadDatagramPacket.getPort();
        threadDatagramPacket = new DatagramPacket(this.dataBuffer, this.dataBuffer.length, address, port);
        String received = new String(threadDatagramPacket.getData(), 0, threadDatagramPacket.getLength());
        System.out.println("in server received " + received);
        
        for(Node node:nodes){
            try {
                sendJoin(node);
            }
            catch (UnknownHostException e) {
                System.out.println("Node unreachable");
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error in socket");
            }
        }
        dataBuffer = new byte[256];
    }
    public void sendJoin(Node node) throws IOException {
        System.out.println("Trying to send join message for node"+node.toString()+" "+node.getIp().toString());
        byte[] bufToSend = "0022 JOIN 0.0.0.0 1234".getBytes();
        DatagramPacket nodeDatagramPacket = new DatagramPacket(bufToSend, bufToSend.length, InetAddress.getByAddress(node.getIp()),node.getPort());
        threadDatagramSocket.send(nodeDatagramPacket);
    }
}
