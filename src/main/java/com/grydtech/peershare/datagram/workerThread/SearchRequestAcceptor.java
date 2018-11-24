package com.grydtech.peershare.datagram.workerThread;

import com.grydtech.peershare.datagram.domain.Node;
import com.grydtech.peershare.datagram.domain.SearchResult;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.stream.Collectors.toList;

/**
 * Created by dileka on 9/27/18.
 */
public class SearchRequestAcceptor implements Runnable {
    
    private int packetCount;
    
    private DatagramSocket threadDatagramSocket = null;
    
    private CopyOnWriteArrayList<Node> routingTable;
    
    private CopyOnWriteArrayList<String> fileNames;
    
    private Node myNode;
    
    private String request;
    
    private Boolean isHomeMade;
    
    private SearchResult searchResult;
    
    private CopyOnWriteArrayList<String> previousSearchRequests;
    
    //private CopyOnWriteArrayList<String> previousSentSearchResponses;
    
    public SearchRequestAcceptor(int packetCount, DatagramSocket socket, CopyOnWriteArrayList<Node> routingTable,
            CopyOnWriteArrayList<String> fileNames, Node myNode, String request, Boolean isHomeMade,
            CopyOnWriteArrayList<String> previousSearchRequests, SearchResult searchResult) {
        
        this.threadDatagramSocket = socket;
        this.routingTable = routingTable;
        this.fileNames = fileNames;
        this.myNode = myNode;
        this.request = request;
        this.isHomeMade = isHomeMade;
        this.searchResult = searchResult;
        this.previousSearchRequests = previousSearchRequests;
        this.packetCount = packetCount;
        // this.previousSentSearchResponses = previousSentSearchResponses;
        System.out.println(packetCount+" SearchRequestAcceptor:Thread started:"+System.currentTimeMillis()/1000  + request);
    }
    
    public void run() {
        StringBuilder searchQuery;
        StringBuilder searchRequest;
        String[] params = request.split(" ");
        
        String previousSearch = previousSearchRequests.stream()
                .filter(s -> s.substring(0, s.length() - 1).equals(request.substring(0, request.length() - 1))).findFirst()
                .orElse(null);
        
        if (previousSearch == null) {
            System.out.println(packetCount+"SearchRequestAcceptor :"+System.currentTimeMillis() + request + " is a new search request");
            System.out.println(packetCount+"SearchRequestAcceptor : no.of hops:" + params[params.length - 1]);
            if (7 < Integer.parseInt(params[params.length - 1])) {
                System.out
                        .println(packetCount+"SearchRequestAcceptor:Number of hops is greater than 7: " + params[6]);
                System.out.println(packetCount+"SearchRequestAcceptor:The request is dropped");
                
            } else {
                System.out.println(packetCount+"SearchRequestAcceptor:The no.of hops <=7");
                searchQuery = new StringBuilder();
                searchRequest = new StringBuilder();
                for (int i = 4; i < params.length - 1; i++) {
                    if (i != params.length - 2) {
                        searchQuery.append(params[i]).append("_");
                        searchRequest.append(params[i]).append(" ");
                    } else {
                        searchQuery.append(params[i]);
                        searchRequest.append(params[i]);
                    }
                }
                System.out.println(packetCount+"SearchRequestAcceptor:search query:" + searchQuery.toString() + " "
                        + searchRequest.toString());
                List<String> foundFiles = fileNames.stream().filter(s -> s.toLowerCase().contains(searchQuery.toString().toLowerCase()))
                        .collect(toList());
                
                if (foundFiles.size() != 0) {
                    System.out.println(packetCount + "SearchRequestAcceptor:The size of foundFiles:" + foundFiles.size());
                    if (isHomeMade) {
                        System.out.println(packetCount + "SearchRequestAcceptor:search request is home made");
                        for (String fileName : foundFiles) {
                            System.out.println(
                                    packetCount + "SearchRequestAcceptor:adding to the search result " + fileName + " "
                                            + myNode.getIpString());
                            searchResult.addFileName(fileName);
                            searchResult.addNode(myNode);
                            searchResult.addSearchResponse(request);
                        }
                        
                    } else {
                        System.out.println(packetCount + "SearchRequestAcceptor:search request is not home made");
                        try {
                            sendSearchResponse(params[2], params[3], foundFiles);
                            System.out.println(packetCount + "SearchRequestAcceptor:search response sent ");
                            
                            System.out.println(packetCount
                                    + "SearchRequestAcceptor: thread waiting to send the second search response burst");
                            
                            Thread.sleep(500);
                            
                            sendSearchResponse(params[2], params[3], foundFiles);
                            System.out.println(
                                    packetCount + "SearchRequestAcceptor: sent the second udp burst" + params[2] + " "
                                            + params[3]);
                            
                        }
                        catch (InterruptedException e) {
                            e.printStackTrace();
                            System.out.println("SearchRequestAcceptor: Error in sending search second udp response burst");
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                            System.out.println(packetCount + "SearchRequestAcceptor: Error in sending search response");
                        }
                    }
                    
                }
                System.out.println(packetCount + "SearchRequestAcceptor:Finished dealing with filenames");
                if (8 > Integer.parseInt(params[params.length - 1])) {
                    Random random = new Random();
                    System.out.println(packetCount + "SearchRequestAcceptor:Trying to send search request for other nodes");
                    for (Node node : routingTable) {
                        try {
                            if (!Objects.equals(node.getIpString(), params[2])) {
                                if(node.isJoined()){
                                    sendSearchRequest(node, Integer.parseInt(params[params.length - 1]) + 1, params[2],
                                            params[3], searchRequest.toString());
                                }
                                else {
                                    sendSearchRequest(node, Integer.parseInt(params[params.length - 1]) +random.nextInt(3) , params[2],
                                            params[3], searchRequest.toString());
                                }
                               
                                System.out.println(
                                        packetCount + "SearchRequestAcceptor: sent the search request" + node.toString()
                                                + " " + params[2] + " " + params[3]);
                                System.out.println(
                                        packetCount + "SearchRequestAcceptor: thread waiting to send the second burst");
                                
                                Thread.sleep(500);
                                
                                sendSearchRequest(node, Integer.parseInt(params[params.length - 1]) + 1, params[2],
                                        params[3], searchRequest.toString());
                                System.out.println(
                                        packetCount + "SearchRequestAcceptor: sent the second udp burst" + node.toString()
                                                + " " + params[2] + " " + params[3]);
                            } else {
                                System.out.println(
                                        packetCount + "SearchRequestAcceptor: Both ip equal" + node.getPort() + " "
                                                + params[3]);
                                if (node.getPort() != Integer.parseInt(params[3])) {
                                    sendSearchRequest(node, Integer.parseInt(params[params.length - 1]) + 1, params[2],
                                            params[3], searchRequest.toString());
                                    System.out.println(
                                            packetCount + "SearchRequestAcceptor: sent the search request" + node.toString()
                                                    + " " + params[2] + " " + params[3]);
                                    System.out.println(
                                            packetCount + "SearchRequestAcceptor: thread waiting to send the second burst");
                                    
                                    Thread.sleep(500);
                                    
                                    sendSearchRequest(node, Integer.parseInt(params[params.length - 1]) + 1, params[2],
                                            params[3], searchRequest.toString());
                                    System.out.println(
                                            packetCount + "SearchRequestAcceptor: sent the second udp burst" + node
                                                    .toString() + " " + params[2] + " " + params[3]);
                                    
                                }
                            }
                            System.out.println(
                                    packetCount + "SearchRequestAcceptor:" + node.getIpString() + " " + params[2] + " "
                                            + params[3]);
                            
                        }
                        catch (InterruptedException e) {
                            System.out.println(packetCount + "SearchRequestAcceptor:second udp burst failed");
                            e.printStackTrace();
                        }
                        catch (UnknownHostException e) {
                            System.out.println(packetCount + "SearchRequestAcceptor:Node unreachable");
                            e.printStackTrace();
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                            System.out.println(packetCount + "SearchRequestAcceptor:Error in socket");
                        }
                    }
                } else {
                    System.out
                            .println(packetCount + "SearchRequestAcceptor:Search request is not forewarded to other nodes");
                }
            }
            
            if (!isHomeMade) {
                
                System.out.println(packetCount
                        + "SearchRequestAcceptor:Trying to check the node whether it exists because not home made");
                String[] ips = params[2].replace(".", " ").split(" ");
                
                System.out.println(
                        packetCount + "SearchRequestAcceptor:Trying to check the node whether it exists " + ips[0] + " "
                                + params[2]);
                Node node = new Node(new byte[] { (byte) Integer.parseInt(ips[0]), (byte) Integer.parseInt(ips[1]),
                        (byte) Integer.parseInt(ips[2]), (byte) Integer.parseInt(ips[3]) }, Integer.parseInt(params[3]),
                        "FromSearchRequest", UUID.randomUUID());
                node.setIpString(params[2]);
                Node newNode = routingTable.stream()
                        .filter(s -> s.getIpString().equals(node.getIpString()) && s.getPort() == node.getPort()).findFirst()
                        .orElse(null);
                if (newNode == null && !(node.getIpString().equals(myNode.getIpString()) && node.getPort() == myNode
                        .getPort())) {
                    System.out.println(packetCount + "SearchRequestAcceptor: adding a new node to table");
                    
                    node.setIdForDisplay(Integer.parseInt(params[3].substring(params[3].length() - 1)));
                    node.setStatus(true);
                    node.setDiscoveredBy("From search response "+request);
                    routingTable.add(node);
                    routingTable.add(node);
                    System.out.println(packetCount + "SearchRequestAcceptor: added a new node to table" + node.toString());
                }
                else if (newNode != null) {
                    //newNode.setJoined(true);
                    newNode.setStatus(true);
                    if (Objects.equals(node.getDiscoveredBy(), "")) {
                        node.setDiscoveredBy("From search request " + request);
                    }
                }
            }
            
            previousSearchRequests.add(request);
            System.out.println(packetCount + "SearchRequestAcceptor: added search request to cache" + System.currentTimeMillis()+request);
            
        } else {
            System.out.println(packetCount + "SearchRequestAcceptor:Search request is abandoned. It was received earlier "+System.currentTimeMillis()
                    + previousSearch + " " + request);
        }
    }
    
    private void sendSearchRequest(Node node, int hop, String ip, String port, String searchQuery) throws IOException {
        System.out.println(
                packetCount + "SearchRequestAcceptor:Trying to send search query for node" + node.toString() + " " + Arrays
                        .toString(node.getIp()));
        String newRequest = "SER " + ip + " " + port + " " + searchQuery + " " + hop;
        System.out.println(packetCount + "SearchRequestAcceptor:in sendSearchRequest method " + newRequest);
        //add a cache to check stop requests
        
        String finalMessage = getMessageLength(newRequest);
        byte[] bufToSend = finalMessage.getBytes();
        DatagramPacket nodeDatagramPacket = new DatagramPacket(bufToSend, bufToSend.length,
                InetAddress.getByAddress(node.getIp()), node.getPort());
        threadDatagramSocket.send(nodeDatagramPacket);
    }
    
    private void sendSearchResponse(String ip, String port, List<String> fileNames) throws IOException {
        
        StringBuilder body = new StringBuilder();
        for (String fileName : fileNames) {
            body.append(fileName).append(" ");
        }
        System.out.println(packetCount + "SearchRequestAcceptor:Trying to send search response for node" + ip + " " + port);
        String messge = getMessageLength(
                "SEROK " + fileNames.size() + " " + myNode.getIpString() + " " + myNode.getPort() + " " + body.toString());
        
        //String sentSearchResponse = previousSentSearchResponses.stream().filter(s -> s.equals(messge + ip + port))
        //   .findFirst().orElse(null);
        //if (sentSearchResponse == null) {
        byte[] bufToSend = messge.getBytes();
        System.out.println(packetCount + "SearchRequestAcceptor:Trying to send search response for node" + messge);
        DatagramPacket nodeDatagramPacket = new DatagramPacket(bufToSend, bufToSend.length, InetAddress.getByName(ip),
                Integer.parseInt(port));
        threadDatagramSocket.send(nodeDatagramPacket);
        System.out.println(packetCount + "SearchRequestAcceptor:sent search response for node");
           /* previousSentSearchResponses.add(messge + ip + port);
        } else {
            System.out.println(
                    "SearchRequestAcceptor:already sent the search response" + sentSearchResponse + " " + messge + " " + ip);
            
        }*/
        
    }
    
    private String getMessageLength(String message) {
        int size = message.length() + 5;
        if (size < 100) {
            return "00" + size + " " + message;
        } else {
            return "0" + size + " " + message;
        }
    }
}
