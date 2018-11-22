package datagram.threadPooled.workerThread;

import datagram.threadPooled.domain.Node;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by dileka on 9/27/18.
 */
public class GossipSender extends Thread {
    
    private DatagramSocket threadDatagramSocket = null;
    
    private CopyOnWriteArrayList<Node> routingTable;
    
    private Node myNode;
    
    private byte[] bufToSend;
    
    private boolean running;
    
    public GossipSender(boolean running, DatagramSocket socket, Node myNode, CopyOnWriteArrayList<Node> routingTable) {
        this.myNode = myNode;
        this.threadDatagramSocket = socket;
        this.routingTable = routingTable;
        this.running = running;
        System.out.println("Gossip Sender: Thread started");
    }
    
    public void run() {
        System.out.println("Gossip Sender:Entering the gossip sending loop");
        while (running) {
            if (routingTable.size() > 0) {
                
                try {
                    System.out.println("Gossip Sender:Gossip thread sleep for 120 seconds ");
                    Thread.sleep(1000 * 60*3);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Gossip Sender:Gossip thread wakes up");
                
                System.out.println("Gossip Sender: Routing table size:" + routingTable.size());
                System.out.println("Gossip Sender:Starting to send the gossip message to all nodes");
                for (Node node : routingTable) {
                    try {
                        if (!Objects.equals(node.getIpString(), myNode.getIpString())) {
                            sendGossip(node);
                            System.out.println("Gossip Sender:Gossip message sent to " + node.toString());
                        } else if (node.getPort() != myNode.getPort()) {
                            sendGossip(node);
                            System.out.println("Gossip Sender:Gossip message sent to " + node.toString());
                            
                        }
                        
                    }
                    catch (UnknownHostException e) {
                        System.out.println("Node unreachable");
                        e.printStackTrace();
                        System.out.println("Gossip Sender:Gossip message failed to " + node.toString());
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("Error in socket");
                        System.out.println("Gossip Sender:Gossip message failed to " + node.toString());
                    }
                }
            } else {
                // System.out.println("Gossip Sender:Routing table size is 0"+routingTable.size());
            }
            
        }
        
    }
    
    private void sendGossip(Node node) throws IOException {
        System.out.println("Gossip Sender:inside send gossip method " + node.toString() + " " + node.getIpString());
        String msg = getFullMessage(
                "GOSSIP " + myNode.getIpString() + " " + myNode.getPort() + " " + myNode.getNodeName() + getGossipMessage());
        bufToSend = msg.getBytes();
        System.out.println("Gossip Sender:Gossip Message:" + msg);
        DatagramPacket nodeDatagramPacket = new DatagramPacket(bufToSend, bufToSend.length,
                InetAddress.getByAddress(node.getIp()), node.getPort());
        threadDatagramSocket.send(nodeDatagramPacket);
        System.out.println("Gossip Sender:Gossip Message sent to " + node.getIpString() + " " + node.getPort() + " " + msg);
    }
    
    private String getFullMessage(String message) {
        int size = message.length() + 5;
        if (size < 100) {
            return "00" + size + " " + message;
        } else {
            return "0" + size + " " + message;
        }
    }
    
    private String getGossipMessage() {
        StringBuilder msg = new StringBuilder("");
        
        for (Node node : routingTable) {
            msg.append(" ").append(node.getIpString()).append(" ").append(node.getPort()).append(" ")
                    .append(node.getNodeName());
        }
        
        return msg.toString();
    }
    
}
