package org.folio.modulesdependencymatrixapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Release {
    @JsonProperty("name")
    private String name;
    @JsonProperty("tag_name")
    private String tagName;
    @JsonProperty("published_at")
    private Date publishedAt;

    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    public String getTagName() {
        return tagName;
    }

    @JsonProperty("tag_name")
    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public Date getPublishedAt() {
        return publishedAt;
    }

    @JsonProperty("published_at")
    public void setPublishedAt(Date publishedAt) {
        this.publishedAt = publishedAt;
    }
}
