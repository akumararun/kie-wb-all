/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.screens.projecteditor.client.editor;

import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.github.gwtbootstrap.client.ui.DropdownButton;
import com.github.gwtbootstrap.client.ui.NavHeader;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.Widget;
import org.guvnor.common.services.project.client.POMEditorPanel;
import org.guvnor.common.services.project.model.Dependency;
import org.guvnor.common.services.project.model.POM;
import org.guvnor.common.services.project.model.ProjectImports;
import org.guvnor.common.services.shared.metadata.model.Metadata;
import org.kie.uberfire.client.common.BusyIndicatorView;
import org.kie.uberfire.client.common.BusyPopup;
import org.kie.uberfire.client.common.popups.errors.ErrorPopup;
import org.kie.workbench.common.screens.projecteditor.client.forms.DependencyGrid;
import org.kie.workbench.common.screens.projecteditor.client.forms.KModuleEditorPanel;
import org.kie.workbench.common.screens.projecteditor.client.resources.ProjectEditorResources;
import org.kie.workbench.common.services.shared.kmodule.KModuleModel;
import org.kie.workbench.common.widgets.configresource.client.widget.unbound.ImportsWidgetPresenter;
import org.kie.workbench.common.widgets.metadata.client.widget.MetadataWidget;

import static com.github.gwtbootstrap.client.ui.resources.ButtonSize.*;

@ApplicationScoped
public class ProjectScreenViewImpl
        extends Composite
        implements ProjectScreenView {


    private static final int GAV_PANEL_INDEX = 0;
    private static final int DEPENDENCY_PANEL_INDEX = 1;
    private static final int GAV_METADATA_PANEL_INDEX = 2;
    private static final int KBASE_PANEL_INDEX = 3;
    private static final int KBASE_METADATA_PANEL_INDEX = 4;
    private static final int IMPORTS_PANEL_INDEX = 5;
    private static final int IMPORTS_METADATA_PANEL_INDEX = 6;

    private POMEditorPanel pomEditorPanel;

    private MetadataWidget pomMetadataWidget;
    private Presenter presenter;
    private KModuleEditorPanel kModuleEditorPanel;
    private MetadataWidget kModuleMetaDataPanel;
    private ImportsWidgetPresenter importsWidgetPresenter;
    private MetadataWidget importsPageMetadata;
    private DependencyGrid dependencyGrid;
    private Boolean supportDeployToRuntime = Boolean.TRUE;

    interface ProjectScreenViewImplBinder
            extends
            UiBinder<Widget, ProjectScreenViewImpl> {

    }

    private static ProjectScreenViewImplBinder uiBinder = GWT.create(ProjectScreenViewImplBinder.class);

    @UiField
    DropdownButton dropDownButton;

    @UiField
    DeckPanel deckPanel;

    @UiField
    NavHeader deploymentsHeader;

    @UiField
    NavLink deploymentDescriptorButton;

    @Inject
    BusyIndicatorView busyIndicatorView;

    @Inject
    DeploymentScreenPopupViewImpl deploymentScreenPopupView;

    public ProjectScreenViewImpl() {
    }

    @Inject
    public ProjectScreenViewImpl(POMEditorPanel pomEditorPanel,
                                 KModuleEditorPanel kModuleEditorPanel,
                                 ImportsWidgetPresenter importsWidgetPresenter,
                                 DependencyGrid dependencyGrid) {

        initWidget(uiBinder.createAndBindUi(this));

        this.pomEditorPanel = pomEditorPanel;
        this.kModuleEditorPanel = kModuleEditorPanel;
        this.importsWidgetPresenter = importsWidgetPresenter;
        this.dependencyGrid = dependencyGrid;

        deckPanel.add(pomEditorPanel);

        deckPanel.add(dependencyGrid);

        this.pomMetadataWidget = new MetadataWidget(busyIndicatorView);
        deckPanel.add(pomMetadataWidget);

        deckPanel.add(kModuleEditorPanel);

        this.kModuleMetaDataPanel = new MetadataWidget(busyIndicatorView);
        deckPanel.add(kModuleMetaDataPanel);

        deckPanel.add(importsWidgetPresenter);

        this.importsPageMetadata = new MetadataWidget(busyIndicatorView);
        deckPanel.add(importsPageMetadata);

    }


    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void showGAVPanel() {
        deckPanel.showWidget(GAV_PANEL_INDEX);
        setGAVDropboxTitle(ProjectEditorResources.CONSTANTS.ProjectGeneralSettings());
    }

    @Override
    public void showGAVMetadataPanel() {
        deckPanel.showWidget(GAV_METADATA_PANEL_INDEX);
        setGAVDropboxTitle(ProjectEditorResources.CONSTANTS.Metadata());
    }

    @UiHandler(value = "generalSettingsButton")
    public void onGeneralSettingsButtonClick(ClickEvent clickEvent) {
        presenter.onGAVPanelSelected();
    }

    @UiHandler(value = "gavMetadataButton")
    public void onGAVMetadataButtonClick(ClickEvent clickEvent) {
        presenter.onGAVMetadataPanelSelected();
    }

    private void setGAVDropboxTitle(String subItem) {
        dropDownButton.setText(ProjectEditorResources.CONSTANTS.ProjectSettings() + ": " + subItem);
    }

    @UiHandler(value = "dependenciesButton")
    public void onDependenciesButtonClick(ClickEvent clickEvent) {
        presenter.onDependenciesSelected();
    }

    @UiHandler(value = "kbaseButton")
    public void onKbaseButtonClick(ClickEvent clickEvent) {
        presenter.onKBasePanelSelected();
    }

    @UiHandler(value = "kbaseMetadataButton")
    public void onKbaseMetadataButtonClick(ClickEvent clickEvent) {
        presenter.onKBaseMetadataPanelSelected();
    }

    @Override
    public void showKBasePanel() {
        deckPanel.showWidget(KBASE_PANEL_INDEX);
        dropDownButton.setText(ProjectEditorResources.CONSTANTS.KnowledgeBaseSettings() + ": " + ProjectEditorResources.CONSTANTS.KnowledgeBasesAndSessions());
        kModuleEditorPanel.refresh();
    }

    @Override
    public void showKBaseMetadataPanel() {
        deckPanel.showWidget(KBASE_METADATA_PANEL_INDEX);
        dropDownButton.setText(ProjectEditorResources.CONSTANTS.KnowledgeBaseSettings() + ": " + ProjectEditorResources.CONSTANTS.Metadata());
    }

    @UiHandler(value = "importsButton")
    public void onImportsButtonClick(ClickEvent clickEvent) {
        presenter.onImportsPanelSelected();
    }

    @UiHandler(value = "importsMetadataButton")
    public void onImportsMetadataButtonClick(ClickEvent clickEvent) {
        presenter.onImportsMetadataPanelSelected();
    }

    @UiHandler(value = "deploymentDescriptorButton")
    public void onDeploymentDescriptorButtonClick(ClickEvent clickEvent) {
        presenter.onDeploymentDescriptorSelected();
    }

    @Override
    public void setImports(ProjectImports projectImports) {
        importsWidgetPresenter.setContent(projectImports, false);
    }

    @Override
    public void setImportsMetadata(Metadata projectImportsMetadata) {
        importsPageMetadata.setContent(projectImportsMetadata, false);
    }

    @Override
    public void showDependenciesPanel() {
        dropDownButton.setText(ProjectEditorResources.CONSTANTS.Dependencies() + ": " + ProjectEditorResources.CONSTANTS.DependenciesList());
        deckPanel.showWidget(DEPENDENCY_PANEL_INDEX);
        dependencyGrid.redraw();
    }

    @Override
    public void showImportsPanel() {
        dropDownButton.setText(ProjectEditorResources.CONSTANTS.Imports() + ": " + ProjectEditorResources.CONSTANTS.ImportSuggestions());
        deckPanel.showWidget(IMPORTS_PANEL_INDEX);
    }

    @Override
    public void showImportsMetadataPanel() {
        dropDownButton.setText(ProjectEditorResources.CONSTANTS.Imports() + ": " + ProjectEditorResources.CONSTANTS.Metadata());
        deckPanel.showWidget(IMPORTS_METADATA_PANEL_INDEX);
    }

    @Override
    public void showBusyIndicator(final String message) {
        BusyPopup.showMessage(message);
    }

    @Override
    public void hideBusyIndicator() {
        BusyPopup.close();
    }

    @Override
    public void setPOM(POM pom) {
        pomEditorPanel.setPOM(pom, false);
    }

    @Override
    public void setDependencies(List<Dependency> dependencies) {
        dependencyGrid.fillList(dependencies);
    }

    @Override
    public void setPomMetadata(Metadata pomMetaData) {
        pomMetadataWidget.setContent(pomMetaData, false);
    }

    @Override
    public void setKModule(KModuleModel kModule) {
        kModuleEditorPanel.setData(kModule, false);
    }

    @Override
    public void setKModuleMetadata(Metadata kModuleMetaData) {
        kModuleMetaDataPanel.setContent(kModuleMetaData, false);
    }

    @Override
    public void switchBusyIndicator(String newMessage) {
        BusyPopup.showMessage(newMessage);
    }

    @Override
    public void showABuildIsAlreadyRunning() {
        ErrorPopup.showMessage(ProjectEditorResources.CONSTANTS.ABuildIsAlreadyRunning());
    }

    @Override
    public DropdownButton getBuildOptionsButton() {
        return new DropdownButton( ProjectEditorResources.CONSTANTS.BuildAndDeploy() ) {{
            setSize( MINI );
            setRightDropdown( true );
            add( new NavLink( ProjectEditorResources.CONSTANTS.Build() ) {{
                addClickHandler( new ClickHandler() {
                    @Override
                    public void onClick( ClickEvent event ) {
                        ((ProjectScreenPresenter)presenter).triggerBuild();
                    }
                } );
            }} );

            add( new NavLink( ProjectEditorResources.CONSTANTS.BuildAndInstall() ) {{
                addClickHandler( new ClickHandler() {
                    @Override
                    public void onClick( ClickEvent event ) {
                        ((ProjectScreenPresenter)presenter).triggerBuildAndInstall();
                    }
                } );
            }} );
            if (supportDeployToRuntime) {
                add( new NavLink( ProjectEditorResources.CONSTANTS.BuildAndDeploy() ) {{
                    addClickHandler( new ClickHandler() {
                        @Override
                        public void onClick( ClickEvent event ) {
                            deploymentScreenPopupView.configure(new Command() {
                                @Override
                                public void execute() {
                                    String username = deploymentScreenPopupView.getUsername();
                                    String password = deploymentScreenPopupView.getPassword();
                                    String serverURL = deploymentScreenPopupView.getServerURL();
                                    ((ProjectScreenPresenter) presenter).triggerBuildAndDeploy(username, password, serverURL);
                                    deploymentScreenPopupView.hide();
                                }
                            });
                            deploymentScreenPopupView.show();
                        }
                    } );
                }} );
            }
        }};
    }

    @Override
    public void setDeployToRuntimeSetting(Boolean supports) {
        this.supportDeployToRuntime = supports;

        if (supports != null) {
            deploymentsHeader.setVisible(supports.booleanValue());
            deploymentDescriptorButton.setVisible(supports.booleanValue());
        }
    }
}
