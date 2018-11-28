package com.grydtech.peershare;

import com.grydtech.peershare.datagram.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.net.BindException;
import java.net.SocketException;

@SpringBootApplication
public class PeerShareApplication {

    @Value("${bootstrap.host}")
    private String bootstrapHost;

    @Value("${bootstrap.port}")
    private int bootstrapPort;

    @Value("${server.host}")
    private String serverHost;

    @Value("${server.port}")
    private int httpPort;

    @Value("${server.name}")
    private String serverName;
    
    @Value("${kafka.host}")
    private String kafkaHost;

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final Server server;

    @Autowired
    public PeerShareApplication(SimpMessagingTemplate simpMessagingTemplate, Server server) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.server = server;
    }

    @EventListener
    public void afterApplicationReady(ApplicationReadyEvent event) {
        try {
            server.initServer(bootstrapHost, bootstrapPort, serverHost, httpPort + 10000, serverName, kafkaHost,simpMessagingTemplate);
            System.out.println("Starter: started successfully");
            server.start();
            Thread.sleep(20 * 100000);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            System.out.println("Starter: Enter the runtime params BS IP,BS port, Server IP, Server Port, username");
        }
        catch (BindException be) {
            be.printStackTrace();
            System.out.println("Starter: Try a different port");
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("Starter: Stopping Server");
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws SocketException {
        SpringApplication.run(PeerShareApplication.class);
    }
}
