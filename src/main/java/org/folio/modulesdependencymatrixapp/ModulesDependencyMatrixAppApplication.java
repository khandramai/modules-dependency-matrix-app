package org.folio.modulesdependencymatrixapp;

import org.folio.modulesdependencymatrixapp.provider.DataProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class ModulesDependencyMatrixAppApplication implements CommandLineRunner {

	@Autowired
	private DataProvider dataProvider;

	public static void main(String[] args) {
		SpringApplication.run(ModulesDependencyMatrixAppApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

	}
}
