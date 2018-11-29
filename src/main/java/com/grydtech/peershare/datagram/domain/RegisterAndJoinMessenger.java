package com.grydtech.peershare.datagram.domain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by dileka on 9/27/18.
 */
public class RegisterAndJoinMessenger {
    
    public int registeredIndex;
    
    private String BSIP;
    
    private int BSPort;
    
    private DatagramSocket threadUDPSocket = null;
    
    private ArrayList<Node> toJoinNodes;
    
    private ArrayList<Node> triedToJoinNodes;
    
    private List<Node> routingTable;
    
    private Socket TCPSocket;
    
    private PrintWriter out;
    
    private BufferedReader in;
    
    private Node myNode;
    
    public RegisterAndJoinMessenger(String BSIP, int BSPort, Node myNode, DatagramSocket socket, ArrayList<Node> toJoinNodes,
            ArrayList<Node> triedToJoinNodes, CopyOnWriteArrayList<Node> routingTable) {
        
        this.BSIP = BSIP;
        this.BSPort = BSPort;
        this.threadUDPSocket = socket;
        this.toJoinNodes = toJoinNodes;
        this.triedToJoinNodes = triedToJoinNodes;
        this.routingTable = routingTable;
        this.myNode = myNode;
    }
    
    public boolean start() throws IOException {
        boolean isRegistered = Register(BSIP, BSPort,
                "REG " + myNode.getIpString() + " " + myNode.getPort() + " " + myNode.getNodeName());
        
        if (isRegistered) {
            boolean isJoinSent;
            System.out.println("Register and Join Messenger:The size of toJoinNodesMethod " + toJoinNodes.size());
            
            isJoinSent = sendJoin();
            if (isJoinSent)
                return true;
        }
        return false;
    }
    
    //Function to register in BS
    private boolean Register(String BSIp, int BSPort, String msg) throws IOException {
        msg = getMessageLength(msg);
        System.out.println("Register and Join Messenger:REG  message from node " + msg);
        TCPSocket = new Socket(BSIp, BSPort);
        out = new PrintWriter(TCPSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(TCPSocket.getInputStream()));
        
        //send UNREG message first
        
        out.println(getMessageLength("UNREG " + myNode.getIpString() + " " + myNode.getPort() + " " + myNode.getNodeName()));
        char[] chars = new char[8192];
        int read = in.read(chars);
        String inMesssage = String.valueOf(chars, 0, read);
        TCPSocket.close();
        out.close();
        in.close();
        
        TCPSocket = new Socket(BSIp, BSPort);
        out = new PrintWriter(TCPSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(TCPSocket.getInputStream()));
        
        out.println(msg);
        chars = new char[8192];
        read = in.read(chars);
        inMesssage = String.valueOf(chars, 0, read);
        System.out.println("Register and Join Messenger:Reply from BS server:" + inMesssage);
        String REGOK = inMesssage.substring(5, 10);
        int lengthOfMessage = Integer.parseInt(inMesssage.substring(0, 4));
        System.out.println("Register and Join Messenger:REGOK message:" + REGOK);
        int code = 0;
        if (lengthOfMessage > 12) {
            code = Integer.parseInt(inMesssage.substring(11, 13).trim());
            System.out.println("Register and Join Messenger:Code " + code);
        }
        
        if ("REGOK".equals(REGOK))
            if (lengthOfMessage == 12) {
                System.out.println("Register and Join Messenger:No hosts connected already");
                return true;
            } else if (98 > code) {
                String[] hostList;
                
                if (code < 10) {
                    hostList = inMesssage.substring(13).trim().split(" ");
                    System.out.println(
                            "Register and Join Messenger:No of Hosts connected already- " + inMesssage.substring(13));
                } else {
                    hostList = inMesssage.substring(14).trim().split(" ");
                    System.out.println(
                            "Register and Join Messenger:No of Hosts connected already- " + inMesssage.substring(14));
                }
                
                System.out.println("Register and Join Messenger:last host string " + hostList + " " + hostList.length);
                registeredIndex = (hostList.length / 3);
                for (int i = 0; i < hostList.length; i += 3) {
                    //for (int i = 0; i < 6; i += 3) {
                    
                    System.out.println(
                            "Register and Join Messenger:Inside the loop:" + hostList[i] + " " + hostList[i + 1] + " "
                                    + hostList[i + 2]);
                    String[] ips = hostList[i].replace(".", " ").split(" ");
                    System.out.println(
                            "Register and Join Messenger:" + Integer.parseInt(ips[0]) + " " + Integer.parseInt(ips[1]) + " "
                                    + Integer.parseInt(ips[2]) + " " + Integer.parseInt(ips[3]));
                    System.out.println("Register and Join Messenger:pppppppppp" + hostList[i + 2]);
                    
                    Node node = new Node(new byte[] { (byte) Integer.parseInt(ips[0]), (byte) Integer.parseInt(ips[1]),
                            (byte) Integer.parseInt(ips[2]), (byte) Integer.parseInt(ips[3]) },
                            Integer.parseInt(hostList[i + 1]), hostList[i + 2], UUID.randomUUID());
                    node.setIpString(hostList[i]);
                    node.setIdForDisplay(Integer.parseInt(hostList[i + 1].substring(hostList[i + 1].length() - 1)));
                    toJoinNodes.add(node);
                    routingTable.add(node);
                    System.out.println("Register and Join Messenger: added new node" + node.toString());
                }
                
                for (Node node : toJoinNodes) {
                    System.out.println("Register and Join Messenger:Node created" + node.toString());
                }
                System.out.println("Register and Join Messenger:The size of routing table" + routingTable.size());
                return true;
                
            } else {
                String errorCode = inMesssage.substring(11);
                switch (errorCode) {
                    case "9999":
                        System.out.println("failed, there is some error in the command");
                        break;
                    case "9998":
                        System.out.println("failed, already registered to you, unregister first");
                        break;
                    case "9997":
                        System.out.println(" failed, registered to another user, try a different IP and port");
                        break;
                    case "9996":
                        System.out.println("failed, can’t register. BS full.");
                    default:
                        System.out.println("Invalid command");
                }
            }
        
        in.close();
        out.close();
        TCPSocket.close();
        return false;
        
    }
    
    public boolean sendJoin() throws IOException {
        if (toJoinNodes.size() <= 2) {
            System.out.println("Register and Join Messenger:Inside Send Join method and sending Join message to all nodes "
                    + toJoinNodes.size());
            ArrayList<Node> nodes = toJoinNodes;
            for (Node node : nodes) {
                triedToJoinNodes.add(node);
                try {
                    String message = getMessageLength("JOIN " + myNode.getIpString() + " " + myNode.getPort());
                    byte[] bufToSend = message.getBytes();
                    DatagramPacket nodeDatagramPacket = new DatagramPacket(bufToSend, bufToSend.length,
                            InetAddress.getByAddress(node.getIp()), node.getPort());
                    node.increaseRetries();
                    threadUDPSocket.send(nodeDatagramPacket);
                    System.out.println("Register and Join Messenger: Successfully sent the join message " + message);
                    
                    threadUDPSocket.send(nodeDatagramPacket);
                    System.out.println("Register and Join Messenger: Successfully sent the 2nd join message " + message);
                }
                catch (UnknownHostException e) {
                    System.out.println("Register and Join Messenger:Node unreachable");
                    e.printStackTrace();
                }
                catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Register and Join Messenger:Error in socket");
                }
            }
            return true;
        } else if (toJoinNodes.size() > 2) {
            Node node;
            for (int i = 0; i < 2; i++) {
                node = toJoinNodes.get(ThreadLocalRandom.current().nextInt(0, toJoinNodes.size()));
                toJoinNodes.remove(node);
                triedToJoinNodes.add(node);
                try {
                    System.out.println(
                            "Register and Join Messenger: Trying to send join message for node" + node.toString() + " "
                                    + node.getIpString());
                    String message = getMessageLength("JOIN " + myNode.getIpString() + " " + myNode.getPort());
                    byte[] bufToSend = message.getBytes();
                    DatagramPacket nodeDatagramPacket = new DatagramPacket(bufToSend, bufToSend.length,
                            InetAddress.getByAddress(node.getIp()), node.getPort());
                    node.increaseRetries();
                    threadUDPSocket.send(nodeDatagramPacket);
                    System.out.println("Register and Join Messenger: Successfully sent the join message " + message);
                    
                    threadUDPSocket.send(nodeDatagramPacket);
                    System.out.println("Register and Join Messenger: Successfully sent the 2nd join message " + message);
                }
                catch (UnknownHostException e) {
                    System.out.println("Register and Join Messenger:Node unreachable");
                    e.printStackTrace();
                }
                catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Register and Join Messenger:Error in socket");
                }
            }
            System.out.println("Register and Join Messenger:Successfully sent the Join Message to 2 nodes");
            return true;
        }
        System.out.println("Register and Join Messenger:No nodes to send the JOIN" + toJoinNodes.size());
        return false;
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
