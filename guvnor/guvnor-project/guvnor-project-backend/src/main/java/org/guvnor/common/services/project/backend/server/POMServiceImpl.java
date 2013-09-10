package org.guvnor.common.services.project.backend.server;

import java.util.Date;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;

import org.guvnor.common.services.backend.exceptions.ExceptionUtilities;
import org.guvnor.common.services.project.builder.events.InvalidateDMOProjectCacheEvent;
import org.guvnor.common.services.project.model.POM;
import org.guvnor.common.services.project.model.Repository;
import org.guvnor.common.services.project.service.POMService;
import org.guvnor.common.services.shared.metadata.MetadataService;
import org.guvnor.common.services.shared.metadata.model.Metadata;
import org.guvnor.m2repo.service.M2RepoService;
import org.jboss.errai.bus.server.annotations.Service;
import org.kie.commons.io.IOService;
import org.kie.commons.java.nio.base.options.CommentedOption;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.backend.vfs.Path;
import org.uberfire.rpc.SessionInfo;
import org.uberfire.security.Identity;
import org.uberfire.workbench.events.ResourceUpdatedEvent;

@Service
@ApplicationScoped
public class POMServiceImpl
        implements POMService {

    private IOService ioService;
    private Paths paths;
    private POMContentHandler pomContentHandler;
    private M2RepoService m2RepoService;
    private MetadataService metadataService;

    private Event<ResourceUpdatedEvent> resourceUpdatedEvent;
    private Event<InvalidateDMOProjectCacheEvent> invalidateDMOProjectCache;

    private Identity identity;

    private SessionInfo sessionInfo;

    public POMServiceImpl() {
        // For Weld
    }

    @Inject
    public POMServiceImpl( final @Named("ioStrategy") IOService ioService,
                           final Paths paths,
                           final POMContentHandler pomContentHandler,
                           final M2RepoService m2RepoService,
                           final MetadataService metadataService,
                           final Event<ResourceUpdatedEvent> resourceUpdatedEvent,
                           final Event<InvalidateDMOProjectCacheEvent> invalidateDMOProjectCache,
                           final Identity identity,
                           final SessionInfo sessionInfo ) {
        this.ioService = ioService;
        this.paths = paths;
        this.pomContentHandler = pomContentHandler;
        this.m2RepoService = m2RepoService;
        this.metadataService = metadataService;
        this.resourceUpdatedEvent = resourceUpdatedEvent;
        this.invalidateDMOProjectCache = invalidateDMOProjectCache;
        this.identity = identity;
        this.sessionInfo = sessionInfo;
    }

    @Override
    public Path create( final Path projectRoot,
                        final String baseURL,
                        final POM pomModel ) {
        org.kie.commons.java.nio.file.Path pathToPOMXML = null;
        try {
            final Repository repository = new Repository();
            repository.setId( "guvnor-m2-repo" );
            repository.setName( "Guvnor M2 Repo" );
            repository.setUrl( m2RepoService.getRepositoryURL( baseURL ) );
            pomModel.addRepository( repository );

            final org.kie.commons.java.nio.file.Path nioRoot = paths.convert( projectRoot );
            pathToPOMXML = nioRoot.resolve( "pom.xml" );

            ioService.createFile( pathToPOMXML );
            ioService.write( pathToPOMXML,
                             pomContentHandler.toString(pomModel) );

            //Don't raise a NewResourceAdded event as this is handled at the Project level in ProjectServices

            return paths.convert( pathToPOMXML );

        } catch ( Exception e ) {
            throw ExceptionUtilities.handleException( e );
        }
    }

    @Override
    public POM load( final Path path ) {
        try {

            return pomContentHandler.toModel( loadPomXMLString(path) );

        } catch ( Exception e ) {
            throw ExceptionUtilities.handleException( e );
        }
    }

    private String loadPomXMLString(Path path) {
        final org.kie.commons.java.nio.file.Path nioPath = paths.convert( path );
        return ioService.readAllString( nioPath );
    }

    @Override
    public Path save( final Path path,
                      final POM content,
                      final Metadata metadata,
                      final String comment ) {
        try {

            if ( metadata == null ) {
                ioService.write( paths.convert(path),
                                 pomContentHandler.toString( content, loadPomXMLString(path) ),
                                 makeCommentedOption( comment ) );
            } else {
                ioService.write( paths.convert( path ),
                                 pomContentHandler.toString( content, loadPomXMLString(path) ),
                                 metadataService.setUpAttributes( path,
                                                                  metadata ),
                                 makeCommentedOption( comment ) );
            }

            //The pom.xml, kmodule.xml and project.imports are all saved from ProjectScreenPresenter
            //We only raise InvalidateDMOProjectCacheEvent and ResourceUpdatedEvent(pom.xml) events once
            //to avoid duplicating events (and re-construction of DMO).

            //Invalidate Project-level DMO cache as POM has changed.
            invalidateDMOProjectCache.fire( new InvalidateDMOProjectCacheEvent( path ) );

            //Signal update to interested parties
            resourceUpdatedEvent.fire( new ResourceUpdatedEvent( path, sessionInfo ) );

            return path;

        } catch ( Exception e ) {
            throw ExceptionUtilities.handleException( e );
        }
    }

    private CommentedOption makeCommentedOption( final String commitMessage ) {
        final String name = identity.getName();
        final Date when = new Date();
        final CommentedOption co = new CommentedOption( name,
                                                        null,
                                                        commitMessage,
                                                        when );
        return co;
    }

}
