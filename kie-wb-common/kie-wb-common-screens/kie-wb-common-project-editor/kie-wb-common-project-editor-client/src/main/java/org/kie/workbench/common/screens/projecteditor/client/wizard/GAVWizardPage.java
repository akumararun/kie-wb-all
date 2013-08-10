package org.kie.workbench.common.screens.projecteditor.client.wizard;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.Widget;
import org.guvnor.common.services.project.model.GAV;
import org.kie.workbench.common.screens.projecteditor.client.forms.ArtifactIdChangeHandler;
import org.kie.workbench.common.screens.projecteditor.client.forms.GAVEditor;
import org.kie.workbench.common.screens.projecteditor.client.forms.GroupIdChangeHandler;
import org.kie.workbench.common.screens.projecteditor.client.forms.VersionChangeHandler;
import org.kie.workbench.common.screens.projecteditor.client.resources.i18n.ProjectEditorConstants;
import org.uberfire.client.wizards.WizardPage;
import org.uberfire.client.wizards.WizardPageStatusChangeEvent;

public class GAVWizardPage
        implements WizardPage {

    private GAV gav;
    private final GAVEditor gavEditor;
    private Event<WizardPageStatusChangeEvent> wizardPageStatusChangeEvent;

    @Inject
    public GAVWizardPage( GAVEditor gavEditor,
                          Event<WizardPageStatusChangeEvent> wizardPageStatusChangeEvent ) {
        this.gavEditor = gavEditor;
        this.wizardPageStatusChangeEvent = wizardPageStatusChangeEvent;
    }

    public void setGav( GAV gav ) {
        this.gav = gav;
        this.gavEditor.setGAV( gav );
        this.gavEditor.addGroupIdChangeHandler( new GroupIdChangeHandler() {
            @Override
            public void onChange( String newGroupId ) {
                final WizardPageStatusChangeEvent event = new WizardPageStatusChangeEvent( GAVWizardPage.this );
                wizardPageStatusChangeEvent.fire( event );
            }
        } );
        this.gavEditor.addArtifactIdChangeHandler( new ArtifactIdChangeHandler() {
            @Override
            public void onChange( String newArtifactId ) {
                final WizardPageStatusChangeEvent event = new WizardPageStatusChangeEvent( GAVWizardPage.this );
                wizardPageStatusChangeEvent.fire( event );
            }
        } );
        this.gavEditor.addVersionChangeHandler( new VersionChangeHandler() {
            @Override
            public void onChange( String newVersion ) {
                final WizardPageStatusChangeEvent event = new WizardPageStatusChangeEvent( GAVWizardPage.this );
                wizardPageStatusChangeEvent.fire( event );
            }
        } );
    }

    @Override
    public String getTitle() {
        return ProjectEditorConstants.INSTANCE.NewProjectWizard();
    }

    @Override
    public boolean isComplete() {
        boolean validGroupId = !( gav.getGroupId() == null || gav.getGroupId().isEmpty() );
        boolean validArtifactId = !( gav.getArtifactId() == null || gav.getArtifactId().isEmpty() );
        boolean validVersion = !( gav.getVersion() == null || gav.getVersion().isEmpty() );
        return validGroupId && validArtifactId && validVersion;
    }

    @Override
    public void initialise() {
        //TODO: -Rikkola-
    }

    @Override
    public void prepareView() {
        //TODO: -Rikkola-
    }

    @Override
    public Widget asWidget() {
        return gavEditor.asWidget();
    }
}
