/*
 * Copyright 2013 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.kie.workbench.common.screens.explorer.client.widgets;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;

import com.google.gwt.user.client.Window;
import org.guvnor.asset.management.social.AssetManagementEventTypes;
import org.guvnor.common.services.project.builder.model.BuildResults;
import org.guvnor.common.services.project.builder.service.BuildService;
import org.guvnor.common.services.project.context.ProjectContextChangeEvent;
import org.guvnor.common.services.project.events.DeleteProjectEvent;
import org.guvnor.common.services.project.events.NewPackageEvent;
import org.guvnor.common.services.project.events.NewProjectEvent;
import org.guvnor.common.services.project.events.RenameProjectEvent;
import org.guvnor.common.services.project.model.Package;
import org.guvnor.common.services.project.model.Project;
import org.guvnor.common.services.project.social.ProjectEventType;
import org.guvnor.structure.config.SystemRepositoryChangedEvent;
import org.guvnor.structure.organizationalunit.NewOrganizationalUnitEvent;
import org.guvnor.structure.organizationalunit.OrganizationalUnit;
import org.guvnor.structure.organizationalunit.RemoveOrganizationalUnitEvent;
import org.guvnor.structure.organizationalunit.RepoAddedToOrganizationaUnitEvent;
import org.guvnor.structure.organizationalunit.RepoRemovedFromOrganizationalUnitEvent;
import org.guvnor.structure.repositories.NewBranchEvent;
import org.guvnor.structure.repositories.Repository;
import org.guvnor.structure.repositories.RepositoryRemovedEvent;
import org.guvnor.structure.repositories.RepositoryUpdatedEvent;
import org.guvnor.structure.repositories.impl.git.GitRepository;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.security.shared.api.identity.User;
import org.kie.uberfire.social.activities.model.ExtendedTypes;
import org.kie.uberfire.social.activities.model.SocialFileSelectedEvent;
import org.kie.workbench.common.screens.explorer.client.utils.Utils;
import org.kie.workbench.common.screens.explorer.client.widgets.navigator.Explorer;
import org.kie.workbench.common.screens.explorer.client.widgets.tagSelector.TagChangedEvent;
import org.kie.workbench.common.screens.explorer.model.FolderItem;
import org.kie.workbench.common.screens.explorer.model.FolderItemType;
import org.kie.workbench.common.screens.explorer.model.FolderListing;
import org.kie.workbench.common.screens.explorer.model.ProjectExplorerContent;
import org.kie.workbench.common.screens.explorer.model.URIStructureExplorerModel;
import org.kie.workbench.common.screens.explorer.service.ExplorerService;
import org.kie.workbench.common.screens.explorer.service.Option;
import org.kie.workbench.common.screens.explorer.service.ProjectExplorerContentQuery;
import org.kie.workbench.common.services.shared.validation.ValidationService;
import org.kie.workbench.common.widgets.client.resources.i18n.CommonConstants;
import org.uberfire.backend.vfs.Path;
import org.uberfire.backend.vfs.VFSService;
import org.uberfire.backend.vfs.impl.LockInfo;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.ext.editor.commons.client.file.CommandWithFileNameAndCommitMessage;
import org.uberfire.ext.editor.commons.client.file.FileNameAndCommitMessage;
import org.uberfire.ext.editor.commons.client.validation.Validator;
import org.uberfire.ext.editor.commons.client.validation.ValidatorCallback;
import org.uberfire.ext.widgets.common.client.callbacks.DefaultErrorCallback;
import org.uberfire.ext.widgets.common.client.callbacks.HasBusyIndicatorDefaultErrorCallback;
import org.uberfire.mvp.ParameterizedCommand;
import org.uberfire.rpc.SessionInfo;
import org.uberfire.security.impl.authz.RuntimeAuthorizationManager;
import org.uberfire.workbench.events.NotificationEvent;
import org.uberfire.workbench.events.ResourceAddedEvent;
import org.uberfire.workbench.events.ResourceBatchChangesEvent;
import org.uberfire.workbench.events.ResourceCopiedEvent;
import org.uberfire.workbench.events.ResourceDeletedEvent;
import org.uberfire.workbench.events.ResourceRenamedEvent;
import org.uberfire.workbench.events.ResourceUpdatedEvent;

public abstract class BaseViewPresenter implements ViewPresenter {

    protected User identity;
    protected RuntimeAuthorizationManager authorizationManager;
    protected Caller<ExplorerService> explorerService;
    protected Caller<BuildService> buildService;
    protected Caller<VFSService> vfsService;
    protected Caller<ValidationService> validationService;
    protected PlaceManager placeManager;
    protected Event<BuildResults> buildResultsEvent;
    protected Event<ProjectContextChangeEvent> contextChangedEvent;

    private Event<NotificationEvent> notification;
    private transient SessionInfo sessionInfo;

    //Active context
    protected OrganizationalUnit activeOrganizationalUnit = null;
    protected Repository activeRepository = null;
    protected Project activeProject = null;
    protected FolderItem activeFolderItem = null;
    protected Package activePackage = null;
    protected FolderListing activeContent = null;
    private boolean isOnLoading = false;
    private Set<Repository> repositories;

    protected Set<String> activeContentTags = new TreeSet<String>( );
    protected String currentTag = null;

    public BaseViewPresenter( final User identity,
                              final RuntimeAuthorizationManager authorizationManager,
                              final Caller<ExplorerService> explorerService,
                              final Caller<BuildService> buildService,
                              final Caller<VFSService> vfsService,
                              final Caller<ValidationService> validationService,
                              final PlaceManager placeManager,
                              final Event<BuildResults> buildResultsEvent,
                              final Event<ProjectContextChangeEvent> contextChangedEvent,
                              final Event<NotificationEvent> notification,
                              final SessionInfo sessionInfo ) {
        this.identity = identity;
        this.authorizationManager = authorizationManager;
        this.explorerService = explorerService;
        this.buildService = buildService;
        this.vfsService = vfsService;
        this.validationService = validationService;
        this.placeManager = placeManager;
        this.buildResultsEvent = buildResultsEvent;
        this.contextChangedEvent = contextChangedEvent;
        this.notification = notification;
        this.sessionInfo = sessionInfo;
    }

    @PostConstruct
    public void init() {
        getView().init( this );
    }

    @Override
    public void update( final Set<Option> options ) {
        setOptions( new HashSet<Option>( options ) );
        getView().setOptions( options );

        if ( options.contains( Option.TREE_NAVIGATOR ) ) {
            getView().setNavType( Explorer.NavType.TREE );
        } else {
            getView().setNavType( Explorer.NavType.BREADCRUMB );
        }
        if ( options.contains( Option.NO_CONTEXT_NAVIGATION ) ) {
            getView().hideHeaderNavigator();
        }
        if (options.contains( Option.SHOW_TAG_FILTER )) {
            getView().showTagFilter();
            refresh(false);
        } else {
            getView().hideTagFilter();
            if (activeContent != null) {
                getView().setItems( activeContent );
            }
        }
    }

    protected abstract void setOptions( final Set<Option> options );

    protected abstract View getView();

    @Override
    public void initialiseViewForActiveContext( final String path ) {
        getView().showBusyIndicator( CommonConstants.INSTANCE.Loading() );

        explorerService.call( getContentCallback(),
                              new HasBusyIndicatorDefaultErrorCallback( getView() ) ).getContent( path,
                                                                                                  getActiveOptions() );
    }

    @Override
    public void initialiseViewForActiveContext( final OrganizationalUnit organizationalUnit ) {
        doInitialiseViewForActiveContext( new ProjectExplorerContentQuery( organizationalUnit ),
                                          true );
    }

    @Override
    public void initialiseViewForActiveContext( final OrganizationalUnit organizationalUnit,
                                                final Repository repository ) {
        doInitialiseViewForActiveContext( new ProjectExplorerContentQuery(
                                                  organizationalUnit,
                                                  repository ),
                                          true );
    }

    @Override
    public void initialiseViewForActiveContext( final OrganizationalUnit organizationalUnit,
                                                final Repository repository,
                                                final Project project ) {
        doInitialiseViewForActiveContext( new ProjectExplorerContentQuery(
                                                  organizationalUnit,
                                                  repository,
                                                  project ),
                                          true );
    }

    @Override
    public void initialiseViewForActiveContext( final OrganizationalUnit organizationalUnit,
                                                final Repository repository,
                                                final Project project,
                                                final Package pkg ) {
        doInitialiseViewForActiveContext( new ProjectExplorerContentQuery(
                                                  organizationalUnit,
                                                  repository,
                                                  project,
                                                  pkg ),
                                          true );
    }

    @Override
    public void refresh() {
        refresh( true );
    }

    @Override
    public void loadContent( final FolderItem item,
                             final Set<Option> options ) {
        explorerService.call( new RemoteCallback<FolderListing>() {
            @Override
            public void callback( FolderListing fl ) {
                getView().getExplorer().loadContent( fl );
            }
        } ).getFolderListing( activeOrganizationalUnit,
                              activeRepository,
                              activeProject,
                              item,
                              options );
    }

    @Override
    public FolderListing getActiveContent() {
        return activeContent;
    }

    @Override
    public void deleteItem( final FolderItem folderItem ) {
        getView().deleteItem( new ParameterizedCommand<String>() {
            @Override
            public void execute( final String comment ) {
                getView().showBusyIndicator( CommonConstants.INSTANCE.Deleting() );
                explorerService.call( new RemoteCallback<Object>() {
                                          @Override
                                          public void callback( Object o ) {
                                              notification.fire( new NotificationEvent( CommonConstants.INSTANCE.ItemDeletedSuccessfully() ) );
                                              refresh( false );
                                          }
                                      },
                                      new HasBusyIndicatorDefaultErrorCallback( getView() ) ).deleteItem( folderItem, comment );
            }
        } );
    }

    @Override
    public void renameItem( final FolderItem folderItem ) {
        final Path path = getFolderItemPath( folderItem );
        getView().renameItem( path,
                              new Validator() {
                                  @Override
                                  public void validate( final String value,
                                                        final ValidatorCallback callback ) {
                                      validationService.call( new RemoteCallback<Object>() {
                                          @Override
                                          public void callback( Object response ) {
                                              if ( Boolean.TRUE.equals( response ) ) {
                                                  callback.onSuccess();
                                              } else {
                                                  callback.onFailure();
                                              }
                                          }
                                      } ).isFileNameValid( path,
                                                           value );
                                  }
                              },
                              new CommandWithFileNameAndCommitMessage() {
                                  @Override
                                  public void execute( final FileNameAndCommitMessage details ) {
                                      getView().showBusyIndicator( CommonConstants.INSTANCE.Renaming() );
                                      explorerService.call(
                                              new RemoteCallback<Void>() {
                                                  @Override
                                                  public void callback( final Void o ) {
                                                      notification.fire( new NotificationEvent( CommonConstants.INSTANCE.ItemRenamedSuccessfully() ) );
                                                      getView().hideBusyIndicator();
                                                      refresh();
                                                  }
                                              },
                                              new HasBusyIndicatorDefaultErrorCallback( getView() )
                                                          ).renameItem( folderItem,
                                                                        details.getNewFileName(),
                                                                        details.getCommitMessage() );
                                  }
                              }
                            );
    }

    @Override
    public void copyItem( final FolderItem folderItem ) {
        final Path path = getFolderItemPath( folderItem );
        getView().copyItem( path,
                            new Validator() {
                                @Override
                                public void validate( final String value,
                                                      final ValidatorCallback callback ) {
                                    validationService.call( new RemoteCallback<Object>() {
                                        @Override
                                        public void callback( Object response ) {
                                            if ( Boolean.TRUE.equals( response ) ) {
                                                callback.onSuccess();
                                            } else {
                                                callback.onFailure();
                                            }
                                        }
                                    } ).isFileNameValid( path,
                                                         value );
                                }
                            },
                            new CommandWithFileNameAndCommitMessage() {
                                @Override
                                public void execute( final FileNameAndCommitMessage details ) {
                                    getView().showBusyIndicator( CommonConstants.INSTANCE.Copying() );
                                    explorerService.call( new RemoteCallback<Void>() {
                                                              @Override
                                                              public void callback( final Void o ) {
                                                                  notification.fire( new NotificationEvent( CommonConstants.INSTANCE.ItemCopiedSuccessfully() ) );
                                                                  getView().hideBusyIndicator();
                                                                  refresh();
                                                              }
                                                          },
                                                          new HasBusyIndicatorDefaultErrorCallback( getView() ) ).copyItem( folderItem,
                                                                                                                            details.getNewFileName(),
                                                                                                                            details.getCommitMessage() );
                                }
                            }
                          );
    }

    @Override
    public void uploadArchivedFolder( final FolderItem folderItem ) {
        if ( folderItem.getItem() instanceof Path ) {
            final Path path = (Path) folderItem.getItem();

            Window.open( URLHelper.getDownloadUrl( path ),
                         "downloading",
                         "resizable=no,scrollbars=yes,status=no" );
        }
    }

    private Path getFolderItemPath( final FolderItem folderItem ) {
        if ( folderItem.getItem() instanceof Package ) {
            final Package pkg = ( (Package) folderItem.getItem() );
            return pkg.getPackageMainSrcPath();
        } else if ( folderItem.getItem() instanceof Path ) {
            return (Path) folderItem.getItem();
        }
        return null;
    }

    private void loadContent( final FolderListing content ) {
        if ( !activeContent.equals( content ) ) {
            setActiveContent( content );
            getView().getExplorer().loadContent( content, null );
        }
    }

    protected void setActiveContent(FolderListing activeContent) {
        this.activeContent = activeContent;
        resetTags( false );
    }

    protected void resetTags( boolean maintainSelection ) {
        if (!isFilterByTagEnabled())
            return;
        if (!maintainSelection) currentTag = null;
        activeContentTags.clear();
        for (FolderItem item : activeContent.getContent()) {
            if (item.getTags() != null) activeContentTags.addAll( item.getTags() );
        }
    }

    @Override
    public String getCurrentTag() {
        return currentTag;
    }

    @Override
    public Set<String> getActiveContentTags() {
        return activeContentTags;
    }

    private void refresh( boolean showLoadingIndicator ) {
        doInitialiseViewForActiveContext(
                new ProjectExplorerContentQuery(
                        activeOrganizationalUnit,
                        activeRepository,
                        activeProject,
                        activePackage,
                        activeFolderItem ),
                showLoadingIndicator );
    }

    private void doInitialiseViewForActiveContext( final ProjectExplorerContentQuery query,
                                                   final boolean showLoadingIndicator ) {
        if ( showLoadingIndicator ) {
            getView().showBusyIndicator( CommonConstants.INSTANCE.Loading() );
        }

        query.setOptions( getActiveOptions() );

        explorerService.call( getContentCallback(),
                              new HasBusyIndicatorDefaultErrorCallback( getView() ) ).getContent( query );
    }

    private RemoteCallback<ProjectExplorerContent> getContentCallback() {
        return new RemoteCallback<ProjectExplorerContent>() {
            @Override
            public void callback( final ProjectExplorerContent content ) {

                boolean signalChange = false;
                boolean buildSelectedProject = false;

                signalChange = setActiveOrganizationalUnit( content );
                if ( setActiveRepository( content ) ) {
                    signalChange = true;
                }

                if ( setActiveProject( content ) ) {
                    signalChange = true;
                    buildSelectedProject = true;
                }

                boolean folderChange = setActiveFolderAndPackage(
                        content );
                if ( signalChange || folderChange ) {
                    fireContextChangeEvent();
                }

                if ( buildSelectedProject ) {
                    buildProject( activeProject );
                }

                setActiveContent( content.getFolderListing() );

                getView().getExplorer().clear();
                repositories = content.getRepositories();
                getView().setContent( content.getOrganizationalUnits(),
                                      activeOrganizationalUnit,
                                      repositories,
                                      activeRepository,
                                      content.getProjects(),
                                      activeProject,
                                      activeContent,
                                      content.getSiblings() );

                if ( activeFolderItem == null ) {
                    setActiveFolderAndPackage( content );
                }
                getView().hideBusyIndicator();
            }

        };
    }

    private boolean setActiveProject( final ProjectExplorerContent content ) {
        if ( Utils.hasProjectChanged( content.getProject(),
                                      activeProject ) ) {
            activeProject = content.getProject();
            return true;
        } else {
            return false;
        }
    }

    private boolean setActiveFolderAndPackage( final ProjectExplorerContent content ) {
        if ( Utils.hasFolderItemChanged( content.getFolderListing().getItem(),
                                         activeFolderItem ) ) {

            activeFolderItem = content.getFolderListing().getItem();
            if ( activeFolderItem != null && activeFolderItem.getItem() != null && activeFolderItem.getItem() instanceof Package ) {
                activePackage = (Package) activeFolderItem.getItem();
            } else if ( activeFolderItem == null || activeFolderItem.getItem() == null ) {
                activePackage = null;
            }

            return true;
        } else {
            return false;
        }
    }

    private boolean setActiveRepository( final ProjectExplorerContent content ) {
        if ( Utils.hasRepositoryChanged( content.getRepository(),
                                         activeRepository ) ) {
            activeRepository = content.getRepository();
            return true;
        } else {
            return false;
        }
    }

    private boolean setActiveOrganizationalUnit( final ProjectExplorerContent content ) {

        if ( Utils.hasOrganizationalUnitChanged( content.getOrganizationalUnit(),
                                                 activeOrganizationalUnit ) ) {
            activeOrganizationalUnit = content.getOrganizationalUnit();
            return true;
        } else {
            return false;
        }
    }

    private void fireContextChangeEvent() {
        if ( activeFolderItem == null ) {
            contextChangedEvent.fire( new ProjectContextChangeEvent( activeOrganizationalUnit,
                                                                     activeRepository,
                                                                     activeProject ) );
            return;
        }

        if ( activeFolderItem.getItem() instanceof Package ) {
            activePackage = (Package) activeFolderItem.getItem();
            contextChangedEvent.fire( new ProjectContextChangeEvent( activeOrganizationalUnit,
                                                                     activeRepository,
                                                                     activeProject,
                                                                     activePackage ) );
        } else if ( activeFolderItem.getType().equals( FolderItemType.FOLDER ) ) {
            explorerService.call( new RemoteCallback<Package>() {
                @Override
                public void callback( final Package pkg ) {
                    if ( Utils.hasPackageChanged( pkg,
                                                  activePackage ) ) {
                        activePackage = pkg;
                        contextChangedEvent.fire( new ProjectContextChangeEvent( activeOrganizationalUnit,
                                                                                 activeRepository,
                                                                                 activeProject,
                                                                                 activePackage ) );
                    } else {
                        contextChangedEvent.fire( new ProjectContextChangeEvent( activeOrganizationalUnit,
                                                                                 activeRepository,
                                                                                 activeProject ) );
                    }
                }
            } ).resolvePackage( activeFolderItem );
        }
    }

    private void buildProject( final Project project ) {
        if ( project == null ) {
            return;
        }
        buildService.call(
                new RemoteCallback<BuildResults>() {
                    @Override
                    public void callback( final BuildResults results ) {
                        buildResultsEvent.fire( results );
                    }
                },
                new DefaultErrorCallback() ).build( project );
    }

    @Override
    public void organizationalUnitSelected( final OrganizationalUnit organizationalUnit ) {
        if ( Utils.hasOrganizationalUnitChanged( organizationalUnit,
                                                 activeOrganizationalUnit ) ) {
            getView().getExplorer().clear();
            initialiseViewForActiveContext( organizationalUnit );
        }
    }

    @Override
    public void branchChanged( final String branch ) {
        if ( activeRepository instanceof GitRepository ) {
            ( (GitRepository) activeRepository ).changeBranch( branch );
            getView().getExplorer().clear();
            ProjectExplorerContentQuery query = new ProjectExplorerContentQuery( activeOrganizationalUnit,
                                                                                 activeRepository,
                                                                                 activeProject );
            doInitialiseViewForActiveContext( query,
                                              true );
        }
    }

    @Override
    public void repositorySelected( final Repository repository ) {
        if ( Utils.hasRepositoryChanged( repository,
                                         activeRepository ) ) {
            getView().getExplorer().clear();
            initialiseViewForActiveContext( activeOrganizationalUnit,
                                            repository );
        }
    }

    @Override
    public void projectSelected( final Project project ) {
        if ( Utils.hasProjectChanged( project,
                                      activeProject ) ) {
            getView().getExplorer().clear();
            initialiseViewForActiveContext( activeOrganizationalUnit,
                                            activeRepository,
                                            project );
        }
    }

    @Override
    public void activeFolderItemSelected( final FolderItem item ) {
        if ( !isOnLoading && Utils.hasFolderItemChanged( item, activeFolderItem ) ) {
            activeFolderItem = item;
            fireContextChangeEvent();

            //Show busy popup. Once Items are loaded it is closed
            getView().showBusyIndicator( CommonConstants.INSTANCE.Loading() );
            explorerService.call( new RemoteCallback<FolderListing>() {
                                      @Override
                                      public void callback( final FolderListing folderListing ) {
                                          isOnLoading = true;
                                          loadContent( folderListing );
                                          getView().setItems( folderListing );
                                          getView().hideBusyIndicator();
                                          isOnLoading = false;
                                      }
                                  },
                                  new HasBusyIndicatorDefaultErrorCallback( getView() ) ).getFolderListing( activeOrganizationalUnit,
                                                                                                            activeRepository,
                                                                                                            activeProject,
                                                                                                            item,
                                                                                                            getActiveOptions() );
        }
    }

    @Override
    public void itemSelected( final FolderItem folderItem ) {
        final Object _item = folderItem.getItem();
        if ( _item == null ) {
            return;
        }
        if ( folderItem.getType().equals( FolderItemType.FILE ) && _item instanceof Path ) {
            placeManager.goTo( (Path) _item );
        } else {
            activeFolderItemSelected( folderItem );
        }
    }

    @Override
    public boolean isVisible() {
        return getView().isVisible();
    }

    @Override
    public void setVisible( final boolean visible ) {
        getView().setVisible( visible );
    }

    public void onTagFilterChanged(@Observes TagChangedEvent event) {
        if (!getView().isVisible()) return;
        if (!isFilterByTagEnabled()) return;
        filterByTag( event.getTag() );

    }

    protected void filterByTag(String tag) {
        currentTag = tag;
        List<FolderItem> filteredItems = new ArrayList<FolderItem>(  );

        for (FolderItem item : activeContent.getContent()) {
            if (tag == null || item.getTags().contains( tag ) || item.getType().equals( FolderItemType.FOLDER )) {
                filteredItems.add( item );
            }
        }

        FolderListing filteredContent = new FolderListing( activeContent.getItem(), filteredItems, activeContent.getSegments() );
        getView().renderItems( filteredContent );
    }

    public void onOrganizationalUnitAdded( @Observes final NewOrganizationalUnitEvent event ) {
        if ( !getView().isVisible() ) {
            return;
        }
        final OrganizationalUnit organizationalUnit = event.getOrganizationalUnit();
        if ( organizationalUnit == null ) {
            return;
        }
        if ( authorizationManager.authorize( organizationalUnit,
                                             identity ) ) {
            refresh( false );
        }
    }

    public void onOrganizationalUnitRemoved( @Observes final RemoveOrganizationalUnitEvent event ) {
        if ( !getView().isVisible() ) {
            return;
        }
        final OrganizationalUnit organizationalUnit = event.getOrganizationalUnit();
        if ( organizationalUnit == null ) {
            return;
        }

        refresh( false );
    }

    public void onRepoAddedToOrganizationaUnitEvent( @Observes final RepoAddedToOrganizationaUnitEvent event ) {
        if ( !getView().isVisible() ) {
            return;
        }
        final Repository repository = event.getRepository();
        if ( repository == null ) {
            return;
        }
        if ( authorizationManager.authorize( repository,
                                             identity ) ) {
            refresh( false );
        }
    }

    public void onRepoRemovedFromOrganizationalUnitEvent( @Observes final RepoRemovedFromOrganizationalUnitEvent event ) {
        if ( !getView().isVisible() ) {
            return;
        }
        refresh( false );
    }

    public void onRepositoryRemovedEvent( @Observes final RepositoryRemovedEvent event ) {
        if ( !getView().isVisible() ) {
            return;
        }
        if ( activeRepository.equals( event.getRepository() ) ) {
            activeRepository = null;
            activeProject = null;
            activePackage = null;
            activeFolderItem = null;
        }
        refresh( false );
    }

    public void onRepositoryUpdatedEvent( @Observes final RepositoryUpdatedEvent event ) {
        if ( repositories != null ) {
            for ( Repository repository : repositories ) {
                if ( repository.getAlias().equals( event.getRepository().getAlias() ) ) {
                    if ( activeRepository != null && activeRepository.getAlias().equals( event.getRepository().getAlias() ) ) {
                        refresh( false );
                    } else {
                        repository.getEnvironment().clear();
                        repository.getEnvironment().putAll( event.getUpdatedRepository().getEnvironment() );
                    }
                }
            }
        }
    }

    public void onProjectAdded( @Observes final NewProjectEvent event ) {
        if ( !getView().isVisible() ) {
            return;
        }
        final Project project = event.getProject();
        if ( project == null ) {
            return;
        }
        if ( !sessionInfo.getId().equals( event.getSessionId() ) ) {
            refresh( false );
            return;
        }

        if ( !Utils.isInRepository( activeRepository,
                                    project ) ) {
            refresh( false );
            return;
        }

        if ( authorizationManager.authorize( project,
                                             identity ) ) {
            doInitialiseViewForActiveContext( new ProjectExplorerContentQuery(
                                                      activeOrganizationalUnit,
                                                      activeRepository,
                                                      project ),
                                              false );
        }
    }

    public void onProjectRename( @Observes final RenameProjectEvent event ) {
        if ( !getView().isVisible() ) {
            return;
        }
        if ( !Utils.isInRepository( activeRepository,
                                    event.getOldProject() ) ) {
            return;
        }
        if ( authorizationManager.authorize( event.getOldProject(),
                                             identity ) ) {
            doInitialiseViewForActiveContext( new ProjectExplorerContentQuery(
                                                      activeOrganizationalUnit,
                                                      activeRepository,
                                                      event.getNewProject() ),
                                              true );
        }
    }

    public void onProjectDelete( @Observes final DeleteProjectEvent event ) {
        if ( !getView().isVisible() ) {
            return;
        }
        if ( !Utils.isInRepository( activeRepository,
                                    event.getProject() ) ) {
            return;
        }
        if ( authorizationManager.authorize( event.getProject(),
                                             identity ) ) {
            if ( activeProject != null && activeProject.equals( event.getProject() ) ) {
                activeProject = null;
            }
            doInitialiseViewForActiveContext( new ProjectExplorerContentQuery(
                                                      activeOrganizationalUnit,
                                                      activeRepository ),
                                              true );
        }
    }

    public void onPackageAdded( @Observes final NewPackageEvent event ) {
        if ( !getView().isVisible() ) {
            return;
        }
        final Package pkg = event.getPackage();
        if ( pkg == null ) {
            return;
        }
        if ( !Utils.isInProject( activeProject,
                                 pkg ) ) {
            return;
        }

        doInitialiseViewForActiveContext( new ProjectExplorerContentQuery(
                                                  activeOrganizationalUnit,
                                                  activeRepository,
                                                  activeProject,
                                                  pkg ),
                                          false );
    }

    // Refresh when a Resource has been updated, if it exists in the active package
    public void onResourceUpdated( @Observes final ResourceUpdatedEvent event ) {
        refresh( event.getPath() );
    }

    // Refresh when a Resource has been added, if it exists in the active package
    public void onResourceAdded( @Observes final ResourceAddedEvent event ) {
        refresh( event.getPath() );
    }

    // Refresh when a Resource has been deleted, if it exists in the active package
    public void onResourceDeleted( @Observes final ResourceDeletedEvent event ) {
        refresh( event.getPath() );
    }

    // Refresh when a Resource has been copied, if it exists in the active package
    public void onResourceCopied( @Observes final ResourceCopiedEvent event ) {
        refresh( event.getDestinationPath() );
    }

    // Refresh when a lock status changes has occurred, if it affects the active package
    public void onLockStatusChange( @Observes final LockInfo lockInfo ) {
        refresh( lockInfo.getFile(), true );
    }

    private void refresh( final Path resource ) {
        refresh( resource, false );
    }

    private void refresh( final Path resource,
                          boolean force ) {
        if ( !getView().isVisible() ) {
            return;
        }
        if ( resource == null || activeProject == null ) {
            return;
        }
        if ( !force && !Utils.isInFolderItem( activeFolderItem,
                                              resource ) ) {
            return;
        }

        explorerService.call( new RemoteCallback<FolderListing>() {
            @Override
            public void callback( final FolderListing folderListing ) {
                activeContent = folderListing;
                if (isFilterByTagEnabled()) {
                    resetTags( true );
                    filterByTag( currentTag );
                }
                else {
                    getView().setItems( folderListing );
                }
            }
        }, new DefaultErrorCallback() ).getFolderListing( activeOrganizationalUnit,
                                                          activeRepository,
                                                          activeProject,
                                                          activeFolderItem,
                                                          getActiveOptions() );
    }

    public void onSocialFileSelected( @Observes final SocialFileSelectedEvent event ) {
        vfsService.call( new RemoteCallback<Path>() {
            @Override
            public void callback( Path path ) {
                openBestSuitedScreen( event.getEventType(), path );
                setupActiveContextFor( path );
            }
        } ).get( event.getUri() );
    }

    private void openBestSuitedScreen( final String eventType,
                                       final Path path ) {
        if ( isRepositoryEvent( eventType ) ) {
            //the event is relative to a Repository and not to a file.
            placeManager.goTo( "repositoryStructureScreen" );
        } else if ( isProjectEvent( eventType ) ) {
            placeManager.goTo( "projectScreen" );
        } else {
            placeManager.goTo( path );
        }
    }

    private boolean isRepositoryEvent( String eventType ) {
        if ( eventType == null || eventType.isEmpty() ) {
            return false;
        }

        if ( ExtendedTypes.NEW_REPOSITORY_EVENT.name().equals( eventType ) ||
                AssetManagementEventTypes.BRANCH_CREATED.name().equals( eventType ) ||
                AssetManagementEventTypes.PROCESS_START.name().equals( eventType ) ||
                AssetManagementEventTypes.PROCESS_END.name().equals( eventType ) ||
                AssetManagementEventTypes.ASSETS_PROMOTED.name().equals( eventType ) ||
                AssetManagementEventTypes.PROJECT_BUILT.name().equals( eventType ) ||
                AssetManagementEventTypes.PROJECT_DEPLOYED.name().equals( eventType ) ||
                AssetManagementEventTypes.REPOSITORY_CHANGE.name().equals( eventType ) ) {

            return true;
        }
        return false;
    }

    private boolean isProjectEvent( final String eventType ) {
        return ProjectEventType.NEW_PROJECT.name().equals( eventType );
    }

    private void setupActiveContextFor( final Path path ) {

        explorerService.call( new RemoteCallback<URIStructureExplorerModel>() {
            @Override
            public void callback( URIStructureExplorerModel model ) {
                initialiseViewForActiveContext( model.getOrganizationalUnit(),
                                                model.getRepository(),
                                                model.getProject() );
            }
        } ).getURIStructureExplorerModel( path );

    }

    // Refresh when a Resource has been renamed, if it exists in the active package
    public void onResourceRenamed( @Observes final ResourceRenamedEvent event ) {
        if ( !getView().isVisible() ) {
            return;
        }
        final Path sourcePath = event.getPath();
        final Path destinationPath = event.getDestinationPath();

        boolean refresh = false;
        if ( Utils.isInFolderItem( activeFolderItem,
                                   sourcePath ) ) {
            refresh = true;
        } else if ( Utils.isInFolderItem( activeFolderItem,
                                          destinationPath ) ) {
            refresh = true;
        }

        if ( refresh ) {
            explorerService.call( new RemoteCallback<FolderListing>() {
                                      @Override
                                      public void callback( final FolderListing folderListing ) {
                                          getView().setItems( folderListing );
                                      }
                                  },
                                  new DefaultErrorCallback() ).getFolderListing( activeOrganizationalUnit,
                                                                                 activeRepository,
                                                                                 activeProject,
                                                                                 activeFolderItem,
                                                                                 getActiveOptions() );
        }
    }

    // Refresh when a batch Resource change has occurred. Simply refresh everything.
    public void onBatchResourceChanges( @Observes final ResourceBatchChangesEvent resourceBatchChangesEvent ) {
        if ( !getView().isVisible() ) {
            return;
        }

        boolean projectChange = false;
        for ( final Path path : resourceBatchChangesEvent.getBatch().keySet() ) {
            if ( path.getFileName().equals( "pom.xml" ) ) {
                projectChange = true;
                break;
            }
        }

        if ( !projectChange ) {
            refresh( false );
        }
    }

    public void onBranchCreated( @Observes final NewBranchEvent event ) {
        if ( isTheSameRepo( event.getRepositoryAlias() ) ) {
            if ( activeRepository instanceof GitRepository ) {
                addBranch( activeRepository, event.getBranchName(), event.getBranchPath() );
            }
        }

        if ( repositories != null ) {
            for ( Repository repository : repositories ) {
                if ( repository.getAlias().equals( event.getRepositoryAlias() ) ) {
                    addBranch( repository, event.getBranchName(), event.getBranchPath() );
                }
            }
        }
    }

    private void addBranch( final Repository repository,
                            final String branchName,
                            final Path branchPath ) {
        ( (GitRepository) repository ).addBranch( branchName, branchPath );
        refresh( false );
    }

    private boolean isTheSameRepo( final String alias ) {
        return activeRepository != null && activeRepository.getAlias().equals( alias );
    }

    public void onSystemRepositoryChanged( @Observes final SystemRepositoryChangedEvent event ) {
        if ( !getView().isVisible() ) {
            return;
        }
        refresh( false );
    }

    public abstract void addOption( final Option option );

    protected boolean isFilterByTagEnabled() {
        return getActiveOptions().contains( Option.SHOW_TAG_FILTER );
    }
}
