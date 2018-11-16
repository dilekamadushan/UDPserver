package datagram.threadPooled.domain;

import java.util.ArrayList;

/**
 * Created by dileka on 10/2/18.
 */
public class Node {
    
    private byte[] ip;
    
    private String ipString;
    
    private String nodeName;
    
    private int port;
    
    private boolean status = false;
    
    private boolean isJoined = false;
    
    private int retries = 0;
    
    private ArrayList<String> searchQueries = new ArrayList<>();
    
    public void addSearchQuery(String searchQuery) {
        this.searchQueries.add(searchQuery);
    }
    
    public ArrayList<String> getSearchQueries() {
        return searchQueries;
        
    }
    
    public Node(byte[] ip, String nodeName, int port) {
        this.ip = ip;
        this.nodeName = nodeName;
        this.port = port;
        
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
    
    public void setRetries(int retries) {
        this.retries += 1;
    }
    
    public byte[] getIp() {
        return ip;
    }
    
    public void setIp(byte[] ip) {
        this.ip = ip;
    }
    
    public String getNodeName() {
        return nodeName;
    }
    
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }
    
    public int getPort() {
        return port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    @Override
    public String toString() {
        
        return "ip " + new String(ip) + " port:" + this.getPort() + " username: " + this.getNodeName();
    }
    
    public boolean isJoined() {
        return isJoined;
    }
    
    public void setJoined(boolean joined) {
        isJoined = joined;
    }
    
    public void setIpString(String ipString) {
        this.ipString = ipString;
    }
    
    public String getIpString() {
        return ipString;
    }
    
    public void setSearchQueries(ArrayList<String> searchQueries) {
        this.searchQueries = searchQueries;
    }
}
