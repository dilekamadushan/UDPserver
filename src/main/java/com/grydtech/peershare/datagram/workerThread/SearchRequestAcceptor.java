package com.grydtech.peershare.datagram.workerThread;

import com.grydtech.peershare.datagram.domain.Node;
import com.grydtech.peershare.datagram.domain.SearchResult;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Objects;
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
    
    private String requestId;
    private int hopsCount=7;
    
    public SearchRequestAcceptor(int packetCount, int hopsCount,DatagramSocket socket, CopyOnWriteArrayList<Node> routingTable,
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
        this.hopsCount=hopsCount;
       // System.out.println(
              //  packetCount + " SearchRequestAcceptor:Thread started:" + System.currentTimeMillis() / 1000 + request);
    }
    
    public void run() {
        StringBuilder searchQuery;
        StringBuilder searchRequest;
        String[] params = request.split(" ");
        requestId=params[params.length-2];
        String hops = params[params.length-1];
        
        String previousSearch = previousSearchRequests.stream()
                .filter(s -> s.substring(0, s.length() - 1).equals(request.substring(0, request.length() - 1))).findFirst()
                .orElse(null);
        
        if (previousSearch == null) {
            if (hopsCount < Integer.parseInt(hops)) {
                System.out.println(packetCount + "SearchRequestAcceptor:Number of hops is greater than 7: " + params[6]);
                
            } else {
                searchQuery = new StringBuilder();
                searchRequest = new StringBuilder();
                for (int i = 4; i < params.length - 2; i++) {
                    if (i != params.length - 3) {
                        searchQuery.append(params[i]).append("_");
                        searchRequest.append(params[i]).append(" ");
                    } else {
                        searchQuery.append(params[i]);
                        searchRequest.append(params[i]);
                    }
                }
               
                List<String> foundFiles = fileNames.stream()
                        .filter(s -> tokenWiseSearch(s.toLowerCase(), searchQuery.toString().toLowerCase()))
                        .collect(toList());
                
                if (foundFiles.size() != 0) {
                    if (isHomeMade) {
                       // System.out.println(packetCount + "SearchRequestAcceptor:search request is home made");
                        for (String fileName : foundFiles) {
                           // System.out.println(packetCount + "SearchRequestAcceptor:adding to the search result " + fileName + " " + myNode.getIpString());
                            searchResult.addFileName(fileName);
                            searchResult.addNode(myNode);
                            searchResult.addSearchResponse(request);
                            searchResult.addSearchHops("0");
                        }
                        
                    } else {
                       // System.out.println(packetCount + "SearchRequestAcceptor:search request is not home made");
                        try {
                            sendSearchResponse(params[2], params[3],hops, foundFiles);
                            //System.out.println(packetCount + "SearchRequestAcceptor:search response sent ");
                            
                            //System.out.println(packetCount
                               //     + "SearchRequestAcceptor: thread waiting to send the second search response burst");
                            
                            Thread.sleep(500);
                            
                            sendSearchResponse(params[2], params[3], hops,foundFiles);
                            //System.out.println(
                                  //  packetCount + "SearchRequestAcceptor: sent the second udp burst" + params[2] + " "
                                         //   + params[3]);
                            
                        }
                        catch (InterruptedException e) {
                            e.printStackTrace();
                           // System.out.println("SearchRequestAcceptor: Error in sending search second udp response burst");
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                            //System.out.println(packetCount + "SearchRequestAcceptor: Error in sending search response");
                        }
                    }
                    
                }
              //  System.out.println(packetCount + "SearchRequestAcceptor:Finished dealing with filenames");
                if (hopsCount > Integer.parseInt(hops)) {
                    //System.out.println(packetCount + "SearchRequestAcceptor:Trying to send search request for other nodes");
                    for (Node node : routingTable) {
                        try {
                            if (!Objects.equals(node.getIpString(), params[2])) {
                                if (node.isJoined()) {
                                    sendSearchRequest(node, Integer.parseInt(params[params.length - 1]) + 1, params[2],
                                            params[3], searchRequest.toString());
                                    
                                   
                                    Thread.sleep(500);
                                    
                                    sendSearchRequest(node, Integer.parseInt(params[params.length - 1]) + 1, params[2],
                                            params[3], searchRequest.toString());
                                    } else if(node.isStatus()) {
                                    sendSearchRequest(node, Integer.parseInt(params[params.length - 1]) + 1,
                                            params[2], params[3], searchRequest.toString());
                                   
                                    Thread.sleep(500);
                                    
                                    sendSearchRequest(node, Integer.parseInt(params[params.length - 1]) + 1,
                                            params[2], params[3], searchRequest.toString());
                                     }
                                
                            } else {
                                if (node.getPort() != Integer.parseInt(params[3])) {
                                    if (node.isJoined()) {
                                        sendSearchRequest(node, Integer.parseInt(params[params.length - 1]) + 1, params[2],
                                                params[3], searchRequest.toString());
                                        
                                       
                                        Thread.sleep(500);
                                        
                                        sendSearchRequest(node, Integer.parseInt(params[params.length - 1]) + 1, params[2],
                                                params[3], searchRequest.toString());
                                        } else if (node.isStatus()){
                                        sendSearchRequest(node,
                                                Integer.parseInt(params[params.length - 1]) + 1, params[2],
                                                params[3], searchRequest.toString());
                                       Thread.sleep(500);
                                        
                                        sendSearchRequest(node,
                                                Integer.parseInt(params[params.length - 1]) + 1, params[2],
                                                params[3], searchRequest.toString());
                                        }
                                    
                                }
                            }
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
                     }
            }
            
            if (!isHomeMade) {
                
                String[] ips = params[2].replace(".", " ").split(" ");
                
                 Node node = new Node(new byte[] { (byte) Integer.parseInt(ips[0]), (byte) Integer.parseInt(ips[1]),
                        (byte) Integer.parseInt(ips[2]), (byte) Integer.parseInt(ips[3]) }, Integer.parseInt(params[3]),
                        "FromSearchRequest", UUID.randomUUID());
                node.setIpString(params[2]);
                Node newNode = routingTable.stream()
                        .filter(s -> s.getIpString().equals(node.getIpString()) && s.getPort() == node.getPort()).findFirst()
                        .orElse(null);
                if (newNode == null && !(node.getIpString().equals(myNode.getIpString()) && node.getPort() == myNode
                        .getPort())) {
                    node.setIdForDisplay(Integer.parseInt(params[3].substring(params[3].length() - 1)));
                    node.setStatus(true);
                    node.setDiscoveredBy("From search response " + request);
                    routingTable.add(node);
                    routingTable.add(node);
                     } else if (newNode != null) {
                    newNode.setStatus(true);
                    if (Objects.equals(newNode.getDiscoveredBy(), "")) {
                        newNode.setDiscoveredBy("From search request " + request);
                    }
                }
            }
            
            previousSearchRequests.add(request);
            
        } else {
           }
    }
    
    private void sendSearchRequest(Node node, int hop, String ip, String port, String searchQuery) throws IOException {
         String newRequest = "SER " + ip + " " + port + " " + searchQuery + " "+requestId +" "+hop;
         String finalMessage = getMessageLength(newRequest);
        byte[] bufToSend = finalMessage.getBytes();
        DatagramPacket nodeDatagramPacket = new DatagramPacket(bufToSend, bufToSend.length,
                InetAddress.getByAddress(node.getIp()), node.getPort());
        threadDatagramSocket.send(nodeDatagramPacket);
    }
    
    private void sendSearchResponse(String ip, String port, String hops, List<String> fileNames) throws IOException {
        
        StringBuilder body = new StringBuilder();
        for (String fileName : fileNames) {
            body.append(" ").append(fileName);
        }
        String messge = getMessageLength(
                "SEROK " + fileNames.size() + " " + myNode.getIpString() + " " + myNode.getPort() + " "+ hops+body.toString());
        
        byte[] bufToSend = messge.getBytes();
         DatagramPacket nodeDatagramPacket = new DatagramPacket(bufToSend, bufToSend.length, InetAddress.getByName(ip),
                Integer.parseInt(port));
        threadDatagramSocket.send(nodeDatagramPacket);
        }
    
    private String getMessageLength(String message) {
        int size = message.length() + 5;
        if (size < 100) {
            return "00" + size + " " + message;
        } else {
            return "0" + size + " " + message;
        }
    }
    
    private boolean tokenWiseSearch(String fileName, String query) {
        String[] fileWords = fileName.split("_");
        String[] queryWords = query.split("_");
        for (String fileWord : fileWords) {
            for (String queryWord : queryWords) {
                if (fileWord.equals(queryWord) && fileName.contains(query)) {
                    return true;
                }
            }
        
        }
        return false;
    }
}
