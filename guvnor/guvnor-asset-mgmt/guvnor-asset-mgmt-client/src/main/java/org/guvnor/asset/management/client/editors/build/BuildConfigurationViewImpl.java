/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
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
package org.guvnor.asset.management.client.editors.build;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.Input;
import org.gwtbootstrap3.client.ui.ListBox;
import org.gwtbootstrap3.client.ui.TextBox;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.workbench.events.NotificationEvent;

@Dependent
public class BuildConfigurationViewImpl extends Composite
        implements BuildConfigurationPresenter.BuildConfigurationView {

    interface Binder
            extends UiBinder<Widget, BuildConfigurationViewImpl> {

    }

    private static Binder uiBinder = GWT.create( Binder.class );

    @Inject
    private PlaceManager placeManager;

    private BuildConfigurationPresenter presenter;

    @UiField
    public ListBox chooseRepositoryBox;

    @UiField
    public ListBox chooseBranchBox;

    @UiField
    public ListBox chooseProjectBox;

    @UiField
    public Button buildButton;

    @UiField
    public TextBox userNameText;

    @UiField
    public Input passwordText;

    @UiField
    public TextBox serverURLText;

    @UiField
    public CheckBox deployToRuntimeCheck;

    @UiField
    public FormGroup deployToRuntimeRow;

    @UiField
    public FormGroup usernameRow;

    @UiField
    public FormGroup passwordRow;

    @UiField
    public FormGroup serverURLRow;

    @Inject
    private Event<NotificationEvent> notification;

    public BuildConfigurationViewImpl() {
        initWidget( uiBinder.createAndBindUi( this ) );
    }

    @Override
    public void init( final BuildConfigurationPresenter presenter ) {
        this.presenter = presenter;
        presenter.loadServerSetting();
        chooseRepositoryBox.addChangeHandler( new ChangeHandler() {

            @Override
            public void onChange( ChangeEvent event ) {
                String value = chooseRepositoryBox.getSelectedValue();

                presenter.loadBranches( value );
            }
        } );

        chooseBranchBox.addChangeHandler( new ChangeHandler() {

            @Override
            public void onChange( ChangeEvent event ) {
                String repo = chooseRepositoryBox.getSelectedValue();
                String branch = chooseBranchBox.getSelectedValue();

                presenter.loadProjects( repo, branch );
            }
        } );

        presenter.loadRepositories();

    }

    public void showHideDeployToRuntimeSection( boolean show ) {
        if ( show ) {
            deployToRuntimeRow.setVisible( true );
            usernameRow.setVisible( true );
            passwordRow.setVisible( true );
            serverURLRow.setVisible( true );

            // by default deploy to runtime inputs are disabled
            userNameText.setEnabled( false );
            passwordText.setEnabled( false );
            serverURLText.setEnabled( false );

            deployToRuntimeCheck.addValueChangeHandler( new ValueChangeHandler<Boolean>() {
                @Override
                public void onValueChange( ValueChangeEvent<Boolean> event ) {
                    if ( event.getValue() ) {
                        userNameText.setEnabled( true );
                        passwordText.setEnabled( true );
                        serverURLText.setEnabled( true );
                    } else {
                        userNameText.setEnabled( false );
                        passwordText.setEnabled( false );
                        serverURLText.setEnabled( false );
                    }
                }
            } );
        } else {
            deployToRuntimeRow.setVisible( false );
            usernameRow.setVisible( false );
            passwordRow.setVisible( false );
            serverURLRow.setVisible( false );
        }
    }

    @UiHandler( "buildButton" )
    public void buildButton( ClickEvent e ) {
        presenter.buildProject( chooseRepositoryBox.getSelectedValue(), chooseBranchBox.getSelectedValue(), chooseProjectBox.getSelectedValue(),
                                userNameText.getText(), passwordText.getText(), serverURLText.getText(), deployToRuntimeCheck.getValue() );

    }

    @Override
    public void displayNotification( String text ) {
        notification.fire( new NotificationEvent( text ) );
    }

    @Override
    public ListBox getChooseBranchBox() {
        return chooseBranchBox;
    }

    @Override
    public ListBox getChooseRepositoryBox() {
        return chooseRepositoryBox;
    }

    @Override
    public ListBox getChooseProjectBox() {
        return chooseProjectBox;
    }

    public Button getBuildButton() {
        return buildButton;
    }

}