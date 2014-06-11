package org.kie.workbench.common.screens.projecteditor.client.forms;

import com.github.gwtbootstrap.client.ui.Icon;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class GAVEditorViewImpl
        extends Composite
        implements GAVEditorView {

    interface Binder
            extends UiBinder<Widget, GAVEditorViewImpl> {

    }

    private static Binder uiBinder = GWT.create( Binder.class );

    @UiField
    TextBox groupIdTextBox;

    @UiField
    Icon gavHelpIcon;

    @UiField
    Icon gavHelpIcon2;

    @UiField
    Icon gavHelpIcon3;

    @UiField
    TextBox artifactIdTextBox;

    @UiField
    TextBox versionTextBox;

    private Presenter presenter;

    public GAVEditorViewImpl() {
        initWidget( uiBinder.createAndBindUi( this ) );

        gavHelpIcon.getElement().getStyle().setPaddingLeft( 10, Style.Unit.PX );
        gavHelpIcon.getElement().getStyle().setCursor( Style.Cursor.POINTER );
        gavHelpIcon2.getElement().getStyle().setPaddingLeft( 10, Style.Unit.PX );
        gavHelpIcon2.getElement().getStyle().setCursor( Style.Cursor.POINTER );
        gavHelpIcon3.getElement().getStyle().setPaddingLeft( 10, Style.Unit.PX );
        gavHelpIcon3.getElement().getStyle().setCursor( Style.Cursor.POINTER );
    }

    @Override
    public void setPresenter( Presenter presenter ) {
        this.presenter = presenter;
    }

    @Override
    public void setGroupId( String id ) {
        groupIdTextBox.setText( id );
    }

    @Override
    public void setArtifactId( String id ) {
        artifactIdTextBox.setText( id );
    }

    @Override
    public void setVersion( String version ) {
        versionTextBox.setText( version );
    }

    @Override
    public void setReadOnly() {
        groupIdTextBox.setReadOnly( true );
        artifactIdTextBox.setReadOnly( true );
        versionTextBox.setReadOnly( true );
    }

    @UiHandler("groupIdTextBox")
    //Use KeyUpEvent as ValueChangeEvent is only fired when the focus is lost
    public void onGroupIdChange( KeyUpEvent event ) {
        presenter.onGroupIdChange( groupIdTextBox.getText() );
    }

    @UiHandler("artifactIdTextBox")
    //Use KeyUpEvent as ValueChangeEvent is only fired when the focus is lost
    public void onArtifactIdChange( KeyUpEvent event ) {
        presenter.onArtifactIdChange( artifactIdTextBox.getText() );
    }

    @UiHandler("versionTextBox")
    //Use KeyUpEvent as ValueChangeEvent is only fired when the focus is lost
    public void onVersionChange( KeyUpEvent event ) {
        presenter.onVersionChange( versionTextBox.getText() );
    }

}
