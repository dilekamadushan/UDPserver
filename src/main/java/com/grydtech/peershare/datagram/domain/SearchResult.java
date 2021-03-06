package com.grydtech.peershare.datagram.domain;

import java.util.ArrayList;

/**
 * Created by dileka on 10/2/18.
 */
public class SearchResult {
    
    private String query;
    
    private ArrayList<Node> nodes;
    
    private ArrayList<String> fileNames;
    
    private ArrayList<String> searchResponses;
    
    private ArrayList<String> searchHops;
    
    private long startTime;
    
    private boolean inUse = false;
    
    public SearchResult(){
        startTime = System.currentTimeMillis();
        fileNames = new ArrayList<>();
        searchResponses = new  ArrayList<>();
        nodes = new ArrayList<>();
        searchHops = new ArrayList<>();
    }
    
    public void addFileName(String fileName) {
        this.fileNames.add(fileName);
    }
    
    public void addSearchResponse(String searchResponse) {
        this.searchResponses.add(searchResponse);
    }
    
    public void addSearchHops(String searchHop) {
        this.searchHops.add(searchHop);
    }
    
    public ArrayList<String> getFileNames() {
    
        return fileNames;
    }
    
    public ArrayList<String> getSearchResponses() {
        
        return searchResponses;
    }
    
    public void setInUse(boolean inUse) {
        this.inUse = inUse;
    }
    
    public boolean isInUse() {
    
        return inUse;
    }
    
    public String getQuery() {
        return query;
    }
    
    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public ArrayList<String> getSearchHops() {
        return searchHops;
    }

    public void setQuery(String query) {
        this.query =query;
    }
    
    public void addNode(Node node) {
        this.nodes.add(node);
    }
    
    @Override
    public String toString(){
        StringBuilder fileInfo =new StringBuilder();
        fileInfo.append("The Search Query is ").append(query).append("\n");
        fileInfo.append("The file names found are: \n");
        for(int i =0;i<fileNames.size();i++){
            fileInfo.append(i+1).append(":").append(fileNames.get(i)).append(" : ").append(nodes.get(i).toString()).append("search Hop:").append(searchHops.get(i)).append(":searchRespose").append(searchResponses.get(i)).append("\n");
        }
        //print all the info in this method
       return fileInfo.toString(); 
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    
    public void reset(){
        this.inUse=false;
        this.fileNames.removeAll(this.fileNames);
        nodes.removeAll(this.nodes);
        this.searchResponses.removeAll(this.searchResponses);
        this.query="";
    }
}
