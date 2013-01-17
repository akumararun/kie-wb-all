package org.kie.guvnor.datamodel.backend.server;

import org.kie.commons.java.nio.file.Path;
import org.kie.guvnor.builder.DefaultBuilderFilter;

/**
 * A filter to ensure only Model related resources are included
 */
public class ModelBuilderFilter extends DefaultBuilderFilter {

    private static final String[] PATTERNS = new String[]{ "pom.xml", ".model.drl" };

    @Override
    public boolean accept( final Path path ) {
        boolean accept = super.accept( path );
        if ( !accept ) {
            return accept;
        }

        final String uri = path.toUri().toString();
        for ( final String pattern : PATTERNS ) {
            if ( uri.substring( uri.length() - pattern.length() ).equals( pattern ) ) {
                return true;
            }
        }
        return false;
    }
}
