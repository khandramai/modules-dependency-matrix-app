package org.folio.modulesdependencymatrixapp.writer;

import org.apache.poi.ss.usermodel.Workbook;
import org.folio.modulesdependencymatrixapp.entity.Dependency;
import org.folio.modulesdependencymatrixapp.entity.Module;

import java.util.List;
import java.util.Map;


public interface Writer {

    Workbook exportToExcel(List<Module> moduleList, Map<String, Dependency> map);

}
