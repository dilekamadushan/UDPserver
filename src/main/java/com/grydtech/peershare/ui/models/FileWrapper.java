package com.grydtech.peershare.ui.models;

public class FileWrapper {

    private String fileId;
    private String fileName;
    private String fileUrl;
    private int hops;

    public FileWrapper(String fileId, String fileName, String fileUrl, int hops) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.hops = hops;
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

    public int getHops() {
        return hops;
    }
}
