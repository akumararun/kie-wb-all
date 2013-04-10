package org.kie.guvnor.testscenario.client;

import com.google.gwt.user.client.ui.IsWidget;
import org.drools.guvnor.models.commons.shared.imports.Imports;
import org.drools.guvnor.models.testscenarios.shared.Scenario;
import org.jboss.errai.ioc.client.api.Caller;
import org.kie.guvnor.commons.ui.client.widget.HasBusyIndicator;
import org.kie.guvnor.datamodel.oracle.DataModelOracle;
import org.kie.guvnor.services.metadata.model.Metadata;
import org.kie.guvnor.testscenario.service.ScenarioTestEditorService;
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
                      final DataModelOracle dmo );

    void showSaveSuccessful();

    String getTitle();

    void initImportsTab( final DataModelOracle dmo,
                         final Imports imports,
                         final boolean readOnly );

    Metadata getMetadata();

    void resetMetadataDirty();
}
