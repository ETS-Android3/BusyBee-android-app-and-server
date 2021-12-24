package com.zuas.bzb.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MarkerDescriptionDTO {

    private String text;

    public MarkerDescriptionDTO() {
    }

    public MarkerDescriptionDTO(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
