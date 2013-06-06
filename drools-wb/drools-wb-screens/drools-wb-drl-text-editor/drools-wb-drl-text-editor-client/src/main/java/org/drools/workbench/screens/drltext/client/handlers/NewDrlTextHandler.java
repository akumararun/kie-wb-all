package org.drools.workbench.screens.drltext.client.handlers;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import org.drools.workbench.screens.drltext.client.resources.i18n.DRLTextEditorConstants;
import org.drools.workbench.screens.drltext.client.resources.images.ImageResources;
import org.drools.workbench.screens.drltext.client.type.DRLResourceType;
import org.drools.workbench.screens.drltext.service.DRLTextEditorService;
import org.jboss.errai.ioc.client.api.Caller;
import org.kie.workbench.common.widgets.client.callbacks.HasBusyIndicatorDefaultErrorCallback;
import org.kie.workbench.common.widgets.client.handlers.DefaultNewResourceHandler;
import org.kie.workbench.common.widgets.client.handlers.NewResourcePresenter;
import org.kie.workbench.common.widgets.client.resources.i18n.CommonConstants;
import org.kie.workbench.common.widgets.client.widget.BusyIndicatorView;
import org.uberfire.backend.vfs.Path;
import org.uberfire.client.mvp.PlaceManager;

/**
 * Handler for the creation of new DRL Text Rules
 */
@ApplicationScoped
public class NewDrlTextHandler extends DefaultNewResourceHandler {

    @Inject
    private Caller<DRLTextEditorService> drlTextService;

    @Inject
    private PlaceManager placeManager;

    @Inject
    private DRLResourceType resourceType;

    @Inject
    private BusyIndicatorView busyIndicatorView;

    @Override
    public String getDescription() {
        return DRLTextEditorConstants.INSTANCE.NewDrlDescription();
    }

    @Override
    public IsWidget getIcon() {
        return new Image( ImageResources.INSTANCE.classImage() );
    }

    @Override
    public void create( final Path contextPath,
                        final String baseFileName,
                        final NewResourcePresenter presenter ) {
        busyIndicatorView.showBusyIndicator( CommonConstants.INSTANCE.Saving() );
        drlTextService.call( getSuccessCallback( presenter ),
                             new HasBusyIndicatorDefaultErrorCallback( busyIndicatorView ) ).create( contextPath,
                                                                                                     buildFileName( resourceType,
                                                                                                                    baseFileName ),
                                                                                                     "",
                                                                                                     "" );
    }

}
