package com.grydtech.peershare.ui.controllers;

import com.grydtech.peershare.files.models.FileInfo;
import com.grydtech.peershare.files.services.TempFileCreator;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Controller
@CrossOrigin
public class HttpController {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpController.class);

    private final TempFileCreator tempFileCreator;

    @Autowired
    public HttpController(TempFileCreator tempFileCreator) {
        this.tempFileCreator = tempFileCreator;
    }

    @GetMapping("download/{id}")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable String id) throws IOException {
        LOGGER.info("file download request received for fileId: \"{}\"", id);

        String fileName = new String(Base64.getDecoder().decode(id.getBytes()), StandardCharsets.UTF_8);

        File file = tempFileCreator.createTempFile(fileName);

        InputStreamResource inputStreamResource = new InputStreamResource(new FileInputStream(file));

        String hash = DigestUtils.sha1Hex(new FileInputStream(file)).toUpperCase();
    
        System.out.println("Generated file " + fileName + " checksum value (sha hash): "+ hash);

        LOGGER.info("generated file checksum value (hash): \"{}\"", hash);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file.getName())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .header("Checksum-SHA1", hash)
                .contentLength(file.length())
                .body(inputStreamResource);
    }
}
