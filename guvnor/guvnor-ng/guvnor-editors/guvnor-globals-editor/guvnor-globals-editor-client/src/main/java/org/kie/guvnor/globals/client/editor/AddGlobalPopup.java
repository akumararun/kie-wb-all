/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.guvnor.globals.client.editor;

import javax.enterprise.context.Dependent;

import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.HelpInline;
import com.github.gwtbootstrap.client.ui.ListBox;
import com.github.gwtbootstrap.client.ui.Modal;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import org.kie.guvnor.globals.client.resources.i18n.GlobalsEditorConstants;
import org.uberfire.client.mvp.Command;

@Dependent
public class AddGlobalPopup extends Composite {

    interface AddGlobalPopupBinder
            extends
            UiBinder<Widget, AddGlobalPopup> {

    }

    private static AddGlobalPopupBinder uiBinder = GWT.create( AddGlobalPopupBinder.class );

    @UiField
    ControlGroup aliasGroup;

    @UiField
    TextBox aliasTextBox;

    @UiField
    HelpInline aliasHelpInline;

    @UiField
    ControlGroup classNameGroup;

    @UiField
    ListBox classNameListBox;

    @UiField
    HelpInline classNameHelpInline;

    @UiField
    Modal popup;

    private Command okCommand;

    public AddGlobalPopup() {
        initWidget( uiBinder.createAndBindUi( this ) );
        popup.setDynamicSafe( true );
        aliasTextBox.addKeyPressHandler( new KeyPressHandler() {
            @Override
            public void onKeyPress( final KeyPressEvent event ) {
                aliasGroup.setType( ControlGroupType.NONE );
                aliasHelpInline.setText( "" );
            }
        } );
    }

    @UiHandler("okButton")
    public void onOKButtonClick( final ClickEvent e ) {

        boolean hasError = false;
        if ( aliasTextBox.getText() == null || aliasTextBox.getText().trim().isEmpty() ) {
            aliasGroup.setType( ControlGroupType.ERROR );
            aliasHelpInline.setText( GlobalsEditorConstants.INSTANCE.aliasIsMandatory() );
            hasError = true;
        } else {
            aliasGroup.setType( ControlGroupType.NONE );
        }

        if ( classNameListBox.getSelectedIndex() < 0 ) {
            classNameGroup.setType( ControlGroupType.ERROR );
            classNameHelpInline.setText( GlobalsEditorConstants.INSTANCE.classNameIsMandatory() );
            hasError = true;
        } else {
            classNameGroup.setType( ControlGroupType.NONE );
        }

        if ( hasError ) {
            return;
        }

        if ( okCommand != null ) {
            okCommand.execute();
        }
        hide();
    }

    public String getAlias() {
        return aliasTextBox.getText();
    }

    public String getClassName() {
        return classNameListBox.getValue();
    }

    @UiHandler("cancelButton")
    public void onCancelButtonClick( final ClickEvent e ) {
        hide();
    }

    public void hide() {
        popup.hide();
    }

    public void setContent( final Command okCommand,
                            final String[] allClassNames ) {
        this.okCommand = okCommand;
        this.classNameListBox.clear();
        this.aliasTextBox.setText( "" );
        for ( String className : allClassNames ) {
            classNameListBox.addItem( className );
        }
    }

    public void show() {
        popup.show();
    }

}
