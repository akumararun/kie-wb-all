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

package org.kie.guvnor.drltext.client.editor;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.inject.New;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.api.Caller;
import org.kie.guvnor.commons.ui.client.menu.FileMenuBuilder;
import org.kie.guvnor.commons.ui.client.resources.i18n.CommonConstants;
import org.kie.guvnor.commons.ui.client.popups.file.CommandWithCommitMessage;
import org.kie.guvnor.commons.ui.client.popups.file.SaveOperationService;
import org.kie.guvnor.datamodel.oracle.DataModelOracle;
import org.kie.guvnor.datamodel.service.DataModelService;
import org.kie.guvnor.drltext.client.resources.i18n.DRLTextEditorConstants;
import org.kie.guvnor.drltext.client.type.DRLResourceType;
import org.kie.guvnor.drltext.service.DRLTextEditorService;
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
import org.uberfire.client.common.MultiPageEditor;
import org.uberfire.client.common.Page;
import org.uberfire.client.mvp.Command;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.workbench.widgets.events.NotificationEvent;
import org.uberfire.client.workbench.widgets.menu.Menus;
import org.uberfire.shared.mvp.PlaceRequest;

/**
 * This is the default rule editor widget (just text editor based).
 */
@Dependent
@WorkbenchEditor(identifier = "DRLEditor", supportedTypes = { DRLResourceType.class })
public class DRLEditorPresenter {

    @Inject
    private Caller<DRLTextEditorService> drlTextEditorService;

    @Inject
    private Caller<DataModelService> dataModelService;

    @Inject
    private Caller<MetadataService> metadataService;

    @Inject
    private Event<NotificationEvent> notification;

    @Inject
    private PlaceManager placeManager;

    @Inject
    private DRLEditorView view;

    private final MetadataWidget metadataWidget = new MetadataWidget();

    @Inject
    private MultiPageEditor multiPage;

    @Inject
    @New
    private FileMenuBuilder menuBuilder;
    private Menus menus;

    private Path path;
    private PlaceRequest place;
    private boolean isReadOnly;

    @OnStart
    public void onStart( final Path path,
                         final PlaceRequest place ) {
        this.path = path;
        this.place = place;
        this.isReadOnly = place.getParameter( "readOnly", null ) == null ? false : true;
        makeMenuBar();

        multiPage.addWidget( view, DRLTextEditorConstants.INSTANCE.DRL() );

        dataModelService.call( new RemoteCallback<DataModelOracle>() {
            @Override
            public void callback( final DataModelOracle model ) {
                drlTextEditorService.call( new RemoteCallback<String>() {
                    @Override
                    public void callback( String response ) {
                        if ( response == null || response.isEmpty() ) {
                            view.setContent( null,
                                             model );
                        } else {
                            view.setContent( response,
                                             model );
                        }
                    }
                } ).load( path );
            }
        } ).getDataModel( path );

        multiPage.addPage( new Page( metadataWidget,
                                     MetadataConstants.INSTANCE.Metadata() ) {
            @Override
            public void onFocus() {
                metadataService.call( new RemoteCallback<Metadata>() {
                    @Override
                    public void callback( final Metadata metadata ) {
                        metadataWidget.setContent( metadata,
                                                   isReadOnly );
                    }
                } ).getMetadata( path );
            }

            @Override
            public void onLostFocus() {
                // Nothing to do here
            }
        } );
    }

    private void makeMenuBar() {
        if ( isReadOnly ) {
            menus = menuBuilder.addRestoreVersion( path ).build();
        } else {
            menus = menuBuilder
                    .addSave( new Command() {
                        @Override
                        public void execute() {
                            onSave();
                        }
                    } )
                    .addCopy( path )
                    .addRename( path )
                    .addDelete( path )
                    .build();
        }
    }

    @OnSave
    public void onSave() {
        new SaveOperationService().save( path, new CommandWithCommitMessage() {
            @Override
            public void execute( final String commitMessage ) {
                drlTextEditorService.call( new RemoteCallback<Path>() {
                    @Override
                    public void callback( final Path response ) {
                        view.setNotDirty();
                        metadataWidget.resetDirty();
                        notification.fire( new NotificationEvent( CommonConstants.INSTANCE.ItemSavedSuccessfully() ) );
                    }
                } ).save( path,
                          view.getContent(),
                          metadataWidget.getContent(),
                          commitMessage );
            }
        } );
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
        return "DRL Editor [" + path.getFileName() + "]";
    }

    @WorkbenchPartView
    public IsWidget getWidget() {
        return multiPage;
    }

    @WorkbenchMenu
    public Menus getMenus() {
        return menus;
    }

}
