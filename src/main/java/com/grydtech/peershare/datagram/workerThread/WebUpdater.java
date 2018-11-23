package com.grydtech.peershare.datagram.workerThread;

import com.grydtech.peershare.datagram.domain.Node;
import com.grydtech.peershare.datagram.domain.SearchResult;

import java.net.DatagramSocket;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by dileka on 9/27/18.
 */
public class WebUpdater extends Thread {
    
    private DatagramSocket threadDatagramSocket = null;
    
    private CopyOnWriteArrayList<Node> routingTable;

    private boolean running;
    
    private SearchResult searchResult;
    
    public WebUpdater(boolean running, SearchResult searchResult) {
        this.running = running;
        this.searchResult = searchResult;
        System.out.println("Web Updater: Thread started");
    }
    
    public void run() {
        System.out.println("Web Updater:Entering the Heart-Beat sending loop");
        
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
    void sendDataToWeb() {

    }
}
