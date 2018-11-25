package datagram.threadPooled.workerThread;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * Created by dileka on 9/27/18.
 */
public class DownloadServer extends Thread {
    
    private ServerSocket serverSocket;
    
    private Socket clientSocket;
    
    private PrintWriter out;
    
    private BufferedReader in;
    
    private int downloadPort;
    
    private boolean running;
    
    public DownloadServer(boolean running, int downloadPort) {
        this.running = running;
        this.downloadPort = downloadPort;
        System.out.println("DownloadServer: Thread started");
    }
    
    public void run() {
        System.out.println("DownloadServer:Entering the file sending loop" + downloadPort);
        while (running) {
            
            try {
                serverSocket = new ServerSocket(downloadPort);
                clientSocket = serverSocket.accept();
                System.out.println("DownloadServer:received a packet");
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String request = null;
                
                request = in.readLine();
                
                if (request.contains("DOWNLOAD")) {
                    System.out.println("DownloadServer:DOWNLOAD request received");
                    out.println(request);
                    System.out.println("DownloadServer:Trying to generate a file" + request.substring(9));
                    File file = generateFile(request.substring(9)+"_server.data");
                    
                    //Specify the file
                    FileInputStream fileInputStream = new FileInputStream(file);
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                    
                    //Get socket's output stream
                    OutputStream os = clientSocket.getOutputStream();
                    
                    //Read File Contents into contents array 
                    byte[] contents;
                    long fileLength = file.length();
                    long current = 0;
                    
                    long start = System.nanoTime();
                    System.out.println("DownloadServer:Trying to send file" + request.substring(9) + start);
                    while (current != fileLength) {
                        int size = 10000;
                        if (fileLength - current >= size)
                            current += size;
                        else {
                            size = (int) (fileLength - current);
                            current = fileLength;
                        }
                        contents = new byte[size];
                        bufferedInputStream.read(contents, 0, size);
                        os.write(contents);
                        System.out.print("DownloadServer:Sending file ... " + (current * 100) / fileLength + "% complete!");
                    }
                    
                    os.flush();
                    //File transfer done. Close the socket connection!
                    clientSocket.close();
                    System.out.println("DownloadServer:File sent succesfully!");
                    
                } else {
                    out.println("DownloadServer:unrecognised request");
                }
                
                serverSocket.close();
                
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            
        }
        
    }
    
    private File generateFile(String fileName) {
        
        int length = (new Random().nextInt(5) + 1) * 1000 * 1000;
        
        byte[] b = new byte[length];
        new Random().nextBytes(b);
        File file = null;
        try {
            String s = new String(b, StandardCharsets.UTF_8);
            file = new File(fileName);
            
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(s);
            fileWriter.close();
            
            byte[] encodedhash = createSha1(file);
            
            System.out.println("Download Server:SHA hash of file in Hexa Decimal" + bytesToHex(encodedhash));
            System.out.println("temporary file generated with size:"+file.length()/(1024*1024)+"MB");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    
       
        
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
    
    public byte[] createSha1(File file) throws Exception  {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        InputStream fis = new FileInputStream(file);
        int n = 0;
        byte[] buffer = new byte[8192];
        while (n != -1) {
            n = fis.read(buffer);
            if (n > 0) {
                digest.update(buffer, 0, n);
            }
        }
        return digest.digest();
    }
    
}
