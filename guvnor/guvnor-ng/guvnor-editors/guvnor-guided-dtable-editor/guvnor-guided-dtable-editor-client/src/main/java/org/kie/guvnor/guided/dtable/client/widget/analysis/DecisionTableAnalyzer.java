/*
 * Copyright 2011 JBoss Inc
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

package org.kie.guvnor.guided.dtable.client.widget.analysis;

import org.kie.guvnor.datamodel.oracle.DataModelOracle;
import org.drools.guvnor.models.commons.oracle.DataType;
import org.kie.guvnor.guided.dtable.client.widget.analysis.action.ActionDetector;
import org.kie.guvnor.guided.dtable.client.widget.analysis.action.ActionDetectorKey;
import org.kie.guvnor.guided.dtable.client.widget.analysis.action.InsertFactActionDetectorKey;
import org.kie.guvnor.guided.dtable.client.widget.analysis.action.SetFieldColActionDetectorKey;
import org.kie.guvnor.guided.dtable.client.widget.analysis.action.UnrecognizedActionDetectorKey;
import org.kie.guvnor.guided.dtable.client.widget.analysis.condition.BooleanConditionDetector;
import org.kie.guvnor.guided.dtable.client.widget.analysis.condition.ConditionDetector;
import org.kie.guvnor.guided.dtable.client.widget.analysis.condition.DateConditionDetector;
import org.kie.guvnor.guided.dtable.client.widget.analysis.condition.EnumConditionDetector;
import org.kie.guvnor.guided.dtable.client.widget.analysis.condition.NumericBigDecimalConditionDetector;
import org.kie.guvnor.guided.dtable.client.widget.analysis.condition.NumericBigIntegerConditionDetector;
import org.kie.guvnor.guided.dtable.client.widget.analysis.condition.NumericByteConditionDetector;
import org.kie.guvnor.guided.dtable.client.widget.analysis.condition.NumericConditionDetector;
import org.kie.guvnor.guided.dtable.client.widget.analysis.condition.NumericDoubleConditionDetector;
import org.kie.guvnor.guided.dtable.client.widget.analysis.condition.NumericFloatConditionDetector;
import org.kie.guvnor.guided.dtable.client.widget.analysis.condition.NumericIntegerConditionDetector;
import org.kie.guvnor.guided.dtable.client.widget.analysis.condition.NumericLongConditionDetector;
import org.kie.guvnor.guided.dtable.client.widget.analysis.condition.NumericShortConditionDetector;
import org.kie.guvnor.guided.dtable.client.widget.analysis.condition.StringConditionDetector;
import org.kie.guvnor.guided.dtable.client.widget.analysis.condition.UnrecognizedConditionDetector;
import org.drools.guvnor.models.guided.dtable.model.ActionCol52;
import org.drools.guvnor.models.guided.dtable.model.ActionInsertFactCol52;
import org.drools.guvnor.models.guided.dtable.model.ActionSetFieldCol52;
import org.drools.guvnor.models.guided.dtable.model.Analysis;
import org.drools.guvnor.models.guided.dtable.model.BRLActionColumn;
import org.drools.guvnor.models.guided.dtable.model.ConditionCol52;
import org.drools.guvnor.models.guided.dtable.model.DTCellValue52;
import org.drools.guvnor.models.guided.dtable.model.GuidedDecisionTable52;
import org.drools.guvnor.models.guided.dtable.model.LimitedEntryCol;
import org.drools.guvnor.models.guided.dtable.model.Pattern52;
import org.kie.guvnor.guided.dtable.model.util.GuidedDecisionTableUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DecisionTableAnalyzer {

    private final DataModelOracle oracle;

    public DecisionTableAnalyzer( final DataModelOracle oracle ) {
        this.oracle = oracle;
    }

    @SuppressWarnings("rawtypes")
    public List<Analysis> analyze( final GuidedDecisionTable52 model ) {
        final GuidedDecisionTableUtils utils = new GuidedDecisionTableUtils( oracle,
                                                                             model );
        final List<List<DTCellValue52>> data = model.getData();
        final List<Analysis> analysisData = new ArrayList<Analysis>( data.size() );
        final List<RowDetector> rowDetectorList = new ArrayList<RowDetector>( data.size() );

        for ( List<DTCellValue52> row : data ) {
            final Integer rowNumber = ( (Integer) row.get( 0 ).getNumericValue() ) - 1;
            RowDetector rowDetector = new RowDetector( rowNumber );
            for ( Pattern52 pattern : model.getPatterns() ) {
                for ( ConditionCol52 conditionCol : pattern.getChildColumns() ) {
                    int columnIndex = model.getExpandedColumns().indexOf( conditionCol );
                    DTCellValue52 visibleCellValue = row.get( columnIndex );
                    DTCellValue52 realCellValue;
                    boolean cellIsNotBlank;
                    if ( conditionCol instanceof LimitedEntryCol ) {
                        realCellValue = ( (LimitedEntryCol) conditionCol ).getValue();
                        cellIsNotBlank = visibleCellValue.getBooleanValue();
                    } else {
                        realCellValue = visibleCellValue;
                        cellIsNotBlank = visibleCellValue.hasValue();
                    }
                    // Blank cells are ignored
                    if ( cellIsNotBlank ) {
                        ConditionDetector conditionDetector = buildConditionDetector( utils,
                                                                                      pattern,
                                                                                      conditionCol,
                                                                                      realCellValue );
                        rowDetector.putOrMergeConditionDetector( conditionDetector );
                    }
                }
            }
            for ( ActionCol52 actionCol : model.getActionCols() ) {
                //BRLActionColumns cannot be analysed
                if ( actionCol instanceof BRLActionColumn ) {
                    continue;
                }
                int columnIndex = model.getExpandedColumns().indexOf( actionCol );
                DTCellValue52 visibleCellValue = row.get( columnIndex );
                DTCellValue52 realCellValue;
                boolean cellIsNotBlank;
                if ( actionCol instanceof LimitedEntryCol ) {
                    realCellValue = ( (LimitedEntryCol) actionCol ).getValue();
                    cellIsNotBlank = visibleCellValue.getBooleanValue();
                } else {
                    realCellValue = visibleCellValue;
                    cellIsNotBlank = visibleCellValue.hasValue();
                }
                // Blank cells are ignored
                if ( cellIsNotBlank ) {
                    ActionDetector actionDetector = buildActionDetector( model,
                                                                         actionCol,
                                                                         realCellValue );
                    rowDetector.putOrMergeActionDetector( actionDetector );
                }
            }
            rowDetectorList.add( rowDetector );
        }
        for ( RowDetector rowDetector : rowDetectorList ) {
            analysisData.add( rowDetector.buildAnalysis( rowDetectorList ) );
        }
        return analysisData;
    }

    @SuppressWarnings("rawtypes")
    private ConditionDetector buildConditionDetector( GuidedDecisionTableUtils utils,
                                                      Pattern52 pattern,
                                                      ConditionCol52 conditionCol,
                                                      DTCellValue52 realCellValue ) {
        String factField = conditionCol.getFactField();
        String operator = conditionCol.getOperator();
        String type = utils.getType( conditionCol );
        // Retrieve "Guvnor" enums
        String[] allValueList = utils.getValueList( conditionCol );
        ConditionDetector newDetector;
        if ( allValueList.length != 0 ) {
            // Guvnor enum
            newDetector = new EnumConditionDetector( pattern,
                                                     factField,
                                                     Arrays.asList( allValueList ),
                                                     realCellValue.getStringValue(),
                                                     operator );
        } else if ( type == null ) {
            // type null means the field is free-format
            newDetector = new UnrecognizedConditionDetector( pattern,
                                                             factField,
                                                             operator );
        } else if ( type.equals( DataType.TYPE_STRING ) ) {
            newDetector = new StringConditionDetector( pattern,
                                                       factField,
                                                       realCellValue.getStringValue(),
                                                       operator );
        } else if ( type.equals( DataType.TYPE_NUMERIC ) ) {
            newDetector = new NumericConditionDetector( pattern,
                                                        factField,
                                                        (BigDecimal) realCellValue.getNumericValue(),
                                                        operator );
        } else if ( type.equals( DataType.TYPE_NUMERIC_BIGDECIMAL ) ) {
            newDetector = new NumericBigDecimalConditionDetector( pattern,
                                                                  factField,
                                                                  (BigDecimal) realCellValue.getNumericValue(),
                                                                  operator );
        } else if ( type.equals( DataType.TYPE_NUMERIC_BIGINTEGER ) ) {
            newDetector = new NumericBigIntegerConditionDetector( pattern,
                                                                  factField,
                                                                  (BigInteger) realCellValue.getNumericValue(),
                                                                  operator );
        } else if ( type.equals( DataType.TYPE_NUMERIC_BYTE ) ) {
            newDetector = new NumericByteConditionDetector( pattern,
                                                            factField,
                                                            (Byte) realCellValue.getNumericValue(),
                                                            operator );
        } else if ( type.equals( DataType.TYPE_NUMERIC_DOUBLE ) ) {
            newDetector = new NumericDoubleConditionDetector( pattern,
                                                              factField,
                                                              (Double) realCellValue.getNumericValue(),
                                                              operator );
        } else if ( type.equals( DataType.TYPE_NUMERIC_FLOAT ) ) {
            newDetector = new NumericFloatConditionDetector( pattern,
                                                             factField,
                                                             (Float) realCellValue.getNumericValue(),
                                                             operator );
        } else if ( type.equals( DataType.TYPE_NUMERIC_INTEGER ) ) {
            newDetector = new NumericIntegerConditionDetector( pattern,
                                                               factField,
                                                               (Integer) realCellValue.getNumericValue(),
                                                               operator );
        } else if ( type.equals( DataType.TYPE_NUMERIC_LONG ) ) {
            newDetector = new NumericLongConditionDetector( pattern,
                                                            factField,
                                                            (Long) realCellValue.getNumericValue(),
                                                            operator );
        } else if ( type.equals( DataType.TYPE_NUMERIC_SHORT ) ) {
            newDetector = new NumericShortConditionDetector( pattern,
                                                             factField,
                                                             (Short) realCellValue.getNumericValue(),
                                                             operator );
        } else if ( type.equals( DataType.TYPE_BOOLEAN ) ) {
            newDetector = new BooleanConditionDetector( pattern,
                                                        factField,
                                                        realCellValue.getBooleanValue(),
                                                        operator );
        } else if ( type.equals( DataType.TYPE_DATE ) ) {
            newDetector = new DateConditionDetector( pattern,
                                                     factField,
                                                     realCellValue.getDateValue(),
                                                     operator );
        } else {
            newDetector = new UnrecognizedConditionDetector( pattern,
                                                             factField,
                                                             operator );
        }
        return newDetector;
    }

    private ActionDetector buildActionDetector( GuidedDecisionTable52 model,
                                                ActionCol52 actionCol,
                                                DTCellValue52 realCellValue ) {
        ActionDetectorKey key;
        if ( actionCol instanceof ActionSetFieldCol52 ) {
            key = new SetFieldColActionDetectorKey( (ActionSetFieldCol52) actionCol );
        } else if ( actionCol instanceof ActionInsertFactCol52 ) {
            key = new InsertFactActionDetectorKey( (ActionInsertFactCol52) actionCol );
        } else {
            key = new UnrecognizedActionDetectorKey( actionCol );
        }
        return new ActionDetector( key,
                                   realCellValue );
    }

}
