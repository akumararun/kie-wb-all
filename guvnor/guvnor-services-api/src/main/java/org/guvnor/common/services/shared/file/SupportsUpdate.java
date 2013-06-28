package org.guvnor.common.services.shared.file;

import org.guvnor.common.services.shared.metadata.model.Metadata;
import org.uberfire.backend.vfs.Path;

public interface SupportsUpdate<T> {

    Path save( final Path path,
               final T content,
               final Metadata metadata,
               final String comment );

}
