package datagram.threadPooled;

import datagram.threadPooled.domain.Node;
import datagram.threadPooled.domain.RegisterAndJoinMessenger;
import datagram.threadPooled.domain.SearchResult;
import datagram.threadPooled.workerThread.GossipAcceptor;
import datagram.threadPooled.workerThread.GossipSender;
import datagram.threadPooled.workerThread.JoinRequestAcceptor;
import datagram.threadPooled.workerThread.JoinResponseAcceptor;
import datagram.threadPooled.workerThread.SearchQueryAcceptor;
import datagram.threadPooled.workerThread.SearchRequestAcceptor;
import datagram.threadPooled.workerThread.SearcheResponseAcceptor;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Created by dileka on 9/29/18.
 */
public class Server extends Thread {
    
    protected ExecutorService threadPool = Executors.newFixedThreadPool(20);
    
    private DatagramSocket UDPsocket;
    
    private boolean running;
    
    private byte[] buf = new byte[256];
    
    private ArrayList<Node> toJoinNodes;
    
    private ArrayList<Node> triedToJoinNodes;
    
    private CopyOnWriteArrayList<Node> routingTable;
    
    private RegisterAndJoinMessenger registerAndJoinMessenger = null;
    
    private String myIP;
    
    private int myPort;
    
    private long programeStartedTime;
    
    private ArrayList<String> fileNames;
    
    private SearchResult searchResult = new SearchResult();
    
    private Node myNode;
    
    private ArrayList<String> previousSearchRequsts;
    
    private int packetCount = 0;
    
    public Server(String BSIp, int BSPort, String myIP, int myPort, int IDForDisplay) throws SocketException {
        programeStartedTime = System.currentTimeMillis();
        this.myIP = myIP;
        this.myPort = myPort;
        UDPsocket = new DatagramSocket(myPort);
        toJoinNodes = new ArrayList<>();
        triedToJoinNodes = new ArrayList<>();
        toJoinNodes = new ArrayList<>();
        routingTable = new CopyOnWriteArrayList<>();
        previousSearchRequsts = new ArrayList<>();
        
        String[] ips = myIP.replace(".", " ").split(" ");
        myNode = new Node(new byte[] { (byte) Integer.parseInt(ips[0]), (byte) Integer.parseInt(ips[1]),
                (byte) Integer.parseInt(ips[2]), (byte) Integer.parseInt(ips[3]) }, UUID.randomUUID(), myPort);
        myNode.setIpString(myIP);
        myNode.setIdForDisplay(IDForDisplay);
        registerAndJoinMessenger = new RegisterAndJoinMessenger(BSIp, BSPort, myNode, UDPsocket, toJoinNodes,
                triedToJoinNodes, routingTable);
    }
    
    public void run() {
        try {
            System.out.println("Server Thread: Before registering the server");
            running = this.registerAndJoinMessenger.start();
            System.out.println("Server Thread:This is the status 1st place" + running);
            System.out.println("Server Thread: Register and join messenger started");
            fileNames = getFile("Files/fileNames.txt");
            SearchQueryAcceptor searchQueryAcceptor = new SearchQueryAcceptor(UDPsocket, routingTable, searchResult,
                    fileNames, myNode, previousSearchRequsts, packetCount, threadPool);
            searchQueryAcceptor.start();
            System.out.println("Server Thread: Query acceptor started");
            GossipSender gossipSender = new GossipSender(UDPsocket, myNode, routingTable);
            gossipSender.start();
            System.out.println("Server Thread: Gossip Sender started");
        }
        catch (ConnectException ce) {
            System.out.println("Server Thread:Bootstrap server unreachable");
            ce.printStackTrace();
            UDPsocket.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            UDPsocket.close();
        }
        long executionTime;
        System.out.println("Server Thread:This is the status " + running);
        if (running)
            System.out.println("Server Thread: Server Successfully Registered at " + myIP + " " + myPort);
        while (running) {
            System.out.println("Server Thread:Server inside the running loop");
            executionTime = System.currentTimeMillis();
            if ((executionTime - programeStartedTime) / 1000 > 300) {
                System.out.println("Server Thread: More than 5 minutes since the start");
                List<Node> nodes = routingTable.stream().filter(Node::isJoined).collect(Collectors.toList());
                if (nodes.size() < 2) {
                    System.out.println(
                            "Server Thread: The 5 minute timeout has occurred and Server stopping due to failure to join with at least two nodes");
                    break;
                }
            }
            System.out.println("Server Thread:Server listening....");
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            try {
                UDPsocket.receive(packet);
                System.out.println("Server Thread: Server received a packet ");
                packetCount += 1;
                System.out.println("Server Thread: Data packets:" + packetCount);
            }
            catch (IOException e) {
                e.printStackTrace();
                System.out.println("Server Thread: Server faced an error when receiving the packet, exiting");
                break;
            }
            
            String request = new String(packet.getData(), 0, packet.getLength());
            System.out.println("Server Thread:In server received " + request);
            boolean whoseResponse = false;
            while (!whoseResponse) {
                System.out.println("Server Thread:Trying to identify the handler for the packet " + request);
                whoseResponse = checkForJoinResponseMessage(request);
                
                if (whoseResponse) {
                    System.out.println(
                            "Server Thread:JOINOK messeage Packet to be handled by Routing Table manager " + routingTable
                                    .size() + " ");
                    this.threadPool.execute(new JoinResponseAcceptor(this.UDPsocket, routingTable, packet));
                    System.out.println("Server Thread: One Routing table manager started");
                    break;
                    
                }
                System.out.println("Server Thread:Not JOINOK message");
                whoseResponse = checkForJoinRequestMessage(request);
                if (whoseResponse) {
                    
                    System.out.println("Server Thread:JOIN messeage Packet to be handled by Routing Table manager ");
                    this.threadPool.execute(new JoinRequestAcceptor(this.UDPsocket, routingTable, myNode, request));
                    System.out.println("Server Thread: One Join Request Acceptor started");
                    break;
                    
                }
                System.out.println("Server Thread:Not JOIN messeage Packet ");
                whoseResponse = checkForSearchResponseMessage(request);
                if (whoseResponse) {
                    
                    System.out.println("Server Thread:SEROK messeage Packet to be handled by SEARCH handler");
                    this.threadPool.execute(new SearcheResponseAcceptor(routingTable, searchResult, request));
                    System.out.println("Server Thread: One Routing table manager started");
                    break;
                    
                }
                
                System.out.println("Server Thread:Not SEROK message");
                whoseResponse = checkForSearchRequestMessage(request);
                if (whoseResponse) {
                    System.out.println("Server Thread:SER messeage Packet to be handled by SEARCH handler ");
                    this.threadPool.execute(
                            new SearchRequestAcceptor(this.UDPsocket, routingTable, fileNames, myNode, request, false,
                                    previousSearchRequsts, searchResult));
                    System.out.println("Server Thread: One Routing table manager started");
                    break;
                }
                
                System.out.println("Server Thread:Not SER message");
                whoseResponse = checkForGossipRequestMessage(request);
                if (whoseResponse) {
                    System.out.println("Server Thread:GOSSIP messeage Packet to be handled by GOSSIP handler ");
                    this.threadPool.execute(new GossipAcceptor(routingTable, myNode, request));
                    System.out.println("Server Thread: One Routing table manager started");
                    break;
                }
                
                System.out.println("Server Thread:Not GOSSIP message");
                System.out.println("Server Thread: Couldn't Figure Out the handler for message " + request);
                whoseResponse = true;
            }
            System.out.println("Server Thread: Figured Out the handler");
            
            System.out.println("Server Thread:Clear the buffer after every message");
            buf = new byte[256];
        }
        System.out.println("Server Thread:Server interrupted, Exiting");
        UDPsocket.close();
        System.out.println("Server Thread:Socket Closed");
    }
    
    public String data(byte[] a) {
        if (a == null)
            return null;
        StringBuilder ret = new StringBuilder();
        int i = 0;
        while (a[i] != 0) {
            ret.append((char) a[i]);
            i++;
        }
        return ret.toString();
    }
    
    private ArrayList<String> getFile(String fileName) {
        
        ArrayList<String> result = new ArrayList<>();
        
        //Get file from resources folder
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        
        try (Scanner scanner = new Scanner(file)) {
            
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                int random = new Random().nextInt(2);
                System.out.println(random);
                if (random % 2 == 0) {
                    String[] words = line.split(" ");
                    Collections.addAll(result, words);
                }
            }
            scanner.close();
            
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        
        return result;
        
    }
    
    public boolean checkForJoinResponseMessage(String request) {
        
        System.out.println("Server Thread:checking for JOINOK response" + request);
        if (request.contains("JOINOK")) {
            return true;
            
        }
        System.out.println("Server Thread:checking for JOINOK response failed" + request);
        return false;
    }
    
    public boolean checkForJoinRequestMessage(String request) {
        
        System.out.println("Server Thread:checking for JOIN request" + request);
        if (request.contains("JOIN")) {
            return true;
        }
        System.out.println("Server Thread:checking for JOIN response failed" + request);
        return false;
    }
    
    public boolean checkForGossipRequestMessage(String request) {
        
        System.out.println("Server Thread:checking for gossip request" + request);
        if (request.contains("GOSSIP")) {
            return true;
        }
        return false;
    }
    
    public boolean checkForSearchRequestMessage(String request) {
        
        System.out.println("Server Thread:checking for search request" + request);
        if (request.contains("SER")) {
            return true;
        }
        return false;
    }
    
    public boolean checkForSearchResponseMessage(String request) {
        
        System.out.println("Server Thread:checking for search response" + request);
        if (request.contains("SEROK")) {
            return true;
        }
        return false;
    }
}
