package datagram.threadPooled.workerThread;

import datagram.threadPooled.domain.Node;
import datagram.threadPooled.domain.SearchResult;

import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

/**
 * Created by dileka on 9/27/18.
 */
public class SearchQueryAcceptor extends Thread {
    
    private ExecutorService executorService;
    
    private DatagramSocket datagramSocket;
    
    private CopyOnWriteArrayList<Node> routingTable;
    
    private ArrayList<String> fileNames;
    
    private SearchResult searchResult;
    
    private Node myNode;
    
    private  ArrayList<String> previousSearchRequests;
    
    private int packetCount;
    
    public SearchQueryAcceptor(DatagramSocket socket, CopyOnWriteArrayList<Node> routingTable, SearchResult searchResult,
            ArrayList<String> fileNames, Node myNode, ArrayList<String> previousSearchRequests, int packetCount, ExecutorService executorService) {
        this.executorService = executorService;
        this.routingTable = routingTable;
        this.datagramSocket = socket;
        this.searchResult = searchResult;
        this.myNode = myNode;
        this.fileNames = fileNames;
        this.previousSearchRequests = previousSearchRequests;
        this.packetCount = packetCount;
        System.out.println("Search Query Acceptor : SearchQueryAcceptor started");
    }
    
    public void run() {
        
        System.out.println("Search Query Acceptor : Here are the names of files I have");
        fileNames.forEach(System.out::println);
        System.out.println("Search Query Acceptor : inside startWork Method");
        Scanner reader = new Scanner(System.in);  // Reading from System.in
        while (true) {
            System.out.println("Search Query Acceptor : inside event loop");
            if (searchResult.isInUse()){
                long currentTime = System.currentTimeMillis();
                System.out.println("Search Query Acceptor :  currentTime:"+currentTime);
                long difference = currentTime-searchResult.getStartTime();
                System.out.println("Search Query Acceptor :  difference:"+difference);
                if(1000*60*3<difference){
                    System.out.println("Search Query Acceptor : searchResult has timed out "+difference); 
                }
            }
            
            System.out.println("Search Query Acceptor :Enter a search query: ");
            String query = reader.nextLine(); // Scans the next token of the input as a string.
            switch (query) {
                case "lsNodes":
                    System.out.println("Search Query Acceptor :The nodes in the routing table are:");
                    routingTable.forEach(n -> System.out.println(n.toString()));
                    break;
                case "lsFiles":
                    System.out.println("Search Query Acceptor :The files in this node are:");
                    fileNames.forEach(System.out::println);
                    break;
                case "lsSearchResult":
                    System.out.println("Search Query Acceptor :The searchResult status now: " + searchResult.toString());
                    break;
                case "lsPacketCount":
                    System.out.println("Search Query Acceptor :The number of data packets received: " + packetCount);
                    break;
                case "check cake":
                    System.out.println("Search Query Acceptor :The number of data packets received: " + packetCount);
                    break;
                default:
                    System.out.println("Search Query Acceptor : in default block:"+query.substring(0, 6)+"ppp");
                    if (query.length() > 7 && "search".equals(query.substring(0, 6))) {
                        System.out.println(
                                "Search Query Acceptor :The user has requested to search for files by name: " + query
                                        .substring(7));
                        if (!searchResult.isInUse()) {
                            System.out.println("Search Query Acceptor :finished reading line " + query);
                            searchResult.setQuery(query.substring(7));
                            executorService.execute(
                                    new SearchRequestAcceptor(this.datagramSocket, routingTable, fileNames, myNode,
                                            getFullMessage(
                                                    "SER " + myNode.getIpString() + " " + myNode.getPort() + " " + query
                                                            .substring(7) + " 0"), true, previousSearchRequests,searchResult));
                            System.out.println("Search Query Acceptor : created a SearchRequestAcceptor thread");
                            
                        } else {
                            System.out.println("Please wait until previous search ends");
                        }
                    }
                    else {
                        System.out.println("Search Query Acceptor : unidentified query");
                    }
                
            }
            
        }
        //once finished
        
    }
    
    public String getFullMessage(String message) {
        int size = message.length() + 5;
        if (size < 100) {
            return "00" + size + " " + message;
        } else {
            return "0" + size + " " + message;
        }
    }
    
}
