package org.kie.guvnor.commons.ui.client.callbacks;

import org.jboss.errai.bus.client.api.Message;
import org.kie.commons.validation.PortablePreconditions;
import org.kie.guvnor.commons.ui.client.widget.HasBusyIndicator;

/**
 * Default Error handler for all views that support HasBusyIndicator
 */
public class HasBusyIndicatorDefaultErrorCallback extends DefaultErrorCallback {

    private HasBusyIndicator view;

    public HasBusyIndicatorDefaultErrorCallback( final HasBusyIndicator view ) {
        this.view = PortablePreconditions.checkNotNull( "view",
                                                        view );
    }

    @Override
    public boolean error( final Message message,
                          final Throwable throwable ) {
        view.hideBusyIndicator();
        return super.error( message,
                            throwable );
    }

}
