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

package org.kie.guvnor.categories.client;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.api.Caller;
import org.kie.guvnor.categories.client.type.CategoryDefinitionResourceType;
import org.kie.guvnor.commons.ui.client.callbacks.DefaultErrorCallback;
import org.kie.guvnor.commons.ui.client.menu.FileMenuBuilder;
import org.kie.guvnor.commons.ui.client.popups.file.CommandWithCommitMessage;
import org.kie.guvnor.commons.ui.client.popups.file.SaveOperationService;
import org.kie.guvnor.commons.ui.client.resources.i18n.CommonConstants;
import org.kie.guvnor.services.metadata.CategoriesService;
import org.kie.guvnor.services.metadata.model.Categories;
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
import org.uberfire.client.mvp.Command;
import org.uberfire.client.workbench.widgets.menu.Menus;

/**
 *
 */
@Dependent
@WorkbenchEditor(identifier = "CategoryManager", supportedTypes = { CategoryDefinitionResourceType.class })
public class CategoriesEditorPresenter {

    @Inject
    private CategoriesEditorView view;

    @Inject
    private Caller<CategoriesService> categoryService;

    @Inject
    private FileMenuBuilder menuBuilder;

    private Path path;

    private Menus menus;

    @OnStart
    public void onStart( final Path path ) {
        this.path = path;
        makeMenuBar();

        view.showBusyIndicator( CommonConstants.INSTANCE.Loading() );
        categoryService.call( getModelSuccessCallback(),
                              new DefaultErrorCallback() ).getContent( path );
    }

    private void makeMenuBar() {
        menus = menuBuilder.addSave( new Command() {
            @Override
            public void execute() {
                onSave();
            }
        } ).build();
    }

    private RemoteCallback<Categories> getModelSuccessCallback() {
        return new RemoteCallback<Categories>() {

            @Override
            public void callback( final Categories content ) {
                view.setContent( content );
                view.hideBusyIndicator();
            }
        };
    }

    @OnSave
    public void onSave() {
        new SaveOperationService().save( path,
                                         new CommandWithCommitMessage() {
                                             @Override
                                             public void execute( final String commitMessage ) {
                                                 view.showBusyIndicator( CommonConstants.INSTANCE.Saving() );
                                                 categoryService.call( new RemoteCallback<Path>() {
                                                     @Override
                                                     public void callback( final Path response ) {
                                                         view.setNotDirty();
                                                         view.hideBusyIndicator();
                                                     }
                                                 } ).save( path,
                                                           view.getContent() );
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
        return "Categories Editor [" + path.getFileName() + "]";
    }

    @WorkbenchPartView
    public IsWidget getWidget() {
        return view;
    }

    @WorkbenchMenu
    public Menus getMenus() {
        return menus;
    }

}
