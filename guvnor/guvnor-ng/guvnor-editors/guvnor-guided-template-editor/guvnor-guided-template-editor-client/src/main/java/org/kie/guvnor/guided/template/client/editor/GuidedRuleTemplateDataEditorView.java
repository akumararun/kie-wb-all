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

package org.kie.guvnor.guided.template.client.editor;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.drools.guvnor.models.guided.template.shared.TemplateModel;
import org.kie.guvnor.datamodel.oracle.DataModelOracle;
import org.kie.guvnor.guided.rule.client.resources.i18n.Constants;

public class GuidedRuleTemplateDataEditorView
        extends Composite
        implements GuidedRuleTemplateEditorPresenter.DataView {

    private final VerticalPanel widgetContainer = new VerticalPanel();

    public GuidedRuleTemplateDataEditorView() {
        initWidget( widgetContainer );
    }

    @Override
    public void setContent( final TemplateModel model,
                            final DataModelOracle dataModel,
                            final EventBus eventBus,
                            final boolean isReadOnly ) {

        //Initialise table to edit data. The widget needs to be added after the containing panel has
        //been added to the DOM and rendered by the browser as the Merged Grid widget needs the
        //parent panel sizes.
        Scheduler.get().scheduleDeferred( new Command() {

            @Override
            public void execute() {
                widgetContainer.clear();
                final TemplateDataTableWidget dataTable = new TemplateDataTableWidget( model,
                                                                                       dataModel,
                                                                                       isReadOnly,
                                                                                       eventBus );
                final Button btnAddRow = new Button( Constants.INSTANCE.AddRow(),
                                                     new ClickHandler() {

                                                         public void onClick( ClickEvent event ) {
                                                             dataTable.appendRow();
                                                         }

                                                     } );

                widgetContainer.add( btnAddRow );
                widgetContainer.add( dataTable );
            }
        } );
    }

    @Override
    public void clear() {
        widgetContainer.clear();
    }
}
