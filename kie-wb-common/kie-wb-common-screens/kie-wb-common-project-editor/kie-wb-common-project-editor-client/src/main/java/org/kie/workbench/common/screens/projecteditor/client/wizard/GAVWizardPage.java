/*
 * Copyright 2014 JBoss Inc
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
package org.kie.workbench.common.screens.projecteditor.client.wizard;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.Widget;
import org.guvnor.common.services.project.client.ArtifactIdChangeHandler;
import org.guvnor.common.services.project.client.GroupIdChangeHandler;
import org.guvnor.common.services.project.client.NameChangeHandler;
import org.guvnor.common.services.project.client.POMEditorPanel;
import org.guvnor.common.services.project.client.VersionChangeHandler;
import org.guvnor.common.services.project.model.POM;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.kie.workbench.common.screens.projecteditor.client.resources.ProjectEditorResources;
import org.kie.workbench.common.screens.projecteditor.service.ProjectScreenService;
import org.uberfire.client.callbacks.Callback;
import org.uberfire.ext.widgets.core.client.wizards.WizardPage;
import org.uberfire.ext.widgets.core.client.wizards.WizardPageStatusChangeEvent;

public class GAVWizardPage
        implements WizardPage {

    private POMEditorPanel pomEditor;
    private GAVWizardPageView view;
    private Event<WizardPageStatusChangeEvent> wizardPageStatusChangeEvent;
    private Caller<ProjectScreenService> projectScreenService;

    @Inject
    public GAVWizardPage( POMEditorPanel pomEditor,
                          GAVWizardPageView view,
                          Event<WizardPageStatusChangeEvent> wizardPageStatusChangeEvent,
                          Caller<ProjectScreenService> projectScreenService ) {
        this.pomEditor = pomEditor;
        this.view = view;
        this.wizardPageStatusChangeEvent = wizardPageStatusChangeEvent;
        this.projectScreenService = projectScreenService;

        // changes are passed on from the pom editor through its view onto the underlying gav editor
        addChangeHandlers();
    }

    private void addChangeHandlers() {
        this.pomEditor.addNameChangeHandler( new NameChangeHandler() {
            @Override
            public void onChange( String newName ) {
                final WizardPageStatusChangeEvent event = new WizardPageStatusChangeEvent( GAVWizardPage.this );
                GAVWizardPage.this.wizardPageStatusChangeEvent.fire( event );
            }
        } );
        this.pomEditor.addGroupIdChangeHandler( new GroupIdChangeHandler() {
            @Override
            public void onChange( String newGroupId ) {
                validateGroupId( pomEditor.getPom().getGav().getGroupId() );
                final WizardPageStatusChangeEvent event = new WizardPageStatusChangeEvent( GAVWizardPage.this );
                GAVWizardPage.this.wizardPageStatusChangeEvent.fire( event );
            }
        } );
        this.pomEditor.addArtifactIdChangeHandler( new ArtifactIdChangeHandler() {
            @Override
            public void onChange( String newArtifactId ) {
                validateArtifactId( pomEditor.getPom().getGav().getArtifactId() );
                final WizardPageStatusChangeEvent event = new WizardPageStatusChangeEvent( GAVWizardPage.this );
                GAVWizardPage.this.wizardPageStatusChangeEvent.fire( event );
            }
        } );
        this.pomEditor.addVersionChangeHandler( new VersionChangeHandler() {
            @Override
            public void onChange( String newVersion ) {
                validateVersion( pomEditor.getPom().getGav().getVersion() );
                final WizardPageStatusChangeEvent event = new WizardPageStatusChangeEvent( GAVWizardPage.this );
                GAVWizardPage.this.wizardPageStatusChangeEvent.fire( event );
            }
        } );
    }

    public void setPom( final POM pom ) {
        this.pomEditor.setPOM( pom,
                               false );

        if ( hasParent( pom ) ) {
            pomEditor.disableGroupID( view.InheritedFromAParentPOM() );
            pomEditor.disableVersion( view.InheritedFromAParentPOM() );
            validateArtifactId( pom.getGav().getArtifactId() );
        } else {
            validateGroupId( pom.getGav().getGroupId() );
            validateArtifactId( pom.getGav().getArtifactId() );
            validateVersion( pom.getGav().getVersion() );
        }
    }

    private void validateGroupId( final String groupId ) {
        projectScreenService.call( new RemoteCallback<Boolean>() {
            @Override
            public void callback( final Boolean result ) {
                pomEditor.setValidGroupID( Boolean.TRUE.equals( result ) );
            }
        } ).validateGroupId( groupId );
    }

    private void validateArtifactId( final String artifactId ) {
        projectScreenService.call( new RemoteCallback<Boolean>() {
            @Override
            public void callback( final Boolean result ) {
                pomEditor.setValidArtifactID( Boolean.TRUE.equals( result ) );
            }
        } ).validateArtifactId( artifactId );
    }

    private void validateVersion( final String version ) {
        projectScreenService.call( new RemoteCallback<Boolean>() {
            @Override
            public void callback( final Boolean result ) {
                pomEditor.setValidVersion( Boolean.TRUE.equals( result ) );
            }
        } ).validateVersion( version );
    }

    private boolean hasParent( POM pom ) {
        return pom.getGav() != null && pom.getGav().getGroupId() != null && !pom.getGav().getGroupId().trim().isEmpty();
    }

    @Override
    public String getTitle() {
        return ProjectEditorResources.CONSTANTS.NewProjectWizard();
    }

    @Override
    public void isComplete( final Callback<Boolean> callback ) {
        projectScreenService.call( new RemoteCallback<Boolean>() {
            @Override
            public void callback( final Boolean result ) {
                callback.callback( Boolean.TRUE.equals( result ) );
            }
        } ).validate( pomEditor.getPom() );
    }

    @Override
    public void initialise() {
    }

    @Override
    public void prepareView() {
    }

    @Override
    public Widget asWidget() {
        return pomEditor.asWidget();
    }
}
