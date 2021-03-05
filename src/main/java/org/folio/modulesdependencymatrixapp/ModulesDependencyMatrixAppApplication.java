package org.folio.modulesdependencymatrixapp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Workbook;
import org.folio.modulesdependencymatrixapp.entity.Dependency;
import org.folio.modulesdependencymatrixapp.entity.Module;
import org.folio.modulesdependencymatrixapp.provider.DataProvider;
import org.folio.modulesdependencymatrixapp.writer.Writer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@SpringBootApplication
public class ModulesDependencyMatrixAppApplication implements CommandLineRunner {

    private static final Logger log = LogManager.getLogger(ModulesDependencyMatrixAppApplication.class);
    @Autowired
    private DataProvider dataProvider;
    @Autowired
    private Writer writer;

    public static void main(String[] args) {
        SpringApplication.run(ModulesDependencyMatrixAppApplication.class, args);

    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Start DataProvider");

        List<Module> dataFromMaster = dataProvider.getDataFromMaster();
        List<Module> dataByTags = dataProvider.getDataFromTag(0);

        log.info("Start Exporting to excel");

        Map<String, Dependency> map = getDependencyMap(dataFromMaster);
        Workbook wb = writer.exportToExcel(dataByTags, map);

        log.info("Start to save file");
        File currDir = new File(".");
        String path = currDir.getAbsolutePath();
        String fileLocation = path.substring(0, path.length() - 1) + "final.xlsx";

        FileOutputStream outputStream = new FileOutputStream(fileLocation);
        wb.write(outputStream);
        wb.close();
    }

    private Map<String, Dependency> getDependencyMap(List<Module> dataFromMaster) {
        Map<String, Dependency> map = new HashMap<>();
        dataFromMaster.stream().distinct().forEach(el -> {
            List<Dependency> provides = el.getProvides();
            if (Objects.nonNull(provides)) {
                provides.forEach(i -> {
                    i.setOwnerName(el.getArtifactId());
                    map.put(i.getId(), i);
                });
            }
        });
        return map;
    }

}
