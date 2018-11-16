package datagram.threadPooled.workerThread;

import datagram.threadPooled.domain.Node;

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
        System.out.println("Gossip Acceptor: Thread started");
    }
    
    public void run() {
        
        addToRoutingTable(gossipMessage);
    }
    
    public void addToRoutingTable(String gossipMessage) {
        System.out.println("Gossip Acceptor: Trying to decode gossip message " + gossipMessage);
        String[] hostList = gossipMessage.split(" ");
        
        for (int i = 2; i < hostList.length; i += 3) {
            
            System.out.println(
                    "Gossip Acceptor:" + hostList[i] + " " + hostList[i + 1] + " " + hostList[i
                            + 2]);
            String[] ips = hostList[i].replace(".", " ").split(" ");
            System.out.println(
                    "Gossip Acceptor:" + Integer.parseInt(ips[0]) + " " + Integer.parseInt(ips[1]) + " "
                            + Integer.parseInt(ips[2]) + " " + Integer.parseInt(ips[3]));
            Node node = new Node(new byte[] { (byte) Integer.parseInt(ips[0]), (byte) Integer.parseInt(ips[1]),
                    (byte) Integer.parseInt(ips[2]), (byte) Integer.parseInt(ips[3]) }, hostList[i + 2],
                    Integer.parseInt(hostList[i + 1]));
            node.setIpString(hostList[i]);
            //routingTable.add(node);
            System.out.println("Gossip AcceptorGossip Acceptor: added new node");
        }
        
    }
}

