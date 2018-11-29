package com.grydtech.peershare;

import com.grydtech.peershare.datagram.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import sun.misc.Regexp;

import java.net.BindException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

@SpringBootApplication
public class PeerShareApplication {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final Server server;

    // Bootstrap
    @Value("${bootstrap.host}")
    private String bootstrapHost;
    @Value("${bootstrap.port}")
    private int bootstrapPort;

    // Server
    @Value("${server.host}")
    private String serverHost;
    @Value("${server.port}")
    private int serverPort;
    @Value("${server.name}")
    private String serverName;

    // Kafka
    @Value("${kafka.host}")
    private String kafkaHost;

    @Autowired
    public PeerShareApplication(SimpMessagingTemplate simpMessagingTemplate, Server server, Environment environment) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.server = server;
    }

    public static void main(String[] args) {
        SpringApplication.run(PeerShareApplication.class);
    }

    public String getServerHost() throws UnknownHostException {
        if (serverHost == null || serverHost.equals("")) {
            try (final DatagramSocket socket = new DatagramSocket()) {
                socket.connect(new InetSocketAddress("8.8.8.8", 10002));
                serverHost = socket.getLocalAddress().getHostAddress().trim();
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }

        if ("0.0.0.0".equals(serverHost) || "127.0.0.1".equals(serverHost)) {
            InetAddress localhost = InetAddress.getLocalHost();
            serverHost = localhost.getHostAddress().trim();
        }
        return serverHost;
    }

    public String getServerName(String serverHost, int serverPort) {
        if (serverName != null && !serverName.equals("")) return serverName;
        String ip = serverHost.split("\\.")[3];
        return String.valueOf(serverPort) + ip;
    }

    @EventListener
    public void afterApplicationReady(ApplicationReadyEvent event) throws UnknownHostException {
        String serverHost = getServerHost();
        System.out.println(serverHost);
        String serverName = getServerName(serverHost, serverPort);

        try {
            server.initServer(bootstrapHost, bootstrapPort, serverHost, serverPort + 10000, serverName, kafkaHost, simpMessagingTemplate);
            System.out.println("Starter: started successfully");
            server.start();
            Thread.sleep(20 * 100000);
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            System.out.println("Starter: Enter the runtime params BS IP,BS port, Server IP, Server Port, username");
        } catch (BindException be) {
            be.printStackTrace();
            System.out.println("Starter: Try a different port");
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("Starter: Stopping Server");
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
