package com.grydtech.peershare.datagram.workerThread;

import com.grydtech.peershare.datagram.domain.Node;
import com.grydtech.peershare.datagram.domain.RegisterAndJoinMessenger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Created by dileka on 9/27/18.
 */
public class HeartBeatSender extends Thread {
    
    private DatagramSocket threadDatagramSocket = null;
    
    private CopyOnWriteArrayList<Node> routingTable;
    
    private Node myNode;
    
    private byte[] bufToSend;
    
    private String BSIP;
    
    private int BSPort;
    
    private boolean running;
    
    private RegisterAndJoinMessenger registerAndJoinMessenger;
    
    public HeartBeatSender(boolean running, RegisterAndJoinMessenger registerAndJoinMessenger, DatagramSocket socket,
            Node myNode, String BSIP, int BSPort, CopyOnWriteArrayList<Node> routingTable) {
        this.myNode = myNode;
        this.threadDatagramSocket = socket;
        this.routingTable = routingTable;
        this.BSIP = BSIP;
        this.BSPort = BSPort;
        this.running = running;
        this.registerAndJoinMessenger = registerAndJoinMessenger;
        System.out.println("Heart-Beat Sender: Thread started");
    }
    
    public void run() {
        System.out.println("Heart-Beat Sender:Entering the Heart-Beat sending loop");
        long start = System.currentTimeMillis();
        while (running) {
            List<Node> joinedNodes = routingTable.stream().filter(Node::isJoined).collect(Collectors.toList());
            if (registerAndJoinMessenger.registeredIndex < 4|| joinedNodes.size() > 1) {
                try {
                    System.out.println("HeartBeat Sender:HeartBeat thread sleep for 120 seconds:"
                            + registerAndJoinMessenger.registeredIndex);
                    Thread.sleep(1000 * 60 * 2);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("HeartBeat Sender:HeartBeat thread wakes up");
                System.out.println("Heart-Beat Sender: Routing table size:" + routingTable.size());
                System.out.println("HeartBeat Sender:Starting to send the Heart-Beat message to all nodes");
                for (Node node : routingTable) {
                    try {
                        if (node.getRetries() == 3) {
                            sendUnRegMessage(node);
                            node.setStatus(false);
                        } else {
                            if (!(Objects.equals(node.getIpString(), myNode.getIpString()) && node.getPort() == myNode
                                    .getPort())) {
                                sendHeartBeat(node);
                                System.out.println("HeartBeat Sender:HeartBeat message sent to " + node.getIpString());
                                
                            }
                        }
                    }
                    catch (UnknownHostException e) {
                        System.out.println("Node unreachable");
                        e.printStackTrace();
                        System.out.println("HeartBeat Sender:HeartBeat message failed to " + node.toString());
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("Error in socket");
                        System.out.println("HeartBeat Sender:HeartBeat message failed to " + node.toString());
                    }
                }
                System.out.println("HeartBeat Sender::" + registerAndJoinMessenger.registeredIndex);
                
            } else if (registerAndJoinMessenger.registeredIndex > 3&& joinedNodes.size()<2 && (((System.currentTimeMillis()-start) / (1000*60))
                    > 5)) {
                // System.out.println("HeartBeat Sender:Routing table size is 0"+routingTable.size());
                System.out.println("HeartBeat Sender:Server failed  to  join with two nodes, retrying");
                try {
                    registerAndJoinMessenger.sendJoin();
                    
                    start = System.currentTimeMillis();
                    System.out.println("HeartBeat Sender:Server resent join  messages");
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
        }
        
    }
    
    private void sendHeartBeat(Node node) throws IOException {
        System.out.println("HeartBeat Sender:inside send Heart Beat method ");
        String msg = getFullMessage("HEARTBEAT " + myNode.getIpString() + " " + myNode.getPort());
        bufToSend = msg.getBytes();
        System.out.println("HeartBeat Sender:HeartBeat Message:" + msg);
        DatagramPacket nodeDatagramPacket = new DatagramPacket(bufToSend, bufToSend.length,
                InetAddress.getByAddress(node.getIp()), node.getPort());
        threadDatagramSocket.send(nodeDatagramPacket);
        node.increaseRetries();
        System.out.println("HeartBeat Sender:HeartBeat Message sent to " + node.toString());
    }
    
    private String getFullMessage(String message) {
        int size = message.length() + 5;
        if (size < 100) {
            return "00" + size + " " + message;
        } else {
            return "0" + size + " " + message;
        }
    }
    
    private void sendUnRegMessage(Node node) throws IOException {
        System.out.println("HeartBeat Sender:inside send UNREG message method");
        String msg = getFullMessage("UNREG " + node.getIpString() + " " + node.getPort());
        bufToSend = msg.getBytes();
        System.out.println("HeartBeat Sender:UnReg Message:" + msg);
        DatagramPacket nodeDatagramPacket = new DatagramPacket(bufToSend, bufToSend.length, InetAddress.getByName(BSIP),
                BSPort);
        threadDatagramSocket.send(nodeDatagramPacket);
        node.setStatus(false);
        System.out.println("HeartBeat Sender:UnReg Message sent to " + BSIP + " " + BSPort + node.toString());
    }
    
}
