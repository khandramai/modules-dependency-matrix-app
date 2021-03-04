package org.folio.modulesdependencymatrixapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;


@Builder
@Setter
@Getter
@ToString
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Module {

    private String id;
    private List<Dependency> requires;
    private List<Dependency> provides;
    private String name;
    private String rmb;
    private String artifactId;
    private String previousReleaseVersion;
    private String previousReleaseData;
    public Module() {
    }

}
