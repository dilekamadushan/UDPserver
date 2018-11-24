package com.grydtech.peershare.datagram.workerThread;

import com.grydtech.peershare.datagram.domain.Node;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by dileka on 9/27/18.
 */
public class LEAVERequestAcceptor extends Thread {
    
    private DatagramSocket datagramSocket;
    
    private CopyOnWriteArrayList<Node> routingTable;
    
    private String request;
    
    private Node myNode;
    
    private byte[] bufToSend;
    
    public LEAVERequestAcceptor(DatagramSocket datagramSocket, CopyOnWriteArrayList<Node> routingTable, Node myNode, String request) {
        
        this.routingTable = routingTable;
        this.request = request;
        this.myNode = myNode;
        this.datagramSocket = datagramSocket;
        System.out.println(" LeaveRequestAcceptor:Thread started " + request);
    }
    
    public void run() {
        String[] params = request.split(" ");
        System.out.println(" LeaveRequestAcceptorAcceptor: " + params[2]);
        
        Node node = routingTable.stream()
                .filter(s -> Objects.equals(s.getIpString(), params[2]) && s.getPort() == Integer.parseInt(params[3]))
                .findFirst().orElse(null);
        if (node != null) {
            
            System.out.println("LeaveRequestAcceptor:inside send gossip method " + node.toString() + " " + node.getIpString());
            String msg = getFullMessage(
                    "LEAVEOK 0");
            bufToSend = msg.getBytes();
            System.out.println("LeaveRequestAcceptor:LEAVEOK Message:" + msg);
            DatagramPacket nodeDatagramPacket = null;
            try {
                nodeDatagramPacket = new DatagramPacket(bufToSend, bufToSend.length,
                        InetAddress.getByAddress(node.getIp()), node.getPort());
                datagramSocket.send(nodeDatagramPacket);
                System.out.println("LeaveRequestAcceptor Message sent to " + node.getIpString() + " " + node.getPort() + " " + msg);
                routingTable.remove(node);
                System.out.println("LeaveRequestAcceptor:LeaveRequest  processed  removed node "+routingTable.size()+" " + node.toString());
    
    
            }
            catch (IOException e) {
                e.printStackTrace();
            }
    
        } else  {
           
            System.out.println("LeaveRequestAcceptor:The node" + params[2] + " " + params[3]
                    + " is not found in the routing table");
            
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
    
}
