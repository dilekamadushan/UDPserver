package com.grydtech.peershare.ui.models;

import java.util.List;

public class SearchResponse {

    private List<FileWrapper> results;

    public SearchResponse(List<FileWrapper> results) {
        this.results = results;
    }

    public List<FileWrapper> getResults() {
        return results;
    }
}
