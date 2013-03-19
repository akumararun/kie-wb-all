package org.kie.guvnor.dtablexls.client.handlers;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import org.jboss.errai.ioc.client.api.Caller;
import org.kie.commons.data.Pair;
import org.kie.guvnor.commons.ui.client.handlers.DefaultNewResourceHandler;
import org.kie.guvnor.commons.ui.client.handlers.NewResourcePresenter;
import org.kie.guvnor.commons.ui.client.popups.file.CommandWithCommitMessage;
import org.kie.guvnor.commons.ui.client.popups.file.SaveOperationService;
import org.kie.guvnor.commons.ui.client.widget.AttachmentFileWidget;
import org.kie.guvnor.commons.ui.client.widget.BusyIndicatorView;
import org.kie.guvnor.dtablexls.client.editor.URLHelper;
import org.kie.guvnor.dtablexls.client.resources.i18n.DecisionTableXLSEditorConstants;
import org.kie.guvnor.dtablexls.client.resources.images.ImageResources;
import org.kie.guvnor.dtablexls.client.type.DecisionTableXLSResourceType;
import org.kie.guvnor.dtablexls.service.DecisionTableXLSService;
import org.uberfire.backend.vfs.Path;
import org.uberfire.backend.vfs.PathFactory;
import org.uberfire.client.common.BusyPopup;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.shared.mvp.PlaceRequest;
import org.uberfire.shared.mvp.impl.PathPlaceRequest;

/**
 * Handler for the creation of new XLS Decision Tables
 */
@ApplicationScoped
public class NewDecisionTableXLSHandler extends DefaultNewResourceHandler {

    @Inject
    private Caller<DecisionTableXLSService> decisionTableXLSService;

    @Inject
    private PlaceManager placeManager;

    @Inject
    private DecisionTableXLSResourceType resourceType;

    @Inject
    private AttachmentFileWidget uploadWidget;

    @Inject
    private BusyIndicatorView busyIndicatorView;

    @PostConstruct
    private void setupExtensions() {
        extensions.add( new Pair<String, AttachmentFileWidget>( DecisionTableXLSEditorConstants.INSTANCE.Upload(),
                                                                uploadWidget ) );
    }

    @Override
    public String getDescription() {
        return DecisionTableXLSEditorConstants.INSTANCE.NewDecisionTableDescription();
    }

    @Override
    public IsWidget getIcon() {
        return new Image( ImageResources.INSTANCE.decisionTableSmall() );
    }

    @Override
    public void create( final Path contextPath,
                        final String baseFileName,
                        final NewResourcePresenter presenter ) {
        new SaveOperationService().save( contextPath,
                                         new CommandWithCommitMessage() {
                                             @Override
                                             public void execute( final String comment ) {
                                                 busyIndicatorView.showBusyIndicator( DecisionTableXLSEditorConstants.INSTANCE.Uploading() );
                                                 uploadWidget.submit( contextPath,
                                                                      buildFileName( resourceType,
                                                                                     baseFileName ),
                                                                      URLHelper.getServletUrl(),
                                                                      new Command() {

                                                                          @Override
                                                                          public void execute() {
                                                                              busyIndicatorView.hideBusyIndicator();
                                                                              presenter.complete();
                                                                              notifySuccess();
                                                                              final Path newPath = PathFactory.newPath( contextPath.getFileSystem(),
                                                                                                                        buildFileName( resourceType,
                                                                                                                                       baseFileName ),
                                                                                                                        contextPath.toURI() );
                                                                              final PlaceRequest place = new PathPlaceRequest( newPath );
                                                                              placeManager.goTo( place );
                                                                          }

                                                                      },
                                                                      new Command() {

                                                                          @Override
                                                                          public void execute() {
                                                                              BusyPopup.close();
                                                                          }
                                                                      }
                                                                    );
                                             }
                                         } );
    }

}
