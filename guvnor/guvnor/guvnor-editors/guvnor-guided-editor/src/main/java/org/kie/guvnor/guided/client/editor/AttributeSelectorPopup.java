/*
 * Copyright 2012 JBoss Inc
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

package org.kie.guvnor.guided.client.editor;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import org.kie.guvnor.guided.client.resources.DroolsGuvnorImageResources;
import org.kie.guvnor.guided.client.resources.DroolsGuvnorImages;
import org.kie.guvnor.guided.client.resources.i18n.Constants;
import org.kie.guvnor.guided.model.RuleAttribute;
import org.kie.guvnor.guided.model.RuleMetadata;
import org.kie.guvnor.guided.model.RuleModel;
import org.uberfire.client.common.DirtyableHorizontalPane;
import org.uberfire.client.common.FormStylePopup;
import org.uberfire.client.common.InfoPopup;

public class AttributeSelectorPopup extends FormStylePopup {

    private final ListBox list = RuleAttributeWidget.getAttributeList();

    private final TextBox box = new TextBox();

    public AttributeSelectorPopup( final RuleModel model,
                                   boolean lockLHS,
                                   boolean lockRHS,
                                   final Command refresh ) {
        super( getImage(),
               Constants.INSTANCE.AddAnOptionToTheRule() );

        setTextBox( model,
                    refresh );

        setListBox( model,
                    refresh );

        setFreezePanel( model,
                        lockLHS,
                        lockRHS,
                        refresh );

    }

    private static Image getImage() {
        Image image = new Image( DroolsGuvnorImageResources.INSTANCE.config() );
        image.setAltText( Constants.INSTANCE.Config() );
        return image;
    }

    private void setTextBox( final RuleModel model,
                             final Command refresh ) {
        box.setVisibleLength( 15 );

        DirtyableHorizontalPane horiz = new DirtyableHorizontalPane();
        horiz.add( box );
        horiz.add( getAddButton( model,
                                 refresh,
                                 box ) );

        addAttribute( Constants.INSTANCE.Metadata3(),
                      horiz );

    }

    private void setListBox( final RuleModel model,
                             final Command refresh ) {
        list.setSelectedIndex( 0 );

        list.addChangeHandler( new ChangeHandler() {
            public void onChange( ChangeEvent event ) {
                String attr = list.getItemText( list.getSelectedIndex() );
                if ( attr.equals( RuleAttributeWidget.LOCK_LHS ) || attr.equals( RuleAttributeWidget.LOCK_RHS ) ) {
                    model.addMetadata( new RuleMetadata( attr,
                                                         "true" ) );
                } else {
                    model.addAttribute( new RuleAttribute( attr,
                                                           "" ) );
                }
                refresh.execute();
                hide();
            }
        } );
        addAttribute( Constants.INSTANCE.Attribute1(),
                      list );

    }

    private void setFreezePanel( final RuleModel model,
                                 boolean lockLHS,
                                 boolean lockRHS,
                                 final Command refresh ) {
        Button freezeConditions = new Button( Constants.INSTANCE.Conditions() );
        freezeConditions.addClickHandler( new ClickHandler() {

            public void onClick( ClickEvent event ) {
                model.addMetadata( new RuleMetadata( RuleAttributeWidget.LOCK_LHS,
                                                     "true" ) );
                refresh.execute();
                hide();
            }
        } );
        Button freezeActions = new Button( Constants.INSTANCE.Actions() );
        freezeActions.addClickHandler( new ClickHandler() {

            public void onClick( ClickEvent event ) {
                model.addMetadata( new RuleMetadata( RuleAttributeWidget.LOCK_RHS,
                                                     "true" ) );
                refresh.execute();
                hide();
            }
        } );
        HorizontalPanel hz = new HorizontalPanel();
        if ( !lockLHS ) {
            hz.add( freezeConditions );
        }
        if ( !lockRHS ) {
            hz.add( freezeActions );
        }
        hz.add( new InfoPopup( Constants.INSTANCE.FrozenAreas(),
                               Constants.INSTANCE.FrozenExplanation() ) );

        if ( hz.getWidgetCount() > 1 ) {
            addAttribute( Constants.INSTANCE.FreezeAreasForEditing(),
                          hz );
        }
    }

    private Image getAddButton( final RuleModel model,
                                final Command refresh,
                                final TextBox box ) {
        final Image addbutton = DroolsGuvnorImages.INSTANCE.NewItem();
        addbutton.setAltText( Constants.INSTANCE.AddMetadataToTheRule() );
        addbutton.setTitle( Constants.INSTANCE.AddMetadataToTheRule() );

        addbutton.addClickHandler( new ClickHandler() {

            public void onClick( ClickEvent event ) {

                model.addMetadata( new RuleMetadata( box.getText(),
                                                     "" ) );
                refresh.execute();
                hide();
            }
        } );
        return addbutton;
    }

}
