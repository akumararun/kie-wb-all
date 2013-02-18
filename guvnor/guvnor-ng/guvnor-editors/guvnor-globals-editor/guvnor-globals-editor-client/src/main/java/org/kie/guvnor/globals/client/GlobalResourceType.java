package org.kie.guvnor.globals.client;

import javax.enterprise.context.ApplicationScoped;

import com.google.gwt.user.client.ui.IsWidget;
import org.uberfire.backend.vfs.Path;
import org.uberfire.client.workbench.file.ResourceType;

@ApplicationScoped
public class GlobalResourceType implements ResourceType {

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public IsWidget getIcon() {
        return null;
    }

    @Override
    public boolean accept( final Path path ) {
        return path.getFileName().endsWith( ".global.drl" );
    }
}
