package com.grydtech.peershare.datagram.workerThread;

import com.grydtech.peershare.datagram.domain.Node;
import com.grydtech.peershare.datagram.domain.SearchResult;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by dileka on 9/27/18.
 */
public class SearcheResponseAcceptor extends Thread {
    
    private int packetCount;
    
    private CopyOnWriteArrayList<Node> routingTable;
    
    private SearchResult searchResult;
    
    private String searchResponse;
    
    private CopyOnWriteArrayList<String> previousSearchResponses;
    
    private Node myNode;
    
    public SearcheResponseAcceptor(int packetCount, CopyOnWriteArrayList<Node> routingTable,
            CopyOnWriteArrayList<String> previousSearchResponses, Node myNode, SearchResult searchResult,
            String searchResponse) {
        
        this.routingTable = routingTable;
        this.searchResult = searchResult;
        this.searchResponse = searchResponse;
        this.myNode = myNode;
        this.previousSearchResponses = previousSearchResponses;
        this.packetCount = packetCount;
        System.out.println(
                packetCount + "Search Response Acceptor:Thread started " + System.currentTimeMillis() + searchResponse);
    }
    
    public void run() {
        
        String[] params = searchResponse.split(" ");
        
        String previousSearchResponse = previousSearchResponses.stream()
                .filter(s -> s.contains(params[2]) && s.contains(params[3]) && s.contains(params[4])).findFirst()
                .orElse(null);
        if (previousSearchResponse == null) {
            System.out.println(
                    packetCount + "Search Response Acceptor:no previous search request " + System.currentTimeMillis()
                            + searchResponse);
            String[] ips = params[3].replace(".", " ").split(" ");
            Node node = new Node(new byte[] { (byte) Integer.parseInt(ips[0]), (byte) Integer.parseInt(ips[1]),
                    (byte) Integer.parseInt(ips[2]), (byte) Integer.parseInt(ips[3]) }, Integer.parseInt(params[4]),
                    "FromSearchResponse", UUID.randomUUID());
            node.setIpString(params[3]);
            
            node.setIdForDisplay(Integer.parseInt(params[4].substring(params[4].length() - 1)));
            
            Node newNode = routingTable.stream()
                    .filter(s -> Objects.equals(s.getIpString(), node.getIpString()) && s.getPort() == node.getPort())
                    .findFirst().orElse(null);
            
            StringBuilder searchQuery = new StringBuilder();
            String[] userQuery = searchResult.getQuery().split(" ");
            for (int i = 0; i < userQuery.length; i++) {
                if (i != userQuery.length - 1) {
                    searchQuery.append(userQuery[i]).append("_");
                } else {
                    searchQuery.append(userQuery[i]);
                }
            }
            
            for (int i = 6; i < params.length; i++) {
                System.out.println(
                        packetCount + "Search Response Acceptor:Checking for response " + params[i].toLowerCase() + " "
                                + searchQuery.toString());
                if (searchResult.isInUse() && params[i].toLowerCase().contains(searchQuery.toString().toLowerCase())) {
                    searchResult.addFileName(params[i]);
                    System.out.println(" Search Response Acceptor:Filename " + params[i]+" "+searchQuery.toString());
                    if (newNode != null) {
                        searchResult.addNode(newNode);
                    } else {
                        searchResult.addNode(node);
                    }
                    searchResult.addSearchResponse(searchResponse);
                    searchResult.addSearchHops(params[5]);
                    System.out.println(packetCount + "Search Response Acceptor:Found node " + node.toString());
                } else {
                    System.out.println(
                            packetCount + "Search Response Acceptor:search response abandoned" + searchResult.isInUse() + " "
                                    + searchQuery.toString().toLowerCase() + " " + params[i].toLowerCase());
                }
            }
            
            if (newNode == null && !(node.getIpString().equals(myNode.getIpString()) && node.getPort() == myNode
                    .getPort())) {
                System.out.println(packetCount + "Search Response Acceptor:Found new node ");
                node.setStatus(true);
                node.setDiscoveredBy("From search response " + searchResponse);
                routingTable.add(node);
                System.out.println(packetCount + "Search Response Acceptor:Added node " + node.toString());
            } else if (newNode != null) {
                //newNode.setJoined(true);
                newNode.setStatus(true);
                if (Objects.equals(node.getDiscoveredBy(), "")) {
                    node.setDiscoveredBy("From search response " + searchResponse);
                }
            }
            previousSearchResponses.add(searchResponse);
            System.out.println(
                    packetCount + "SearchResponseAcceptor: added search response to cache" + System.currentTimeMillis()
                            + searchResponse);
            
        } else {
            System.out.println(
                    packetCount + "SearchResponseAcceptor:Search request is not processed. It was received earlier " + System
                            .currentTimeMillis() + previousSearchResponse + " " + searchResponse);
        }
    }
}
