package datagram.threadPooled.workerThread;

import datagram.threadPooled.domain.Node;
import datagram.threadPooled.domain.SearchResult;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by dileka on 9/27/18.
 */
public class SearcheResponseAcceptor implements Runnable {
    
    private CopyOnWriteArrayList<Node> routingTable;
    
    private SearchResult searchResult;
    
    private String searchResponse;
    
    public SearcheResponseAcceptor(CopyOnWriteArrayList<Node> routingTable, SearchResult searchResult, String searchResponse) {
        
        this.routingTable = routingTable;
        this.searchResult = searchResult;
        this.searchResponse = searchResponse;
    }
    
    public void run() {
        String[] params = searchResponse.split(" ");
        String[] ips = params[3].replace(".", " ").split(" ");
        Node node = new Node(new byte[] { (byte) Integer.parseInt(ips[0]), (byte) Integer.parseInt(ips[1]),
                (byte) Integer.parseInt(ips[2]), (byte) Integer.parseInt(ips[3]) }, UUID.randomUUID(), Integer.parseInt(params[4]));
        for (int i = 5; i < params.length; i++) {
            if (params[i].contains(searchResult.getQuery())) {
                searchResult.addFileName(params[i]);
                searchResult.addNode(node);
            }
        }
        synchronized (this){
            
        Node newNode = routingTable.stream().filter(s -> s.getIp() == node.getIp()).findFirst().orElse(null);
        if (newNode == null) {
            routingTable.add(node);
        }
        } 
    }
}
