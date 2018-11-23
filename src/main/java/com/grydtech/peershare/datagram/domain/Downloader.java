package com.grydtech.peershare.datagram.domain;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by dileka on 11/8/18.
 */
public class Downloader {
    
    public Downloader()
    {
        System.out.println("Downloader: Downloader object created");   
    }
    
    public void sendHTTPRequest(String URL) throws IOException {
        System.out.println("Downloader: Inside send HTTP method");
        CloseableHttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(URL);
        System.out.println("Downloader: Before sending HTTP request");
        HttpResponse response = client.execute(request);
        System.out.println("Downloader: sent HTTP request");
        System.out.println("Downloader: Response Code : "
                + response.getStatusLine().getStatusCode());
    
        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));
    
        StringBuilder result = new StringBuilder();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        System.out.println("Downloader: response: "+result.toString());
    }
    
}
