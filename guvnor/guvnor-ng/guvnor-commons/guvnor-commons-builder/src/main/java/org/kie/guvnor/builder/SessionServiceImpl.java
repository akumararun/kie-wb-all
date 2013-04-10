package org.kie.guvnor.builder;

import javax.inject.Inject;

import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.guvnor.commons.service.session.SessionService;
import org.kie.guvnor.m2repo.service.M2RepoService;
import org.kie.guvnor.services.exceptions.GenericPortableException;
import org.uberfire.backend.vfs.Path;

public class SessionServiceImpl
        implements SessionService {

    private LRUBuilderCache cache;
    private M2RepoService m2RepoService;

    public SessionServiceImpl() {
        //Empty constructor for Weld
    }

    @Inject
    public SessionServiceImpl( final LRUBuilderCache cache,
                               M2RepoService m2RepoService ) {
        this.cache = cache;
        this.m2RepoService = m2RepoService;
    }

    @Override
    public KieSession newKieSession( Path pathToPom,
                                     String sessionName ) {

        final Builder builder = cache.assertBuilder( pathToPom );

        KieContainer kieContainer = null;

        try {
            kieContainer = builder.getKieContainer();

        } catch ( RuntimeException e ) {
            throw new GenericPortableException( e.getMessage() );
        }

        return kieContainer.newKieSession( sessionName );
    }

}
