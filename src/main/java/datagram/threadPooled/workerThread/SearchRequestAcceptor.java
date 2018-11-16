package datagram.threadPooled.workerThread;

import datagram.threadPooled.domain.Node;
import datagram.threadPooled.domain.SearchResult;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.stream.Collectors.toList;

/**
 * Created by dileka on 9/27/18.
 */
public class SearchRequestAcceptor implements Runnable {
    
    private DatagramSocket threadDatagramSocket = null;
    
    private CopyOnWriteArrayList<Node> routingTable;
    
    private ArrayList<String> fileNames;
    
    private Node myNode;
    
    private String request;
    
    private Boolean isHomeMade;
    
    private SearchResult searchResult;
    
    private ArrayList<String> previousSearchRequests;
    
    public SearchRequestAcceptor(DatagramSocket socket, CopyOnWriteArrayList<Node> routingTable, ArrayList<String> fileNames,
            Node myNode, String request, Boolean isHomeMade, ArrayList<String> previousSearchRequests,
            SearchResult searchResult) {
        
        this.threadDatagramSocket = socket;
        this.routingTable = routingTable;
        this.fileNames = fileNames;
        this.myNode = myNode;
        this.request = request;
        this.isHomeMade = isHomeMade;
        this.searchResult = searchResult;
        this.previousSearchRequests = previousSearchRequests;
        System.out.println(" SearchRequestAcceptor:Thread started:" + request);
    }
    
    public void run() {
        String[] params = request.split(" ");
        
        String previousSearch = previousSearchRequests.stream()
                .filter(s -> s.contains(params[2]) && s.contains(params[4]) && s.contains(params[3])).findFirst()
                .orElse(null);
        
        if (previousSearch == null) {
            System.out.println(" SearchRequestAcceptor: " + request+" is a new search request");
            System.out.println(" SearchRequestAcceptor: no.of hops:" + params[5]);
            if (3 < Integer.parseInt(params[5])) {
                System.out.println(" SearchRequestAcceptor:Number of hops is greater than 3: " + params[6]);
                System.out.println(" SearchRequestAcceptor:The request is dropped");
                
            } else {
                System.out.println(" SearchRequestAcceptor:The no.of hops <=3");
                List<String> foundFiles = fileNames.stream().filter(s -> s.contains(params[4])).collect(toList());
                
                if (foundFiles.size()!=0) {
                    System.out.println(" SearchRequestAcceptor:The size of foundFiles:"+foundFiles.size());
                    if (isHomeMade) {
                        System.out.println(" SearchRequestAcceptor:search request is home made");
                        for (String fileName : foundFiles) {
                            System.out.println(" SearchRequestAcceptor:adding to the search result "+fileName+" "+myNode.getIpString());
                            searchResult.addFileName(fileName);
                            searchResult.addNode(myNode);
                        }
                        
                    } else {
                        System.out.println(" SearchRequestAcceptor:search request is not home made");
                        try {
                            sendSearchResponse(params[2], params[3], foundFiles);
                            System.out.println(" SearchRequestAcceptor:search response sent ");
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                            System.out.println("SearchRequestAcceptor: Error in sending search response");
                        }
                    }
                    
                }
                System.out.println("SearchRequestAcceptor:Finished dealing with filenames");
                if (3 > Integer.parseInt(params[5])) {
                    System.out.println("SearchRequestAcceptor:Trying to send search request for other nodes");
                    for (Node node : routingTable) {
                        try {
                            if (!(Objects.equals(node.getIpString(), params[2]) && node.getPort()==Integer.parseInt(params[3])) ){
                                sendSearchRequest(node, Integer.parseInt(params[5]) + 1, params[2], params[3], params[4]);
                            }
                            System.out.println("SearchRequestAcceptor:"+node.getIpString()+" "+params[2]+" "+params[3]);
    
                        }
                        catch (UnknownHostException e) {
                            System.out.println(" SearchRequestAcceptor:Node unreachable");
                            e.printStackTrace();
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                            System.out.println("SearchRequestAcceptor:Error in socket");
                        }
                    }
                } else {
                    System.out.println("SearchRequestAcceptor:Search request is not forewarded to other nodes");
                }
            }
            
        } else {
            System.out.println(
                    "SearchRequestAcceptor:Search request is not forewarded. It was received earlier " + previousSearch + " "
                            + request);
        }
        
    }
    
    public void sendSearchRequest(Node node, int hop, String ip, String port, String fileName) throws IOException {
        System.out.println("SearchRequestAcceptor:Trying to send search query for node" + node.toString() + " " + Arrays
                .toString(node.getIp()));
        String newRequest = "SER " + ip + " " + port + " " + fileName + " " + hop;
        System.out.println("SearchRequestAcceptor:in sendSearchRequest method " + newRequest);
        byte[] bufToSend = getMessageLength(newRequest).getBytes();
        DatagramPacket nodeDatagramPacket = new DatagramPacket(bufToSend, bufToSend.length,
                InetAddress.getByAddress(node.getIp()), node.getPort());
        threadDatagramSocket.send(nodeDatagramPacket);
    }
    
    public void sendSearchResponse(String ip, String port, List<String> fileNames) throws IOException {
        
        StringBuilder body = new StringBuilder();
        for (String fileName : fileNames) {
            body.append(fileName).append(" ");
        }
        System.out.println("SearchRequestAcceptor:Trying to send search response for node" + ip + " " + port);
        String messge = getMessageLength(
                "SEROK " + fileNames.size() + " " + myNode.getIpString() + " " + myNode.getPort() + " " + body.toString());
        byte[] bufToSend = messge.getBytes();
        System.out.println("SearchRequestAcceptor:Trying to send search response for node" + messge);
        DatagramPacket nodeDatagramPacket = new DatagramPacket(bufToSend, bufToSend.length, InetAddress.getByName(ip),
                Integer.parseInt(port));
        threadDatagramSocket.send(nodeDatagramPacket);
        System.out.println("SearchRequestAcceptor:sent search response for node");
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
