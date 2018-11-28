package com.grydtech.peershare.ui.services;

import com.grydtech.peershare.ui.models.DownloadRequest;
import com.grydtech.peershare.ui.models.DownloadResponse;

import java.io.IOException;

public interface Downloader {
    DownloadResponse download(DownloadRequest downloadRequest) throws IOException;
}
