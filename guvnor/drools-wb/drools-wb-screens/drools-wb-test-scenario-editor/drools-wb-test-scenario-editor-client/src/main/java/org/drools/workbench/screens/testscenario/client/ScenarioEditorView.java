package org.drools.workbench.screens.testscenario.client;

import com.google.gwt.user.client.ui.IsWidget;
import org.drools.workbench.models.commons.shared.imports.Imports;
import org.drools.workbench.models.testscenarios.shared.Scenario;
import org.drools.workbench.screens.testscenario.service.ScenarioTestEditorService;
import org.jboss.errai.ioc.client.api.Caller;
import org.kie.workbench.widgets.common.client.widget.HasBusyIndicator;
import org.kie.guvnor.datamodel.oracle.PackageDataModelOracle;
import org.kie.guvnor.services.metadata.model.Metadata;
import org.uberfire.backend.vfs.Path;

public interface ScenarioEditorView
        extends IsWidget,
                HasBusyIndicator {

    void showCanNotSaveReadOnly();

    void renderEditor();

    void addTestRunnerWidget( final Scenario scenario,
                              final Caller<ScenarioTestEditorService> testScenarioEditorService,
                              final Path path );

    void addMetaDataPage( final Path path,
                          final boolean isReadOnly );

    void setScenario( final String packageName,
                      final Scenario scenario,
                      final PackageDataModelOracle dmo );

    void showSaveSuccessful();

    String getTitle();

    void initImportsTab( final PackageDataModelOracle dmo,
                         final Imports imports,
                         final boolean readOnly );

    Metadata getMetadata();

    void resetMetadataDirty();
    
    void addBulkRunTestScenarioPanel( final Path path, final boolean isReadOnly );
}
