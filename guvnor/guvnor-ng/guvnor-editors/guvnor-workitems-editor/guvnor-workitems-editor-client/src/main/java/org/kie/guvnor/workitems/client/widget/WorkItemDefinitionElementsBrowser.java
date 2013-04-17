/*
 * Copyright 2010 JBoss Inc
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
package org.kie.guvnor.workitems.client.widget;

import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.kie.guvnor.workitems.client.resources.i18n.WorkItemsEditorConstants;
import org.kie.guvnor.workitems.model.WorkItemsMetaContent;
import org.uberfire.client.mvp.UberView;

public class WorkItemDefinitionElementsBrowser extends Composite implements HasWorkItemMetaData,
                                                                            UberView<WorkItemDefinitionElementSelectedListener> {

    @Inject
    private WorkItemMetaDataLoader metaDataLoader;

    private final VerticalPanel mainPanel = new VerticalPanel();
    private final VerticalPanel elementsPanel = new VerticalPanel();
    private final ListBox importsList = new ListBox();
    private final ListBox imagesList = new ListBox();

    private WorkItemDefinitionElementSelectedListener presenter;

    private class PanelButton extends Button {

        public PanelButton( final String html,
                            final String pasteValue ) {
            super( html );
            this.setWidth( "100px" );
            this.addClickHandler( new LeafClickHandler( html,
                                                        pasteValue ) );
        }

    }

    private class LeafClickHandler implements ClickHandler {

        final private String title;
        final private String pasteValue;

        public LeafClickHandler( final String title,
                                 final String pasteValue ) {
            this.title = title;
            this.pasteValue = pasteValue;
        }

        public void onClick( final ClickEvent event ) {
            elementSelected( this.title,
                             this.pasteValue );
        }
    }

    public WorkItemDefinitionElementsBrowser() {
        mainPanel.add( new Label( WorkItemsEditorConstants.INSTANCE.BrowserTitle() ) );
        mainPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_CENTER );
        mainPanel.setSpacing( 5 );

        //Element definitions
        elementsPanel.setSpacing( 5 );
        mainPanel.add( elementsPanel );

        //Imports
        mainPanel.add( importsList );
        importsList.addItem( WorkItemsEditorConstants.INSTANCE.ChooseImportClass() );
        importsList.addItem( "BooleanDataType",
                             "import org.drools.core.process.core.datatype.impl.type.BooleanDataType;" );
        importsList.addItem( "EnumDataType",
                             "import org.drools.core.process.core.datatype.impl.type.EnumDataType;" );
        importsList.addItem( "FloatDataType",
                             "import org.drools.core.process.core.datatype.impl.type.FloatDataType;" );
        importsList.addItem( "IntegerDataType",
                             "import org.drools.core.process.core.datatype.impl.type.IntegerDataType;" );
        importsList.addItem( "ListDataType",
                             "import org.drools.core.process.core.datatype.impl.type.ListDataType;" );
        importsList.addItem( "ObjectDataType",
                             "import org.drools.core.process.core.datatype.impl.type.ObjectDataType;" );
        importsList.addItem( "StringDataType",
                             "import org.drools.core.process.core.datatype.impl.type.StringDataType;" );
        importsList.addItem( "UndefinedDataType",
                             "import org.drools.core.process.core.datatype.impl.type.UndefinedDataType;" );

        importsList.setVisibleItemCount( 9 );
        importsList.setSelectedIndex( 0 );

        importsList.addChangeHandler( new ChangeHandler() {
            public void onChange( ChangeEvent event ) {
                elementSelected( importsList.getItemText( importsList.getSelectedIndex() ),
                                 importsList.getValue( importsList.getSelectedIndex() ) );
            }
        } );

        //Images
        mainPanel.add( imagesList );
        imagesList.addItem( WorkItemsEditorConstants.INSTANCE.ChooseIcon() );
        imagesList.setVisibleItemCount( 1 );
        imagesList.setSelectedIndex( 0 );

        imagesList.addChangeHandler( new ChangeHandler() {
            public void onChange( ChangeEvent event ) {
                elementSelected( imagesList.getItemText( imagesList.getSelectedIndex() ),
                                 imagesList.getValue( imagesList.getSelectedIndex() ) );
            }
        } );

        initWidget( mainPanel );
    }

    @PostConstruct
    public void loadMetaData() {
        metaDataLoader.loadMetaContent( this );
    }

    @Override
    public void init( final WorkItemDefinitionElementSelectedListener presenter ) {
        this.presenter = presenter;
    }

    @Override
    public void setMetaData( final WorkItemsMetaContent metaData ) {
        final List<String> workItemImages = metaData.getWorkItemImages();
        final Map<String, String> workItemElementDefinitions = metaData.getWorkItemElementDefinitions();
        for ( Map.Entry<String, String> entry : workItemElementDefinitions.entrySet() ) {
            elementsPanel.add( new PanelButton( entry.getKey(),
                                                entry.getValue() ) );
        }

        //TODO How do Work Item images function under 6.0?
        for ( String workItemImage : workItemImages ) {
            imagesList.addItem( workItemImage );
        }
    }

    private void elementSelected( final String title,
                                  final String value ) {
        presenter.onElementSelected( title,
                                     value );
    }

}
