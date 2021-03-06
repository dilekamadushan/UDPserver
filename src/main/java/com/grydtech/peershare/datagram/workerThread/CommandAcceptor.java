package com.grydtech.peershare.datagram.workerThread;

import com.grydtech.peershare.datagram.domain.Node;
import com.grydtech.peershare.datagram.domain.SearchResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

/**
 * Created by dileka on 9/27/18.
 */
public class CommandAcceptor extends Thread {
    
    private ExecutorService executorService;
    
    private DatagramSocket datagramSocket;
    
    private CopyOnWriteArrayList<Node> routingTable;
    
    private CopyOnWriteArrayList<String> fileNames;
    
    private SearchResult searchResult;
    
    private Node myNode;
    
    private CopyOnWriteArrayList<String> previousSearchRequests;
    
    private CopyOnWriteArrayList<String> previousSearchResponses;
    
    private int packetCount;
    
    private String BSIP;
    
    private int BSport;
    
    private boolean running;
    
    private int searchRequestId = 0;
    
    private int hopsCount = 7;
    
    public CommandAcceptor(boolean running, DatagramSocket socket, CopyOnWriteArrayList<Node> routingTable,
            SearchResult searchResult, CopyOnWriteArrayList<String> fileNames, Node myNode,
            CopyOnWriteArrayList<String> previousSearchRequests, CopyOnWriteArrayList<String> previousSearchResponses,
            int packetCount, int hopsCount, String BSIP, int BSport, ExecutorService executorService) {
        this.executorService = executorService;
        this.routingTable = routingTable;
        this.datagramSocket = socket;
        this.searchResult = searchResult;
        this.myNode = myNode;
        this.fileNames = fileNames;
        this.previousSearchRequests = previousSearchRequests;
        this.previousSearchResponses = previousSearchResponses;
        this.packetCount = packetCount;
        this.hopsCount=hopsCount;
        this.BSIP = BSIP;
        this.BSport = BSport;
        this.running = running;
        System.out.println("Search Query Acceptor : CommandAcceptor started");
    }
    
    public void run() {
        
        Scanner reader = new Scanner(System.in);  // Reading from System.in
        while (running) {
            
            //
            // 
            // System.out.println("Search Query Acceptor :Enter a search query: ");
            String query = reader.nextLine(); // Scans the next token of the input as a string.
            switch (query) {
                case "lsNodesJoined":
                    System.out.println("Search Query Acceptor :The nodes in the routing table are:");
                    routingTable.forEach(n -> {
                        if (n.isJoined()) {
                            System.out.println(n.toString());
                        }
                    });
                    
                    break;
                case "lsNodesFound":
                    System.out.println("Search Query Acceptor :The nodes in the routing table are:");
                    routingTable.forEach(n -> {
                        if (n.isStatus()) {
                            System.out.println(n.toString());
                        }
                    });
                    
                    break;
                case "lsFiles":
                    System.out.println("Search Query Acceptor :The files in this node are:");
                    fileNames.forEach(System.out::println);
                    break;
                case "lsMyInfo":
                    System.out.println("Search Query Acceptor :Printing self info:");
                    System.out.println(myNode.toString());
                    break;
                case "lsSearchResult":
                    System.out.println("Search Query Acceptor :The searchResult status now: " + searchResult.toString());
                    break;
                case "lsPacketCount":
                    System.out.println("Search Query Acceptor :The number of data packets received: " + packetCount);
                    break;
                case "lsSearchRequests":
                    System.out.println(
                            "Search Query Acceptor :The search requests received so far: " + previousSearchRequests.size());
                    previousSearchRequests.forEach(System.out::println);
                    break;
                case "lsSearchResponses":
                    System.out.println(
                            "Search Query Acceptor :The search responses received so far: " + previousSearchResponses
                                    .size());
                    previousSearchResponses.forEach(System.out::println);
                    break;
                case "resetSearchResult":
                    System.out.println("Search Query Acceptor :The search result is reset ");
                    searchResult.reset();
                    previousSearchRequests.removeAll(previousSearchRequests);
                    previousSearchResponses.removeAll(previousSearchResponses);
                    break;
                case "leave":
                    System.out.println("Search Query Acceptor :The system is trying to leave the system ");
                    try {
                        sendLEAVEAndUNREGMessage(BSIP, BSport);
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("Search Query Acceptor :The system is trying to leave the system ");
                    }
                    break;
                case "lsNodesSpecial":
                    System.out.println("Search Query Acceptor :The nodes in the routing table are:");
                    routingTable.forEach(n -> System.out.println(n.toString()));
                    
                    break;
                default:
                    if (query.length() > 7 && "search".equals(query.substring(0, 6))) {
                        System.out.println(
                                "Search Query Acceptor :The user has requested to search for files by name: " + query
                                        .substring(7));
                        submitSearchRequest(query.substring(7));
                    } else if (query.length() > 8 && "addFile".equals(query.substring(0, 7))) {
                        System.out.println("Search Query Acceptor : User requested to add a File:" + query.substring(8));
                        fileNames.add(query.substring(8));
                    } else if (query.length() > 8 && "setHops".equals(query.substring(0, 7))) {
                        System.out.println("Search Query Acceptor : User has requested to reset hops:" + query.substring(8));
                        hopsCount = Integer.parseInt(query.substring(8));
                    } else {
                        System.out.println("Search Query Acceptor : unidentified query:" + query);
                        
                    }
                
            }
            
        }
        //once finished
    }
    
    public void submitSearchRequest(String keyword) {
        if (!Objects.equals("", keyword)) {
            searchResult.reset();
            previousSearchRequests.removeAll(previousSearchRequests);
            previousSearchResponses.removeAll(previousSearchResponses);
            searchResult.setQuery(keyword);
            searchResult.setInUse(true);
            executorService.execute(
                    new SearchRequestAcceptor(packetCount, hopsCount, this.datagramSocket, routingTable, fileNames, myNode,
                            getFullMessage("SER " + myNode.getIpString() + " " + myNode.getPort() + " " + keyword + " "
                                    + searchRequestId + " 0"), true, previousSearchRequests, searchResult));
            searchRequestId += 1;
        } else {
            System.out.println("Search Query Acceptor: accepted query: " + keyword
                    + " User has entered empty query, terminating search result");
            
        }
        
    }
    
    private String getFullMessage(String message) {
        int size = message.length() + 5;
        if (size < 100) {
            return "00" + size + " " + message;
        } else {
            return "0" + size + " " + message;
        }
    }
    
    private void sendLEAVEAndUNREGMessage(String BSIP, Integer BSport) throws IOException {
        System.out.println("Search Query Acceptor:inside send leave message method " + BSIP + " " + BSport);
        String msg = getFullMessage("LEAVE " + myNode.getIpString() + " " + myNode.getPort());
        byte[] bufToSend = msg.getBytes();
        System.out.println("Search Query Acceptor:Leave Message:" + msg);
        
        running = false;
        
        for (Node node : routingTable) {
            DatagramPacket nodeDatagramPacket = new DatagramPacket(bufToSend, bufToSend.length,
                    InetAddress.getByAddress(node.getIp()), node.getPort());
            datagramSocket.send(nodeDatagramPacket);
            System.out.println(
                    "Search Query Acceptor:Leave Message sent to " + node.getIpString() + " " + node.getPort() + " " + msg);
            try {
                Thread.sleep(500);
                datagramSocket.send(nodeDatagramPacket);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            
        }
        
        Socket TCPSocket = new Socket(BSIP, BSport);
        PrintWriter out = new PrintWriter(TCPSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(TCPSocket.getInputStream()));
        
        out.println(getFullMessage("UNREG " + myNode.getIpString() + " " + myNode.getPort() + " " + myNode.getNodeName()));
        
        char[] chars = new char[8192];
        int read = in.read(chars);
        String inMesssage = String.valueOf(chars, 0, read);
        System.out.println("Command Executor:Reply from BS server:" + inMesssage);
        if (inMesssage.length() == 12) {
            System.out.println("Command Executor:Unregistering successful:" + inMesssage);
        }
        
        out.close();
        in.close();
        TCPSocket.close();
    }
    
}
