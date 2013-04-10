package org.kie.guvnor.datamodel.backend.server.cache;

import java.io.IOException;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.drools.core.rule.TypeMetaInfo;
import org.drools.guvnor.models.commons.shared.imports.Import;
import org.drools.guvnor.models.commons.shared.imports.Imports;
import org.kie.commons.io.IOService;
import org.kie.commons.java.nio.file.Files;
import org.kie.commons.validation.PortablePreconditions;
import org.kie.guvnor.builder.Builder;
import org.kie.guvnor.builder.LRUBuilderCache;
import org.kie.guvnor.commons.service.builder.model.BuildMessage;
import org.kie.guvnor.commons.service.builder.model.BuildResults;
import org.kie.guvnor.datamodel.backend.server.builder.projects.ProjectDefinitionBuilder;
import org.kie.guvnor.datamodel.events.InvalidateDMOProjectCacheEvent;
import org.kie.guvnor.datamodel.oracle.ProjectDefinition;
import org.kie.guvnor.project.model.PackageConfiguration;
import org.kie.guvnor.project.service.POMService;
import org.kie.guvnor.project.service.ProjectService;
import org.kie.guvnor.services.cache.LRUCache;
import org.kie.scanner.KieModuleMetaData;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.backend.vfs.Path;

/**
 * A simple LRU cache for Project DataModelOracles
 */
@ApplicationScoped
@Named("ProjectDataModelOracleCache")
public class LRUProjectDataModelOracleCache extends LRUCache<Path, ProjectDefinition> {

    private static final String ERROR_CLASS_NOT_FOUND = "Class not found";

    private static final String ERROR_IO = "IO Error";

    @Inject
    private Paths paths;

    @Inject
    private POMService pomService;

    @Inject
    @Named("ioStrategy")
    private IOService ioService;

    @Inject
    private ProjectService projectService;

    @Inject
    private LRUBuilderCache cache;

    @Inject
    private Event<BuildResults> buildResultsEvent;

    public synchronized void invalidateProjectCache( @Observes final InvalidateDMOProjectCacheEvent event ) {
        PortablePreconditions.checkNotNull( "event",
                                            event );
        final Path resourcePath = event.getResourcePath();
        final Path projectPath = projectService.resolveProject( resourcePath );

        //If resource was not within a Project there's nothing to invalidate
        if ( projectPath != null ) {
            invalidateCache( projectPath );
        }
    }

    //Check the ProjectDefinition for the Project has been created, otherwise create one!
    public synchronized ProjectDefinition assertProjectDataModelOracle( final Path projectPath ) {
        ProjectDefinition projectDefinition = getEntry( projectPath );
        if ( projectDefinition == null ) {
            projectDefinition = makeProjectDefinition( projectPath );
            setEntry( projectPath,
                      projectDefinition );
        }
        return projectDefinition;
    }

    private ProjectDefinition makeProjectDefinition( final Path projectPath ) {
        //Get a Builder for the project
        final Builder builder = cache.assertBuilder( projectPath );

        //Create the ProjectDefinition...
        final BuildResults results = builder.build();
        final KieModuleMetaData metaData = KieModuleMetaData.Factory.newKieModuleMetaData( builder.getKieModuleIgnoringErrors() );
        final ProjectDefinitionBuilder pdBuilder = ProjectDefinitionBuilder.newProjectDefinitionBuilder();

        //Add all classes from the KieModule metaData
        for ( final String packageName : metaData.getPackages() ) {
            for ( final String className : metaData.getClasses( packageName ) ) {
                final Class clazz = metaData.getClass( packageName,
                                                       className );
                final TypeMetaInfo typeMetaInfo = metaData.getTypeMetaInfo( clazz );
                try {
                    pdBuilder.addClass( clazz,
                                        typeMetaInfo.isEvent() );
                } catch ( IOException ioe ) {
                    results.addBuildMessage( makeMessage( ERROR_IO,
                                                          ioe ) );
                }
            }
        }

        //Add external imports. The availability of these classes is checked in Builder and failed fast. Here we load them into the DMO
        final org.kie.commons.java.nio.file.Path nioExternalImportsPath = paths.convert( projectPath ).resolve( "project.imports" );
        if ( Files.exists( nioExternalImportsPath ) ) {
            final Path externalImportsPath = paths.convert( nioExternalImportsPath );
            final PackageConfiguration packageConfiguration = projectService.load( externalImportsPath );
            final Imports imports = packageConfiguration.getImports();
            for ( final Import item : imports.getImports() ) {
                try {
                    Class clazz = this.getClass().getClassLoader().loadClass( item.getType() );
                    pdBuilder.addClass( clazz );
                } catch ( ClassNotFoundException cnfe ) {
                    //This should not happen as Builder would have failed to load them and failed fast.
                    results.addBuildMessage( makeMessage( ERROR_CLASS_NOT_FOUND,
                                                          cnfe ) );
                } catch ( IOException ioe ) {
                    results.addBuildMessage( makeMessage( ERROR_IO,
                                                          ioe ) );
                }
            }
        }

        //Report any errors to the user
        if ( !results.getMessages().isEmpty() ) {
            buildResultsEvent.fire( results );
        }

        return pdBuilder.build();
    }

    private BuildMessage makeMessage( final String prefix,
                                      final Exception e ) {
        final BuildMessage buildMessage = new BuildMessage();
        buildMessage.setLevel( BuildMessage.Level.ERROR );
        buildMessage.setText( prefix + ": " + e.getMessage() );
        return buildMessage;
    }

}
