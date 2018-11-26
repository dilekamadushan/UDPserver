package com.grydtech.peershare.datagram.domain;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by dileka on 10/2/18.
 */
public class Node {
    
    private byte[] ip;
    
    private String ipString;
    
    private UUID systemUsername;
    
    private String nodeName;
    
    private int idForDisplay = 0;
    
    private int port;
    
    private boolean status = false;
    
    private boolean isJoined = false;
    
    private String discoveredBy = "";
    
    private int retries = 0;
    
    private ArrayList<String> searchQueries = new ArrayList<>();
    
    public Node(byte[] ip, int port, String nodeName, UUID systemUsername) {
        this.ip = ip;
        this.nodeName = nodeName;
        this.port = port;
        this.systemUsername = systemUsername;
        
    }
    
    public void addSearchQuery(String searchQuery) {
        this.searchQueries.add(searchQuery);
    }
    
    public ArrayList<String> getSearchQueries() {
        return searchQueries;
        
    }
    
    public void setSearchQueries(ArrayList<String> searchQueries) {
        this.searchQueries = searchQueries;
    }
    
    public String getNodeName() {
        return nodeName;
    }
    
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }
    
    public UUID getSystemUsername() {
        return systemUsername;
    }
    
    public void setSystemUsername(UUID systemUsername) {
        this.systemUsername = systemUsername;
    }
    
    public int getIdForDisplay() {
        return idForDisplay;
    }
    
    public void setIdForDisplay(int idForDisplay) {
        this.idForDisplay = idForDisplay;
    }
    
    public boolean isStatus() {
        
        return status;
    }
    
    public void setStatus(boolean status) {
        
        this.status = status;
    }
    
    public int getRetries() {
        return retries;
    }
    
    public void increaseRetries() {
        this.retries += 1;
    }
    
    public void decreaseRetries() {
        if (this.retries > 0) {
            this.retries -= 1;
        }
    }
    
    public byte[] getIp() {
        return ip;
    }
    
    public void setIp(byte[] ip) {
        this.ip = ip;
    }
    
    public int getPort() {
        return port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    @Override
    public String toString() {
        
        return "ip:" + getIpString() + " port:" + this.getPort() + " nodeName:" + getNodeName() + " " + systemUsername + ":"
                + this.getSystemUsername().toString() + " isJoined:" + isJoined() + "  status:" + status + " retries:"
                + retries + " discovered by:" + discoveredBy;
    }
    
    public boolean isJoined() {
        return isJoined;
    }
    
    public void setJoined(boolean joined) {
        isJoined = joined;
        status = true;
        setDiscoveredBy("From Join Message");
    }
    
    public String getIpString() {
        return ipString;
    }
    
    public void setIpString(String ipString) {
        this.ipString = ipString;
    }
    
    public String getDiscoveredBy() {
        return discoveredBy;
    }
    
    public void setDiscoveredBy(String discoveredBy) {
        this.discoveredBy = discoveredBy;
    }
}
