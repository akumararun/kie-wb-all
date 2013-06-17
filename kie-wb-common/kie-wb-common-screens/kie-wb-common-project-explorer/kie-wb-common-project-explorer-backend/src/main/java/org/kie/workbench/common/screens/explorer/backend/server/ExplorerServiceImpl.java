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

package org.kie.workbench.common.screens.explorer.backend.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.errai.bus.server.annotations.Service;
import org.kie.commons.io.IOService;
import org.kie.commons.java.nio.file.DirectoryStream;
import org.kie.commons.java.nio.file.Files;
import org.kie.commons.validation.PortablePreconditions;
import org.kie.workbench.common.screens.explorer.model.Item;
import org.kie.workbench.common.screens.explorer.service.ExplorerService;
import org.kie.workbench.common.services.backend.file.LinkedDotFileFilter;
import org.kie.workbench.common.services.backend.file.LinkedMetaInfFolderFilter;
import org.kie.workbench.common.services.project.service.ProjectService;
import org.kie.workbench.common.services.shared.context.Package;
import org.kie.workbench.common.services.shared.context.Project;
import org.uberfire.backend.group.Group;
import org.uberfire.backend.group.GroupService;
import org.uberfire.backend.repositories.Repository;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.backend.vfs.Path;
import org.uberfire.security.Identity;
import org.uberfire.security.authz.AuthorizationManager;

@Service
@ApplicationScoped
public class ExplorerServiceImpl
        implements ExplorerService {

    private static final String MAIN_SRC_PATH = "src/main/java";
    private static final String TEST_SRC_PATH = "src/test/java";
    private static final String MAIN_RESOURCES_PATH = "src/main/resources";
    private static final String TEST_RESOURCES_PATH = "src/test/resources";

    private static String[] sourcePaths = { MAIN_SRC_PATH, MAIN_RESOURCES_PATH, TEST_SRC_PATH, TEST_RESOURCES_PATH };

    private LinkedDotFileFilter dotFileFilter = new LinkedDotFileFilter();
    private LinkedMetaInfFolderFilter metaDataFileFilter = new LinkedMetaInfFolderFilter();

    @Inject
    @Named("ioStrategy")
    private IOService ioService;

    @Inject
    private ProjectService projectService;

    @Inject
    private GroupService groupService;

    @Inject
    private AuthorizationManager authorizationManager;

    @Inject
    @SessionScoped
    private Identity identity;

    @Inject
    private Paths paths;

    public ExplorerServiceImpl() {
        // Boilerplate sacrifice for Weld
    }

    public ExplorerServiceImpl( final IOService ioService,
                                final AuthorizationManager authorizationManager,
                                final ProjectService projectService,
                                final GroupService groupService,
                                final Identity identity,
                                final Paths paths ) {
        this.ioService = ioService;
        this.authorizationManager = authorizationManager;
        this.projectService = projectService;
        this.groupService = groupService;
        this.identity = identity;
        this.paths = paths;
    }

    @Override
    public Collection<Group> getGroups() {
        final Collection<Group> groups = groupService.getGroups();
        final Collection<Group> authorizedGroups = new ArrayList<Group>();
        for ( Group group : groups ) {
            if ( authorizationManager.authorize( group,
                                                 identity ) ) {
                authorizedGroups.add( group );
            }
        }
        return authorizedGroups;
    }

    @Override
    public Collection<Repository> getRepositories( final Group group ) {
        final Collection<Repository> authorizedRepositories = new HashSet<Repository>();
        for ( Repository repository : group.getRepositories() ) {
            if ( authorizationManager.authorize( repository,
                                                 identity ) ) {
                authorizedRepositories.add( repository );
            }
        }
        return authorizedRepositories;
    }

    @Override
    public Collection<Project> getProjects( final Repository repository ) {
        final Collection<Project> authorizedProjects = new HashSet<Project>();
        final Path repositoryRoot = repository.getRoot();
        final DirectoryStream<org.kie.commons.java.nio.file.Path> nioRepositoryPaths = ioService.newDirectoryStream( paths.convert( repositoryRoot ) );
        for ( org.kie.commons.java.nio.file.Path nioRepositoryPath : nioRepositoryPaths ) {
            if ( Files.isDirectory( nioRepositoryPath ) ) {
                final org.uberfire.backend.vfs.Path projectPath = paths.convert( nioRepositoryPath );
                final Project project = projectService.resolveProject( projectPath );
                if ( project != null ) {
                    authorizedProjects.add( project );
                }
            }
        }
        return authorizedProjects;
    }

    @Override
    public Collection<Package> getPackages( final Project project ) {
        final Set<String> packageNames = new HashSet<String>();
        final Path projectRoot = project.getRootPath();
        final org.kie.commons.java.nio.file.Path nioProjectRootPath = paths.convert( projectRoot );
        for ( String src : sourcePaths ) {
            final org.kie.commons.java.nio.file.Path nioPackageRootSrcPath = nioProjectRootPath.resolve( src );
            packageNames.addAll( getPackageNames( nioProjectRootPath,
                                                  nioPackageRootSrcPath ) );
        }

        final Collection<Package> packages = new HashSet<Package>();
        final org.kie.commons.java.nio.file.Path nioPackagesRootPath = nioProjectRootPath.resolve( MAIN_SRC_PATH );
        for ( String packagePathSuffix : packageNames ) {
            final org.kie.commons.java.nio.file.Path nioPackagePath = nioPackagesRootPath.resolve( packagePathSuffix );
            packages.add( makePackage( nioPackagePath ) );
        }

        return packages;
    }

    private Set<String> getPackageNames( final org.kie.commons.java.nio.file.Path nioProjectRootPath,
                                         final org.kie.commons.java.nio.file.Path nioPackageSrcPath ) {
        final Set<String> packageNames = new HashSet<String>();
        if ( !Files.exists( nioPackageSrcPath ) ) {
            return packageNames;
        }
        packageNames.add( getPackagePathSuffix( nioProjectRootPath,
                                                nioPackageSrcPath ) );
        final DirectoryStream<org.kie.commons.java.nio.file.Path> nioChildPackageSrcPaths = ioService.newDirectoryStream( nioPackageSrcPath,
                                                                                                                          metaDataFileFilter );
        for ( org.kie.commons.java.nio.file.Path nioChildPackageSrcPath : nioChildPackageSrcPaths ) {
            if ( Files.isDirectory( nioChildPackageSrcPath ) ) {
                packageNames.addAll( getPackageNames( nioProjectRootPath,
                                                      nioChildPackageSrcPath ) );
            }
        }
        return packageNames;
    }

    private String getPackagePathSuffix( final org.kie.commons.java.nio.file.Path nioProjectRootPath,
                                         final org.kie.commons.java.nio.file.Path nioPackagePath ) {
        final org.kie.commons.java.nio.file.Path nioMainSrcPath = nioProjectRootPath.resolve( MAIN_SRC_PATH );
        final org.kie.commons.java.nio.file.Path nioTestSrcPath = nioProjectRootPath.resolve( TEST_SRC_PATH );
        final org.kie.commons.java.nio.file.Path nioMainResourcesPath = nioProjectRootPath.resolve( MAIN_RESOURCES_PATH );
        final org.kie.commons.java.nio.file.Path nioTestResourcesPath = nioProjectRootPath.resolve( TEST_RESOURCES_PATH );

        String packageName = null;
        org.kie.commons.java.nio.file.Path packagePath = null;
        if ( nioPackagePath.startsWith( nioMainSrcPath ) ) {
            packagePath = nioMainSrcPath.relativize( nioPackagePath );
            packageName = packagePath.toString();
        } else if ( nioPackagePath.startsWith( nioTestSrcPath ) ) {
            packagePath = nioTestSrcPath.relativize( nioPackagePath );
            packageName = packagePath.toString();
        } else if ( nioPackagePath.startsWith( nioMainResourcesPath ) ) {
            packagePath = nioMainResourcesPath.relativize( nioPackagePath );
            packageName = packagePath.toString();
        } else if ( nioPackagePath.startsWith( nioTestResourcesPath ) ) {
            packagePath = nioTestResourcesPath.relativize( nioPackagePath );
            packageName = packagePath.toString();
        }

        return packageName;
    }

    private Package makePackage( final org.kie.commons.java.nio.file.Path nioPackageSrcPath ) {
        final Package pkg = projectService.resolvePackage( paths.convert( nioPackageSrcPath,
                                                                          false ) );
        return pkg;
    }

    @Override
    public Collection<Item> getItems( final Package pkg ) {
        final Collection<Item> items = new HashSet<Item>();
        items.addAll( getItems( pkg.getPackageMainSrcPath() ) );
        items.addAll( getItems( pkg.getPackageTestSrcPath() ) );
        items.addAll( getItems( pkg.getPackageMainResourcesPath() ) );
        items.addAll( getItems( pkg.getPackageTestResourcesPath() ) );
        return items;
    }

    private Collection<Item> getItems( final Path packagePath ) {
        final Collection<Item> items = new HashSet<Item>();
        final org.kie.commons.java.nio.file.Path nioPackagePath = paths.convert( packagePath );
        if ( Files.exists( nioPackagePath ) ) {
            final DirectoryStream<org.kie.commons.java.nio.file.Path> nioPaths = ioService.newDirectoryStream( nioPackagePath,
                                                                                                               dotFileFilter );
            for ( org.kie.commons.java.nio.file.Path nioPath : nioPaths ) {
                if ( Files.isRegularFile( nioPath ) ) {
                    final org.uberfire.backend.vfs.Path path = paths.convert( nioPath );
                    final String fileName = getBaseFileName( path.getFileName() );
                    final Item item = new Item( path,
                                                fileName );
                    items.add( item );
                }
            }
        }
        return items;
    }

    private String getBaseFileName( final String fileName ) {
        final int dotIndex = fileName.indexOf( "." );
        //Return dotFiles with the leading dot
        return ( dotIndex > 0 ? fileName.substring( 0,
                                                    dotIndex ) : fileName );
    }

    @Override
    public Collection<Item> handleResourceEvent( final Package pkg,
                                                 final Path resource ) {
        PortablePreconditions.checkNotNull( "pkg",
                                            pkg );
        PortablePreconditions.checkNotNull( "resource",
                                            resource );
        final Package resourcePackage = projectService.resolvePackage( resource );
        if ( resourcePackage == null ) {
            return null;
        }
        return getItems( pkg );
    }

}
