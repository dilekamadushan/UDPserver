package datagram.threadPooled.workerThread;

import datagram.threadPooled.domain.Node;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by dileka on 9/27/18.
 */
public class HeartBeatRequestAcceptor extends Thread {
    
    private CopyOnWriteArrayList<Node> routingTable;
    
    private String request;
    
    private Node myNode;
    
    public HeartBeatRequestAcceptor(CopyOnWriteArrayList<Node> routingTable, Node myNode, String request) {
        
        this.routingTable = routingTable;
        this.request = request;
        this.myNode = myNode;
        System.out.println(" HeartBeatRequestAcceptor:Thread started " + request);
    }
    
    public void run() {
        String[] params = request.split(" ");
        System.out.println(" HeartBeatRequestAcceptor: " + params[2]);
        
        Node node = routingTable.stream()
                .filter(s -> Objects.equals(s.getIpString(), params[2]) && s.getPort() == Integer.parseInt(params[3]))
                .findFirst().orElse(null);
        if (node != null) {
            node.decreaseRetries();
            System.out.println("HeartBeatRequestAcceptor:heart beat message processed " + node.toString());
        } else if (!(Objects.equals(myNode.getIpString(), params[2]) && myNode.getPort() == Integer.parseInt(params[3]))) {
            System.out.println(
                    "HeartBeatRequestAcceptor:The node who sent message: " + request + " is not in the routing table");
            String[] ips = params[2].replace(".", " ").split(" ");
            node = new Node(new byte[] { (byte) Integer.parseInt(ips[0]), (byte) Integer.parseInt(ips[1]),
                    (byte) Integer.parseInt(ips[2]), (byte) Integer.parseInt(ips[3]) }, Integer.parseInt(params[3]),
                    "FromHeartBeat", UUID.randomUUID());
            node.setIpString(params[2]);
            node.setIdForDisplay(Integer.parseInt(params[3].substring(params[3].length() - 1)));
            node.setStatus(true);
            routingTable.add(node);
            System.out.println("HeartBeatRequestAcceptor:The node" + node.getIpString() + " " + node.getPort()
                    + " is added in the routing table");
            
        }
        
    }
    
}
