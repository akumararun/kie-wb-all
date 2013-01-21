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

package org.kie.guvnor.projecteditor.client.forms;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;
import org.kie.guvnor.project.model.Dependency;
import org.kie.guvnor.project.model.GroupArtifactVersionModel;
import org.kie.guvnor.projecteditor.client.resources.i18n.ProjectEditorConstants;
import org.uberfire.client.workbench.widgets.events.NotificationEvent;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.List;

public class GroupArtifactVersionEditorPanelViewImpl
        extends ResizeComposite
        implements GroupArtifactVersionEditorPanelView {

    private InlineLabel tabTitleLabel = new InlineLabel(ProjectEditorConstants.INSTANCE.ProjectModel());

    interface GroupArtifactVersionEditorPanelViewImplBinder
            extends
            UiBinder<Widget, GroupArtifactVersionEditorPanelViewImpl> {

    }

    private static GroupArtifactVersionEditorPanelViewImplBinder uiBinder = GWT.create(GroupArtifactVersionEditorPanelViewImplBinder.class);

    private final Event<NotificationEvent> notificationEvent;

    @UiField(provided = true)
    GAVEditor gavEditor;

    @UiField(provided = true)
    DependencyGrid dependencyGrid;

    @Inject
    public GroupArtifactVersionEditorPanelViewImpl(Event<NotificationEvent> notificationEvent,
                                                   GAVEditor gavEditor,
                                                   DependencyGrid dependencyGrid) {
        this.gavEditor = gavEditor;
        this.dependencyGrid = dependencyGrid;
        initWidget(uiBinder.createAndBindUi(this));
        this.notificationEvent = notificationEvent;
    }


    @Override
    public void showSaveSuccessful(String fileName) {
        notificationEvent.fire(new NotificationEvent(ProjectEditorConstants.INSTANCE.SaveSuccessful(fileName)));
    }

    @Override
    public IsWidget getTitleWidget() {
        return tabTitleLabel;
    }

    @Override
    public void setDependencies(List<Dependency> dependencies) {
        dependencyGrid.fillList(dependencies);
    }

    @Override
    public void setGAV(GroupArtifactVersionModel gav) {
        gavEditor.setGAV(gav);
    }

    @Override
    public void addArtifactIdChangeHandler(ArtifactIdChangeHandler changeHandler) {
        gavEditor.addArtifactIdChangeHandler(changeHandler);
    }

    @Override
    public void setTitleText(String titleText) {
        tabTitleLabel.setText(titleText);
    }

    @Override
    public void onResize() {
        setPixelSize(getParent().getOffsetWidth(),
                getParent().getOffsetHeight());
        super.onResize();
    }
}
