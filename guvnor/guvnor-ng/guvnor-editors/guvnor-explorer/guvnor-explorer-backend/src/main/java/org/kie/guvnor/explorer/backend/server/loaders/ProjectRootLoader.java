package org.kie.guvnor.explorer.backend.server.loaders;

import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;

import org.kie.commons.io.IOService;
import org.kie.commons.java.nio.file.Files;
import org.kie.guvnor.explorer.model.FileItem;
import org.kie.guvnor.explorer.model.FolderItem;
import org.kie.guvnor.explorer.model.Item;
import org.kie.guvnor.explorer.model.ItemNames;
import org.kie.guvnor.explorer.model.ParentFolderItem;
import org.kie.guvnor.services.backend.file.LinkedDotFileFilter;
import org.kie.guvnor.services.backend.file.LinkedMetaInfFolderFilter;
import org.kie.guvnor.services.file.LinkedFilter;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.backend.vfs.Path;

/**
 * Loader to add Projects structure being pom.xml and immediate packages under src/main/resources
 */
@Dependent
@Named("projectRootList")
public class ProjectRootLoader implements ItemsLoader {

    private static final String POM_PATH = "pom.xml";
    private static final String PROJECT_IMPORTS_PATH = "project.imports";
    private static final String SOURCE_JAVA_RESOURCES_PATH = "src/main/java";
    private static final String SOURCE_RESOURCES_PATH = "src/main/resources";
    private static final String TEST_JAVA_RESOURCES_PATH = "src/test/java";
    private static final String TEST_RESOURCES_PATH = "src/test/resources";

    private final LinkedFilter filter;

    @Inject
    @Named("ioStrategy")
    private IOService ioService;

    @Inject
    private Paths paths;

    public ProjectRootLoader() {
        filter = new LinkedDotFileFilter();
        filter.setNextFilter( new LinkedMetaInfFolderFilter() );
    }

    @Override
    public List<Item> load( final Path path,
                            final Path projectRoot ) {

        //Check Path exists
        final List<Item> items = new ArrayList<Item>();
        if ( !Files.exists( paths.convert( path ) ) ) {
            return items;
        }

        //Add pom.xml file
        final org.kie.commons.java.nio.file.Path pRoot = paths.convert( projectRoot );
        final org.kie.commons.java.nio.file.Path pomPath = pRoot.resolve( POM_PATH );
        final String projectName = pRoot.getFileName().toString();
        if ( Files.exists( pomPath ) ) {
            items.add( new FileItem( paths.convert( pomPath ),
                                     "Project definition '" + projectName + "'" ) );
        }

        //Add Project Imports
        final org.kie.commons.java.nio.file.Path projectImportsPath = pRoot.resolve( PROJECT_IMPORTS_PATH );
        if ( Files.exists( projectImportsPath ) ) {
            items.add( new FileItem( paths.convert( projectImportsPath ),
                                     "External imports" ) );
        }

        //Add Items within Project's Java Source Resources path
        final org.kie.commons.java.nio.file.Path srcJavaResourcesPath = pRoot.resolve( SOURCE_JAVA_RESOURCES_PATH );
        if ( Files.exists( srcJavaResourcesPath ) ) {
            items.add( new FolderItem( paths.convert( srcJavaResourcesPath ),
                                       ItemNames.SOURCE_JAVA ) );
        }

        //Add Items within Project's Source Resources path
        final org.kie.commons.java.nio.file.Path srcResourcesPath = pRoot.resolve( SOURCE_RESOURCES_PATH );
        if ( Files.exists( srcResourcesPath ) ) {
            items.add( new FolderItem( paths.convert( srcResourcesPath ),
                                       ItemNames.SOURCE_RESOURCES ) );
        }

        //Add Items within Project's Java Test Resources path
        final org.kie.commons.java.nio.file.Path testJavaResourcesPath = pRoot.resolve( TEST_JAVA_RESOURCES_PATH );
        if ( Files.exists( testJavaResourcesPath ) ) {
            items.add( new FolderItem( paths.convert( testJavaResourcesPath ),
                                       ItemNames.TEST_JAVA ) );
        }

        //Add Items within Project's Test Resources path
        final org.kie.commons.java.nio.file.Path testResourcesPath = pRoot.resolve( TEST_RESOURCES_PATH );
        if ( Files.exists( testResourcesPath ) ) {
            items.add( new FolderItem( paths.convert( testResourcesPath ),
                                       ItemNames.TEST_RESOURCES ) );
        }

        //Add ability to move up one level in the hierarchy
        items.add( new ParentFolderItem( paths.convert( pRoot.getParent() ),
                                         ".." ) );

        return items;
    }

}
