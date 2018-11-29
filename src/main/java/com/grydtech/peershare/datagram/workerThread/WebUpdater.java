package com.grydtech.peershare.datagram.workerThread;

import com.grydtech.peershare.datagram.domain.Node;
import com.grydtech.peershare.datagram.domain.SearchResult;
import com.grydtech.peershare.ui.models.FileWrapper;
import com.grydtech.peershare.ui.models.SearchResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Created by dileka on 9/27/18.
 */
public class WebUpdater extends Thread {
    
    private final SimpMessagingTemplate simpMessagingTemplate;
    
    private boolean running;
    
    private SearchResult searchResult;
    
    public WebUpdater(boolean running, SearchResult searchResult, SimpMessagingTemplate simpMessagingTemplate) {
        this.running = running;
        this.searchResult = searchResult;
        this.simpMessagingTemplate = simpMessagingTemplate;
        System.out.println("Web Updater: Thread started");
    }
    
    public void run() {
        System.out.println("Web Updater: Entering data sending loop");
        
        while (running) {
            sendDataToWeb();
            try {
                Thread.sleep(500);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    //To implement
    private void sendDataToWeb() {
        List<FileWrapper> results = new ArrayList<>();
        
        for (int i = 0; i < searchResult.getFileNames().size(); i++) {
            Node node = searchResult.getNodes().get(i);
            String fileName = searchResult.getFileNames().get(i);
            String fileId = generateBase64(fileName);
            String fileUrl = String.format("http://%s:%d/download/%s", node.getIpString(), node.getPort() - 10000, fileId);
            int hops = Integer.parseInt(searchResult.getSearchHops().get(i));
            
            results.add(new FileWrapper(fileId, fileName, fileUrl, hops));
        }
        
        simpMessagingTemplate.convertAndSend("/topic/results", new SearchResponse(results));
    }
    
    private String generateBase64(String s) {
        return new String(Base64.getEncoder().encode(s.getBytes()), StandardCharsets.UTF_8);
    }
}
