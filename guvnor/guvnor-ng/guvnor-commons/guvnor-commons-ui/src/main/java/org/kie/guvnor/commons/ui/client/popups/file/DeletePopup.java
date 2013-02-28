/*
 * Copyright 2005 JBoss Inc
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

package org.kie.guvnor.commons.ui.client.popups.file;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;
import org.kie.guvnor.commons.ui.client.resources.CommonImages;
import org.uberfire.client.common.FormStylePopup;

public class DeletePopup extends FormStylePopup {

    final private TextBox checkInCommentTextBox = new TextBox();

    public DeletePopup( final CommandWithCommitMessage command ) {
        super( CommonImages.INSTANCE.edit(), "Delete this item" );

        checkInCommentTextBox.setTitle( "Check in comment" );
        checkInCommentTextBox.setWidth( "200px" );
        addAttribute( "Check in comment:", checkInCommentTextBox );

        final HorizontalPanel hp = new HorizontalPanel();
        final Button create = new Button( "Delete item" );
        create.addClickHandler( new ClickHandler() {
            public void onClick( ClickEvent arg0 ) {

                if ( !Window.confirm( "Are you sure you want to delete this asset?" ) ) {
                    return;
                }

                hide();
                command.execute( checkInCommentTextBox.getText() );
            }
        } );
        hp.add( create );

        Button cancel = new Button( "Cancel" );
        cancel.addClickHandler( new ClickHandler() {
            public void onClick( ClickEvent arg0 ) {
                hide();
            }
        } );
        hp.add( new HTML( "&nbsp" ) );
        hp.add( cancel );
        addAttribute( "", hp );

    }

}
