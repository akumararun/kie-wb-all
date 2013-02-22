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
package org.kie.guvnor.guided.dtable.client.widget.table;

import com.google.gwt.event.shared.EventBus;
import org.kie.guvnor.datamodel.oracle.DataModelOracle;
import org.drools.guvnor.models.commons.oracle.DataType;
import org.kie.guvnor.decoratedgrid.client.widget.AbstractCellFactory;
import org.kie.guvnor.decoratedgrid.client.widget.DecoratedGridCellValueAdaptor;
import org.kie.guvnor.decoratedgrid.client.widget.cells.PopupDialectDropDownEditCell;
import org.kie.guvnor.decoratedgrid.client.widget.cells.PopupDropDownEditCell;
import org.kie.guvnor.decoratedgrid.client.widget.cells.PopupTextEditCell;
import org.kie.guvnor.decoratedgrid.client.widget.cells.RowNumberCell;
import org.kie.guvnor.guided.dtable.client.widget.table.cells.AnalysisCell;
import org.kie.guvnor.guided.dtable.client.widget.table.cells.PopupBoundPatternDropDownEditCell;
import org.kie.guvnor.guided.dtable.client.widget.table.cells.PopupValueListDropDownEditCell;
import org.drools.guvnor.models.guided.dtable.model.ActionInsertFactCol52;
import org.drools.guvnor.models.guided.dtable.model.ActionRetractFactCol52;
import org.drools.guvnor.models.guided.dtable.model.ActionSetFieldCol52;
import org.drools.guvnor.models.guided.dtable.model.ActionWorkItemCol52;
import org.drools.guvnor.models.guided.dtable.model.ActionWorkItemInsertFactCol52;
import org.drools.guvnor.models.guided.dtable.model.ActionWorkItemSetFieldCol52;
import org.drools.guvnor.models.guided.dtable.model.Analysis;
import org.drools.guvnor.models.guided.dtable.model.AnalysisCol52;
import org.drools.guvnor.models.guided.dtable.model.AttributeCol52;
import org.drools.guvnor.models.guided.dtable.model.BRLActionVariableColumn;
import org.drools.guvnor.models.guided.dtable.model.BRLConditionVariableColumn;
import org.drools.guvnor.models.guided.dtable.model.BRLRuleModel;
import org.drools.guvnor.models.guided.dtable.model.BaseColumn;
import org.drools.guvnor.models.guided.dtable.model.ConditionCol52;
import org.drools.guvnor.models.guided.dtable.model.DTColumnConfig52;
import org.drools.guvnor.models.guided.dtable.model.GuidedDecisionTable52;
import org.drools.guvnor.models.guided.dtable.model.LimitedEntryCol;
import org.drools.guvnor.models.guided.dtable.model.RowNumberCol52;
import org.kie.guvnor.guided.dtable.model.util.GuidedDecisionTableUtils;
import org.kie.guvnor.guided.rule.client.editor.RuleAttributeWidget;

/**
 * A Factory to provide the Cells for given coordinate for Decision Tables.
 */
public class DecisionTableCellFactory extends AbstractCellFactory<BaseColumn> {

    private GuidedDecisionTableUtils utils;
    private GuidedDecisionTable52 model;

    /**
     * Construct a Cell Factory for a specific Decision Table
     * @param oracle SuggestionCompletionEngine to assist with drop-downs
     * @param model GuidedDecisionTable52 Decision table model
     * @param dropDownManager DropDownManager for dependent cells
     * @param isReadOnly Should cells be created for a read-only mode of operation
     * @param eventBus An EventBus on which cells can subscribe to events
     */
    public DecisionTableCellFactory( final GuidedDecisionTable52 model,
                                     final DataModelOracle oracle,
                                     final DecisionTableDropDownManager dropDownManager,
                                     final boolean isReadOnly,
                                     final EventBus eventBus ) {
        super( oracle,
               dropDownManager,
               isReadOnly,
               eventBus );
        if ( model == null ) {
            throw new IllegalArgumentException( "model cannot be null" );
        }
        if ( oracle == null ) {
            throw new IllegalArgumentException( "oracle cannot be null" );
        }
        this.model = model;
        this.utils = new GuidedDecisionTableUtils( oracle,
                                                   model );
    }

    /**
     * Create a Cell for the given DTColumnConfig
     * @param column The Decision Table model column
     * @return A Cell
     */
    public DecoratedGridCellValueAdaptor<? extends Comparable<?>> getCell( BaseColumn column ) {

        //This is the cell that will be used to edit values; its type can differ to the "fieldType" 
        //of the underlying model. For example a "Guvnor-enum" requires a drop-down list of potential 
        //values whereas the "fieldType" may be a String. 
        DecoratedGridCellValueAdaptor<? extends Comparable<?>> cell = makeTextCell();

        if ( column instanceof RowNumberCol52 ) {
            cell = makeRowNumberCell();

        } else if ( column instanceof AttributeCol52 ) {
            AttributeCol52 attrCol = (AttributeCol52) column;
            String attrName = attrCol.getAttribute();
            if ( attrName.equals( RuleAttributeWidget.SALIENCE_ATTR ) ) {
                if ( attrCol.isUseRowNumber() ) {
                    cell = makeRowNumberCell();
                } else {
                    cell = makeNumericIntegerCell();
                }
            } else if ( attrName.equals( GuidedDecisionTable52.ENABLED_ATTR ) ) {
                cell = makeBooleanCell();
            } else if ( attrName.equals( GuidedDecisionTable52.NO_LOOP_ATTR ) ) {
                cell = makeBooleanCell();
            } else if ( attrName.equals( GuidedDecisionTable52.DURATION_ATTR ) ) {
                cell = makeNumericLongCell();
            } else if ( attrName.equals( GuidedDecisionTable52.TIMER_ATTR ) ) {
                cell = makeTimerCell();
            } else if ( attrName.equals( GuidedDecisionTable52.CALENDARS_ATTR ) ) {
                cell = makeCalendarsCell();
            } else if ( attrName.equals( GuidedDecisionTable52.AUTO_FOCUS_ATTR ) ) {
                cell = makeBooleanCell();
            } else if ( attrName.equals( GuidedDecisionTable52.LOCK_ON_ACTIVE_ATTR ) ) {
                cell = makeBooleanCell();
            } else if ( attrName.equals( GuidedDecisionTable52.DATE_EFFECTIVE_ATTR ) ) {
                cell = makeDateCell();
            } else if ( attrName.equals( GuidedDecisionTable52.DATE_EXPIRES_ATTR ) ) {
                cell = makeDateCell();
            } else if ( attrName.equals( GuidedDecisionTable52.DIALECT_ATTR ) ) {
                cell = makeDialectCell();
            } else if ( attrName.equals( GuidedDecisionTable52.NEGATE_RULE_ATTR ) ) {
                cell = makeBooleanCell();
            }

        } else if ( column instanceof LimitedEntryCol ) {
            cell = makeBooleanCell();

        } else if ( column instanceof BRLConditionVariableColumn ) {
            //Before ConditionCol52 as this is a sub-class
            cell = derieveCellFromCondition( (BRLConditionVariableColumn) column );

        } else if ( column instanceof ConditionCol52 ) {
            cell = derieveCellFromCondition( (ConditionCol52) column );

        } else if ( column instanceof ActionWorkItemSetFieldCol52 ) {
            //Before ActionSetFieldCol52 as this is a sub-class
            cell = makeBooleanCell();

        } else if ( column instanceof ActionWorkItemInsertFactCol52 ) {
            //Before ActionInsertFactCol52 as this is a sub-class
            cell = makeBooleanCell();

        } else if ( column instanceof ActionSetFieldCol52 ) {
            cell = derieveCellFromAction( (ActionSetFieldCol52) column );

        } else if ( column instanceof ActionInsertFactCol52 ) {
            cell = derieveCellFromAction( (ActionInsertFactCol52) column );

        } else if ( column instanceof ActionRetractFactCol52 ) {
            cell = derieveCellFromAction( (ActionRetractFactCol52) column );

        } else if ( column instanceof ActionWorkItemCol52 ) {
            cell = makeBooleanCell();

        } else if ( column instanceof BRLActionVariableColumn ) {
            cell = derieveCellFromAction( (BRLActionVariableColumn) column );

        } else if ( column instanceof AnalysisCol52 ) {
            cell = makeRowAnalysisCell();
        }

        return cell;

    }

    // Make a new Cell for Condition columns
    private DecoratedGridCellValueAdaptor<? extends Comparable<?>> derieveCellFromCondition( ConditionCol52 col ) {

        //Operators "is null" and "is not null" require a boolean cell
        if ( col.getOperator() != null && ( col.getOperator().equals( "== null" ) || col.getOperator().equals( "!= null" ) ) ) {
            return makeBooleanCell();
        }

        //Check if the column has a "Value List" or an enumeration. Value List takes precedence
        final String factType = model.getPattern( col ).getFactType();
        final String fieldName = col.getFactField();
        if ( utils.hasValueList( col ) ) {
            return makeValueListCell( col );

        } else if ( oracle.hasEnums( factType,
                                     fieldName ) ) {
            return makeEnumCell( factType,
                                 fieldName );
        }

        return derieveCellFromModel( col );
    }

    // Make a new Cell for BRLConditionVariableColumn columns
    private DecoratedGridCellValueAdaptor<? extends Comparable<?>> derieveCellFromCondition( BRLConditionVariableColumn col ) {

        //Check if the column has an enumeration
        final String factType = col.getFactType();
        final String fieldName = col.getFactField();
        if ( oracle.hasEnums( factType,
                              fieldName ) ) {
            return makeEnumCell( factType,
                                 fieldName );
        }

        return derieveCellFromModel( col );
    }

    // Make a new Cell for ActionSetField columns
    private DecoratedGridCellValueAdaptor<? extends Comparable<?>> derieveCellFromAction( ActionSetFieldCol52 col ) {

        //Check if the column has a "Value List" or an enumeration. Value List takes precedence
        final String factType = utils.getBoundFactType( col.getBoundName() );
        final String fieldName = col.getFactField();
        if ( utils.hasValueList( col ) ) {
            return makeValueListCell( col );

        } else if ( oracle.hasEnums( factType,
                                     fieldName ) ) {
            return makeEnumCell( factType,
                                 fieldName );
        }

        return derieveCellFromModel( col );
    }

    // Make a new Cell for ActionInsertFact columns
    private DecoratedGridCellValueAdaptor<? extends Comparable<?>> derieveCellFromAction( ActionInsertFactCol52 col ) {

        //Check if the column has a "Value List" or an enumeration. Value List takes precedence
        final String factType = col.getFactType();
        final String fieldName = col.getFactField();
        if ( utils.hasValueList( col ) ) {
            return makeValueListCell( col );

        } else if ( oracle.hasEnums( factType,
                                     fieldName ) ) {
            return makeEnumCell( factType,
                                 fieldName );
        }

        return derieveCellFromModel( col );
    }

    // Make a new Cell for ActionRetractFactCol52 columns
    private DecoratedGridCellValueAdaptor<? extends Comparable<?>> derieveCellFromAction( ActionRetractFactCol52 col ) {

        //Drop down of possible patterns
        PopupBoundPatternDropDownEditCell pudd = new PopupBoundPatternDropDownEditCell( eventBus,
                                                                                        isReadOnly );
        BRLRuleModel rm = new BRLRuleModel( model );
        pudd.setFactBindings( rm.getLHSBoundFacts() );
        return new DecoratedGridCellValueAdaptor<String>( pudd,
                                                          eventBus );
    }

    // Make a new Cell for BRLActionVariableColumn columns
    private DecoratedGridCellValueAdaptor<? extends Comparable<?>> derieveCellFromAction( BRLActionVariableColumn col ) {

        //Check if the column has an enumeration
        final String factType = col.getFactType();
        final String fieldName = col.getFactField();
        if ( oracle.hasEnums( factType,
                              fieldName ) ) {
            return makeEnumCell( factType,
                                 fieldName );
        }

        return derieveCellFromModel( col );
    }

    //Get Cell applicable to Model's data-type
    private DecoratedGridCellValueAdaptor<? extends Comparable<?>> derieveCellFromModel( DTColumnConfig52 col ) {

        //Extended Entry...
        DecoratedGridCellValueAdaptor<? extends Comparable<?>> cell = makeTextCell();

        //Get a cell based upon the data-type
        String type = utils.getType( col );

        if ( type.equals( DataType.TYPE_NUMERIC ) ) {
            cell = makeNumericCell();
        } else if ( type.equals( DataType.TYPE_NUMERIC_BIGDECIMAL ) ) {
            cell = makeNumericBigDecimalCell();
        } else if ( type.equals( DataType.TYPE_NUMERIC_BIGINTEGER ) ) {
            cell = makeNumericBigIntegerCell();
        } else if ( type.equals( DataType.TYPE_NUMERIC_BYTE ) ) {
            cell = makeNumericByteCell();
        } else if ( type.equals( DataType.TYPE_NUMERIC_DOUBLE ) ) {
            cell = makeNumericDoubleCell();
        } else if ( type.equals( DataType.TYPE_NUMERIC_FLOAT ) ) {
            cell = makeNumericFloatCell();
        } else if ( type.equals( DataType.TYPE_NUMERIC_INTEGER ) ) {
            cell = makeNumericIntegerCell();
        } else if ( type.equals( DataType.TYPE_NUMERIC_LONG ) ) {
            cell = makeNumericLongCell();
        } else if ( type.equals( DataType.TYPE_NUMERIC_SHORT ) ) {
            cell = makeNumericShortCell();
        } else if ( type.equals( DataType.TYPE_BOOLEAN ) ) {
            cell = makeBooleanCell();
        } else if ( type.equals( DataType.TYPE_DATE ) ) {
            cell = makeDateCell();
        }

        return cell;
    }

    // Make a new Cell for Dialect columns
    private DecoratedGridCellValueAdaptor<String> makeDialectCell() {
        PopupDialectDropDownEditCell pudd = new PopupDialectDropDownEditCell( isReadOnly );
        return new DecoratedGridCellValueAdaptor<String>( pudd,
                                                          eventBus );
    }

    // Make a new Cell for Row Number columns
    private DecoratedGridCellValueAdaptor<Integer> makeRowNumberCell() {
        return new DecoratedGridCellValueAdaptor<Integer>( new RowNumberCell(),
                                                           eventBus );
    }

    // Make a new Cell for Timer columns
    private DecoratedGridCellValueAdaptor<String> makeTimerCell() {
        return new DecoratedGridCellValueAdaptor<String>( new PopupTextEditCell( isReadOnly ),
                                                          eventBus );
    }

    // Make a new Cell for Calendars columns
    private DecoratedGridCellValueAdaptor<String> makeCalendarsCell() {
        return new DecoratedGridCellValueAdaptor<String>( new PopupTextEditCell( isReadOnly ),
                                                          eventBus );
    }

    // Make a new Cell for Rule Analysis columns
    private DecoratedGridCellValueAdaptor<Analysis> makeRowAnalysisCell() {
        return new DecoratedGridCellValueAdaptor<Analysis>( new AnalysisCell(),
                                                            eventBus );
    }

    //Get a cell for a Value List
    private DecoratedGridCellValueAdaptor<? extends Comparable<?>> makeValueListCell( DTColumnConfig52 col ) {

        // Columns with "Value Lists" are always Text (for now)
        PopupValueListDropDownEditCell pudd = new PopupValueListDropDownEditCell( utils.getValueList( col ),
                                                                                  isReadOnly );
        DecoratedGridCellValueAdaptor<? extends Comparable<?>> cell = new DecoratedGridCellValueAdaptor<String>( pudd,
                                                                                                                 eventBus );
        return cell;
    }

    //Get a cell for a Value List
    private DecoratedGridCellValueAdaptor<? extends Comparable<?>> makeEnumCell( String factType,
                                                                                 String fieldName ) {

        // Columns with enumerations are always Text
        PopupDropDownEditCell pudd = new PopupDropDownEditCell( factType,
                                                                fieldName,
                                                                oracle,
                                                                dropDownManager,
                                                                isReadOnly );
        DecoratedGridCellValueAdaptor<? extends Comparable<?>> cell = new DecoratedGridCellValueAdaptor<String>( pudd,
                                                                                                                 eventBus );
        return cell;
    }

}
