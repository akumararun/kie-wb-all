/*
 * Copyright 2013 JBoss Inc
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

package org.kie.guvnor.services.backend.version;

import org.jboss.errai.bus.server.annotations.Service;
import org.kie.commons.io.IOService;
import org.kie.commons.java.nio.base.options.CommentedOption;
import org.kie.commons.java.nio.base.version.VersionAttributeView;
import org.kie.commons.java.nio.base.version.VersionRecord;
import org.kie.guvnor.services.version.VersionService;
import org.kie.guvnor.services.version.model.PortableVersionRecord;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.backend.vfs.Path;
import org.uberfire.security.Identity;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

import static org.kie.commons.java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Service
@ApplicationScoped
public class VersionServiceImpl implements VersionService {

    @Inject
    @Named("ioStrategy")
    private IOService ioService;

    @Inject
    private Paths paths;

    @Inject
    private Identity identity;

    @Override
    public List<VersionRecord> getVersion( final Path path ) {

        final List<VersionRecord> records = ioService.getFileAttributeView( paths.convert( path ), VersionAttributeView.class ).readAttributes().history().records();

        final List<VersionRecord> result = new ArrayList<VersionRecord>( records.size() );

        for ( final VersionRecord record : records ) {
            result.add( new PortableVersionRecord( record.id(), record.author(), record.comment(), record.date(), record.uri() ) );
        }

        return result;
    }

    @Override
    public Path restore( final Path _path,
                         final String comment ) {
        final org.kie.commons.java.nio.file.Path path = paths.convert( _path );

        final org.kie.commons.java.nio.file.Path target = path.getFileSystem().getPath( path.toString() );

        return paths.convert( ioService.copy( path, target, REPLACE_EXISTING, new CommentedOption( identity.getName(), comment ) ) );
    }
}
