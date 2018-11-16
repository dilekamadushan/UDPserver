package datagram.threadPooled.workerThread;

import datagram.threadPooled.domain.Node;
import datagram.threadPooled.domain.SearchResult;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by dileka on 9/27/18.
 */
public class JoinRequestAcceptor implements Runnable {
    
    private DatagramSocket threadDatagramSocket = null;
    
    private CopyOnWriteArrayList<Node> routingTable;
    
    private ArrayList<String> fileNames;
    
    private Node myNode;
    
    private String request;
    
    private Boolean isHomeMade;
    
    private SearchResult searchResult;
    
    public JoinRequestAcceptor(DatagramSocket socket, CopyOnWriteArrayList<Node> routingTable, Node myNode, String request) {
        
        this.threadDatagramSocket = socket;
        this.routingTable = routingTable;
        this.myNode = myNode;
        this.request = request;
        System.out.println(" JoinRequestAcceptor:Thread started " + request);
    }
    
    public void run() {
        String[] params = request.split(" ");
        System.out.println(" JoinRequestAcceptor: " + params[2]);
        
        try {
            sendJOINResponse(params[2], params[3]);
        }
        catch (IOException e) {
            System.out.println("JoinRequestAcceptor: Failed to send Join Response " + params[2] + " " + params[3]);
            e.printStackTrace();
        }
        
        Node node = routingTable.stream().filter(s -> Objects.equals(s.getIpString(), params[2])).findFirst().orElse(null);
        if (node != null) {
            node.setStatus(true);
            node.setJoined(true);
            System.out.println("JoinRequestAcceptor:previous node was joined ");
        } else {
            System.out.println("JoinRequestAcceptor:The node who sent message: " + request + " is not in the routing table");
            String[] ips = params[2].replace(".", " ").split(" ");
            node = new Node(new byte[] { (byte) Integer.parseInt(ips[0]), (byte) Integer.parseInt(ips[1]),
                    (byte) Integer.parseInt(ips[2]), (byte) Integer.parseInt(ips[3]) }, UUID.randomUUID(),
                    Integer.parseInt(params[3]));
            node.setIpString(params[2]);
            node.setJoined(true);
            node.setStatus(true);
            routingTable.add(node);
            System.out.println("JoinRequestAcceptor:The node" + node.getIpString() + " " + node.getPort()
                    + " is added in the routing table");
            
        }
        for (Node peer : routingTable) {
            peer.toString();
            
        }
        
    }
    
    public void sendJOINResponse(String ip, String port) throws IOException {
        
        System.out.println("JoinRequestAcceptor:Trying to send join response for node" + ip + " " + port);
        String messge = getMessageLength("JOINOK 0");
        byte[] bufToSend = messge.getBytes();
        DatagramPacket nodeDatagramPacket = new DatagramPacket(bufToSend, bufToSend.length, InetAddress.getByName(ip),
                Integer.parseInt(port));
        threadDatagramSocket.send(nodeDatagramPacket);
        System.out.println("JoinRequestAcceptor: sent join response for node" + ip + " " + port + " " + messge);
        
    }
    
    public String getMessageLength(String message) {
        int size = message.length() + 5;
        if (size < 100) {
            return "00" + size + " " + message;
        } else {
            return "0" + size + " " + message;
        }
    }
}
