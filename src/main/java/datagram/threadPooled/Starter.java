package datagram.threadPooled;

import java.net.BindException;
import java.net.SocketException;

/**
 * Created by dileka on 9/29/18.
 */
public class Starter {
    
    public static void main(String[] args) throws SocketException {
        Server server;
        
        try {
            server = new Server(args[0],Integer.parseInt(args[1]), args[2], Integer.parseInt(args[3]),args[4]);
            System.out.println("Starter: started successfully");
            new Thread(server).start();
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
        }
        
    }
}
