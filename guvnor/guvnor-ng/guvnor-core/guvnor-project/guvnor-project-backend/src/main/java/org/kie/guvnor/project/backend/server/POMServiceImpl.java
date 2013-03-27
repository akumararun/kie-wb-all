package org.kie.guvnor.project.backend.server;

import java.util.Date;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jboss.errai.bus.server.annotations.Service;
import org.kie.commons.io.IOService;
import org.kie.commons.java.nio.IOException;
import org.kie.commons.java.nio.base.options.CommentedOption;
import org.kie.commons.java.nio.file.FileAlreadyExistsException;
import org.kie.commons.java.nio.file.InvalidPathException;
import org.kie.commons.java.nio.file.NoSuchFileException;
import org.kie.guvnor.datamodel.events.InvalidateDMOProjectCacheEvent;
import org.kie.guvnor.m2repo.service.M2RepoService;
import org.kie.guvnor.project.model.POM;
import org.kie.guvnor.project.model.Repository;
import org.kie.guvnor.project.service.POMService;
import org.kie.guvnor.services.exceptions.FileAlreadyExistsPortableException;
import org.kie.guvnor.services.exceptions.GenericPortableException;
import org.kie.guvnor.services.exceptions.InvalidPathPortableException;
import org.kie.guvnor.services.exceptions.NoSuchFilePortableException;
import org.kie.guvnor.services.exceptions.SecurityPortableException;
import org.kie.guvnor.services.metadata.MetadataService;
import org.kie.guvnor.services.metadata.model.Metadata;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.backend.vfs.Path;
import org.uberfire.client.workbench.widgets.events.ResourceUpdatedEvent;
import org.uberfire.security.Identity;

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
                           final Identity identity ) {
        this.ioService = ioService;
        this.paths = paths;
        this.pomContentHandler = pomContentHandler;
        this.m2RepoService = m2RepoService;
        this.metadataService = metadataService;
        this.resourceUpdatedEvent = resourceUpdatedEvent;
        this.invalidateDMOProjectCache = invalidateDMOProjectCache;
        this.identity = identity;
    }

    @Override
    public Path create( final Path projectRoot, final String baseURL ) {
        org.kie.commons.java.nio.file.Path pathToPOMXML = null;
        try {
            final POM pomModel = new POM();
            final Repository repository = new Repository();
            repository.setId( "guvnor-m2-repo" );
            repository.setName( "Guvnor M2 Repo" );
            repository.setUrl( m2RepoService.getRepositoryURL(baseURL) );
            pomModel.addRepository( repository );

            final org.kie.commons.java.nio.file.Path nioRoot = paths.convert( projectRoot );
            pathToPOMXML = nioRoot.resolve( "pom.xml" );

            ioService.createFile( pathToPOMXML );
            ioService.write( pathToPOMXML,
                             pomContentHandler.toString( pomModel ) );

            //Don't raise a NewResourceAdded event as this is handled at the Project level in ProjectServices

            return paths.convert( pathToPOMXML );

        } catch ( InvalidPathException e ) {
            throw new InvalidPathPortableException( pathToPOMXML.toUri().toString() );

        } catch ( SecurityException e ) {
            throw new SecurityPortableException( pathToPOMXML.toUri().toString() );

        } catch ( IllegalArgumentException e ) {
            throw new GenericPortableException( e.getMessage() );

        } catch ( FileAlreadyExistsException e ) {
            throw new FileAlreadyExistsPortableException( pathToPOMXML.toUri().toString() );

        } catch ( IOException e ) {
            throw new GenericPortableException( e.getMessage() );

        } catch ( java.io.IOException e ) {
            throw new GenericPortableException( e.getMessage() );

        } catch ( UnsupportedOperationException e ) {
            throw new GenericPortableException( e.getMessage() );

        }
    }

    @Override
    public POM load( final Path path ) {
        try {
            final org.kie.commons.java.nio.file.Path nioPath = paths.convert( path );
            final String content = ioService.readAllString( nioPath );

            return pomContentHandler.toModel( content );

        } catch ( NoSuchFileException e ) {
            throw new NoSuchFilePortableException( path.toURI() );

        } catch ( IllegalArgumentException e ) {
            throw new GenericPortableException( e.getMessage() );

        } catch ( IOException e ) {
            throw new GenericPortableException( e.getMessage() );

        } catch ( java.io.IOException e ) {
            throw new GenericPortableException( e.getMessage() );

        } catch ( XmlPullParserException e ) {
            throw new GenericPortableException( e.getMessage() );

        }
    }

    @Override
    public Path save( final Path path,
                      final POM content,
                      final Metadata metadata,
                      final String comment ) {
        try {
            if ( metadata == null ) {
                ioService.write( paths.convert( path ),
                                 pomContentHandler.toString( content ),
                                 makeCommentedOption( comment ) );
            } else {
                ioService.write( paths.convert( path ),
                                 pomContentHandler.toString( content ),
                                 metadataService.setUpAttributes( path,
                                                                  metadata ),
                                 makeCommentedOption( comment ) );
            }

            //Invalidate Project-level DMO cache as POM has changed.
            invalidateDMOProjectCache.fire( new InvalidateDMOProjectCacheEvent( path ) );

            //Signal update to interested parties
            resourceUpdatedEvent.fire( new ResourceUpdatedEvent( path ) );

            return path;

        } catch ( InvalidPathException e ) {
            throw new InvalidPathPortableException( path.toURI() );

        } catch ( SecurityException e ) {
            throw new SecurityPortableException( path.toURI() );

        } catch ( IllegalArgumentException e ) {
            throw new GenericPortableException( e.getMessage() );

        } catch ( FileAlreadyExistsException e ) {
            throw new FileAlreadyExistsPortableException( path.toURI() );

        } catch ( IOException e ) {
            throw new GenericPortableException( e.getMessage() );

        } catch ( java.io.IOException e ) {
            throw new GenericPortableException( e.getMessage() );

        } catch ( UnsupportedOperationException e ) {
            throw new GenericPortableException( e.getMessage() );

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
