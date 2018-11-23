package com.grydtech.peershare.datagram;

import com.grydtech.peershare.datagram.domain.Node;
import com.grydtech.peershare.datagram.domain.RegisterAndJoinMessenger;
import com.grydtech.peershare.datagram.domain.SearchResult;
import com.grydtech.peershare.datagram.workerThread.CommandAcceptor;
import com.grydtech.peershare.datagram.workerThread.GossipAcceptor;
import com.grydtech.peershare.datagram.workerThread.GossipSender;
import com.grydtech.peershare.datagram.workerThread.HeartBeatRequestAcceptor;
import com.grydtech.peershare.datagram.workerThread.HeartBeatSender;
import com.grydtech.peershare.datagram.workerThread.JoinRequestAcceptor;
import com.grydtech.peershare.datagram.workerThread.JoinResponseAcceptor;
import com.grydtech.peershare.datagram.workerThread.KafkaLogger;
import com.grydtech.peershare.datagram.workerThread.KafkaProducer;
import com.grydtech.peershare.datagram.workerThread.SearchRequestAcceptor;
import com.grydtech.peershare.datagram.workerThread.SearcheResponseAcceptor;
import com.grydtech.peershare.datagram.workerThread.WebUpdater;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
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
@Component
public class Server extends Thread {

    private ExecutorService threadPool = Executors.newFixedThreadPool(20);

    private DatagramSocket UDPsocket;

    private boolean running;

    private byte[] buf = new byte[256];

    private ArrayList<Node> toJoinNodes;

    private ArrayList<Node> triedToJoinNodes;

    private CopyOnWriteArrayList<Node> routingTable;

    private RegisterAndJoinMessenger registerAndJoinMessenger = null;

    private String myIP;

    private String BSIP;

    private int myPort;

    private int BSPort;

    private long programeStartedTime;

    private CopyOnWriteArrayList<String> fileNames;

    private SearchResult searchResult = new SearchResult();

    private Node myNode;

    private CopyOnWriteArrayList<String> previousSearchRequsts;

    private CopyOnWriteArrayList<String> previousSearchResponses;

    // private CopyOnWriteArrayList<String> previousSentSearchResponses;

    private int packetCount = 0;

    private KafkaLogger kafkaLogger;

    private WebUpdater webUpdater;

    private int myIdForDisplay;

    private SimpMessagingTemplate simpMessagingTemplate;

    private CommandAcceptor commandAcceptor;


    public void initServer(String BSIp, int BSPort, String myIP, int myPort, String nodeName, SimpMessagingTemplate simpMessagingTemplate) throws SocketException {
        programeStartedTime = System.currentTimeMillis();
        this.myIP = myIP;
        this.BSIP = BSIp;
        this.myPort = myPort;
        this.BSPort = BSPort;
        UDPsocket = new DatagramSocket(this.myPort);
        toJoinNodes = new ArrayList<>();
        triedToJoinNodes = new ArrayList<>();
        toJoinNodes = new ArrayList<>();
        routingTable = new CopyOnWriteArrayList<>();
        previousSearchRequsts = new CopyOnWriteArrayList<>();
        previousSearchResponses = new CopyOnWriteArrayList<>();
        //previousSentSearchResponses = new CopyOnWriteArrayList<>();

        String[] ips = myIP.replace(".", " ").split(" ");
        myNode = new Node(new byte[]{(byte) Integer.parseInt(ips[0]), (byte) Integer.parseInt(ips[1]),
                (byte) Integer.parseInt(ips[2]), (byte) Integer.parseInt(ips[3])}, this.myPort, nodeName,
                UUID.randomUUID());
        myNode.setIpString(myIP);
        String s = String.valueOf(myPort);
        this.myIdForDisplay = Integer.parseInt(s.substring(s.length() - 1));
        myNode.setIdForDisplay(myIdForDisplay);
        registerAndJoinMessenger = new RegisterAndJoinMessenger(BSIp, BSPort, myNode, UDPsocket, toJoinNodes,
                triedToJoinNodes, routingTable);

        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    public void run() {
        try {
            System.out.println("Server Thread: Before registering the server");
            running = this.registerAndJoinMessenger.start();
            System.out.println("Server Thread:This is the status 1st place" + running);
            System.out.println("Server Thread: Register and join messenger started");
            fileNames = getFile("Files/fileNames.txt");
            System.out.println("Server Thread: No of files is:" + fileNames.size());
            commandAcceptor = new CommandAcceptor(running, UDPsocket, routingTable, searchResult, fileNames, myNode,
                    previousSearchRequsts, previousSearchResponses, packetCount, BSIP, BSPort, threadPool);
            commandAcceptor.start();
            System.out.println("Server Thread: Query acceptor started");
            GossipSender gossipSender = new GossipSender(running, UDPsocket, myNode, routingTable);
            gossipSender.start();
            System.out.println("Server Thread: Gossip Sender started");

            KafkaProducer kafkaProducer = new KafkaProducer(myNode, routingTable, running);
            kafkaProducer.start();
            System.out.println("Server Thread: Kafka producer started");

            HeartBeatSender heartBeatSender = new HeartBeatSender(running, registerAndJoinMessenger, UDPsocket, myNode, BSIP, BSPort, routingTable);
            heartBeatSender.start();
            System.out.println("Server Thread: Heart Beat Sender started");
            kafkaLogger = new KafkaLogger(running);

            System.out.println("Server Thread: Kafka logger started");
            // webUpdater = new WebUpdater(running,myNode,BSIP,BSPort,searchResult);
            System.out.println("Server Thread: Web Updater started");

            webUpdater = new WebUpdater(running, searchResult, simpMessagingTemplate);
            webUpdater.start();
        } catch (ConnectException ce) {
            System.out.println("Server Thread:Bootstrap server unreachable");
            ce.printStackTrace();
            UDPsocket.close();
        } catch (IOException e) {
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
            if ((executionTime - programeStartedTime) / 1000 > 12000) {
                System.out.println("Server Thread: More than 10 minutes since the start");
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
            } catch (IOException e) {
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
                    kafkaLogger.log(myIdForDisplay, "JOINOK");
                    break;

                }
                whoseResponse = checkForJoinRequestMessage(request);
                if (whoseResponse) {

                    System.out.println("Server Thread:JOIN messeage Packet to be handled by Join Requestor ");
                    this.threadPool.execute(new JoinRequestAcceptor(this.UDPsocket, routingTable, myNode, request));
                    System.out.println("Server Thread: One Join Request Acceptor started");
                    kafkaLogger.log(myIdForDisplay, "JOIN");
                    break;

                }
                System.out.println("Server Thread:Not JOIN messeage Packet ");
                whoseResponse = checkForSearchResponseMessage(request);
                if (whoseResponse) {

                    System.out.println("Server Thread:SEROK messeage Packet to be handled by SEARCH Response handler");
                    this.threadPool.execute(
                            new SearcheResponseAcceptor(packetCount, routingTable, previousSearchResponses, myNode, searchResult,
                                    request));
                    System.out.println("Server Thread: One Search Response Acceptor started");
                    break;

                }

                System.out.println("Server Thread:Not SEROK message");
                whoseResponse = checkForSearchRequestMessage(request);
                if (whoseResponse) {
                    System.out.println("Server Thread:SER messeage Packet to be handled by SEARCH Request handler ");
                    this.threadPool.execute(
                            new SearchRequestAcceptor(packetCount, this.UDPsocket, routingTable, fileNames, myNode, request, false,
                                    previousSearchRequsts, searchResult));
                    System.out.println("Server Thread: One Search Request Acceptor started");
                    kafkaLogger.log(myIdForDisplay, "SEROK");
                    break;
                }

                System.out.println("Server Thread:Not SER message");
                whoseResponse = checkForGossipRequestMessage(request);
                if (whoseResponse) {
                    System.out.println("Server Thread:GOSSIP messeage Packet to be handled by GOSSIP handler ");
                    this.threadPool.execute(new GossipAcceptor(routingTable, myNode, request));
                    System.out.println("Server Thread: One GOSSIP handler started");
                    kafkaLogger.log(myIdForDisplay, "SER");
                    break;
                }

                System.out.println("Server Thread:Not GOSSIP message");
                whoseResponse = checkForHeartBeatMessage(request);
                if (whoseResponse) {
                    System.out.println("Server Thread:HeartBeat messeage Packet to be handled by HeartBeat handler ");
                    this.threadPool.execute(new HeartBeatRequestAcceptor(routingTable, myNode, request));
                    System.out.println("Server Thread: Heart Beat Acceptor started");
                    break;
                }

                System.out.println("Server Thread:Not HeartBeat message");
                System.out.println("Server Thread: Couldn't Figure Out the handler for message " + request);
                whoseResponse = true;
            }
            System.out.println("Server Thread:Clear the buffer after every message");
            buf = new byte[256];
        }
        System.out.println("Server Thread:Server interrupted, Exiting");
        UDPsocket.close();
        kafkaLogger.closeProducer();
        System.out.println("Server Thread:Socket Closed");
    }

    public void submitSearchRequest(String keyword) {
        commandAcceptor.submitSearchRequest(keyword);
    }

    private CopyOnWriteArrayList<String> getFile(String fileName) {

        CopyOnWriteArrayList<String> result = new CopyOnWriteArrayList<>();

        //Get file from resources folder
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());

        try (Scanner scanner = new Scanner(file)) {

            while (result.size() < 5) {
                String line = scanner.nextLine();
                int random = new Random().nextInt(100);
                System.out.println(random);
                if (random % 2 == 1) {
                    String[] words = line.split(" ");
                    StringBuilder stringBuilder = new StringBuilder();

                    for (int i = 0; i < words.length; i++) {
                        if (i != words.length - 1) {
                            stringBuilder.append(words[i]).append("_");
                        } else {
                            stringBuilder.append(words[i]);
                        }
                    }
                    //Collections.addAll(result, line);
                    result.add(stringBuilder.toString());
                }
            }
            scanner.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;

    }

    private boolean checkForJoinResponseMessage(String request) {

        System.out.println("Server Thread:checking for JOINOK response" + request);
        if (request.contains("JOINOK")) {
            return true;

        }
        System.out.println("Server Thread:checking for JOINOK response failed" + request);
        return false;
    }

    private boolean checkForJoinRequestMessage(String request) {

        System.out.println("Server Thread:checking for JOIN request" + request);
        if (request.contains("JOIN")) {
            return true;
        }
        System.out.println("Server Thread:checking for JOIN response failed" + request);
        return false;
    }

    private boolean checkForGossipRequestMessage(String request) {

        System.out.println("Server Thread:checking for gossip request" + request);
        return request.contains("GOSSIP");
    }

    private boolean checkForSearchRequestMessage(String request) {

        System.out.println("Server Thread:checking for search request" + request);
        return request.contains("SER");
    }

    private boolean checkForSearchResponseMessage(String request) {

        System.out.println("Server Thread:checking for search response" + request);
        return request.contains("SEROK");
    }

    private boolean checkForHeartBeatMessage(String request) {

        System.out.println("Server Thread:checking for HeartBeat" + request);
        return request.contains("HEARTBEAT");
    }

    public boolean checkForLeaveResponseMessage(String request) {

        System.out.println("Server Thread:checking for LeaveOk" + request);
        return request.contains("LEAVEOK");
    }

}