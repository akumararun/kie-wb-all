package org.kie.guvnor.projectconfigscreen.client.forms;

import javax.inject.Inject;

import com.google.gwt.user.client.Window;
import org.drools.guvnor.models.commons.shared.imports.Imports;
import org.kie.workbench.widgets.configresource.client.resources.i18n.ImportConstants;
import org.kie.workbench.widgets.configresource.client.widget.unbound.ImportsWidgetPresenter;
import org.kie.workbench.widgets.metadata.client.resources.i18n.MetadataConstants;
import org.kie.workbench.widgets.metadata.client.widget.MetadataWidget;
import org.kie.guvnor.commons.ui.client.resources.i18n.CommonConstants;
import org.kie.guvnor.services.metadata.model.Metadata;
import org.uberfire.backend.vfs.Path;
import org.uberfire.client.common.BusyPopup;
import org.uberfire.client.common.MultiPageEditorView;
import org.uberfire.client.common.Page;

public class ProjectConfigScreenViewImpl
        extends MultiPageEditorView
        implements ProjectConfigScreenView {

    private final ImportsWidgetPresenter importsWidget;

    private MetadataWidget metadataWidget;

    private Presenter presenter;

    @Inject
    public ProjectConfigScreenViewImpl( final ImportsWidgetPresenter importsWidget,
                                        final MetadataWidget metadataWidget ) {
        this.importsWidget = importsWidget;
        this.metadataWidget = metadataWidget;
        addPage( new Page( importsWidget,
                           ImportConstants.INSTANCE.Imports() ) {
            @Override
            public void onFocus() {
            }

            @Override
            public void onLostFocus() {
            }
        } );

        addPage( new Page( metadataWidget,
                           MetadataConstants.INSTANCE.Metadata() ) {
            @Override
            public void onFocus() {
                presenter.onShowMetadata();
            }

            @Override
            public void onLostFocus() {
            }
        } );
    }

    @Override
    public void setPresenter( final Presenter presenter ) {
        this.presenter = presenter;
    }

    @Override
    public void setImports( final Path path,
                            final Imports imports ) {
        importsWidget.setContent( imports,
                                  false );
    }

    @Override
    public void setMetadata( final Metadata metadata ) {
        metadataWidget.setContent( metadata,
                                   false );
    }

    @Override
    public Metadata getMetadata() {
        return metadataWidget.getContent();
    }

    @Override
    public boolean isDirty() {
        return importsWidget.isDirty();
    }

    @Override
    public void setNotDirty() {
        importsWidget.setNotDirty();
    }

    @Override
    public boolean confirmClose() {
        return Window.confirm( CommonConstants.INSTANCE.DiscardUnsavedData() );
    }

    @Override
    public void alertReadOnly() {
        Window.alert( CommonConstants.INSTANCE.CantSaveReadOnly() );
    }

    @Override
    public void showBusyIndicator( final String message ) {
        BusyPopup.showMessage( message );
    }

    @Override
    public void hideBusyIndicator() {
        BusyPopup.close();
    }

}
