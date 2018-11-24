package com.grydtech.peershare.files.services.impl;

import com.grydtech.peershare.files.services.TempFileCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Random;

@Service
public class TempFileCreatorImpl implements TempFileCreator {

    private static final Logger LOGGER = LoggerFactory.getLogger(TempFileCreatorImpl.class);
    private static final Random random = new Random();

    @Override
    public File createTempFile(String fileName) throws IOException {
      
        File file = File.createTempFile(fileName,"data");

        int length = (random.nextInt(10) + 1) * 1000;

        byte[] array = new byte[length];
        random.nextBytes(array);
        String s = new String(array, StandardCharsets.UTF_8);

        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        bw.write(s);
        bw.close();

        LOGGER.info("temporary file generated with random content");
        return file;
    }
}
