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
package org.kie.guvnor.editors.guided.client.editor.templates;

import com.google.web.bindery.event.shared.EventBus;
import org.kie.guvnor.datamodel.api.DataModel;
import org.kie.guvnor.decoratedgrid.client.widget.AbstractCellFactory;
import org.kie.guvnor.decoratedgrid.client.widget.DecoratedGridCellValueAdaptor;
import org.kie.guvnor.decoratedgrid.client.widget.cells.PopupDropDownEditCell;
import org.kie.guvnor.editors.guided.model.DataType;

public class TemplateDataCellFactory
        extends AbstractCellFactory<TemplateDataColumn> {

    private final DataModel sce;

    /**
     * Construct a Cell Factory for a specific Template Data Widget
     * @param sce SuggestionCompletionEngine to assist with drop-downs
     * @param dropDownManager DropDownManager for dependent cells
     * @param isReadOnly Should cells be created for a read-only mode of operation
     * @param eventBus EventBus to which cells can send update events
     */
    public TemplateDataCellFactory( DataModel sce,
                                    TemplateDropDownManager dropDownManager,
                                    boolean isReadOnly,
                                    EventBus eventBus ) {
        super( dropDownManager, isReadOnly, eventBus );
        this.sce = sce;
    }

    /**
     * Create a Cell for the given TemplateDataColumn
     * @param column The Template Data Table model column
     * @return A Cell
     */
    public DecoratedGridCellValueAdaptor<? extends Comparable<?>> getCell( TemplateDataColumn column ) {

        DecoratedGridCellValueAdaptor<? extends Comparable<?>> cell = null;

        //Check if the column has an enumeration
        String factType = column.getFactType();
        String factField = column.getFactField();
        if ( sce.hasEnums( factType,
                           factField ) ) {

            // Columns with lists of values, enums etc are always Text (for now)
            PopupDropDownEditCell pudd = new PopupDropDownEditCell( factType,
                                                                    factField,
                                                                    sce,
                                                                    dropDownManager,
                                                                    isReadOnly );
            cell = new DecoratedGridCellValueAdaptor<String>( pudd,
                                                              eventBus );

        } else {
            String dataType = column.getDataType();
            if ( column.getDataType().equals( DataType.TYPE_BOOLEAN ) ) {
                cell = makeBooleanCell();
            } else if ( column.getDataType().equals( DataType.TYPE_DATE ) ) {
                cell = makeDateCell();
            } else if ( dataType.equals( DataType.TYPE_NUMERIC ) ) {
                cell = makeNumericCell();
            } else if ( dataType.equals( DataType.TYPE_NUMERIC_BIGDECIMAL ) ) {
                cell = makeNumericBigDecimalCell();
            } else if ( dataType.equals( DataType.TYPE_NUMERIC_BIGINTEGER ) ) {
                cell = makeNumericBigIntegerCell();
            } else if ( dataType.equals( DataType.TYPE_NUMERIC_BYTE ) ) {
                cell = makeNumericByteCell();
            } else if ( dataType.equals( DataType.TYPE_NUMERIC_DOUBLE ) ) {
                cell = makeNumericDoubleCell();
            } else if ( dataType.equals( DataType.TYPE_NUMERIC_FLOAT ) ) {
                cell = makeNumericFloatCell();
            } else if ( dataType.equals( DataType.TYPE_NUMERIC_INTEGER ) ) {
                cell = makeNumericIntegerCell();
            } else if ( dataType.equals( DataType.TYPE_NUMERIC_LONG ) ) {
                cell = makeNumericLongCell();
            } else if ( dataType.equals( DataType.TYPE_NUMERIC_SHORT ) ) {
                cell = makeNumericShortCell();
            } else {
                cell = makeTextCell();
            }
        }

        return cell;

    }

}
