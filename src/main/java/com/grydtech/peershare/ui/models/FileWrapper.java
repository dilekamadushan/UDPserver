package com.grydtech.peershare.ui.models;

public class FileWrapper {

    private String fileId;
    private String fileName;
    private String fileUrl;

    public FileWrapper(String fileId, String fileName, String fileUrl) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
    }

    public String getFileId() {
        return fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }
}
