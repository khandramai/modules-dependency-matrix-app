package org.folio.modulesdependencymatrixapp.provider;

import org.folio.modulesdependencymatrixapp.entity.Module;

import java.util.List;

public interface DataProvider {

    List<Module> getDataFromMaster();

    List<Module> getDataFromTag(int number);
}
