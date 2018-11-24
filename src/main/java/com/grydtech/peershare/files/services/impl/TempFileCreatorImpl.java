package com.grydtech.peershare.files.services.impl;

import com.grydtech.peershare.files.services.TempFileCreator;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

@Service
public class TempFileCreatorImpl implements TempFileCreator {

    private static final Logger LOGGER = LoggerFactory.getLogger(TempFileCreatorImpl.class);
    private static final Random random = new Random();
    private static final String tmpdir = System.getProperty("java.io.tmpdir");

    @Override
    public File createTempFile(String fileName) throws IOException {
      
        File file = File.createTempFile(fileName,".data");

        int length = (random.nextInt(10) + 1) * 1024*1024;

        byte[] array = new byte[length];
        random.nextBytes(array);
        String s = new String(array, StandardCharsets.UTF_8);

        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        bw.write(s);
        bw.close();

        LOGGER.info("temporary file generated with random content");
    
        String md5Hash = DigestUtils.md5Hex(new FileInputStream(file)).toUpperCase();
        String sha1Hash = DigestUtils.sha1Hex(new FileInputStream(file)).toUpperCase();
    
        LOGGER.info("checksum value (md5): \"{}\"", md5Hash);
        LOGGER.info("checksum value (sha1): \"{}\"", sha1Hash);
    
        return file;
        
        
    }
}
