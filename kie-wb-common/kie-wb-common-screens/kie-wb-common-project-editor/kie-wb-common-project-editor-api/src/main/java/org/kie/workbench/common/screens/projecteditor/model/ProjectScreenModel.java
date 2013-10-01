package org.kie.workbench.common.screens.projecteditor.model;

import org.drools.workbench.models.datamodel.oracle.ProjectDataModelOracle;
import org.guvnor.common.services.project.model.KModuleModel;
import org.guvnor.common.services.project.model.POM;
import org.guvnor.common.services.project.model.ProjectImports;
import org.guvnor.common.services.shared.metadata.model.Metadata;
import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class ProjectScreenModel {

    private POM pom;
    private KModuleModel KModule;
    private Metadata POMMetaData;
    private Metadata KModuleMetaData;
    private ProjectImports projectImports;
    private Metadata projectImportsMetaData;
    private ProjectDataModelOracle projectDataModelOracle;

    public POM getPOM() {
        return pom;
    }

    public void setPOM(POM pom) {
        this.pom = pom;
    }

    public void setKModule(KModuleModel KModule) {
        this.KModule = KModule;
    }

    public KModuleModel getKModule() {
        return KModule;
    }

    public void setPOMMetaData(Metadata POMMetaData) {
        this.POMMetaData = POMMetaData;
    }

    public Metadata getPOMMetaData() {
        return POMMetaData;
    }

    public void setKModuleMetaData(Metadata KModuleMetaData) {
        this.KModuleMetaData = KModuleMetaData;
    }

    public Metadata getKModuleMetaData() {
        return KModuleMetaData;
    }

    public void setProjectImports(ProjectImports projectImports) {
        this.projectImports = projectImports;
    }

    public ProjectImports getProjectImports() {
        return projectImports;
    }

    public void setProjectImportsMetaData(Metadata projectImportsMetaData) {
        this.projectImportsMetaData = projectImportsMetaData;
    }

    public Metadata getProjectImportsMetaData() {
        return projectImportsMetaData;
    }

    public ProjectDataModelOracle getProjectDataModelOracle() {
        return projectDataModelOracle;
    }

    public void setProjectDataModelOracle(ProjectDataModelOracle projectDataModelOracle) {
        this.projectDataModelOracle = projectDataModelOracle;
    }
}
