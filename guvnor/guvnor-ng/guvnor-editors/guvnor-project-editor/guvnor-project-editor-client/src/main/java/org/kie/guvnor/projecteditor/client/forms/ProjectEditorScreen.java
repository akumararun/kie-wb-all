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

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.api.Caller;
import org.kie.guvnor.projecteditor.client.messages.MessageService;
import org.kie.guvnor.projecteditor.client.resources.i18n.ProjectEditorConstants;
import org.kie.guvnor.projecteditor.model.builder.Messages;
import org.kie.guvnor.projecteditor.service.ProjectEditorService;
import org.uberfire.backend.vfs.Path;
import org.uberfire.client.annotations.OnStart;
import org.uberfire.client.annotations.WorkbenchEditor;
import org.uberfire.client.annotations.WorkbenchMenu;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.mvp.Command;
import org.uberfire.client.workbench.widgets.menu.MenuBar;
import org.uberfire.client.workbench.widgets.menu.impl.DefaultMenuBar;
import org.uberfire.client.workbench.widgets.menu.impl.DefaultMenuItemCommand;

@WorkbenchEditor(identifier = "projectEditorScreen", fileTypes = "xml")
public class ProjectEditorScreen
        implements ProjectEditorScreenView.Presenter {

    private final ProjectEditorScreenView view;
    private final GroupArtifactVersionEditorPanel gavPanel;
    private final KModuleEditorPanel kModuleEditorPanel;
    private final Caller<ProjectEditorService> projectEditorServiceCaller;
    private Path pathToPomXML;
    private Path pathToKModuleXML;
    private final MessageService messageService;

    @Inject
    public ProjectEditorScreen(ProjectEditorScreenView view,
                               GroupArtifactVersionEditorPanel gavPanel,
                               KModuleEditorPanel kModuleEditorPanel,
                               Caller<ProjectEditorService> projectEditorServiceCaller,
                               MessageService messageService) {
        this.view = view;
        this.gavPanel = gavPanel;
        this.kModuleEditorPanel = kModuleEditorPanel;
        this.projectEditorServiceCaller = projectEditorServiceCaller;
        this.messageService = messageService;

        view.setPresenter(this);
        view.setGroupArtifactVersionEditorPanel(gavPanel);
    }

    @OnStart
    public void init(Path path) {

        pathToPomXML = path;
        gavPanel.init(path);
        projectEditorServiceCaller.call(
                new RemoteCallback<Path>() {
                    @Override
                    public void callback(Path pathToKModuleXML) {
                        ProjectEditorScreen.this.pathToKModuleXML = pathToKModuleXML;
                        if (pathToKModuleXML != null) {
                            setUpKProject(pathToKModuleXML);
                        }
                    }
                }
        ).pathToRelatedKModuleFileIfAny(path);
    }

    private void setUpKProject(Path path) {
        view.setKModuleEditorPanel(kModuleEditorPanel);
        kModuleEditorPanel.init(path);
    }

    @WorkbenchPartTitle
    public String getTitle() {
        return ProjectEditorConstants.INSTANCE.ProjectModel(); // TODO needs to be set later, to what ever the artifact name is -Rikkola-
    }

    @WorkbenchPartView
    public Widget asWidget() {
        return view.asWidget();
    }

    @Override
    public void onKModuleToggleOn() {
        projectEditorServiceCaller.call(
                new RemoteCallback<Path>() {
                    @Override
                    public void callback(Path pathToKProject) {
                        pathToKModuleXML = pathToKProject;
                        setUpKProject(pathToKProject);
                    }
                }
        ).setUpKModuleStructure(pathToPomXML);
    }

    @WorkbenchMenu
    public MenuBar buildMenuBar() {
        MenuBar menuBar = new DefaultMenuBar();

        menuBar.addItem(new DefaultMenuItemCommand(
                view.getSaveMenuItemText(),
                new Command() {
                    @Override
                    public void execute() {
                        // We need to use callback here or jgit will break when we save two files at the same time.
                        gavPanel.save(new com.google.gwt.user.client.Command() {
                            @Override
                            public void execute() {
                                if (pathToKModuleXML != null) {
                                    kModuleEditorPanel.save();
                                }
                            }
                        });

                    }
                }
        ));
        menuBar.addItem(new DefaultMenuItemCommand(
                view.getBuildMenuItemText(),
                new Command() {
                    @Override
                    public void execute() {
                        projectEditorServiceCaller.call(
                                new RemoteCallback<Messages>() {
                                    @Override
                                    public void callback(Messages messages) {
                                        if (messages.isEmpty()) {
                                            view.showBuildSuccessful();
                                        } else {
                                            messageService.addMessages(messages);
                                        }
                                    }
                                }
                        ).build(pathToPomXML);
                    }
                }
        ));
        if (pathToKModuleXML == null) {
            menuBar.addItem(new DefaultMenuItemCommand(
                    view.getEnableKieProjectMenuItemText(),
                    new Command() {
                        @Override
                        public void execute() {
                            onKModuleToggleOn();
                        }
                    }
            ));
        }

        return menuBar;
    }
}
