package com.grydtech.peershare.ui.controllers;

import com.grydtech.peershare.datagram.Server;
import com.grydtech.peershare.ui.models.SearchRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketController.class);

    private final Server server;

    @Autowired
    public WebSocketController(Server server) {
        this.server = server;
    }

    @MessageMapping("/search")
    public void search(SearchRequest searchRequest) {
        LOGGER.info("file search request received searchText: \"{}\"", searchRequest.getSearchText());

        server.submitSearchRequest(searchRequest.getSearchText());
    }
}
