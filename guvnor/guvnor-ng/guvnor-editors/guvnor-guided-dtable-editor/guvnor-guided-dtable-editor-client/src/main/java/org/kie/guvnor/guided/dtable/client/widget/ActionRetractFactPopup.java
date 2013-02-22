/*
 * Copyright 2011 JBoss Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.kie.guvnor.guided.dtable.client.widget;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import org.kie.guvnor.guided.dtable.client.resources.i18n.Constants;
import org.drools.guvnor.models.guided.dtable.model.ActionCol52;
import org.drools.guvnor.models.guided.dtable.model.ActionRetractFactCol52;
import org.drools.guvnor.models.guided.dtable.model.BRLRuleModel;
import org.drools.guvnor.models.guided.dtable.model.DTCellValue52;
import org.drools.guvnor.models.guided.dtable.model.GuidedDecisionTable52;
import org.drools.guvnor.models.guided.dtable.model.GuidedDecisionTable52.TableFormat;
import org.drools.guvnor.models.guided.dtable.model.LimitedEntryActionRetractFactCol52;
import org.drools.guvnor.models.guided.dtable.model.LimitedEntryCol;
import org.uberfire.client.common.FormStylePopup;

import java.util.List;

/**
 * A popup to define the parameters of an Action to retract a Fact
 */
public class ActionRetractFactPopup extends FormStylePopup {

    private ActionRetractFactCol52 editingCol;
    private GuidedDecisionTable52 model;
    private BRLRuleModel rm;

    public ActionRetractFactPopup( final GuidedDecisionTable52 model,
                                   final GenericColumnCommand refreshGrid,
                                   final ActionRetractFactCol52 col,
                                   final boolean isNew,
                                   final boolean isReadOnly ) {
        this.rm = new BRLRuleModel( model );
        this.editingCol = cloneActionRetractColumn( col );
        this.model = model;

        setTitle( Constants.INSTANCE.ColumnConfigurationRetractAFact() );
        setModal( false );

        //Show available pattern bindings, if Limited Entry
        if ( model.getTableFormat() == TableFormat.LIMITED_ENTRY ) {
            final LimitedEntryActionRetractFactCol52 ler = (LimitedEntryActionRetractFactCol52) editingCol;
            final ListBox patterns = loadBoundFacts( ler.getValue().getStringValue() );
            patterns.setEnabled( !isReadOnly );
            if ( !isReadOnly ) {
                patterns.addClickHandler( new ClickHandler() {

                    public void onClick( ClickEvent event ) {
                        int index = patterns.getSelectedIndex();
                        if ( index > -1 ) {
                            ler.getValue().setStringValue( patterns.getValue( index ) );
                        }
                    }

                } );
            }
            addAttribute( Constants.INSTANCE.FactToRetractColon(),
                          patterns );
        }

        //Column header
        final TextBox header = new TextBox();
        header.setText( col.getHeader() );
        header.setEnabled( !isReadOnly );
        if ( !isReadOnly ) {
            header.addChangeHandler( new ChangeHandler() {
                public void onChange( ChangeEvent event ) {
                    editingCol.setHeader( header.getText() );
                }
            } );
        }
        addAttribute( Constants.INSTANCE.ColumnHeaderDescription(),
                      header );

        //Hide column tick-box
        addAttribute( Constants.INSTANCE.HideThisColumn(),
                      DTCellValueWidgetFactory.getHideColumnIndicator( editingCol ) );

        Button apply = new Button( Constants.INSTANCE.ApplyChanges() );
        apply.addClickHandler( new ClickHandler() {
            public void onClick( ClickEvent w ) {
                if ( null == editingCol.getHeader()
                        || "".equals( editingCol.getHeader() ) ) {
                    Window.alert( Constants.INSTANCE.YouMustEnterAColumnHeaderValueDescription() );
                    return;
                }
                if ( isNew ) {
                    if ( !unique( editingCol.getHeader() ) ) {
                        Window.alert( Constants.INSTANCE.ThatColumnNameIsAlreadyInUsePleasePickAnother() );
                        return;
                    }

                } else {
                    if ( !col.getHeader().equals( editingCol.getHeader() ) ) {
                        if ( !unique( editingCol.getHeader() ) ) {
                            Window.alert( Constants.INSTANCE.ThatColumnNameIsAlreadyInUsePleasePickAnother() );
                            return;
                        }
                    }
                }

                // Pass new\modified column back for handling
                refreshGrid.execute( editingCol );
                hide();
            }
        } );
        addAttribute( "",
                      apply );

    }

    private boolean unique( String header ) {
        for ( ActionCol52 o : model.getActionCols() ) {
            if ( o.getHeader().equals( header ) ) {
                return false;
            }
        }
        return true;
    }

    private ActionRetractFactCol52 cloneActionRetractColumn( ActionRetractFactCol52 col ) {
        ActionRetractFactCol52 clone = null;
        if ( col instanceof LimitedEntryCol ) {
            clone = new LimitedEntryActionRetractFactCol52();
            DTCellValue52 dcv = new DTCellValue52( ( (LimitedEntryCol) col ).getValue().getStringValue() );
            ( (LimitedEntryCol) clone ).setValue( dcv );
        } else {
            clone = new ActionRetractFactCol52();
        }
        clone.setHeader( col.getHeader() );
        clone.setHideColumn( col.isHideColumn() );
        return clone;
    }

    private ListBox loadBoundFacts( String binding ) {
        ListBox listBox = new ListBox();
        listBox.addItem( Constants.INSTANCE.Choose() );
        List<String> factBindings = rm.getLHSBoundFacts();

        for ( int index = 0; index < factBindings.size(); index++ ) {
            String boundName = factBindings.get( index );
            if ( !"".equals( boundName ) ) {
                listBox.addItem( boundName );
                if ( boundName.equals( binding ) ) {
                    listBox.setSelectedIndex( index + 1 );
                }
            }
        }

        listBox.setEnabled( listBox.getItemCount() > 1 );
        if ( listBox.getItemCount() == 1 ) {
            listBox.clear();
            listBox.addItem( Constants.INSTANCE.NoPatternBindingsAvailable() );
        }
        return listBox;
    }

}
