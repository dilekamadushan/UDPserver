package com.grydtech.peershare.datagram.workerThread;

import com.grydtech.peershare.datagram.domain.Node;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by dileka on 9/27/18.
 */
public class GossipAcceptor extends Thread {
    
    private CopyOnWriteArrayList<Node> routingTable;
    
    private String gossipMessage;
    
    private Node myNode;
    
    public GossipAcceptor(CopyOnWriteArrayList<Node> routingTable, Node myNode, String gossipMessage) {
        
        this.routingTable = routingTable;
        this.gossipMessage = gossipMessage;
        this.myNode = myNode;
        //System.out.println("Gossip Acceptor: Thread started:" + gossipMessage);
    }
    
    public void run() {
        
        addToRoutingTable(gossipMessage);
    }
    
    private void addToRoutingTable(String gossipMessage) {
       // System.out.println("Gossip Acceptor: Trying to decode gossip message " + gossipMessage);
        String[] hostList = gossipMessage.substring(12).split(" ");
        if (hostList.length % 2 == 0) {
            for (int i = 0; i < hostList.length; i += 2) {
                
               // System.out.println("Gossip Acceptor:Inside the addTable method :" + hostList[i] + " " + hostList[i + 1] );
                String[] ips = hostList[i].replace(".", " ").split(" ");
               // System.out.println(
                        //"Register and Join Messenger:" + Integer.parseInt(ips[0]) + " " + Integer.parseInt(ips[1]) + " "
                             //   + Integer.parseInt(ips[2]) + " " + Integer.parseInt(ips[3]));
                
                Node node = new Node(new byte[] { (byte) Integer.parseInt(ips[0]), (byte) Integer.parseInt(ips[1]),
                        (byte) Integer.parseInt(ips[2]), (byte) Integer.parseInt(ips[3]) },
                        Integer.parseInt(hostList[i + 1]), "FromGossip", UUID.randomUUID());
                node.setIpString(hostList[i]);
                node.setIdForDisplay(Integer.parseInt(hostList[i + 1].substring(hostList[i + 1].length() - 1)));
                
                Node member = routingTable.stream()
                        .filter(s -> s.getIpString().equals(node.getIpString()) && s.getPort() == node.getPort()).findFirst()
                        .orElse(null);
                
                if (member == null && !(node.getIpString().equals(myNode.getIpString()) && node.getPort() == myNode
                        .getPort())) {
                    node.setStatus(true);
                    node.setDiscoveredBy("From Gossip Message");
                    routingTable.add(node);
                    System.out.println("Gossip Acceptor: A new node added:" + node.toString());
                } else if (member != null) {
                    //member.setJoined(true);
                    member.setStatus(true);
                    if (Objects.equals(member.getDiscoveredBy(), "")) {
                        member.setDiscoveredBy("From gossip request " + gossipMessage);
                    }
                }
            }
        } else {
            System.out.println("Gossip Acceptor:Failed Problem in parsing gossip message:" + gossipMessage);
        }
        
    }
}
