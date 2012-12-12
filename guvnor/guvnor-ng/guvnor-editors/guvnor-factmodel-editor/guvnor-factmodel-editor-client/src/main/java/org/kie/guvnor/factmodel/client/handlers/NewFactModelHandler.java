package org.kie.guvnor.factmodel.client.handlers;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.api.Caller;
import org.kie.guvnor.commons.ui.client.handlers.DefaultNewResourceHandler;
import org.kie.guvnor.factmodel.client.resources.ImageResources;
import org.kie.guvnor.factmodel.client.resources.i18n.Constants;
import org.kie.guvnor.factmodel.model.FactModels;
import org.kie.guvnor.factmodel.service.FactModelService;
import org.uberfire.backend.vfs.Path;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.shared.mvp.PlaceRequest;
import org.uberfire.shared.mvp.impl.DefaultPlaceRequest;

/**
 * Handler for the creation of new Fact Models
 */
@ApplicationScoped
public class NewFactModelHandler extends DefaultNewResourceHandler {

    private static String FILE_TYPE = "model.drl";

    @Inject
    private Caller<FactModelService> factModelService;

    @Inject
    private PlaceManager placeManager;

    @Override
    public String getFileType() {
        return FILE_TYPE;
    }

    @Override
    public String getDescription() {
        return Constants.INSTANCE.newFactModelDescription();
    }

    @Override
    public IsWidget getIcon() {
        return new Image( ImageResources.INSTANCE.factModelIcon() );
    }

    @Override
    public void create( final String fileName ) {
        final Path path = buildFullPathName( fileName );
        final FactModels factModel = new FactModels();
        factModelService.call( new RemoteCallback<Void>() {
            @Override
            public void callback( Void aVoid ) {
                notifySuccess();
                final PlaceRequest place = new DefaultPlaceRequest( "FactModelsEditor" );
                place.addParameter( "path:uri",
                                    path.toURI() );
                place.addParameter( "path:name",
                                    path.getFileName() );
                placeManager.goTo( place );
            }
        } ).save( path,
                  factModel );
    }

}
