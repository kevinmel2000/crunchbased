package com.github.zyro.crunchbase.entity;

public class ExternalLink {

    private String external_url;
    private String title;

    public String getExternal_url() {
        return external_url;
    }

    public void setExternal_url(String external_url) {
        this.external_url = external_url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "ExternalLink{" +
                "external_url='" + external_url + '\'' +
                ", title='" + title + '\'' +
                '}';
    }

}