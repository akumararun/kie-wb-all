package org.kie.guvnor.datamodel.backend.server.cache;

import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.kie.commons.validation.PortablePreconditions;
import org.kie.guvnor.datamodel.events.InvalidateDMOPackageCacheEvent;
import org.kie.guvnor.datamodel.events.InvalidateDMOProjectCacheEvent;
import org.kie.guvnor.datamodel.oracle.DataModelOracle;
import org.kie.guvnor.project.service.ProjectService;
import org.uberfire.backend.vfs.Path;

/**
 * A simple LRU cache for Package DataModelOracles
 */
@ApplicationScoped
@Named("PackageDataModelOracleCache")
public class LRUDataModelOracleCache extends LRUCache<Path, DataModelOracle> {

    @Inject
    private ProjectService projectService;

    public void invalidatePackageCache( @Observes final InvalidateDMOPackageCacheEvent event ) {
        PortablePreconditions.checkNotNull( "event",
                                            event );
        final Path resourcePath = event.getResourcePath();
        final Path packagePath = projectService.resolvePackage( resourcePath );
        invalidateCache( packagePath );
    }

    public void invalidateProjectPackagesCache( @Observes final InvalidateDMOProjectCacheEvent event ) {
        PortablePreconditions.checkNotNull( "event",
                                            event );
        final Path resourcePath = event.getResourcePath();
        final Path projectPath = projectService.resolveProject( resourcePath );
        final String projectUri = projectPath.toURI();

        final List<Path> cacheEntriesToInvalidate = new ArrayList<Path>();

        for ( final Path packagePath : getKeys() ) {
            final String packageUri = packagePath.toURI();
            if ( packageUri.startsWith( projectUri ) ) {
                cacheEntriesToInvalidate.add( packagePath );
            }
        }

        for ( final Path packagePath : cacheEntriesToInvalidate ) {
            invalidateCache( packagePath );
        }
    }

}
