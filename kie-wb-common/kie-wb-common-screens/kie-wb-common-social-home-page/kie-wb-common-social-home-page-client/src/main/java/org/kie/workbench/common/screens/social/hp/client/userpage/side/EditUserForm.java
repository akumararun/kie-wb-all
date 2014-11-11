/*
 * Copyright 2013 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.workbench.common.screens.social.hp.client.userpage.side;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import org.kie.uberfire.social.activities.model.SocialUser;
import org.uberfire.ext.widgets.common.client.common.popups.BaseModal;
import org.uberfire.mvp.ParameterizedCommand;

@Dependent
public class EditUserForm
        extends PopupPanel {

    private SocialUser socialUser;

    @UiField
    Button save;

    @UiField
    Button cancel;

    @UiField
    BaseModal popup;

    @UiField
    TextBox emailTextBox;

    @UiField
    TextBox realNameTextBox;

    ParameterizedCommand<SocialUser> updateCommand;

    interface Binder
            extends
            UiBinder<Widget, EditUserForm> {

    }

    private static Binder uiBinder = GWT.create( Binder.class );

    @PostConstruct
    public void init() {
        setWidget( uiBinder.createAndBindUi( this ) );

    }

    @UiHandler("save")
    void save( final ClickEvent event ) {
        socialUser.setEmail( emailTextBox.getText() );
        socialUser.setRealName( realNameTextBox.getText() );
        popup.hide();
        updateCommand.execute( socialUser );
    }

    @UiHandler("cancel")
    void cancel( final ClickEvent event ) {
        popup.hide();
    }

    public void show( SocialUser socialUser,
                      ParameterizedCommand<SocialUser> updateCommand ) {
        this.socialUser = socialUser;
        this.updateCommand = updateCommand;
        this.emailTextBox.setText( socialUser.getEmail() );
        this.realNameTextBox.setText( socialUser.getRealName() );
        popup.show();
    }

}
