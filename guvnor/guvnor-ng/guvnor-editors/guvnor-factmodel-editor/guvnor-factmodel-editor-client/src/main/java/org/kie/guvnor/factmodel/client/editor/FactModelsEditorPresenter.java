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

package org.kie.guvnor.factmodel.client.editor;

import java.util.List;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.New;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.api.Caller;
import org.kie.guvnor.commons.ui.client.menu.FileMenuBuilder;
import org.kie.guvnor.commons.ui.client.resources.i18n.CommonConstants;
import org.kie.guvnor.commons.ui.client.popups.file.CommandWithCommitMessage;
import org.kie.guvnor.commons.ui.client.popups.file.SaveOperationService;
import org.kie.guvnor.configresource.client.widget.bound.ImportsWidgetPresenter;
import org.kie.guvnor.datamodel.oracle.DataModelOracle;
import org.kie.guvnor.factmodel.client.type.FactModelResourceType;
import org.kie.guvnor.factmodel.model.FactMetaModel;
import org.kie.guvnor.factmodel.model.FactModelContent;
import org.kie.guvnor.factmodel.model.FactModels;
import org.kie.guvnor.factmodel.service.FactModelService;
import org.kie.guvnor.metadata.client.widget.MetadataWidget;
import org.kie.guvnor.services.metadata.MetadataService;
import org.kie.guvnor.services.metadata.model.Metadata;
import org.kie.guvnor.services.version.events.RestoreEvent;
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
import org.uberfire.client.common.MultiPageEditor;
import org.uberfire.client.common.Page;
import org.uberfire.client.mvp.Command;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.workbench.widgets.events.NotificationEvent;
import org.uberfire.client.workbench.widgets.menu.Menus;
import org.uberfire.shared.mvp.PlaceRequest;

@Dependent
@WorkbenchEditor(identifier = "FactModelsEditor", supportedTypes = { FactModelResourceType.class }, priority = 100)
public class FactModelsEditorPresenter {

    @Inject
    private ImportsWidgetPresenter importsWidget;

    @Inject
    private Caller<FactModelService> factModelService;

    @Inject
    private Caller<MetadataService> metadataService;

    @Inject
    private FactModelsEditorView view;

    @Inject
    private ViewSourceView viewSource;

    private final MetadataWidget metadataWidget = new MetadataWidget();

    @Inject
    private MultiPageEditor multiPage;

    @Inject
    private Event<NotificationEvent> notification;

    @Inject
    private PlaceManager placeManager;

    @Inject
    @New
    private FileMenuBuilder menuBuilder;
    private Menus menus;

    private Path path;
    private PlaceRequest place;
    private boolean isReadOnly;

    private FactModels model;
    private DataModelOracle oracle;
    private List<FactMetaModel> superTypes;

    @OnStart
    public void onStart( final Path path,
                         final PlaceRequest place ) {
        this.path = path;
        this.place = place;
        this.isReadOnly = place.getParameter( "readOnly", null ) == null ? false : true;
        makeMenuBar();

        multiPage.addWidget( view,
                             CommonConstants.INSTANCE.EditTabTitle() );

        multiPage.addPage( new Page( viewSource,
                                     CommonConstants.INSTANCE.SourceTabTitle() ) {
            @Override
            public void onFocus() {
                factModelService.call( new RemoteCallback<String>() {
                    @Override
                    public void callback( final String response ) {
                        viewSource.setContent( response );
                    }
                } ).toSource( path, view.getContent() );
            }

            @Override
            public void onLostFocus() {
                viewSource.clear();
            }
        } );

        multiPage.addWidget( importsWidget,
                             CommonConstants.INSTANCE.ConfigTabTitle() );

        multiPage.addWidget( metadataWidget,
                             CommonConstants.INSTANCE.MetadataTabTitle() );

        loadContent();
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

    private void loadContent() {
        factModelService.call( new RemoteCallback<FactModelContent>() {
            @Override
            public void callback( final FactModelContent content ) {
                model = content.getFactModels();
                oracle = content.getDataModel();
                oracle.filter( model.getImports() );
                superTypes = content.getSuperTypes();

                final ModelNameHelper modelNameHelper = new ModelNameHelper();

                for ( final FactMetaModel currentModel : superTypes ) {
                    modelNameHelper.getTypeDescriptions().put( currentModel.getName(),
                                                               currentModel.getName() );
                }

                view.setContent( model,
                                 superTypes,
                                 modelNameHelper );

                importsWidget.setContent( oracle,
                                          model.getImports(),
                                          isReadOnly );
            }
        } ).loadContent( path );

        metadataService.call( new RemoteCallback<Metadata>() {
            @Override
            public void callback( final Metadata metadata ) {
                metadataWidget.setContent( metadata,
                                           isReadOnly );
            }
        } ).getMetadata( path );
    }

    @OnSave
    public void onSave() {
        if ( isReadOnly ) {
            view.alertReadOnly();
            return;
        }

        new SaveOperationService().save( path, new CommandWithCommitMessage() {
            @Override
            public void execute( final String comment ) {
                factModelService.call( new RemoteCallback<Path>() {
                    @Override
                    public void callback( final Path response ) {
                        view.setNotDirty();
                        metadataWidget.resetDirty();
                        notification.fire( new NotificationEvent( CommonConstants.INSTANCE.ItemSavedSuccessfully() ) );
                    }
                } ).save( path,
                          view.getContent(),
                          metadataWidget.getContent(),
                          comment );
            }
        } );
    }

    @WorkbenchPartView
    public IsWidget getWidget() {
        return multiPage;
    }

    @OnClose
    public void onClose() {
        this.path = null;
    }

    @IsDirty
    public boolean isDirty() {
        if ( isReadOnly ) {
            return false;
        }
        return ( view.isDirty() || importsWidget.isDirty() || metadataWidget.isDirty() );
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
        if ( isReadOnly ) {
            return "Read Only Fact Models Viewer [" + path.getFileName() + "]";
        }
        return "Fact Models Editor [" + path.getFileName() + "]";
    }

    @WorkbenchMenu
    public Menus getMenus() {
        return menus;
    }

    public void onRestore( @Observes RestoreEvent restore ) {
        if ( path == null || restore == null || restore.getPath() == null ) {
            return;
        }
        if ( path.equals( restore.getPath() ) ) {
            loadContent();
            notification.fire( new NotificationEvent( CommonConstants.INSTANCE.ItemRestored() ) );
        }
    }

}
