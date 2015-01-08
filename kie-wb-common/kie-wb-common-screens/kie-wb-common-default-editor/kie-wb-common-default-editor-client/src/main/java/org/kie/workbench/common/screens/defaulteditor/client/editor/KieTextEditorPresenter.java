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
package org.kie.workbench.common.screens.defaulteditor.client.editor;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import org.guvnor.common.services.shared.metadata.model.Metadata;
import org.guvnor.common.services.shared.metadata.model.Overview;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.kie.workbench.common.screens.defaulteditor.service.DefaultEditorService;
import org.kie.workbench.common.widgets.client.resources.i18n.CommonConstants;
import org.kie.workbench.common.widgets.metadata.client.KieEditor;
import org.uberfire.backend.vfs.ObservablePath;
import org.uberfire.client.annotations.WorkbenchMenu;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartTitleDecoration;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.ext.editor.commons.client.file.SaveOperationService;
import org.uberfire.ext.editor.commons.client.validation.DefaultFileNameValidator;
import org.uberfire.ext.widgets.common.client.callbacks.HasBusyIndicatorDefaultErrorCallback;
import org.uberfire.ext.widgets.common.client.common.BusyIndicatorView;
import org.uberfire.ext.widgets.core.client.editors.texteditor.TextResourceType;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.ParameterizedCommand;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.workbench.events.NotificationEvent;
import org.uberfire.workbench.model.menu.Menus;

public class KieTextEditorPresenter
        extends KieEditor {

    protected KieTextEditorView view;

    @Inject
    private Caller<DefaultEditorService> defaultEditorService;

    @Inject
    private Event<NotificationEvent> notification;

    @Inject
    private BusyIndicatorView busyIndicatorView;

    @Inject
    private DefaultFileNameValidator fileNameValidator;

    @Inject
    private PlaceManager placeManager;

    private Metadata metadata;

    @Inject
    public KieTextEditorPresenter( final KieTextEditorView baseView ) {
        super( baseView );
        view = baseView;
    }

    public void onStartup( final ObservablePath path,
                           final PlaceRequest place ) {
        super.init( path,
                    place,
                    new TextResourceType() );

        view.onStartup( path );

        if ( isReadOnly ) {
            view.makeReadOnly();
        }
    }

    protected void makeMenuBar() {
        menus = menuBuilder
                .addSave(
                        new Command() {
                            @Override
                            public void execute() {
                                onSave();
                            }
                        } )
                .addCopy( versionRecordManager.getCurrentPath(),
                          fileNameValidator )
                .addRename( versionRecordManager.getCurrentPath(),
                            fileNameValidator )
                .addDelete( versionRecordManager.getCurrentPath() )
                .addNewTopLevelMenu( versionRecordManager.buildMenu() )
                .build();
    }

    @Override
    protected Command onValidate() {
        // not used
        return null;
    }

    @Override
    protected void loadContent() {
        defaultEditorService.call( new RemoteCallback<Overview>() {
            @Override
            public void callback( Overview overview ) {

                resetEditorPages( overview );

                metadata = overview.getMetadata();

                createOriginalHash(view.getContent());
            }
        } ).loadOverview( versionRecordManager.getCurrentPath() );
    }

    @Override
    protected void save() {
        busyIndicatorView.showBusyIndicator( CommonConstants.INSTANCE.Saving() );
        new SaveOperationService().save( versionRecordManager.getCurrentPath(),
                                         new ParameterizedCommand<String>() {
                                             @Override
                                             public void execute( final String commitMessage ) {
                                                 defaultEditorService.call( getSaveSuccessCallback(view.getContent().hashCode()),
                                                                            new HasBusyIndicatorDefaultErrorCallback( busyIndicatorView ) ).save( versionRecordManager.getCurrentPath(),
                                                                                                                                                  view.getContent(),
                                                                                                                                                  metadata,
                                                                                                                                                  commitMessage );

                                             }
                                         }
                                       );

    }

    @WorkbenchMenu
    public Menus getMenus() {
        return menus;
    }

    @WorkbenchPartTitle
    public String getTitleText() {
        return super.getTitleText();
    }

    @WorkbenchPartTitleDecoration
    public IsWidget getTitle() {
        return super.getTitle();
    }

    public IsWidget getWidget() {
        return super.getWidget();
    }

}