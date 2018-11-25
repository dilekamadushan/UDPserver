package datagram.threadPooled.workerThread;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.security.MessageDigest;

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
        System.out.println("DownloadReceive:Entering the file receiving loop" + fileName);
        
        try {
            //Initialize socket
            clientSocket = new Socket(InetAddress.getByName(downloadIP), downloadPort);
            
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            
            out.println("DOWNLOAD " + fileName);
            System.out.println("DownloadReceive:Download request sent");
            
            byte[] contents = new byte[1000];
    
            File file = new File(fileName+"_client.data");
            System.out.println("DownloadReceive:Size when creating file"+file.length());
            //Initialize the FileOutputStream to the output file's full path.
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            InputStream inputStream = clientSocket.getInputStream();
            
            //No of bytes read in one read() call
            int bytesRead = 0;
            System.out.println("DownloadReceiver: started Downloading file");
            while ((bytesRead = inputStream.read(contents)) != -1)
                bufferedOutputStream.write(contents, 0, bytesRead);
            
            bufferedOutputStream.flush();
            clientSocket.close();
            
            System.out.println("DownloadReceiver:File saved successfully!" + file.length());
            
            byte[] encodedhash = createSha1(file);
            
            System.out.println("Download Receiver:SHA hash of file" + bytesToHex(encodedhash));
            System.out.println("temporary file generated with size:" + file.length() / (1024 * 1024) + "MB");
            
            inputStream.close();
            fileOutputStream.close();
            bufferedOutputStream.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    public byte[] createSha1(File file) throws Exception {
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
