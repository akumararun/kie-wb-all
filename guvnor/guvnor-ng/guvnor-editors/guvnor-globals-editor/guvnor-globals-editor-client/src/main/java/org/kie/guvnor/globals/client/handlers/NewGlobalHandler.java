package org.kie.guvnor.globals.client.handlers;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.api.Caller;
import org.kie.guvnor.commons.ui.client.handlers.DefaultNewResourceHandler;
import org.kie.guvnor.commons.ui.client.popups.file.CommandWithCommitMessage;
import org.kie.guvnor.commons.ui.client.popups.file.SaveOperationService;
import org.kie.guvnor.globals.client.resources.i18n.GlobalsEditorConstants;
import org.kie.guvnor.globals.client.resources.images.GlobalsEditorImageResources;
import org.kie.guvnor.globals.client.type.GlobalResourceType;
import org.kie.guvnor.globals.model.GlobalsModel;
import org.kie.guvnor.globals.service.GlobalsEditorService;
import org.uberfire.backend.vfs.Path;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.shared.mvp.PlaceRequest;
import org.uberfire.shared.mvp.impl.PathPlaceRequest;

/**
 * Handler for the creation of new DRL Text Rules
 */
@ApplicationScoped
public class NewGlobalHandler extends DefaultNewResourceHandler {

    @Inject
    private Caller<GlobalsEditorService> globalsService;

    @Inject
    private PlaceManager placeManager;

    @Inject
    private GlobalResourceType resourceType;

    @Override
    public String getDescription() {
        return GlobalsEditorConstants.INSTANCE.newGlobalDescription();
    }

    @Override
    public IsWidget getIcon() {
        return new Image( GlobalsEditorImageResources.INSTANCE.globalsIcon() );
    }

    @Override
    public void create( final Path contextPath,
                        final String baseFileName ) {
        final GlobalsModel model = new GlobalsModel();

        new SaveOperationService().save( contextPath,
                                         new CommandWithCommitMessage() {
                                             @Override
                                             public void execute( final String comment ) {
                                                 globalsService.call( new RemoteCallback<Path>() {
                                                     @Override
                                                     public void callback( final Path path ) {
                                                         notifySuccess();
                                                         final PlaceRequest place = new PathPlaceRequest( path );
                                                         placeManager.goTo( place );
                                                     }
                                                 } ).create( contextPath,
                                                             buildFileName( resourceType,
                                                                            baseFileName ),
                                                             model,
                                                             comment );
                                             }
                                         } );
    }

}
