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

package org.kie.guvnor.dsltext.client.editor;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.inject.New;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.api.Caller;
import org.kie.guvnor.commons.service.validation.model.BuilderResult;
import org.kie.guvnor.commons.ui.client.handlers.CopyPopup;
import org.kie.guvnor.commons.ui.client.handlers.DeletePopup;
import org.kie.guvnor.commons.ui.client.handlers.RenameCommand;
import org.kie.guvnor.commons.ui.client.handlers.RenamePopup;
import org.kie.guvnor.commons.ui.client.menu.ResourceMenuBuilder;
import org.kie.guvnor.commons.ui.client.resources.i18n.CommonConstants;
import org.kie.guvnor.commons.ui.client.save.CommandWithCommitMessage;
import org.kie.guvnor.commons.ui.client.save.SaveOperationService;
import org.kie.guvnor.dsltext.client.resources.i18n.DSLTextEditorConstants;
import org.kie.guvnor.dsltext.service.DSLTextEditorService;
import org.kie.guvnor.errors.client.widget.ShowBuilderErrorsWidget;
import org.kie.guvnor.metadata.client.resources.i18n.MetadataConstants;
import org.kie.guvnor.metadata.client.widget.MetadataWidget;
import org.kie.guvnor.services.metadata.MetadataService;
import org.kie.guvnor.services.metadata.model.Metadata;
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
import org.uberfire.client.mvp.Command;
import org.uberfire.client.workbench.widgets.events.NotificationEvent;
import org.uberfire.client.workbench.widgets.menu.MenuBar;

import static org.kie.guvnor.commons.ui.client.menu.ResourceMenuBuilder.*;

/**
 * A text based editor for Domain Specific Language definitions
 */
@Dependent
@WorkbenchEditor(identifier = "DSLEditor", fileTypes = "*.dsl")
public class DSLEditorPresenter {

    public interface View
            extends
            IsWidget {

        void setContent( final String content );

        String getContent();

        boolean isDirty();

        void setNotDirty();

        boolean confirmClose();
    }

    @Inject
    private View view;

    @Inject
    private Caller<DSLTextEditorService> dslTextEditorService;

    @Inject
    private Caller<MetadataService> metadataService;

    @Inject
    private Event<NotificationEvent> notification;

    @Inject @New
    private MultiPageEditor multiPageEditor;

    @Inject @New
    private ResourceMenuBuilder menuBuilder;

    private final MetadataWidget metadataWidget = new MetadataWidget();

    private Path path;

    @OnStart
    public void onStart( final Path path ) {
        this.path = path;

        dslTextEditorService.call( new RemoteCallback<String>() {
            @Override
            public void callback( String response ) {
                if ( response == null || response.isEmpty() ) {
                    view.setContent( null );
                } else {
                    view.setContent( response );

                }
            }
        } ).load(path);

        metadataService.call( new RemoteCallback<Metadata>() {
            @Override
            public void callback( final Metadata metadata ) {
                metadataWidget.setContent( metadata, false );
            }
        } ).getMetadata( path );
    }

    @OnSave
    public void onSave() {
        new SaveOperationService().save(path, new CommandWithCommitMessage() {
            @Override
            public void execute(final String commitMessage) {
                dslTextEditorService.call(new RemoteCallback<Void>() {
                    @Override
                    public void callback(Void response) {
                        view.setNotDirty();
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
                dslTextEditorService.call(new RemoteCallback<Path>() {
                    @Override
                    public void callback(Path response) {
                        view.setNotDirty();
                        metadataWidget.resetDirty();
                        notification.fire(new NotificationEvent(CommonConstants.INSTANCE.ItemDeletedSuccessfully(), NotificationEvent.NotificationType.DEFAULT, NotificationEvent.RefreshType.REFRESH));
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
                dslTextEditorService.call(new RemoteCallback<Path>() {
                    @Override
                    public void callback(Path response) {
                        view.setNotDirty();
                        metadataWidget.resetDirty();
                        notification.fire(new NotificationEvent(CommonConstants.INSTANCE.ItemRenamedSuccessfully(), NotificationEvent.NotificationType.DEFAULT, NotificationEvent.RefreshType.REFRESH));
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
                dslTextEditorService.call(new RemoteCallback<Path>() {
                    @Override
                    public void callback(Path response) {
                        view.setNotDirty();
                        metadataWidget.resetDirty();
                        notification.fire(new NotificationEvent(CommonConstants.INSTANCE.ItemCopiedSuccessfully(), NotificationEvent.NotificationType.DEFAULT, NotificationEvent.RefreshType.REFRESH));
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
        if ( isDirty() ) {
            return view.confirmClose();
        }
        return true;
    }

    @WorkbenchPartTitle
    public String getTitle() {
        return "DSL Editor [" + path.getFileName() + "]";
    }

    @WorkbenchPartView
    public IsWidget getWidget() {
        multiPageEditor.addWidget(view, DSLTextEditorConstants.INSTANCE.Edit());
        multiPageEditor.addWidget(metadataWidget, MetadataConstants.INSTANCE.Metadata());
        return multiPageEditor;
    }

    @WorkbenchMenu
    public MenuBar buildMenuBar() {
        return menuBuilder.addFileMenu().addValidation( new Command() {
            @Override
            public void execute() {
                LoadingPopup.showMessage( CommonConstants.INSTANCE.WaitWhileValidating() );
                dslTextEditorService.call( new RemoteCallback<BuilderResult>() {
                    @Override
                    public void callback( BuilderResult response ) {
                        final ShowBuilderErrorsWidget pop = new ShowBuilderErrorsWidget( response );
                        LoadingPopup.close();
                        pop.show();
                    }
                } ).validate( path,
                              view.getContent() );
            }
        } ).addSave( new Command() {
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
        } ).build();
    }

}
