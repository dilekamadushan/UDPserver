package datagram.threadPooled.workerThread;

import datagram.threadPooled.domain.Node;

import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by dileka on 9/27/18.
 */
public class GossipAcceptor extends Thread {
    
    private CopyOnWriteArrayList<Node> routingTable;
    
    private String gossipMessage;
    
    public GossipAcceptor(CopyOnWriteArrayList<Node> routingTable, Node myNode, String gossipMessage) {
        
        this.routingTable = routingTable;
        this.gossipMessage = gossipMessage;
        System.out.println("Gossip Sender: Thread started");
    }
    
    public void run() {
        
        addToRoutingTable(gossipMessage);
    }
    
    public void addToRoutingTable(String gossipMessage) {
        System.out.println("Gossip Sender: Trying to decode gossip message " + gossipMessage);
        String[] params = gossipMessage.split(" ");
        String[] hostList = params[3].trim().split(" ");
        for (int i = 0; i < hostList.length; i += 3) {
            
            System.out.println(
                    "Register and Join Messenger:Inside the loop:" + hostList[i] + " " + hostList[i + 1] + " " + hostList[i
                            + 2]);
            String[] ips = hostList[i].replace(".", " ").split(" ");
            System.out.println(
                    "Register and Join Messenger:" + Integer.parseInt(ips[0]) + " " + Integer.parseInt(ips[1]) + " "
                            + Integer.parseInt(ips[2]) + " " + Integer.parseInt(ips[3]));
            UUID uuid = UUID.fromString(hostList[i+2]);
            Node node = new Node(new byte[] { (byte) Integer.parseInt(ips[0]), (byte) Integer.parseInt(ips[1]),
                    (byte) Integer.parseInt(ips[2]), (byte) Integer.parseInt(ips[3]) }, uuid,
                    Integer.parseInt(hostList[i + 1]));
            node.setIpString(hostList[i]);
            routingTable.add(node);
            System.out.println("Register and Join Messenger: added new node");
        }
        
    }
}
