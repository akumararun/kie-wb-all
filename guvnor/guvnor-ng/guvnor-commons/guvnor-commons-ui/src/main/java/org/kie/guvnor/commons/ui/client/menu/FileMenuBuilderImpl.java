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

package org.kie.guvnor.commons.ui.client.menu;

import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import com.google.gwt.core.client.Callback;
import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.api.Caller;
import org.kie.commons.data.Pair;
import org.kie.guvnor.commons.ui.client.callbacks.DefaultErrorCallback;
import org.kie.guvnor.commons.ui.client.popups.file.CommandWithCommitMessage;
import org.kie.guvnor.commons.ui.client.popups.file.CommandWithFileNameAndCommitMessage;
import org.kie.guvnor.commons.ui.client.popups.file.CopyPopup;
import org.kie.guvnor.commons.ui.client.popups.file.DeletePopup;
import org.kie.guvnor.commons.ui.client.popups.file.FileNameAndCommitMessage;
import org.kie.guvnor.commons.ui.client.popups.file.RenamePopup;
import org.kie.guvnor.commons.ui.client.resources.i18n.CommonConstants;
import org.kie.guvnor.services.file.CopyService;
import org.kie.guvnor.services.file.DeleteService;
import org.kie.guvnor.services.file.RenameService;
import org.uberfire.backend.vfs.Path;
import org.uberfire.client.mvp.Command;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.workbench.widgets.events.NotificationEvent;
import org.uberfire.client.workbench.widgets.menu.MenuFactory;
import org.uberfire.client.workbench.widgets.menu.MenuItem;
import org.uberfire.client.workbench.widgets.menu.Menus;
import org.uberfire.shared.mvp.impl.PathPlaceRequest;

import static org.uberfire.client.workbench.widgets.menu.MenuFactory.*;

/**
 *
 */
@Dependent
public class FileMenuBuilderImpl
        implements FileMenuBuilder {

    @Inject
    private RestoreVersionCommandProvider restoreVersionCommandProvider;

    @Inject
    private Caller<DeleteService> deleteService;

    @Inject
    private Caller<RenameService> renameService;

    @Inject
    private Caller<CopyService> copyService;

    @Inject
    private Event<NotificationEvent> notification;

    @Inject
    private PlaceManager placeManager;

    private Command saveCommand = null;
    private Command deleteCommand = null;
    private Command renameCommand = null;
    private Command copyCommand = null;
    private Command restoreCommand = null;
    private List<Pair<String, Command>> otherCommands = new ArrayList<Pair<String, Command>>();

    @Override
    public FileMenuBuilder addSave( final Command command ) {
        this.saveCommand = command;
        return this;
    }

    @Override
    public FileMenuBuilder addDelete( final Command command ) {
        this.deleteCommand = command;
        return this;
    }

    @Override
    public FileMenuBuilder addDelete( final Path path ) {
        addDelete( path, new Callback<Void, Void>() {
            @Override
            public void onFailure( Void reason ) {

            }

            @Override
            public void onSuccess( Void result ) {

            }
        } );

        return this;
    }

    @Override
    public FileMenuBuilder addDelete( final Path path,
                                      final Callback<Void, Void> callback ) {
        this.deleteCommand = new Command() {
            @Override
            public void execute() {
                final DeletePopup popup = new DeletePopup( new CommandWithCommitMessage() {
                    @Override
                    public void execute( final String comment ) {
                        deleteService.call( getDeleteSuccessCallback( path,
                                                                      callback ),
                                            new DefaultErrorCallback() ).delete( path,
                                                                                 comment );
                    }
                } );

                popup.show();
            }
        };
        return this;
    }

    private RemoteCallback<Void> getDeleteSuccessCallback( final Path path,
                                                           final Callback<Void, Void> callback ) {
        return new RemoteCallback<Void>() {

            @Override
            public void callback( final Void response ) {
                notification.fire( new NotificationEvent( CommonConstants.INSTANCE.ItemDeletedSuccessfully() ) );
                placeManager.closePlace( new PathPlaceRequest( path ) );
                callback.onSuccess( null );
            }
        };
    }

    @Override
    public FileMenuBuilder addRename( final Command command ) {
        this.renameCommand = command;
        return this;
    }

    @Override
    public FileMenuBuilder addRename( final Path path ) {
        addRename( path, new Callback<Path, Void>() {
            @Override
            public void onFailure( Void reason ) {

            }

            @Override
            public void onSuccess( Path result ) {

            }
        } );

        return this;
    }

    @Override
    public FileMenuBuilder addRename( final Path path,
                                      final Callback<Path, Void> callback ) {
        this.renameCommand = new Command() {
            @Override
            public void execute() {
                final RenamePopup popup = new RenamePopup( new CommandWithFileNameAndCommitMessage() {
                    @Override
                    public void execute( final FileNameAndCommitMessage details ) {
                        renameService.call( getRenameSuccessCallback( callback ),
                                            new DefaultErrorCallback() ).rename( path,
                                                                                 details.getNewFileName(),
                                                                                 details.getCommitMessage() );
                    }
                } );

                popup.show();
            }
        };

        return this;
    }

    private RemoteCallback<Path> getRenameSuccessCallback( final Callback<Path, Void> callback ) {
        return new RemoteCallback<Path>() {

            @Override
            public void callback( final Path path ) {
                notification.fire( new NotificationEvent( CommonConstants.INSTANCE.ItemRenamedSuccessfully() ) );
                callback.onSuccess( path );
            }
        };
    }

    @Override
    public FileMenuBuilder addCopy( final Command command ) {
        this.copyCommand = command;
        return this;
    }

    @Override
    public FileMenuBuilder addCopy( final Path path ) {
        addCopy( path, new Callback<Path, Void>() {
            @Override
            public void onFailure( Void reason ) {

            }

            @Override
            public void onSuccess( Path result ) {

            }
        } );

        return this;
    }

    @Override
    public FileMenuBuilder addCopy( final Path path,
                                    final Callback<Path, Void> callback ) {
        this.copyCommand = new Command() {
            @Override
            public void execute() {
                final CopyPopup popup = new CopyPopup( new CommandWithFileNameAndCommitMessage() {
                    @Override
                    public void execute( final FileNameAndCommitMessage details ) {
                        copyService.call( getCopySuccessCallback( callback ),
                                          new DefaultErrorCallback() ).copy( path,
                                                                             details.getNewFileName(),
                                                                             details.getCommitMessage() );
                    }
                } );
                popup.show();
            }
        };

        return this;
    }

    private RemoteCallback<Path> getCopySuccessCallback( final Callback<Path, Void> callback ) {
        return new RemoteCallback<Path>() {

            @Override
            public void callback( final Path path ) {
                notification.fire( new NotificationEvent( CommonConstants.INSTANCE.ItemCopiedSuccessfully() ) );
                callback.onSuccess( path );
            }
        };
    }

    @Override
    public FileMenuBuilder addRestoreVersion( final Path path ) {
        this.restoreCommand = restoreVersionCommandProvider.getCommand( path );
        return this;
    }

    @Override
    public FileMenuBuilder addCommand( final String caption,
                                       final Command command ) {
        this.otherCommands.add( new Pair<String, Command>( caption,
                                                           command ) );
        return this;
    }

    @Override
    public Menus build() {
        return MenuFactory
                .newTopLevelMenu( CommonConstants.INSTANCE.File() )
                .withItems( getItems() )
                .endMenu().build();
    }

    private List<MenuItem> getItems() {
        final List<MenuItem> menuItems = new ArrayList<MenuItem>();
        if ( saveCommand != null ) {
            menuItems.add( newSimpleItem( CommonConstants.INSTANCE.Save() )
                                   .respondsWith( saveCommand )
                                   .endMenu().build().getItems().get( 0 ) );
        }

        if ( deleteCommand != null ) {
            menuItems.add( newSimpleItem( CommonConstants.INSTANCE.Delete() )
                                   .respondsWith( deleteCommand )
                                   .endMenu().build().getItems().get( 0 ) );
        }

        if ( renameCommand != null ) {
            menuItems.add( newSimpleItem( CommonConstants.INSTANCE.Rename() )
                                   .respondsWith( renameCommand )
                                   .endMenu().build().getItems().get( 0 ) );
        }

        if ( copyCommand != null ) {
            menuItems.add( newSimpleItem( CommonConstants.INSTANCE.Copy() )
                                   .respondsWith( copyCommand )
                                   .endMenu().build().getItems().get( 0 ) );
        }

        if ( restoreCommand != null ) {
            menuItems.add( newSimpleItem( CommonConstants.INSTANCE.Restore() )
                                   .respondsWith( restoreCommand )
                                   .endMenu().build().getItems().get( 0 ) );
        }

        for ( Pair<String, Command> other : otherCommands ) {
            menuItems.add( newSimpleItem( other.getK1() )
                                   .respondsWith( other.getK2() )
                                   .endMenu().build().getItems().get( 0 ) );
        }

        return menuItems;
    }
}
