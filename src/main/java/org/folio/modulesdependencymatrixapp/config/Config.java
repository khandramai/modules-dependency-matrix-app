package org.folio.modulesdependencymatrixapp.config;

import org.folio.modulesdependencymatrixapp.provider.impl.GitHubDataProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class Config {
    @Bean
    GitHubDataProvider gitHubDataProvider(@Value("${github.token}") String gitHubToken) throws IOException {
        return new GitHubDataProvider(gitHubToken);
    }
}
