package org.folio.modulesdependencymatrixapp.provider;

import org.folio.modulesdependencymatrixapp.Module;
import java.util.List;

public interface DataProvider {

    public List<Module> getDataFromMaster();

    public List<Module> getDataFromTag(int number);
}
