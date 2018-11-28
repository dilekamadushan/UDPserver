package com.grydtech.peershare.ui.controllers;

import com.grydtech.peershare.datagram.Server;
import com.grydtech.peershare.ui.models.DownloadRequest;
import com.grydtech.peershare.ui.models.DownloadResponse;
import com.grydtech.peershare.ui.models.SearchRequest;
import com.grydtech.peershare.ui.services.Downloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.io.IOException;

@Controller
public class WebSocketController {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketController.class);

    private final Server server;
    private final Downloader downloader;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public WebSocketController(Server server, Downloader downloader, SimpMessagingTemplate simpMessagingTemplate) {
        this.server = server;
        this.downloader = downloader;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @MessageMapping("/search")
    public void search(SearchRequest searchRequest) {
        LOGGER.info("file search request received searchText: \"{}\"", searchRequest.getSearchText());

        server.submitSearchRequest(searchRequest.getSearchText());
    }

    @MessageMapping("/download")
    public void download(DownloadRequest downloadRequest) throws IOException {
        LOGGER.info("download request received");

        DownloadResponse downloadResponse = downloader.download(downloadRequest);
        simpMessagingTemplate.convertAndSend("/topic/download", downloadResponse);
    }
}
