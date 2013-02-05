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

package org.kie.guvnor.guided.rule.client;

import com.google.gwt.user.client.ui.IsWidget;
import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.api.Caller;
import org.kie.guvnor.commons.service.validation.model.BuilderResult;
import org.kie.guvnor.commons.ui.client.handlers.CopyPopup;
import org.kie.guvnor.commons.ui.client.handlers.DeletePopup;
import org.kie.guvnor.commons.ui.client.handlers.RenameCommand;
import org.kie.guvnor.commons.ui.client.handlers.RenamePopup;
import org.kie.guvnor.commons.ui.client.resources.i18n.CommonConstants;
import org.kie.guvnor.commons.ui.client.save.CommandWithCommitMessage;
import org.kie.guvnor.commons.ui.client.save.SaveOperationService;
import org.kie.guvnor.configresource.client.widget.ImportsWidget;
import org.kie.guvnor.datamodel.oracle.DataModelOracle;
import org.kie.guvnor.errors.client.widget.ShowBuilderErrorsWidget;
import org.kie.guvnor.guided.rule.model.GuidedEditorContent;
import org.kie.guvnor.guided.rule.model.RuleModel;
import org.kie.guvnor.guided.rule.service.GuidedRuleEditorService;
import org.kie.guvnor.metadata.client.resources.i18n.MetadataConstants;
import org.kie.guvnor.metadata.client.widget.MetadataWidget;
import org.kie.guvnor.services.config.ResourceConfigService;
import org.kie.guvnor.services.metadata.MetadataService;
import org.kie.guvnor.services.metadata.model.Metadata;
import org.kie.guvnor.viewsource.client.screen.ViewSourceView;
import org.uberfire.backend.vfs.Path;
import org.uberfire.client.annotations.IsDirty;
import org.uberfire.client.annotations.OnClose;
import org.uberfire.client.annotations.OnMayClose;
import org.uberfire.client.annotations.OnSave;
import org.uberfire.client.annotations.OnStart;
import org.uberfire.client.annotations.WorkbenchEditor;
import org.uberfire.client.annotations.WorkbenchMenu;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.common.LoadingPopup;
import org.uberfire.client.common.MultiPageEditor;
import org.uberfire.client.common.Page;
import org.uberfire.client.mvp.Command;
import org.uberfire.client.workbench.widgets.events.NotificationEvent;
import org.uberfire.client.workbench.widgets.menu.MenuBar;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import static org.kie.guvnor.commons.ui.client.menu.ResourceMenuBuilder.newResourceMenuBuilder;

@Dependent
@WorkbenchEditor(identifier = "GuidedRuleEditor", fileTypes = "*.gre.drl")
public class GuidedRuleEditorPresenter {

    private ImportsWidget importsWidget;

    @Inject
    private GuidedRuleEditorView view;

    @Inject
    private ViewSourceView viewSource;

    @Inject
    private MultiPageEditor multiPage;

    @Inject
    private Caller<GuidedRuleEditorService> service;

    @Inject
    private Event<NotificationEvent> notification;

    @Inject
    private Caller<MetadataService> metadataService;

    private final MetadataWidget metadataWidget = new MetadataWidget();

    private Path path = null;


    @OnStart
    public void onStart(final Path path) {
        this.path = path;
        importsWidget = new ImportsWidget(path);

        multiPage.addWidget(view, CommonConstants.INSTANCE.EditTabTitle());


        multiPage.addPage(new Page(viewSource,
                CommonConstants.INSTANCE.SourceTabTitle()) {
            @Override
            public void onFocus() {
                service.call(new RemoteCallback<String>() {
                    @Override
                    public void callback(final String response) {
                        viewSource.setContent(response);
                    }
                }).toSource(view.getContent());
            }

            @Override
            public void onLostFocus() {
                viewSource.clear();
            }
        });

        multiPage.addWidget(importsWidget, CommonConstants.INSTANCE.ConfigTabTitle());

        multiPage.addPage(new Page(metadataWidget,
                MetadataConstants.INSTANCE.Metadata()) {
            @Override
            public void onFocus() {
                metadataService.call(new RemoteCallback<Metadata>() {
                    @Override
                    public void callback(Metadata metadata) {
                        metadataWidget.setContent(metadata,
                                false);
                    }
                }).getMetadata(path);
            }

            @Override
            public void onLostFocus() {
                // Nothing to do here
            }
        });

        service.call(new RemoteCallback<GuidedEditorContent>() {
            @Override
            public void callback(final GuidedEditorContent response) {
                view.setContent(path,
                        response.getRuleModel(),
                        response.getDataModel());
                importsWidget.setImports(response.getRuleModel().getImports().getImports());
            }
        }).loadContent(path);
    }

    @OnSave
    public void onSave() {
        new SaveOperationService().save(path, new CommandWithCommitMessage() {
            @Override
            public void execute(final String commitMessage) {
                service.call(new RemoteCallback<Path>() {
                    @Override
                    public void callback(Path response) {
                        view.setNotDirty();
                        metadataWidget.resetDirty();
                        notification.fire(new NotificationEvent(CommonConstants.INSTANCE.ItemSavedSuccessfully()));
                    }
                }).save(path,
                        view.getContent(),
                        metadataWidget.getContent(),
                        commitMessage);
            }
        });
    }
    
    public void onDelete() {
        DeletePopup popup = new DeletePopup(new CommandWithCommitMessage() {
            @Override
            public void execute(final String comment) {
                service.call(new RemoteCallback<Path>() {
                    @Override
                    public void callback(Path response) {
                        view.setNotDirty();
                        metadataWidget.resetDirty();
                        notification.fire(new NotificationEvent(CommonConstants.INSTANCE.ItemDeletedSuccessfully()));
                    }
                }).delete(path,
                          comment);
            }
        });
        
        popup.show();
    }
    
    public void onRename() {
        RenamePopup popup = new RenamePopup(new RenameCommand() {
            @Override
            public void execute(final String newName, final String comment) {
                service.call(new RemoteCallback<Path>() {
                    @Override
                    public void callback(Path response) {
                        view.setNotDirty();
                        metadataWidget.resetDirty();
                        notification.fire(new NotificationEvent(CommonConstants.INSTANCE.ItemRenamedSuccessfully()));
                    }
                }).rename(path,
                          newName,
                          comment);
            }
        });
        
        popup.show();
    }
    
    public void onCopy() {
        CopyPopup popup = new CopyPopup(new RenameCommand() {
            @Override
            public void execute(final String newName, final String comment) {
                service.call(new RemoteCallback<Path>() {
                    @Override
                    public void callback(Path response) {
                        view.setNotDirty();
                        metadataWidget.resetDirty();
                        notification.fire(new NotificationEvent(CommonConstants.INSTANCE.ItemCopiedSuccessfully()));
                    }
                }).copy(path,
                        newName,
                        comment);
            }
        });
        
        popup.show();
    }
    
    @IsDirty
    public boolean isDirty() {
        return view.isDirty();
    }

    @OnClose
    public void onClose() {
        this.path = null;
    }

    @OnMayClose
    public boolean checkIfDirty() {
        if (isDirty()) {
            return view.confirmClose();
        }
        return true;
    }

    @WorkbenchPartTitle
    public String getTitle() {
        return "Guided Editor [" + path.getFileName() + "]";
    }

    @WorkbenchPartView
    public IsWidget getWidget() {

        return multiPage;
    }

    @WorkbenchMenu
    public MenuBar buildMenuBar() {
        return newResourceMenuBuilder().addValidation(new Command() {
            @Override
            public void execute() {
                LoadingPopup.showMessage(CommonConstants.INSTANCE.WaitWhileValidating());
                service.call(new RemoteCallback<BuilderResult>() {
                    @Override
                    public void callback(BuilderResult response) {
                        final ShowBuilderErrorsWidget pop = new ShowBuilderErrorsWidget(response);
                        LoadingPopup.close();
                        pop.show();
                    }
                }).validate(path,
                        view.getContent());
            }
        }).addSave(new Command() {
            @Override
            public void execute() {
                onSave();
            }
        } ).addDelete( new Command() {
            @Override
            public void execute() {
                onDelete();
            }
        } ).addRename( new Command() {
            @Override
            public void execute() {
                onRename();
            }
        } ).addCopy( new Command() {
            @Override
            public void execute() {
                onCopy();
            }
        }).build();
    }

}
