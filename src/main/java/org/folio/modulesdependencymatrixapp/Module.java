package org.folio.modulesdependencymatrixapp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Module {
    private String id;
    private String name;
    private List<Dependency> requires;
    private List<Dependency> provides;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Dependency> getRequires() {
        return requires;
    }

    public void setRequires(List<Dependency> requires) {
        this.requires = requires;
    }

    public List<Dependency> getProvides() {
        return provides;
    }

    public void setProvides(List<Dependency> provides) {
        this.provides = provides;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Module{" +
                "name='" + name + '\'' +
                ", requires=" + requires +
                ", provides=" + provides +
                '}';
    }
}