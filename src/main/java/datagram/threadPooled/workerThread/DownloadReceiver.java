package datagram.threadPooled.workerThread;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * Created by dileka on 9/27/18.
 */
public class DownloadReceiver extends Thread {
    
    private Socket clientSocket;
    
    private PrintWriter out;
    
    private BufferedReader in;
    
    private String downloadIP;
    
    private String fileName;
    
    private int downloadPort;
    
    private boolean running;
    
    public DownloadReceiver(boolean running, String downloadIP, int downloadPort, String fileName) {
        
        this.running = running;
        this.downloadIP = downloadIP;
        this.downloadPort = downloadPort;
        this.fileName = fileName;
        System.out.println("DownloadReceiver: Thread started");
    }
    
    public void run() {
        System.out.println("DownloadReceive:Entering the file receiving loop"+fileName);
        while (running) {
            
            try {
                //Initialize socket
                clientSocket = new Socket(InetAddress.getByName(downloadIP), downloadPort);
    
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    
                out.println("DOWNLOAD "+fileName);
                System.out.println("DownloadReceive:Download request sent");
                
                byte[] contents = new byte[1000];
                
                //Initialize the FileOutputStream to the output file's full path.
                FileOutputStream fileOutputStream = new FileOutputStream("./" + fileName+".txt");
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
                InputStream inputStream = clientSocket.getInputStream();
                
                //No of bytes read in one read() call
                int bytesRead = 0;
                System.out.println("DownloadReceiver: started Downloading file");
                while ((bytesRead = inputStream.read(contents)) != -1)
                    bufferedOutputStream.write(contents, 0, bytesRead);
                
                bufferedOutputStream.flush();
                clientSocket.close();
                
                System.out.println("DownloadReceiver:File saved successfully!");
    
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
    
                byte[] encodedhash = digest.digest(s.getBytes(StandardCharsets.UTF_8));
    
                System.out.println("Download Server:SHA hash of file" + bytesToHex(encodedhash));
                
            }
            catch (IOException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        
    }
    
    private String getFullMessage(String message) {
        int size = message.length() + 5;
        if (size < 100) {
            return "00" + size + " " + message;
        } else {
            return "0" + size + " " + message;
        }
    }
    
    private File generateFile(String fileName) {
        
        int length = (new Random().nextInt(10) + 1) * 1000 * 1000;
        
        byte[] b = new byte[length];
        new Random().nextBytes(b);
        File file = null;
        try {
            String s = new String(b, StandardCharsets.UTF_8);
            file = new File(fileName);
            
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(s);
            fileWriter.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        
        System.out.println("temporary file generated with random content");
        return file;
        
    }
    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte aHash : hash) {
            String hex = Integer.toHexString(0xff & aHash);
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
    
}
