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

package org.kie.workbench.common.widgets.errors.client.widget;

import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ScrollPanel;
import org.kie.workbench.common.services.shared.validation.model.BuilderResult;
import org.kie.workbench.common.services.shared.validation.model.BuilderResultLine;
import org.kie.workbench.common.widgets.errors.client.resources.ImageResources;
import org.kie.workbench.common.widgets.errors.client.resources.i18n.Constants;
import org.uberfire.client.common.FormStylePopup;
import org.uberfire.client.common.SmallLabel;

public class ShowBuilderErrorsWidget extends FormStylePopup {

    public ShowBuilderErrorsWidget( final BuilderResult result ) {
        super();
        if ( result == null || result.getLines() == null || result.getLines().size() == 0 ) {
            setWidth( 200 + "px" );
            setTitle( Constants.INSTANCE.ValidationResultsDotDot() );
            final HorizontalPanel h = new HorizontalPanel();
            h.add( new SmallLabel( AbstractImagePrototype.create( ImageResources.INSTANCE.greenTick() ).getHTML() + "<i>"
                                           + Constants.INSTANCE.ItemValidatedSuccessfully() + "</i>" ) );
            addRow( h );
        } else {
            setup( ImageResources.INSTANCE.packageBuilder(), Constants.INSTANCE.ValidationResults() );

            final FlexTable errTable = new FlexTable();
            errTable.setStyleName( "build-Results" ); //NON-NLS
            for ( int i = 0; i < result.getLines().size(); i++ ) {
                int row = i;
                final BuilderResultLine res = result.getLines().get( i );
                errTable.setWidget( row,
                                    0,
                                    new Image( ImageResources.INSTANCE.error() ) );
                errTable.setText( row,
                                  1,
                                  "[" + res.getResourceName() + "] " + res.getMessage() );
            }
            final ScrollPanel scroll = new ScrollPanel( errTable );
            scroll.setWidth( "100%" );
            addRow( scroll );
        }
    }
}
