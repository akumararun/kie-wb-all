/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.guvnor.testscenario.client;

import java.util.List;
import javax.enterprise.event.Event;
import javax.enterprise.inject.New;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.drools.guvnor.models.commons.shared.imports.Imports;
import org.drools.guvnor.models.testscenarios.shared.CallFixtureMap;
import org.drools.guvnor.models.testscenarios.shared.ExecutionTrace;
import org.drools.guvnor.models.testscenarios.shared.Fixture;
import org.drools.guvnor.models.testscenarios.shared.FixtureList;
import org.drools.guvnor.models.testscenarios.shared.FixturesMap;
import org.drools.guvnor.models.testscenarios.shared.Scenario;
import org.drools.guvnor.models.testscenarios.shared.VerifyFact;
import org.drools.guvnor.models.testscenarios.shared.VerifyRuleFired;
import org.jboss.errai.ioc.client.api.Caller;
import org.kie.guvnor.commons.ui.client.callbacks.HasBusyIndicatorDefaultErrorCallback;
import org.kie.guvnor.commons.ui.client.resources.i18n.CommonConstants;
import org.kie.guvnor.commons.ui.client.widget.BusyIndicatorView;
import org.kie.guvnor.configresource.client.widget.bound.ImportsWidgetPresenter;
import org.kie.guvnor.datamodel.oracle.DataModelOracle;
import org.kie.guvnor.metadata.client.callbacks.MetadataSuccessCallback;
import org.kie.guvnor.metadata.client.resources.i18n.MetadataConstants;
import org.kie.guvnor.metadata.client.widget.MetadataWidget;
import org.kie.guvnor.services.metadata.MetadataService;
import org.kie.guvnor.services.metadata.model.Metadata;
import org.kie.guvnor.testscenario.service.ScenarioTestEditorService;
import org.uberfire.backend.vfs.Path;
import org.uberfire.client.common.DirtyableFlexTable;
import org.uberfire.client.common.MultiPageEditor;
import org.uberfire.client.common.Page;
import org.uberfire.client.workbench.widgets.events.NotificationEvent;

public class ScenarioEditorViewImpl
        implements ScenarioEditorView,
                   ScenarioParentWidget {

    private final Event<NotificationEvent> notification;
    private final VerticalPanel layout = new VerticalPanel();

    private ScenarioWidgetComponentCreator scenarioWidgetComponentCreator;

    private final ImportsWidgetPresenter importsWidget;

    private MetadataWidget metadataWidget;

    private MultiPageEditor multiPage;

    private BusyIndicatorView busyIndicatorView;
    private Caller<MetadataService> metadataService;

    @Inject
    public ScenarioEditorViewImpl( final @New ImportsWidgetPresenter importsWidget,
                                   final @New MultiPageEditor multiPage,
                                   final @New MetadataWidget metadataWidget,
                                   Caller<MetadataService> metadataService,
                                   final Event<NotificationEvent> notification,
                                   final BusyIndicatorView busyIndicatorView ) {
        this.importsWidget = importsWidget;
        this.multiPage = multiPage;
        this.metadataWidget = metadataWidget;
        this.metadataService = metadataService;
        this.notification = notification;
        this.busyIndicatorView = busyIndicatorView;

        multiPage.addWidget( layout, "Test Scenario" );

        layout.setWidth( "100%" );

        multiPage.addWidget( importsWidget,
                             CommonConstants.INSTANCE.ConfigTabTitle() );

    }

    @Override
    public Widget asWidget() {
        return multiPage.asWidget();

    }

    public String getTitle() {
        return "Test Scenario";
    }

    @Override
    public void initImportsTab( DataModelOracle dmo,
                                Imports imports,
                                boolean readOnly ) {
        importsWidget.setContent(
                dmo,
                imports,
                readOnly );
    }

    @Override
    public Metadata getMetadata() {
        return metadataWidget.getContent();
    }

    @Override
    public void resetMetadataDirty() {
        metadataWidget.resetDirty();
    }

    private void createWidgetForEditorLayout( DirtyableFlexTable editorLayout,
                                              int layoutRow,
                                              int layoutColumn,
                                              Widget widget ) {
        editorLayout.setWidget( layoutRow,
                                layoutColumn,
                                widget );
    }

    public void renderEditor() {

        if ( this.layout.getWidgetCount() == 2 ) {
            this.layout.remove( 1 );
        }

        DirtyableFlexTable editorLayout = scenarioWidgetComponentCreator.createDirtyableFlexTable();
        this.layout.add( editorLayout );
        ScenarioHelper scenarioHelper = new ScenarioHelper();

        List<Fixture> fixtures = scenarioHelper.lumpyMap( getScenario().getFixtures() );
        List<ExecutionTrace> listExecutionTrace = scenarioHelper.getExecutionTraceFor( fixtures );

        int layoutRow = 1;
        int executionTraceLine = 0;
        ExecutionTrace previousExecutionTrace = null;
        for ( final Fixture fixture : fixtures ) {
            if ( fixture instanceof ExecutionTrace ) {
                ExecutionTrace currentExecutionTrace = (ExecutionTrace) fixture;
                createWidgetForEditorLayout( editorLayout,
                                             layoutRow,
                                             0,
                                             scenarioWidgetComponentCreator.createExpectPanel( currentExecutionTrace ) );

                executionTraceLine++;
                if ( executionTraceLine >= listExecutionTrace.size() ) {
                    executionTraceLine = listExecutionTrace.size() - 1;
                }
                createWidgetForEditorLayout( editorLayout,
                                             layoutRow,
                                             1,
                                             scenarioWidgetComponentCreator.createExecutionWidget( currentExecutionTrace ) );
                editorLayout.setHorizontalAlignmentForFlexCellFormatter( layoutRow,
                                                                         2,
                                                                         HasHorizontalAlignment.ALIGN_LEFT );

                previousExecutionTrace = currentExecutionTrace;

            } else if ( fixture instanceof FixturesMap ) {
                createWidgetForEditorLayout( editorLayout,
                                             layoutRow,
                                             0,
                                             scenarioWidgetComponentCreator.createGivenLabelButton( listExecutionTrace,
                                                                                                    executionTraceLine,
                                                                                                    previousExecutionTrace ) );
                layoutRow++;
                createWidgetForEditorLayout( editorLayout,
                                             layoutRow,
                                             1,
                                             scenarioWidgetComponentCreator.createGivenPanel( listExecutionTrace,
                                                                                              executionTraceLine,
                                                                                              (FixturesMap) fixture ) );
            } else if ( fixture instanceof CallFixtureMap ) {
                createWidgetForEditorLayout( editorLayout,
                                             layoutRow,
                                             0,
                                             scenarioWidgetComponentCreator.createCallMethodLabelButton( listExecutionTrace,
                                                                                                         executionTraceLine,
                                                                                                         previousExecutionTrace ) );
                layoutRow++;
                createWidgetForEditorLayout( editorLayout,
                                             layoutRow,
                                             1,
                                             scenarioWidgetComponentCreator.createCallMethodOnGivenPanel( listExecutionTrace,
                                                                                                          executionTraceLine,
                                                                                                          (CallFixtureMap) fixture ) );
            } else {
                FixtureList fixturesList = (FixtureList) fixture;
                Fixture first = fixturesList.get( 0 );

                if ( first instanceof VerifyFact ) {
                    createWidgetForEditorLayout( editorLayout,
                                                 layoutRow,
                                                 1,
                                                 scenarioWidgetComponentCreator.createVerifyFactsPanel( listExecutionTrace,
                                                                                                        executionTraceLine,
                                                                                                        fixturesList ) );
                } else if ( first instanceof VerifyRuleFired ) {
                    createWidgetForEditorLayout( editorLayout,
                                                 layoutRow,
                                                 1,
                                                 scenarioWidgetComponentCreator.createVerifyRulesFiredWidget( fixturesList ) );
                }

            }
            layoutRow++;
        }

        // add more execution sections.
        createWidgetForEditorLayout( editorLayout,
                                     layoutRow,
                                     0,
                                     scenarioWidgetComponentCreator.createAddExecuteButton() );
        layoutRow++;
        createWidgetForEditorLayout( editorLayout,
                                     layoutRow,
                                     0,
                                     scenarioWidgetComponentCreator.createSmallLabel() );

        // config section
        createWidgetForEditorLayout( editorLayout,
                                     layoutRow,
                                     1,
                                     scenarioWidgetComponentCreator.createConfigWidget() );

        layoutRow++;

        // global section
        HorizontalPanel horizontalPanel = scenarioWidgetComponentCreator.createHorizontalPanel();
        createWidgetForEditorLayout( editorLayout,
                                     layoutRow,
                                     0,
                                     horizontalPanel );

        createWidgetForEditorLayout( editorLayout,
                                     layoutRow,
                                     1,
                                     scenarioWidgetComponentCreator.createGlobalPanel( scenarioHelper,
                                                                                       previousExecutionTrace ) );
    }

    @Override
    public void addTestRunnerWidget( final Scenario scenario,
                                     final Caller<ScenarioTestEditorService> service,
                                     final Path path ) {
        layout.add( new TestRunnerWidget( scenario, service, path ) );
    }

    @Override
    public void addMetaDataPage( final Path path,
                                 final boolean isReadOnly ) {
        multiPage.addPage( new Page( metadataWidget,
                                     MetadataConstants.INSTANCE.Metadata() ) {
            @Override
            public void onFocus() {
                metadataWidget.showBusyIndicator( CommonConstants.INSTANCE.Loading() );
                metadataService.call( new MetadataSuccessCallback( metadataWidget,
                                                                   isReadOnly ),
                                      new HasBusyIndicatorDefaultErrorCallback( metadataWidget ) ).getMetadata( path );
            }

            @Override
            public void onLostFocus() {
                // Nothing to do here.
            }
        } );
    }

    @Override
    public void setScenario( String packageName,
                             Scenario scenario,
                             DataModelOracle dmo ) {
        String[] availableRules = { }; // TODO: Load available rules -Rikkola-
        scenarioWidgetComponentCreator = new ScenarioWidgetComponentCreator( packageName, this, dmo, availableRules );
        scenarioWidgetComponentCreator.setScenario( scenario );
        scenarioWidgetComponentCreator.setShowResults( false );
    }

    @Override
    public void showSaveSuccessful() {
        notification.fire( new NotificationEvent( CommonConstants.INSTANCE.ItemSavedSuccessfully() ) );
    }

    void setShowResults( boolean showResults ) {
        scenarioWidgetComponentCreator.setShowResults( showResults );
    }

    public void setScenario( Scenario scenario ) {
        scenarioWidgetComponentCreator.setScenario( scenario );
    }

    public Scenario getScenario() {
        return scenarioWidgetComponentCreator.getScenario();
    }

    @Override
    public void showCanNotSaveReadOnly() {
        Window.alert( CommonConstants.INSTANCE.CantSaveReadOnly() );
    }

    @Override
    public void showBusyIndicator( final String message ) {
        busyIndicatorView.showBusyIndicator( message );
    }

    @Override
    public void hideBusyIndicator() {
        busyIndicatorView.hideBusyIndicator();
    }

}
