package datagram.threadPooled.workerThread;

import datagram.threadPooled.domain.Node;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by dileka on 9/27/18.
 */
public class JoinResponseAcceptor extends Thread {
    
    private DatagramSocket threadDatagramSocket = null;
    
    private CopyOnWriteArrayList<Node> routingTable;
    
    private DatagramPacket datagramPacket;
    
    public JoinResponseAcceptor(DatagramSocket socket, CopyOnWriteArrayList<Node> routingTable, DatagramPacket datagramPacket) {
        
        this.threadDatagramSocket = socket;
        this.routingTable = routingTable;
        this.datagramPacket = datagramPacket;
    }
    
    public void run() {
        addToNodeList(datagramPacket);
    }
    
    public void addToNodeList(DatagramPacket datagramPacket) {
        InetAddress address = datagramPacket.getAddress();
        int port = datagramPacket.getPort();
        Node node = routingTable.stream().filter(s -> s.getIpString().equals(datagramPacket.getAddress().getHostAddress())).findFirst()
                .orElse(null);
        System.out.println("Routing Table Manager: A node responded to the Join message:" + address + " " + port+" "+routingTable.get(0).getIpString());
        System.out.println("Routing Table Manager: "+datagramPacket.getAddress().getHostAddress());
        if(node != null){
            node.setStatus(true);
            node.setJoined(true);
            System.out.println("Routing Table Manager:The new Routing table ");
        }
        for(Node peer:routingTable){
            System.out.println("Routing Table Manager: "+peer.getIpString());
            System.out.println("Routing Table Manager: "+peer.toString());
            
        }
    }
}
