package org.drools.workbench.screens.dtablexls.client.type;

import javax.enterprise.context.ApplicationScoped;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import org.drools.workbench.screens.dtablexls.client.resources.DecisionTableXLSResources;
import org.drools.workbench.screens.dtablexls.type.DecisionTableXLSResourceTypeDefinition;
import org.uberfire.client.workbench.type.ClientResourceType;

@ApplicationScoped
public class DecisionTableXLSResourceType
        extends DecisionTableXLSResourceTypeDefinition
        implements ClientResourceType {

    private static final Image IMAGE = new Image( DecisionTableXLSResources.INSTANCE.images().decisionTableIcon() );

    @Override
    public IsWidget getIcon() {
        return null;
    }

}
