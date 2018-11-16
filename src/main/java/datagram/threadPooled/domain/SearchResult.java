package datagram.threadPooled.domain;

import java.util.ArrayList;
import java.util.Timer;

/**
 * Created by dileka on 10/2/18.
 */
public class SearchResult {
    
    private String query="";
    
    private ArrayList<Node> nodes;
    
    private ArrayList<String> fileNames;
    
    private long startTime;
    
    private boolean inUse = false;
    
    public SearchResult(){
        startTime = System.currentTimeMillis();
        fileNames = new ArrayList<>();
        nodes = new ArrayList<>();
    }
    
    public void addFileName(String fileName) {
        this.fileNames.add(fileName);
    }
    
    public ArrayList<String> getFileNames() {
    
        return fileNames;
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
    
    public void setQuery(String query) {
        this.query = query;
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
            fileInfo.append(fileNames.get(i)).append(" : ").append(nodes.get(i).toString()).append("\n");
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
}
