package datagram.threadPooled.domain;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by dileka on 10/2/18.
 */
public class Node {
    
    private byte[] ip;
    
    private String ipString;
    
    private UUID username;
    
    private int idForDisplay;
    
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
    
    public Node(byte[] ip, UUID username, int port) {
        this.ip = ip;
        this.username = username;
        this.port = port;
        
    }
    
    public void setUsername(UUID username) {
        this.username = username;
    }
    
    public void setIdForDisplay(int idForDisplay) {
        this.idForDisplay = idForDisplay;
    }
    
    public UUID getUsername() {
        return username;
    }
    
    public int getIdForDisplay() {
        return idForDisplay;
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
    
    
    public int getPort() {
        return port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    @Override
    public String toString() {
        
        return "ip " + new String(ip) + " port:" + this.getPort() + " username: " + this.getUsername().toString();
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
