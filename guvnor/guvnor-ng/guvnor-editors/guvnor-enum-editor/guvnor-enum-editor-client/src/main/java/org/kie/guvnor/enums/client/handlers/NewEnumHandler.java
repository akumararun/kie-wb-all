package org.kie.guvnor.enums.client.handlers;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.api.Caller;
import org.kie.guvnor.commons.ui.client.handlers.DefaultNewResourceHandler;
import org.kie.guvnor.commons.ui.client.popups.file.CommandWithCommitMessage;
import org.kie.guvnor.commons.ui.client.popups.file.SaveOperationService;
import org.kie.guvnor.enums.client.resources.i18n.Constants;
import org.kie.guvnor.enums.client.resources.images.ImageResources;
import org.kie.guvnor.enums.client.type.EnumResourceType;
import org.kie.guvnor.enums.service.EnumService;
import org.uberfire.backend.vfs.Path;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.shared.mvp.PlaceRequest;
import org.uberfire.shared.mvp.impl.PathPlaceRequest;

/**
 * Handler for the creation of new Enumerations
 */
@ApplicationScoped
public class NewEnumHandler extends DefaultNewResourceHandler {

    @Inject
    private Caller<EnumService> enumService;

    @Inject
    private PlaceManager placeManager;

    @Inject
    private EnumResourceType resourceType;

    @Override
    public String getDescription() {
        return Constants.INSTANCE.newEnumDescription();
    }

    @Override
    public IsWidget getIcon() {
        return new Image( ImageResources.INSTANCE.enumsIcon() );
    }

    @Override
    public void create( final Path contextPath,
                        final String baseFileName ) {
        new SaveOperationService().save( contextPath,
                                         new CommandWithCommitMessage() {
                                             @Override
                                             public void execute( final String comment ) {
                                                 enumService.call( new RemoteCallback<Path>() {
                                                     @Override
                                                     public void callback( final Path path ) {
                                                         notifySuccess();
                                                         final PlaceRequest place = new PathPlaceRequest( path );
                                                         placeManager.goTo( place );
                                                     }
                                                 } ).create( contextPath,
                                                             buildFileName( resourceType,
                                                                            baseFileName ),
                                                             "",
                                                             comment );
                                             }
                                         } );
    }

}
