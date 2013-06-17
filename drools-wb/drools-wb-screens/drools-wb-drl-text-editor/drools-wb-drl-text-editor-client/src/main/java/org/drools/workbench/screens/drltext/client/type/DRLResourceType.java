package org.drools.workbench.screens.drltext.client.type;

import javax.enterprise.context.ApplicationScoped;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import org.drools.workbench.screens.drltext.client.resources.DRLTextEditorResources;
import org.drools.workbench.screens.drltext.type.DRLResourceTypeDefinition;
import org.uberfire.client.workbench.type.ClientResourceType;

@ApplicationScoped
public class DRLResourceType
        extends DRLResourceTypeDefinition
        implements ClientResourceType {

    private static final Image IMAGE = new Image( DRLTextEditorResources.INSTANCE.images().DRLIcon() );

    @Override
    public IsWidget getIcon() {
        return IMAGE;
    }
}
