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

package org.kie.guvnor.guided.rule.backend.server.util;

import org.drools.compiler.DrlParser;
import org.drools.compiler.DroolsParserException;
import org.drools.core.util.ReflectiveVisitor;
import org.drools.lang.descr.AccumulateDescr;
import org.drools.lang.descr.AndDescr;
import org.drools.lang.descr.AttributeDescr;
import org.drools.lang.descr.BaseDescr;
import org.drools.lang.descr.CollectDescr;
import org.drools.lang.descr.ConditionalElementDescr;
import org.drools.lang.descr.EntryPointDescr;
import org.drools.lang.descr.ExprConstraintDescr;
import org.drools.lang.descr.NotDescr;
import org.drools.lang.descr.OrDescr;
import org.drools.lang.descr.PackageDescr;
import org.drools.lang.descr.PatternDescr;
import org.drools.lang.descr.PatternSourceDescr;
import org.drools.lang.descr.RuleDescr;
import org.kie.guvnor.datamodel.model.DSLSentence;
import org.kie.guvnor.datamodel.model.FieldNature;
import org.kie.guvnor.datamodel.model.IAction;
import org.kie.guvnor.datamodel.model.IPattern;
import org.kie.guvnor.datamodel.model.SharedConstants;
import org.kie.guvnor.datamodel.model.workitems.HasBinding;
import org.kie.guvnor.datamodel.model.workitems.PortableBooleanParameterDefinition;
import org.kie.guvnor.datamodel.model.workitems.PortableFloatParameterDefinition;
import org.kie.guvnor.datamodel.model.workitems.PortableIntegerParameterDefinition;
import org.kie.guvnor.datamodel.model.workitems.PortableObjectParameterDefinition;
import org.kie.guvnor.datamodel.model.workitems.PortableParameterDefinition;
import org.kie.guvnor.datamodel.model.workitems.PortableStringParameterDefinition;
import org.kie.guvnor.datamodel.model.workitems.PortableWorkDefinition;
import org.kie.guvnor.datamodel.oracle.DataType;
import org.kie.guvnor.datamodel.oracle.OperatorsOracle;
import org.kie.guvnor.guided.rule.model.ActionCallMethod;
import org.kie.guvnor.guided.rule.model.ActionExecuteWorkItem;
import org.kie.guvnor.guided.rule.model.ActionFieldFunction;
import org.kie.guvnor.guided.rule.model.ActionFieldList;
import org.kie.guvnor.guided.rule.model.ActionFieldValue;
import org.kie.guvnor.guided.rule.model.ActionGlobalCollectionAdd;
import org.kie.guvnor.guided.rule.model.ActionInsertFact;
import org.kie.guvnor.guided.rule.model.ActionInsertLogicalFact;
import org.kie.guvnor.guided.rule.model.ActionRetractFact;
import org.kie.guvnor.guided.rule.model.ActionSetField;
import org.kie.guvnor.guided.rule.model.ActionUpdateField;
import org.kie.guvnor.guided.rule.model.ActionWorkItemFieldValue;
import org.kie.guvnor.guided.rule.model.BaseSingleFieldConstraint;
import org.kie.guvnor.guided.rule.model.CEPWindow;
import org.kie.guvnor.guided.rule.model.CompositeFactPattern;
import org.kie.guvnor.guided.rule.model.CompositeFieldConstraint;
import org.kie.guvnor.guided.rule.model.ConnectiveConstraint;
import org.kie.guvnor.guided.rule.model.ExpressionField;
import org.kie.guvnor.guided.rule.model.ExpressionFormLine;
import org.kie.guvnor.guided.rule.model.ExpressionUnboundFact;
import org.kie.guvnor.guided.rule.model.FactPattern;
import org.kie.guvnor.guided.rule.model.FieldConstraint;
import org.kie.guvnor.guided.rule.model.FieldNatureType;
import org.kie.guvnor.guided.rule.model.FreeFormLine;
import org.kie.guvnor.guided.rule.model.FromAccumulateCompositeFactPattern;
import org.kie.guvnor.guided.rule.model.FromCollectCompositeFactPattern;
import org.kie.guvnor.guided.rule.model.FromCompositeFactPattern;
import org.kie.guvnor.guided.rule.model.FromEntryPointFactPattern;
import org.kie.guvnor.guided.rule.model.HasParameterizedOperator;
import org.kie.guvnor.guided.rule.model.IFactPattern;
import org.kie.guvnor.guided.rule.model.RuleAttribute;
import org.kie.guvnor.guided.rule.model.RuleModel;
import org.kie.guvnor.guided.rule.model.SingleFieldConstraint;
import org.kie.guvnor.guided.rule.model.SingleFieldConstraintEBLeftSide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class persists the rule model to DRL and back
 */
public class BRDRLPersistence
        implements
        BRLPersistence {

    private static final String WORKITEM_PREFIX = "wi";

    private static final BRLPersistence INSTANCE = new BRDRLPersistence();

    public static final String DEFAULT_DIALECT = "mvel";

    //This is the default dialect for rules not specifying one explicitly
    protected DRLConstraintValueBuilder constraintValueBuilder = DRLConstraintValueBuilder.getBuilder( DEFAULT_DIALECT );

    //Keep a record of all variable bindings for Actions that depend on them
    protected Map<String, IFactPattern> bindingsPatterns;
    protected Map<String, FieldConstraint> bindingsFields;

    protected BRDRLPersistence() {
    }

    public static BRLPersistence getInstance() {
        return INSTANCE;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.drools.ide.common.server.util.BRLPersistence#marshal(org.drools.guvnor
     * .client.modeldriven.brl.RuleModel)
     */
    public String marshal( RuleModel model ) {
        return marshalRule( model );
    }

    protected String marshalRule( RuleModel model ) {
        boolean isDSLEnhanced = model.hasDSLSentences();
        bindingsPatterns = new HashMap<String, IFactPattern>();
        bindingsFields = new HashMap<String, FieldConstraint>();

        StringBuilder buf = new StringBuilder();
        this.marshalHeader( model,
                            buf );
        this.marshalMetadata( buf,
                              model );
        this.marshalAttributes( buf,
                                model );

        buf.append( "\twhen\n" );
        this.marshalLHS( buf,
                         model,
                         isDSLEnhanced );
        buf.append( "\tthen\n" );
        this.marshalRHS( buf,
                         model,
                         isDSLEnhanced );
        this.marshalFooter( buf );
        return buf.toString();
    }

    protected void marshalFooter( StringBuilder buf ) {
        buf.append( "end\n" );
    }

    protected void marshalHeader( RuleModel model,
                                  StringBuilder buf ) {
        buf.append( "rule \"" + marshalRuleName( model ) + "\"" );
        if ( null != model.parentName && model.parentName.length() > 0 ) {
            buf.append( " extends \"" + model.parentName + "\"\n" );
        } else {
            buf.append( '\n' );
        }
    }

    protected String marshalRuleName( RuleModel model ) {
        return model.name;
    }

    /**
     * Marshal model attributes
     * @param buf
     * @param model
     */
    private void marshalAttributes( StringBuilder buf,
                                    RuleModel model ) {
        boolean hasDialect = false;
        for ( int i = 0; i < model.attributes.length; i++ ) {
            RuleAttribute attr = model.attributes[ i ];

            buf.append( "\t" );
            buf.append( attr );

            buf.append( "\n" );
            if ( attr.getAttributeName().equals( "dialect" ) ) {
                constraintValueBuilder = DRLConstraintValueBuilder.getBuilder( attr.getValue() );
                hasDialect = true;
            }
        }
        // Un comment below for mvel
        if ( !hasDialect ) {
            RuleAttribute attr = new RuleAttribute( "dialect",
                                                    DEFAULT_DIALECT );
            buf.append( "\t" );
            buf.append( attr );
            buf.append( "\n" );
        }
    }

    /**
     * Marshal model metadata
     * @param buf
     * @param model
     */
    private void marshalMetadata( StringBuilder buf,
                                  RuleModel model ) {
        if ( model.metadataList != null ) {
            for ( int i = 0; i < model.metadataList.length; i++ ) {
                buf.append( "\t" ).append( model.metadataList[ i ] ).append( "\n" );
            }
        }
    }

    /**
     * Marshal LHS patterns
     * @param buf
     * @param model
     */
    private void marshalLHS( StringBuilder buf,
                             RuleModel model,
                             boolean isDSLEnhanced ) {
        String indentation = "\t\t";
        String nestedIndentation = indentation;
        boolean isNegated = model.isNegated();

        if ( model.lhs != null ) {
            if ( isNegated ) {
                nestedIndentation += "\t";
                buf.append( indentation );
                buf.append( "not (\n" );
            }
            LHSPatternVisitor visitor = getLHSPatternVisitor( isDSLEnhanced,
                                                              buf,
                                                              nestedIndentation,
                                                              isNegated );
            for ( IPattern cond : model.lhs ) {
                visitor.visit( cond );
            }
            if ( model.isNegated() ) {
                //Delete the spurious " and ", added by LHSPatternVisitor.visitFactPattern, when the rule is negated
                buf.delete( buf.length() - 5,
                            buf.length() );
                buf.append( "\n" );
                buf.append( indentation );
                buf.append( ")\n" );
            }
        }
    }

    protected LHSPatternVisitor getLHSPatternVisitor( boolean isDSLEnhanced,
                                                      StringBuilder buf,
                                                      String nestedIndentation,
                                                      boolean isNegated ) {
        return new LHSPatternVisitor( isDSLEnhanced,
                                      bindingsPatterns,
                                      bindingsFields,
                                      constraintValueBuilder,
                                      buf,
                                      nestedIndentation,
                                      isNegated );
    }

    private void marshalRHS( StringBuilder buf,
                             RuleModel model,
                             boolean isDSLEnhanced ) {
        String indentation = "\t\t";
        if ( model.rhs != null ) {

            //Add boiler-plate for actions operating on Dates
            Map<String, List<ActionFieldValue>> classes = getRHSClassDependencies( model );
            if ( classes.containsKey( DataType.TYPE_DATE ) ) {
                buf.append( indentation );
                buf.append( "java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(\"" + System.getProperty( "drools.dateformat" ) + "\");\n" );
            }

            //Add boiler-plate for actions operating on WorkItems
            if ( !getRHSWorkItemDependencies( model ).isEmpty() ) {
                buf.append( indentation );
                buf.append( "org.kie.process.instance.WorkItemManager wim = (org.kie.process.instance.WorkItemManager) drools.getWorkingMemory().getWorkItemManager();\n" );
            }

            //Marshall the model itself
            RHSActionVisitor actionVisitor = getRHSActionVisitor( isDSLEnhanced,
                                                                  buf,
                                                                  indentation );
            for ( IAction action : model.rhs ) {
                actionVisitor.visit( action );
            }
        }
    }

    protected RHSActionVisitor getRHSActionVisitor( boolean isDSLEnhanced,
                                                    StringBuilder buf,
                                                    String indentation ) {
        return new RHSActionVisitor( isDSLEnhanced,
                                     bindingsPatterns,
                                     bindingsFields,
                                     constraintValueBuilder,
                                     buf,
                                     indentation );
    }

    private Map<String, List<ActionFieldValue>> getRHSClassDependencies( RuleModel model ) {
        if ( model != null ) {
            RHSClassDependencyVisitor dependencyVisitor = new RHSClassDependencyVisitor();
            for ( IAction action : model.rhs ) {
                dependencyVisitor.visit( action );
            }
            return dependencyVisitor.getRHSClasses();
        }

        Map<String, List<ActionFieldValue>> empty = Collections.emptyMap();
        return empty;
    }

    private List<PortableWorkDefinition> getRHSWorkItemDependencies( RuleModel model ) {
        if ( model != null ) {
            List<PortableWorkDefinition> workItems = new ArrayList<PortableWorkDefinition>();
            for ( IAction action : model.rhs ) {
                if ( action instanceof ActionExecuteWorkItem ) {
                    workItems.add( ( (ActionExecuteWorkItem) action ).getWorkDefinition() );
                }
            }
            return workItems;
        }

        List<PortableWorkDefinition> empty = Collections.emptyList();
        return empty;
    }

    public static class LHSPatternVisitor extends ReflectiveVisitor {

        private StringBuilder buf;
        private boolean isDSLEnhanced;
        private boolean isPatternNegated;
        private String indentation;
        private Map<String, IFactPattern> bindingsPatterns;
        private Map<String, FieldConstraint> bindingsFields;
        protected DRLConstraintValueBuilder constraintValueBuilder;

        public LHSPatternVisitor( boolean isDSLEnhanced,
                                  Map<String, IFactPattern> bindingsPatterns,
                                  Map<String, FieldConstraint> bindingsFields,
                                  DRLConstraintValueBuilder constraintValueBuilder,
                                  StringBuilder b,
                                  String indentation,
                                  boolean isPatternNegated ) {
            this.isDSLEnhanced = isDSLEnhanced;
            this.bindingsPatterns = bindingsPatterns;
            this.bindingsFields = bindingsFields;
            this.constraintValueBuilder = constraintValueBuilder;
            this.indentation = indentation;
            this.isPatternNegated = isPatternNegated;
            buf = b;
        }

        public void visitFactPattern( FactPattern pattern ) {
            buf.append( indentation );
            if ( isDSLEnhanced ) {
                // adding passthrough markup
                buf.append( ">" );
            }
            generateFactPattern( pattern );
            if ( isPatternNegated ) {
                buf.append( " and " );
            }
            buf.append( "\n" );
        }

        public void visitFreeFormLine( FreeFormLine ffl ) {
            String[] lines = ffl.getText().split( "\\n|\\r\\n" );
            for ( String line : lines ) {
                this.buf.append( indentation );
                if ( isDSLEnhanced ) {
                    buf.append( ">" );
                }
                this.buf.append( line + "\n" );
            }
        }

        public void visitCompositeFactPattern( CompositeFactPattern pattern ) {
            buf.append( indentation );
            if ( isDSLEnhanced ) {
                // adding passthrough markup
                buf.append( ">" );
            }
            if ( CompositeFactPattern.COMPOSITE_TYPE_EXISTS.equals( pattern.getType() ) ) {
                renderCompositeFOL( pattern );
            } else if ( CompositeFactPattern.COMPOSITE_TYPE_NOT.equals( pattern.getType() ) ) {
                renderCompositeFOL( pattern );
            } else if ( CompositeFactPattern.COMPOSITE_TYPE_OR.equals( pattern.getType() ) ) {
                buf.append( "( " );
                if ( pattern.getPatterns() != null ) {
                    for ( int i = 0; i < pattern.getPatterns().length; i++ ) {
                        if ( i > 0 ) {
                            buf.append( " " );
                            buf.append( pattern.getType() );
                            buf.append( " " );
                        }
                        renderSubPattern( pattern,
                                          i );
                    }
                }
                buf.append( " )\n" );
            }
        }

        public void visitFromCompositeFactPattern( final FromCompositeFactPattern pattern ) {
            visitFromCompositeFactPattern( pattern,
                                           false );
        }

        public void visitFromCompositeFactPattern( final FromCompositeFactPattern pattern,
                                                   final boolean isSubPattern ) {
            buf.append( indentation );
            if ( !isSubPattern && isDSLEnhanced ) {
                // adding passthrough markup
                buf.append( ">" );
            }
            if ( pattern.getFactPattern() != null ) {
                generateFactPattern( pattern.getFactPattern() );
            }
            buf.append( " from " );
            renderExpression( pattern.getExpression() );
            buf.append( "\n" );
        }

        public void visitFromCollectCompositeFactPattern( final FromCollectCompositeFactPattern pattern ) {
            visitFromCollectCompositeFactPattern( pattern,
                                                  false );
        }

        public void visitFromCollectCompositeFactPattern( final FromCollectCompositeFactPattern pattern,
                                                          final boolean isSubPattern ) {
            buf.append( indentation );
            if ( !isSubPattern && isDSLEnhanced ) {
                // adding passthrough markup
                buf.append( ">" );
            }
            if ( pattern.getFactPattern() != null ) {
                generateFactPattern( pattern.getFactPattern() );
            }
            buf.append( " from collect ( " );
            if ( pattern.getRightPattern() != null ) {
                if ( pattern.getRightPattern() instanceof FactPattern ) {
                    generateFactPattern( (FactPattern) pattern.getRightPattern() );
                } else if ( pattern.getRightPattern() instanceof FromAccumulateCompositeFactPattern ) {
                    visitFromAccumulateCompositeFactPattern( (FromAccumulateCompositeFactPattern) pattern.getRightPattern(),
                                                             isSubPattern );
                } else if ( pattern.getRightPattern() instanceof FromCollectCompositeFactPattern ) {
                    visitFromCollectCompositeFactPattern( (FromCollectCompositeFactPattern) pattern.getRightPattern(),
                                                          isSubPattern );
                } else if ( pattern.getRightPattern() instanceof FromEntryPointFactPattern ) {
                    visitFromEntryPointFactPattern( (FromEntryPointFactPattern) pattern.getRightPattern(),
                                                    isSubPattern );
                } else if ( pattern.getRightPattern() instanceof FromCompositeFactPattern ) {
                    visitFromCompositeFactPattern( (FromCompositeFactPattern) pattern.getRightPattern(),
                                                   isSubPattern );
                } else if ( pattern.getRightPattern() instanceof FreeFormLine ) {
                    visitFreeFormLine( (FreeFormLine) pattern.getRightPattern() );
                } else {
                    throw new IllegalArgumentException( "Unsupported pattern " + pattern.getRightPattern() + " for FROM COLLECT" );
                }
            }
            buf.append( ") \n" );
        }

        public void visitFromAccumulateCompositeFactPattern( final FromAccumulateCompositeFactPattern pattern ) {
            visitFromAccumulateCompositeFactPattern( pattern,
                                                     false );
        }

        public void visitFromAccumulateCompositeFactPattern( final FromAccumulateCompositeFactPattern pattern,
                                                             final boolean isSubPattern ) {
            buf.append( indentation );
            if ( !isSubPattern && isDSLEnhanced ) {
                // adding passthrough markup
                buf.append( ">" );
            }
            if ( pattern.getFactPattern() != null ) {
                generateFactPattern( pattern.getFactPattern() );
            }
            buf.append( " from accumulate ( " );
            if ( pattern.getSourcePattern() != null ) {
                if ( pattern.getSourcePattern() instanceof FactPattern ) {
                    generateFactPattern( (FactPattern) pattern.getSourcePattern() );
                } else if ( pattern.getSourcePattern() instanceof FromAccumulateCompositeFactPattern ) {
                    visitFromAccumulateCompositeFactPattern( (FromAccumulateCompositeFactPattern) pattern.getSourcePattern(),
                                                             isSubPattern );
                } else if ( pattern.getSourcePattern() instanceof FromCollectCompositeFactPattern ) {
                    visitFromCollectCompositeFactPattern( (FromCollectCompositeFactPattern) pattern.getSourcePattern(),
                                                          isSubPattern );
                } else if ( pattern.getSourcePattern() instanceof FromEntryPointFactPattern ) {
                    visitFromEntryPointFactPattern( (FromEntryPointFactPattern) pattern.getSourcePattern(),
                                                    isSubPattern );
                } else if ( pattern.getSourcePattern() instanceof FromCompositeFactPattern ) {
                    visitFromCompositeFactPattern( (FromCompositeFactPattern) pattern.getSourcePattern(),
                                                   isSubPattern );
                } else {
                    throw new IllegalArgumentException( "Unsupported pattern " + pattern.getSourcePattern() + " for FROM ACCUMULATE" );
                }
            }
            buf.append( ",\n" );

            if ( pattern.useFunctionOrCode().equals( FromAccumulateCompositeFactPattern.USE_FUNCTION ) ) {
                buf.append( indentation + "\t" );
                buf.append( pattern.getFunction() );
            } else {
                buf.append( indentation + "\tinit( " );
                buf.append( pattern.getInitCode() );
                buf.append( " ),\n" );
                buf.append( indentation + "\taction( " );
                buf.append( pattern.getActionCode() );
                buf.append( " ),\n" );
                if ( pattern.getReverseCode() != null && !pattern.getReverseCode().trim().equals( "" ) ) {
                    buf.append( indentation + "\treverse( " );
                    buf.append( pattern.getReverseCode() );
                    buf.append( " ),\n" );
                }
                buf.append( indentation + "\tresult( " );
                buf.append( pattern.getResultCode() );
                buf.append( " )\n" );
            }
            buf.append( ") \n" );

        }

        public void visitFromEntryPointFactPattern( final FromEntryPointFactPattern pattern ) {
            visitFromEntryPointFactPattern( pattern,
                                            false );
        }

        public void visitFromEntryPointFactPattern( final FromEntryPointFactPattern pattern,
                                                    final boolean isSubPattern ) {
            buf.append( indentation );
            if ( !isSubPattern && isDSLEnhanced ) {
                // adding passthrough markup
                buf.append( ">" );
            }
            if ( pattern.getFactPattern() != null ) {
                generateFactPattern( pattern.getFactPattern() );
            }
            buf.append( " from entry-point \"" + pattern.getEntryPointName() + "\"\n" );
        }

        private void renderCompositeFOL( CompositeFactPattern pattern ) {
            buf.append( pattern.getType() );
            if ( pattern.getPatterns() != null ) {
                buf.append( " (" );
                for ( int i = 0; i < pattern.getPatterns().length; i++ ) {
                    renderSubPattern( pattern,
                                      i );
                    if ( i != pattern.getPatterns().length - 1 ) {
                        buf.append( " and " );
                    }
                }
                buf.append( ") \n" );
            }
        }

        private void renderSubPattern( CompositeFactPattern pattern,
                                       int subIndex ) {
            if ( pattern.getPatterns() == null || pattern.getPatterns().length == 0 ) {
                return;
            }
            IFactPattern subPattern = pattern.getPatterns()[ subIndex ];
            if ( subPattern instanceof FactPattern ) {
                this.generateFactPattern( (FactPattern) subPattern );
            } else if ( subPattern instanceof FromAccumulateCompositeFactPattern ) {
                this.visitFromAccumulateCompositeFactPattern( (FromAccumulateCompositeFactPattern) subPattern,
                                                              true );
            } else if ( subPattern instanceof FromCollectCompositeFactPattern ) {
                this.visitFromCollectCompositeFactPattern( (FromCollectCompositeFactPattern) subPattern,
                                                           true );
            } else if ( subPattern instanceof FromCompositeFactPattern ) {
                this.visitFromCompositeFactPattern( (FromCompositeFactPattern) subPattern,
                                                    true );
            } else {
                throw new IllegalStateException( "Unsupported Pattern: " + subPattern.getClass().getName() );
            }
        }

        private void renderExpression( ExpressionFormLine expression ) {
            buf.append( expression.getText() );
        }

        public void visitDSLSentence( final DSLSentence sentence ) {
            buf.append( indentation );
            buf.append( sentence.interpolate() );
            buf.append( "\n" );
        }

        private void generateFactPattern( FactPattern pattern ) {
            if ( pattern.isNegated() ) {
                buf.append( "not " );
            } else if ( pattern.isBound() ) {
                bindingsPatterns.put( pattern.getBoundName(),
                                      pattern );
                buf.append( pattern.getBoundName() );
                buf.append( " : " );
            }
            if ( pattern.getFactType() != null ) {
                buf.append( pattern.getFactType() );
            }
            buf.append( "( " );

            // top level constraints
            if ( pattern.getConstraintList() != null ) {
                generateConstraints( pattern );
            }
            buf.append( ")" );

            //Add CEP window definition
            CEPWindow window = pattern.getWindow();
            if ( window.isDefined() ) {
                buf.append( " " );
                buf.append( window.getOperator() );
                buf.append( buildOperatorParameterDRL( window.getParameters() ) );
            }
        }

        private void generateConstraints( FactPattern pattern ) {
            int printedCount = 0;
            for ( int i = 0; i < pattern.getFieldConstraints().length; i++ ) {
                StringBuilder buffer = new StringBuilder();
                generateConstraint( pattern.getConstraintList().getConstraints()[ i ],
                                    false,
                                    buffer );
                if ( buffer.length() > 0 ) {
                    if ( printedCount > 0 ) {
                        buf.append( ", " );
                    }
                    buf.append( buffer );
                    printedCount++;
                }
            }
        }

        /**
         * Recursively process the nested constraints. It will only put brackets
         * in for the ones that aren't at top level. This makes for more
         * readable DRL in the most common cases.
         */
        private void generateConstraint( FieldConstraint con,
                                         boolean nested,
                                         StringBuilder buf ) {
            if ( con instanceof CompositeFieldConstraint ) {
                CompositeFieldConstraint cfc = (CompositeFieldConstraint) con;
                if ( nested ) {
                    buf.append( "( " );
                }
                FieldConstraint[] nestedConstraints = cfc.getConstraints();
                if ( nestedConstraints != null ) {
                    for ( int i = 0; i < nestedConstraints.length; i++ ) {
                        generateConstraint( nestedConstraints[ i ],
                                            true,
                                            buf );
                        if ( i < ( nestedConstraints.length - 1 ) ) {
                            // buf.append(" ) ");
                            buf.append( cfc.getCompositeJunctionType() + " " );
                            // buf.append(" ( ");
                        }
                    }
                }
                if ( nested ) {
                    buf.append( ")" );
                }
            } else {
                generateSingleFieldConstraint( (SingleFieldConstraint) con,
                                               buf );
            }
        }

        private void generateSingleFieldConstraint( final SingleFieldConstraint constr,
                                                    StringBuilder buf ) {
            if ( constr.getConstraintValueType() == BaseSingleFieldConstraint.TYPE_PREDICATE ) {
                buf.append( "eval( " );
                buf.append( constr.getValue() );
                buf.append( " )" );
            } else {
                if ( constr.isBound() ) {
                    bindingsFields.put( constr.getFieldBinding(),
                                        constr );
                    buf.append( constr.getFieldBinding() );
                    buf.append( " : " );
                }
                if ( ( constr.getOperator() != null
                        && ( constr.getValue() != null
                        || constr.getOperator().equals( "== null" )
                        || constr.getOperator().equals( "!= null" ) ) )
                        || constr.getFieldBinding() != null
                        || constr.getConstraintValueType() == BaseSingleFieldConstraint.TYPE_EXPR_BUILDER_VALUE
                        || constr instanceof SingleFieldConstraintEBLeftSide ) {
                    SingleFieldConstraint parent = (SingleFieldConstraint) constr.getParent();
                    StringBuilder parentBuf = new StringBuilder();
                    while ( parent != null ) {
                        String fieldName = parent.getFieldName();
                        parentBuf.insert( 0,
                                          fieldName + "." );
                        parent = (SingleFieldConstraint) parent.getParent();
                    }
                    buf.append( parentBuf );
                    if ( constr instanceof SingleFieldConstraintEBLeftSide ) {
                        buf.append( ( (SingleFieldConstraintEBLeftSide) constr ).getExpressionLeftSide().getText() );
                    } else {
                        String fieldName = constr.getFieldName();
                        buf.append( fieldName );
                    }
                }

                Map<String, String> parameters = null;
                if ( constr instanceof HasParameterizedOperator ) {
                    HasParameterizedOperator hop = constr;
                    parameters = hop.getParameters();
                }

                if ( constr instanceof SingleFieldConstraintEBLeftSide ) {
                    SingleFieldConstraintEBLeftSide sfexp = (SingleFieldConstraintEBLeftSide) constr;
                    addFieldRestriction( buf,
                                         sfexp.getConstraintValueType(),
                                         sfexp.getExpressionLeftSide().getGenericType(),
                                         sfexp.getOperator(),
                                         parameters,
                                         sfexp.getValue(),
                                         sfexp.getExpressionValue() );
                } else {
                    addFieldRestriction( buf,
                                         constr.getConstraintValueType(),
                                         constr.getFieldType(),
                                         constr.getOperator(),
                                         parameters,
                                         constr.getValue(),
                                         constr.getExpressionValue() );
                }

                // and now do the connectives.
                if ( constr.getConnectives() != null ) {
                    for ( int j = 0; j < constr.getConnectives().length; j++ ) {
                        final ConnectiveConstraint conn = constr.getConnectives()[ j ];

                        parameters = null;
                        if ( conn instanceof HasParameterizedOperator ) {
                            HasParameterizedOperator hop = (HasParameterizedOperator) conn;
                            parameters = hop.getParameters();
                        }

                        addFieldRestriction( buf,
                                             conn.getConstraintValueType(),
                                             conn.getFieldType(),
                                             conn.getOperator(),
                                             parameters,
                                             conn.getValue(),
                                             conn.getExpressionValue() );
                    }
                }

            }
        }

        private void addFieldRestriction( final StringBuilder buf,
                                          final int type,
                                          final String fieldType,
                                          final String operator,
                                          final Map<String, String> parameters,
                                          final String value,
                                          final ExpressionFormLine expression ) {
            if ( operator == null ) {
                return;
            }

            buf.append( " " );
            buf.append( operator );

            if ( parameters != null && parameters.size() > 0 ) {
                buf.append( buildOperatorParameterDRL( parameters ) );
            }

            switch ( type ) {
                case BaseSingleFieldConstraint.TYPE_RET_VALUE:
                    buildReturnValueFieldValue( value,
                                                buf );
                    break;
                case BaseSingleFieldConstraint.TYPE_LITERAL:
                    buildLiteralFieldValue( operator,
                                            type,
                                            fieldType,
                                            value,
                                            buf );
                    break;
                case BaseSingleFieldConstraint.TYPE_EXPR_BUILDER_VALUE:
                    buildExpressionFieldValue( expression,
                                               buf );
                    break;
                case BaseSingleFieldConstraint.TYPE_TEMPLATE:
                    buildTemplateFieldValue( type,
                                             fieldType,
                                             value,
                                             buf );
                    break;
                case BaseSingleFieldConstraint.TYPE_ENUM:
                    buildEnumFieldValue( operator,
                                         type,
                                         fieldType,
                                         value,
                                         buf );
                    break;
                default:
                    buildDefaultFieldValue( operator,
                                            value,
                                            buf );
            }
        }

        protected void buildReturnValueFieldValue( String value,
                                                   StringBuilder buf ) {
            buf.append( " " );
            buf.append( "( " );
            buf.append( value );
            buf.append( " )" );
            buf.append( " " );
        }

        protected StringBuilder buildOperatorParameterDRL( Map<String, String> parameters ) {
            String className = parameters.get( SharedConstants.OPERATOR_PARAMETER_GENERATOR );
            if ( className == null ) {
                throw new IllegalStateException( "Implementation of 'org.kie.guvnor.guided.server.util.OperatorParameterDRLBuilder' undefined. Unable to build Operator Parameter DRL." );
            }

            try {
                OperatorParameterDRLBuilder builder = (OperatorParameterDRLBuilder) Class.forName( className ).newInstance();
                return builder.buildDRL( parameters );
            } catch ( ClassNotFoundException cnfe ) {
                throw new IllegalStateException( "Unable to generate Operator DRL using class '" + className + "'.",
                                                 cnfe );
            } catch ( IllegalAccessException iae ) {
                throw new IllegalStateException( "Unable to generate Operator DRL using class '" + className + "'.",
                                                 iae );
            } catch ( InstantiationException ie ) {
                throw new IllegalStateException( "Unable to generate Operator DRL using class '" + className + "'.",
                                                 ie );
            }

        }

        protected void buildLiteralFieldValue( String operator,
                                               int type,
                                               String fieldType,
                                               String value,
                                               StringBuilder buf ) {
            if ( OperatorsOracle.operatorRequiresList( operator ) ) {
                populateValueList( buf,
                                   type,
                                   fieldType,
                                   value );
            } else {
                if ( !operator.equals( "== null" ) && !operator.equals( "!= null" ) ) {
                    buf.append( " " );
                    constraintValueBuilder.buildLHSFieldValue( buf,
                                                               type,
                                                               fieldType,
                                                               value );
                }
            }
            buf.append( " " );
        }

        private void populateValueList( final StringBuilder buf,
                                        final int type,
                                        final String fieldType,
                                        final String value ) {
            String workingValue = value.trim();
            if ( workingValue.startsWith( "(" ) ) {
                workingValue = workingValue.substring( 1 );
            }
            if ( workingValue.endsWith( ")" ) ) {
                workingValue = workingValue.substring( 0,
                                                       workingValue.length() - 1 );
            }
            final String[] values = workingValue.split( "," );
            buf.append( " ( " );
            for ( String v : values ) {
                v = v.trim();
                if ( v.startsWith( "\"" ) ) {
                    v = v.substring( 1 );
                }
                if ( v.endsWith( "\"" ) ) {
                    v = v.substring( 0,
                                     v.length() - 1 );
                }
                constraintValueBuilder.buildLHSFieldValue( buf,
                                                           type,
                                                           fieldType,
                                                           v );
                buf.append( ", " );
            }
            buf.delete( buf.length() - 2,
                        buf.length() );
            buf.append( " )" );
        }

        protected void buildExpressionFieldValue( ExpressionFormLine expression,
                                                  StringBuilder buf ) {
            if ( expression != null ) {
                buf.append( " " );
                buf.append( expression.getText() );
                buf.append( " " );
            }
        }

        protected void buildTemplateFieldValue( int type,
                                                String fieldType,
                                                String value,
                                                StringBuilder buf ) {
            buf.append( " " );
            constraintValueBuilder.buildLHSFieldValue( buf,
                                                       type,
                                                       fieldType,
                                                       "@{" + value + "}" );
            buf.append( " " );
        }

        private void buildEnumFieldValue( String operator,
                                          int type,
                                          String fieldType,
                                          String value,
                                          StringBuilder buf ) {

            if ( OperatorsOracle.operatorRequiresList( operator ) ) {
                populateValueList( buf,
                                   type,
                                   fieldType,
                                   value );
            } else {
                if ( !operator.equals( "== null" ) && !operator.equals( "!= null" ) ) {
                    buf.append( " " );
                    constraintValueBuilder.buildLHSFieldValue( buf,
                                                               type,
                                                               fieldType,
                                                               value );
                }
            }
            buf.append( " " );
        }

        protected void buildDefaultFieldValue( String operator,
                                               String value,
                                               StringBuilder buf ) {
            if ( !operator.equals( "== null" ) && !operator.equals( "!= null" ) ) {
                buf.append( " " );
                buf.append( value );
            }
            buf.append( " " );
        }

    }

    public static class RHSActionVisitor extends ReflectiveVisitor {

        private StringBuilder buf;
        private boolean isDSLEnhanced;
        private String indentation;
        private int idx = 0;
        private Map<String, IFactPattern> bindingsPatterns;
        private Map<String, FieldConstraint> bindingsFields;
        protected DRLConstraintValueBuilder constraintValueBuilder;

        //Keep a record of Work Items that are instantiated for Actions that depend on them
        private Set<String> instantiatedWorkItems;

        public RHSActionVisitor( boolean isDSLEnhanced,
                                 Map<String, IFactPattern> bindingsPatterns,
                                 Map<String, FieldConstraint> bindingsFields,
                                 DRLConstraintValueBuilder constraintValueBuilder,
                                 StringBuilder b,
                                 String indentation ) {
            this.isDSLEnhanced = isDSLEnhanced;
            this.bindingsPatterns = bindingsPatterns;
            this.bindingsFields = bindingsFields;
            this.constraintValueBuilder = constraintValueBuilder;
            this.indentation = indentation;
            this.instantiatedWorkItems = new HashSet<String>();
            buf = b;
        }

        public void visitActionInsertFact( final ActionInsertFact action ) {
            this.generateInsertCall( action,
                                     false );
        }

        public void visitActionInsertLogicalFact( final ActionInsertLogicalFact action ) {
            this.generateInsertCall( action,
                                     true );
        }

        public void visitFreeFormLine( FreeFormLine ffl ) {
            String[] lines = ffl.getText().split( "\\n|\\r\\n" );
            for ( String line : lines ) {
                this.buf.append( indentation );
                if ( isDSLEnhanced ) {
                    buf.append( ">" );
                }
                this.buf.append( line + "\n" );
            }
        }

        private void generateInsertCall( final ActionInsertFact action,
                                         final boolean isLogic ) {
            buf.append( indentation );
            if ( isDSLEnhanced ) {
                buf.append( ">" );
            }
            if ( action.getFieldValues().length == 0 && action.getBoundName() == null ) {
                buf.append( ( isLogic ) ? "insertLogical( new " : "insert( new " );

                buf.append( action.getFactType() );
                buf.append( "() );\n" );
            } else {
                buf.append( action.getFactType() );
                if ( action.getBoundName() == null ) {
                    buf.append( " fact" );
                    buf.append( idx );
                } else {
                    buf.append( " " + action.getBoundName() );
                }
                buf.append( " = new " );
                buf.append( action.getFactType() );
                buf.append( "();\n" );
                if ( action.getBoundName() == null ) {
                    generateSetMethodCalls( "fact" + idx,
                                            action.getFieldValues() );
                } else {
                    generateSetMethodCalls( action.getBoundName(),
                                            action.getFieldValues() );
                }

                buf.append( indentation );
                if ( isDSLEnhanced ) {
                    buf.append( ">" );
                }
                if ( isLogic ) {
                    buf.append( "insertLogical( " );
                    if ( action.getBoundName() == null ) {
                        buf.append( "fact" );
                        buf.append( idx++ );
                    } else {
                        buf.append( action.getBoundName() );
                    }
                    buf.append( " );\n" );
                } else {
                    buf.append( "insert( " );
                    if ( action.getBoundName() == null ) {
                        buf.append( "fact" );
                        buf.append( idx++ );
                    } else {
                        buf.append( action.getBoundName() );
                    }

                    buf.append( " );\n" );
                }
                //                buf.append(idx++);
                //                buf.append(" );\n");
            }
        }

        public void visitActionUpdateField( final ActionUpdateField action ) {
            this.visitActionSetField( action );
            buf.append( indentation );
            if ( isDSLEnhanced ) {
                buf.append( ">" );
            }
            buf.append( "update( " );
            buf.append( action.getVariable() );
            buf.append( " );\n" );
        }

        public void visitActionGlobalCollectionAdd( final ActionGlobalCollectionAdd add ) {
            buf.append( indentation );
            if ( isDSLEnhanced ) {
                buf.append( ">" );
            }
            buf.append( add.getGlobalName() + ".add( " + add.getFactName() + " );\n" );
        }

        public void visitActionRetractFact( final ActionRetractFact action ) {
            buf.append( indentation );
            if ( isDSLEnhanced ) {
                buf.append( ">" );
            }
            buf.append( "retract( " );
            buf.append( action.getVariableName() );
            buf.append( " );\n" );
        }

        public void visitDSLSentence( final DSLSentence sentence ) {
            buf.append( indentation );
            buf.append( sentence.interpolate() );
            buf.append( "\n" );
        }

        public void visitActionExecuteWorkItem( final ActionExecuteWorkItem action ) {
            String wiName = action.getWorkDefinition().getName();
            String wiImplName = WORKITEM_PREFIX + wiName;

            instantiatedWorkItems.add( wiName );

            buf.append( indentation );
            buf.append( "org.kie.process.instance.impl.WorkItemImpl " );
            buf.append( wiImplName );
            buf.append( " = new org.kie.process.instance.impl.WorkItemImpl();\n" );
            buf.append( indentation );
            buf.append( wiImplName );
            buf.append( ".setName( \"" );
            buf.append( wiName );
            buf.append( "\" );\n" );
            for ( PortableParameterDefinition ppd : action.getWorkDefinition().getParameters() ) {
                makeWorkItemParameterDRL( ppd,
                                          wiImplName );
            }
            buf.append( indentation );
            buf.append( "wim.internalExecuteWorkItem( " );
            buf.append( wiImplName );
            buf.append( " );\n" );
        }

        private void makeWorkItemParameterDRL( PortableParameterDefinition ppd,
                                               String wiImplName ) {
            boolean makeParameter = true;

            //Only add bound parameters if their binding exists (i.e. the corresponding column has a value or - for Limited Entry - is true)
            if ( ppd instanceof HasBinding ) {
                HasBinding hb = (HasBinding) ppd;
                if ( hb.isBound() ) {
                    String binding = hb.getBinding();
                    makeParameter = isBindingValid( binding );
                }
            }
            if ( makeParameter ) {
                buf.append( indentation );
                buf.append( wiImplName );
                buf.append( ".getParameters().put( \"" );
                buf.append( ppd.getName() );
                buf.append( "\", " );
                buf.append( ppd.asString() );
                buf.append( " );\n" );
            }
        }

        private boolean isBindingValid( String binding ) {
            if ( bindingsPatterns.containsKey( binding ) ) {
                return true;
            }
            if ( bindingsFields.containsKey( binding ) ) {
                return true;
            }
            return false;
        }

        public void visitActionSetField( final ActionSetField action ) {
            if ( action instanceof ActionCallMethod ) {
                this.generateSetMethodCallsMethod( (ActionCallMethod) action,
                                                   action.getFieldValues() );
            } else {
                this.generateSetMethodCalls( action.getVariable(),
                                             action.getFieldValues() );
            }
        }

        private void generateSetMethodCalls( final String variableName,
                                             final ActionFieldValue[] fieldValues ) {
            for ( int i = 0; i < fieldValues.length; i++ ) {
                buf.append( indentation );
                if ( isDSLEnhanced ) {
                    buf.append( ">" );
                }
                buf.append( variableName );

                ActionFieldValue fieldValue = fieldValues[ i ];
                if ( fieldValue instanceof ActionFieldFunction ) {
                    buf.append( "." );
                    buf.append( fieldValue.getField() );
                } else {
                    buf.append( ".set" );
                    buf.append( Character.toUpperCase( fieldValues[ i ].getField().charAt( 0 ) ) );
                    buf.append( fieldValues[ i ].getField().substring( 1 ) );
                }
                buf.append( "( " );
                generateSetMethodCallParameterValue( buf,
                                                     fieldValue );
                buf.append( " );\n" );
            }
        }

        private void generateSetMethodCallParameterValue( StringBuilder buf,
                                                          ActionFieldValue fieldValue ) {
            if ( fieldValue.isFormula() ) {
                buildFormulaFieldValue( fieldValue,
                                        buf );
            } else if ( fieldValue.getNature() == FieldNatureType.TYPE_TEMPLATE ) {
                buildTemplateFieldValue( fieldValue,
                                         buf );
            } else if ( fieldValue instanceof ActionWorkItemFieldValue ) {
                buildWorkItemFieldValue( (ActionWorkItemFieldValue) fieldValue,
                                         buf );
            } else {
                buildDefaultFieldValue( fieldValue,
                                        buf );
            }
        }

        protected void buildFormulaFieldValue( ActionFieldValue fieldValue,
                                               StringBuilder buf ) {
            buf.append( fieldValue.getValue().substring( 1 ) );
        }

        protected void buildTemplateFieldValue( ActionFieldValue fieldValue,
                                                StringBuilder buf ) {
            constraintValueBuilder.buildRHSFieldValue( buf,
                                                       fieldValue.getType(),
                                                       "@{" + fieldValue.getValue() + "}" );
        }

        protected void buildWorkItemFieldValue( ActionWorkItemFieldValue afv,
                                                StringBuilder buf ) {
            if ( instantiatedWorkItems.contains( afv.getWorkItemName() ) ) {
                buf.append( "(" );
                buf.append( afv.getWorkItemParameterClassName() );
                buf.append( ") " );
                buf.append( WORKITEM_PREFIX );
                buf.append( afv.getWorkItemName() );
                buf.append( ".getResult( \"" );
                buf.append( afv.getWorkItemParameterName() );
                buf.append( "\" )" );
            } else {
                buf.append( "null" );
            }
        }

        protected void buildDefaultFieldValue( ActionFieldValue fieldValue,
                                               StringBuilder buf ) {
            constraintValueBuilder.buildRHSFieldValue( buf,
                                                       fieldValue.getType(),
                                                       fieldValue.getValue() );
        }

        private void generateSetMethodCallsMethod( final ActionCallMethod action,
                                                   final FieldNature[] fieldValues ) {
            buf.append( indentation );
            if ( isDSLEnhanced ) {
                buf.append( ">" );
            }
            buf.append( action.getVariable() );
            buf.append( "." );

            buf.append( action.getMethodName() );

            buf.append( "( " );
            boolean isFirst = true;
            for ( int i = 0; i < fieldValues.length; i++ ) {
                ActionFieldFunction valueFunction = (ActionFieldFunction) fieldValues[ i ];
                if ( isFirst == true ) {
                    isFirst = false;
                } else {
                    buf.append( ", " );
                }

                constraintValueBuilder.buildRHSFieldValue( buf,
                                                           valueFunction.getType(),
                                                           valueFunction.getValue() );
            }
            buf.append( " );\n" );

        }
    }

    public static class RHSClassDependencyVisitor extends ReflectiveVisitor {

        private Map<String, List<ActionFieldValue>> classes = new HashMap<String, List<ActionFieldValue>>();

        public void visitFreeFormLine( FreeFormLine ffl ) {
            //Do nothing other than preventing ReflectiveVisitor recording an error
        }

        public void visitActionGlobalCollectionAdd( final ActionGlobalCollectionAdd add ) {
            //Do nothing other than preventing ReflectiveVisitor recording an error
        }

        public void visitActionRetractFact( final ActionRetractFact action ) {
            //Do nothing other than preventing ReflectiveVisitor recording an error
        }

        public void visitDSLSentence( final DSLSentence sentence ) {
            //Do nothing other than preventing ReflectiveVisitor recording an error
        }

        public void visitActionInsertFact( final ActionInsertFact action ) {
            getClasses( action.getFieldValues() );
        }

        public void visitActionInsertLogicalFact( final ActionInsertLogicalFact action ) {
            getClasses( action.getFieldValues() );
        }

        public void visitActionUpdateField( final ActionUpdateField action ) {
            getClasses( action.getFieldValues() );
        }

        public void visitActionSetField( final ActionSetField action ) {
            getClasses( action.getFieldValues() );
        }

        public void visitActionExecuteWorkItem( final ActionExecuteWorkItem action ) {
            //Do nothing other than preventing ReflectiveVisitor recording an error
        }

        public Map<String, List<ActionFieldValue>> getRHSClasses() {
            return classes;
        }

        private void getClasses( ActionFieldValue[] fieldValues ) {
            for ( ActionFieldValue afv : fieldValues ) {
                String type = afv.getType();
                List<ActionFieldValue> afvs = classes.get( type );
                if ( afvs == null ) {
                    afvs = new ArrayList<ActionFieldValue>();
                    classes.put( type,
                                 afvs );
                }
                afvs.add( afv );
            }
        }

    }

    /**
     * @see BRLPersistence#unmarshal(String)
     */
    public RuleModel unmarshal( String str ) {
        return getRuleModel( preprocessDSL( str ) );
    }

    public RuleModel unmarshalUsingDSL( final String str, final String... dsls ) {
        return getRuleModel( parseDSLs( preprocessDSL( str ), dsls ) );
    }

    private ExpandedDRLInfo parseDSLs(ExpandedDRLInfo expandedDRLInfo, String[] dsls) {
        for (String dsl : dsls) {
            for (String line : dsl.split("\n")) {
                String dslPattern = line.trim();
                if ( dslPattern.length() > 0 ) {
                    if ( dslPattern.startsWith("[when]") ) {
                        expandedDRLInfo.lhsDslPatterns.add(extractDslPattern(dslPattern.substring("[when]".length())));
                    } else if ( dslPattern.startsWith("[then]") ) {
                        expandedDRLInfo.rhsDslPatterns.add(extractDslPattern(dslPattern.substring("[then]".length())));
                    } else if ( dslPattern.startsWith("[]") ) {
                        String pattern = extractDslPattern(dslPattern.substring("[]".length()));
                        expandedDRLInfo.lhsDslPatterns.add(pattern);
                        expandedDRLInfo.rhsDslPatterns.add(pattern);
                    }
                }
            }
        }
        return expandedDRLInfo;
    }

    private String extractDslPattern(String line) {
        return line.substring(0, line.indexOf('=')).trim();
    }

    private RuleModel getRuleModel(ExpandedDRLInfo expandedDRLInfo) {
        RuleDescr ruleDescr = parseDrl(expandedDRLInfo.plainDrl);
        RuleModel m = new RuleModel();
        m.name = ruleDescr.getName();
        boolean isJavaDialect = parseAttributes( m, ruleDescr.getAttributes() );
        Map<String, String> boundParams = parseLhs( m, ruleDescr.getLhs(), expandedDRLInfo );
        parseRhs( m, (String) ruleDescr.getConsequence(), isJavaDialect, boundParams, expandedDRLInfo );
        return m;
    }

    private ExpandedDRLInfo preprocessDSL( String str ) {
        boolean hasDsl = false;
        StringBuilder drl = new StringBuilder();
        String thenLine = null;
        List<String> lhsStatements = new ArrayList<String>();
        List<String> rhsStatements = new ArrayList<String>();
        int statementsWithoutParanthesis = 0;

        String[] lines = str.split("\n");
        RuleSection ruleSection = RuleSection.HEADER;
        for (String line : lines) {
            if ( ruleSection == RuleSection.HEADER ) {
                drl.append(line).append("\n");
                if ( line.contains("when") ) {
                    ruleSection = RuleSection.LHS;
                }
                continue;
            }
            if ( ruleSection == RuleSection.LHS && line.contains("then") ) {
                thenLine = line;
                ruleSection = RuleSection.RHS;
                continue;
            }
            if ( line.trim().startsWith(">") ) {
                hasDsl = true;
            } else if ( line.indexOf('(') < 0 ) {
                statementsWithoutParanthesis++;
            }
            if ( ruleSection == RuleSection.LHS ) {
                lhsStatements.add(line);
            } else {
                rhsStatements.add(line);
            }
        }

        hasDsl |= statementsWithoutParanthesis == lhsStatements.size() + rhsStatements.size();

        ExpandedDRLInfo expandedDRLInfo = new ExpandedDRLInfo(hasDsl);
        if ( !hasDsl ) {
            expandedDRLInfo.plainDrl = str;
            return expandedDRLInfo;
        }

        int lineCounter = -1;
        for (String statement : lhsStatements) {
            lineCounter++;
            String trimmed = statement.trim();
            if ( trimmed.startsWith(">") ) {
                drl.append(trimmed.substring(1)).append("\n");
            } else {
                expandedDRLInfo.dslStatementsInLhs.put(lineCounter, trimmed);
            }
        }

        drl.append(thenLine).append("\n");

        lineCounter = -1;
        for (String statement : rhsStatements) {
            lineCounter++;
            String trimmed = statement.trim();
            if (trimmed.endsWith("end")) {
                trimmed = trimmed.substring(0, trimmed.length()-3).trim();
            }
            if (trimmed.length() > 0) {
                if ( trimmed.startsWith(">") ) {
                    drl.append(trimmed.substring(1)).append("\n");
                } else {
                    expandedDRLInfo.dslStatementsInRhs.put(lineCounter, trimmed);
                }
            }
        }

        expandedDRLInfo.plainDrl = drl.toString();

        return expandedDRLInfo;
    }

    private enum RuleSection { HEADER, LHS, RHS }

    private static class ExpandedDRLInfo {
        private final boolean hasDsl;
        private String plainDrl;

        private Map<Integer, String> dslStatementsInLhs;
        private Map<Integer, String> dslStatementsInRhs;

        private List<String> lhsDslPatterns;
        private List<String> rhsDslPatterns;

        private ExpandedDRLInfo(boolean hasDsl) {
            this.hasDsl = hasDsl;
            dslStatementsInLhs = new HashMap<Integer, String>();
            dslStatementsInRhs = new HashMap<Integer, String>();
            lhsDslPatterns = new ArrayList<String>();
            rhsDslPatterns = new ArrayList<String>();
        }
    }

    private RuleDescr parseDrl( String drl ) {
        DrlParser drlParser = new DrlParser();
        PackageDescr packageDescr = null;
        try {
            packageDescr = drlParser.parse( true, drl );
        } catch ( DroolsParserException e ) {
            throw new RuntimeException( e );
        }
        return packageDescr.getRules().get( 0 );
    }

    private boolean parseAttributes( RuleModel m,
                                     Map<String, AttributeDescr> attributes ) {
        boolean isJavaDialect = false;
        for ( Map.Entry<String, AttributeDescr> entry : attributes.entrySet() ) {
            String name = entry.getKey();
            String value = entry.getValue().getValue();
            RuleAttribute ruleAttribute = new RuleAttribute( name, value );
            m.addAttribute( ruleAttribute );
            isJavaDialect |= name.equals( "dialect" ) && value.equals( "java" );
        }
        return isJavaDialect;
    }

    private Map<String, String> parseLhs( RuleModel m,
                                          AndDescr lhs,
                                          ExpandedDRLInfo expandedDRLInfo ) {
        Map<String, String> boundParams = new HashMap<String, String>();
        int lineCounter = -1;
        for ( BaseDescr descr : lhs.getDescrs() ) {
            lineCounter = parseDslInLhs(m, expandedDRLInfo, lineCounter);
            m.addLhsItem( parseBaseDescr( descr, boundParams ) );
        }
        parseDslInLhs(m, expandedDRLInfo, lineCounter);
        return boundParams;
    }

    private int parseDslInLhs(RuleModel m, ExpandedDRLInfo expandedDRLInfo, int lineCounter) {
        lineCounter++;
        if ( expandedDRLInfo.hasDsl ) {
            String dslLine = expandedDRLInfo.dslStatementsInLhs.get(lineCounter);
            while ( dslLine != null ) {
                m.addLhsItem(toDSLSentence(expandedDRLInfo.lhsDslPatterns, dslLine));
                dslLine = expandedDRLInfo.dslStatementsInLhs.get(++lineCounter);
            }
        }
        return lineCounter;
    }

    private IPattern parseBaseDescr( BaseDescr descr,
                                     Map<String, String> boundParams ) {
        if ( descr instanceof PatternDescr ) {
            return parsePatternDescr( (PatternDescr) descr, boundParams );
        } else if ( descr instanceof AndDescr ) {
            AndDescr andDescr = (AndDescr) descr;
            return parseBaseDescr( andDescr.getDescrs().get( 0 ), boundParams );
        } else if ( descr instanceof ConditionalElementDescr ) {
            return parseExistentialElementDescr( (ConditionalElementDescr) descr, boundParams );
        }
        return null;
    }

    private IFactPattern parsePatternDescr( PatternDescr pattern,
                                            Map<String, String> boundParams ) {
        if ( pattern.getSource() != null ) {
            return parsePatternSource( pattern, pattern.getSource(), boundParams );
        }
        return getFactPattern( pattern, boundParams );
    }

    private FactPattern getFactPattern( PatternDescr pattern,
                                        Map<String, String> boundParams ) {
        String type = pattern.getObjectType();
        FactPattern factPattern = new FactPattern( type );
        parseConstraint( factPattern, pattern.getConstraint() );
        if ( pattern.getIdentifier() != null ) {
            String identifier = pattern.getIdentifier();
            factPattern.setBoundName( identifier );
            boundParams.put( identifier, type );
        }
        return factPattern;
    }

    private IFactPattern parsePatternSource( PatternDescr pattern,
                                             PatternSourceDescr patternSource,
                                             Map<String, String> boundParams ) {
        if ( patternSource instanceof AccumulateDescr ) {
            AccumulateDescr accumulate = (AccumulateDescr) patternSource;
            FromAccumulateCompositeFactPattern fac = new FromAccumulateCompositeFactPattern();
            fac.setSourcePattern( parseBaseDescr( accumulate.getInput(), boundParams ) );
            fac.setFactPattern( new FactPattern( pattern.getObjectType() ) );
            for ( AccumulateDescr.AccumulateFunctionCallDescr func : accumulate.getFunctions() ) {
                String funcName = func.getFunction();
                boolean first = true;
                StringBuilder sb = new StringBuilder();
                for ( String param : func.getParams() ) {
                    if ( first ) {
                        first = false;
                    } else {
                        sb.append( ", " );
                    }
                    sb.append( param );
                }
                fac.setFunction( funcName + "(" + sb + ")" );
                break;
            }
            return fac;
        } else if ( patternSource instanceof CollectDescr ) {
            CollectDescr collect = (CollectDescr) patternSource;
            FromCollectCompositeFactPattern fac = new FromCollectCompositeFactPattern();
            fac.setRightPattern( parseBaseDescr( collect.getInputPattern(), boundParams ) );
            fac.setFactPattern( new FactPattern( pattern.getObjectType() ) );
            return fac;
        } else if ( patternSource instanceof EntryPointDescr ) {
            EntryPointDescr entryPoint = (EntryPointDescr) patternSource;
            FromEntryPointFactPattern fep = new FromEntryPointFactPattern();
            fep.setEntryPointName( entryPoint.getText() );
            fep.setFactPattern( getFactPattern( pattern, boundParams ) );
            return fep;
        }
        throw new RuntimeException( "Unknown pattern source " + patternSource );
    }

    private CompositeFactPattern parseExistentialElementDescr( ConditionalElementDescr conditionalDescr,
                                                               Map<String, String> boundParams ) {
        CompositeFactPattern comp = conditionalDescr instanceof NotDescr ?
                new CompositeFactPattern( CompositeFactPattern.COMPOSITE_TYPE_NOT ) :
                conditionalDescr instanceof OrDescr ?
                        new CompositeFactPattern( CompositeFactPattern.COMPOSITE_TYPE_OR ) :
                        new CompositeFactPattern( CompositeFactPattern.COMPOSITE_TYPE_EXISTS );
        addPatternToComposite( conditionalDescr, comp, boundParams );
        return comp;
    }

    private void addPatternToComposite( ConditionalElementDescr conditionalDescr,
                                        CompositeFactPattern comp,
                                        Map<String, String> boundParams ) {
        for ( Object descr : conditionalDescr.getDescrs() ) {
            if ( descr instanceof PatternDescr ) {
                comp.addFactPattern( parsePatternDescr( (PatternDescr) descr, boundParams ) );
            } else if ( descr instanceof ConditionalElementDescr ) {
                addPatternToComposite( (ConditionalElementDescr) descr, comp, boundParams );
            }
        }
    }

    private void parseConstraint( FactPattern factPattern,
                                  ConditionalElementDescr constraint ) {
        for ( BaseDescr descr : constraint.getDescrs() ) {
            if ( descr instanceof ExprConstraintDescr ) {
                ExprConstraintDescr exprConstraint = (ExprConstraintDescr) descr;
                Expr expr = parseExpr( exprConstraint.getExpression() );
                factPattern.addConstraint( expr.asFieldConstraint( factPattern ) );
            }
        }
    }

    private static final String[] OPERATORS = new String[]{ "==", "!=", "<=", ">=", "<", ">", "not in", "in" };

    private static String findOperator( String expr ) {
        for ( String op : OPERATORS ) {
            if ( expr.contains( op ) ) {
                return op;
            }
        }
        return null;
    }

    private void parseRhs( RuleModel m,
                           String rhs,
                           boolean isJavaDialect,
                           Map<String, String> boundParams,
                           ExpandedDRLInfo expandedDRLInfo ) {
        PortableWorkDefinition pwd = null;
        Map<String, List<String>> setStatements = new HashMap<String, List<String>>();
        Map<String, String> factsType = new HashMap<String, String>();

        int lineCounter = -1;
        for ( String line : rhs.split( "\n" ) ) {
            lineCounter++;
            if ( expandedDRLInfo.hasDsl ) {
                String dslLine = expandedDRLInfo.dslStatementsInRhs.get(lineCounter);
                while ( dslLine != null ) {
                    m.addRhsItem(toDSLSentence(expandedDRLInfo.rhsDslPatterns, dslLine));
                    dslLine = expandedDRLInfo.dslStatementsInRhs.get(++lineCounter);
                }
            }
            line = line.trim();
            if ( line.startsWith( "insertLogical" ) ) {
                String fact = unwrapParenthesis( line );
                String type = getStatementType( fact, factsType );
                if ( type != null ) {
                    ActionInsertLogicalFact action = new ActionInsertLogicalFact( type );
                    m.addRhsItem( action );
                    if ( factsType.containsKey( fact ) ) {
                        addSettersToAction( setStatements, fact, action, isJavaDialect );
                    }
                }
            } else if ( line.startsWith( "insert" ) ) {
                String fact = unwrapParenthesis( line );
                String type = getStatementType( fact, factsType );
                if ( type != null ) {
                    ActionInsertFact action = new ActionInsertFact( type );
                    m.addRhsItem( action );
                    if ( factsType.containsKey( fact ) ) {
                        action.setBoundName( fact );
                        addSettersToAction( setStatements, fact, action, isJavaDialect );
                    }
                }
            } else if ( line.startsWith( "update" ) ) {
                String variable = unwrapParenthesis( line );
                ActionUpdateField action = new ActionUpdateField();
                action.setVariable( variable );
                m.addRhsItem( action );
                addSettersToAction( setStatements, variable, action, isJavaDialect );
            } else if ( line.startsWith( "retract" ) ) {
                String variable = unwrapParenthesis( line );
                m.addRhsItem( new ActionRetractFact( variable ) );
            } else if ( line.startsWith( "org.kie.process.instance.impl.WorkItemImpl wiWorkItem" ) ) {
                ActionExecuteWorkItem awi = new ActionExecuteWorkItem();
                pwd = new PortableWorkDefinition();
                pwd.setName( "WorkItem" );
                awi.setWorkDefinition( pwd );
                m.addRhsItem( awi );
            } else if ( line.startsWith( "wiWorkItem.getParameters().put" ) ) {
                String statement = line.substring( "wiWorkItem.getParameters().put".length() );
                statement = unwrapParenthesis( statement );
                int commaPos = statement.indexOf( ',' );
                String name = statement.substring( 0, commaPos ).trim();
                String value = statement.substring( commaPos + 1 ).trim();
                pwd.addParameter( buildPortableParameterDefinition( name, value, boundParams ) );
            } else if ( line.startsWith( "wim.internalExecuteWorkItem" ) || line.startsWith( "wiWorkItem.setName" ) ) {
                // ignore
            } else {
                int dotPos = line.indexOf( '.' );
                int argStart = line.indexOf( '(' );
                if ( dotPos > 0 && argStart > dotPos ) {
                    String variable = line.substring( 0, dotPos ).trim();
                    String methodName = line.substring( dotPos + 1, argStart ).trim();
                    if ( isJavaIdentifier( methodName ) ) {
                        if ( getSettedField( methodName ) != null ) {
                            List<String> setters = setStatements.get( variable );
                            if ( setters == null ) {
                                setters = new ArrayList<String>();
                                setStatements.put( variable, setters );
                            }
                            setters.add( line );
                        } else {
                            ActionCallMethod acm = new ActionCallMethod();
                            acm.setMethodName( methodName );
                            acm.setVariable( variable );
                            m.addRhsItem( acm );
                            String params = unwrapParenthesis( line );
                            for ( String param : params.split( "," ) ) {
                                param = param.trim();
                                if ( param.length() == 0 ) {
                                    continue;
                                }
                                String dataType = inferDataType( param, isJavaDialect );
                                acm.addFieldValue( new ActionFieldFunction( null, adjustParam( dataType, param, isJavaDialect ), dataType ) );
                            }
                        }
                        continue;
                    }
                }

                int eqPos = line.indexOf( '=' );
                if ( eqPos > 0 ) {
                    String field = line.substring( 0, eqPos ).trim();
                    String[] split = field.split( " " );
                    if ( split.length == 2 ) {
                        factsType.put( split[ 1 ], split[ 0 ] );
                    }
                }
            }
        }

        for ( Map.Entry<String, List<String>> entry : setStatements.entrySet() ) {
            ActionSetField action = new ActionSetField( entry.getKey() );
            addSettersToAction( entry.getValue(), action, isJavaDialect );
            m.addRhsItem( action );
        }

        if ( expandedDRLInfo.hasDsl ) {
            String dslLine = expandedDRLInfo.dslStatementsInRhs.get(++lineCounter);
            while ( dslLine != null ) {
                m.addRhsItem(toDSLSentence(expandedDRLInfo.rhsDslPatterns, dslLine));
                dslLine = expandedDRLInfo.dslStatementsInRhs.get(++lineCounter);
            }
        }
    }

    private DSLSentence toDSLSentence(List<String> dslPatterns, String dslLine) {
        DSLSentence dslSentence = new DSLSentence();
        for (String dslPattern : dslPatterns) {
            String regex = dslPattern.replaceAll("\\{\\s*[\\:\\.\\w]+\\s*\\}", "(.*)");
            Matcher m = Pattern.compile(regex).matcher(dslLine);
            if ( m.matches() ) {
                dslSentence.setDefinition( dslPattern );
                for (int i = 0; i < m.groupCount(); i++) {
                    dslSentence.getValues().get( i ).setValue( m.group(i+1) );
                }
                return dslSentence;
            }
        }
        dslSentence.setDefinition( dslLine );
        return dslSentence;
    }

    private PortableParameterDefinition buildPortableParameterDefinition( String name,
                                                                          String value,
                                                                          Map<String, String> boundParams ) {
        PortableParameterDefinition paramDef;
        String type = boundParams.get( value );
        if ( type != null ) {
            if ( type.equals( "Boolean" ) ) {
                paramDef = new PortableBooleanParameterDefinition();
            } else if ( type.equals( "String" ) ) {
                paramDef = new PortableStringParameterDefinition();
            } else if ( type.equals( "Float" ) ) {
                paramDef = new PortableBooleanParameterDefinition();
            } else if ( type.equals( "Integer" ) ) {
                paramDef = new PortableIntegerParameterDefinition();
            } else {
                paramDef = new PortableObjectParameterDefinition();
            }
            ( (HasBinding) paramDef ).setBinding( value );
        } else if ( value.equals( "true" ) || value.equals( "false" ) || value.equals( "Boolean.TRUE" ) || value.equals( "Boolean.FALSE" ) ) {
            paramDef = new PortableBooleanParameterDefinition();
            boolean b = value.equals( "true" ) || value.equals( "Boolean.TRUE" );
            ( (PortableBooleanParameterDefinition) paramDef ).setValue( b );
        } else if ( value.startsWith( "\"" ) ) {
            paramDef = new PortableStringParameterDefinition();
            ( (PortableStringParameterDefinition) paramDef ).setValue( value.substring( 1, value.length() - 1 ) );
        } else if ( Character.isDigit( value.charAt( 0 ) ) ) {
            if ( value.endsWith( "f" ) ) {
                paramDef = new PortableFloatParameterDefinition();
                ( (PortableFloatParameterDefinition) paramDef ).setValue( Float.parseFloat( value ) );
            } else {
                paramDef = new PortableIntegerParameterDefinition();
                ( (PortableIntegerParameterDefinition) paramDef ).setValue( Integer.parseInt( value ) );
            }
        } else {
            throw new RuntimeException( "Unknown parameter " + value );
        }
        paramDef.setName( name.substring( 1, name.length() - 1 ) );
        return paramDef;
    }

    private void addSettersToAction( Map<String, List<String>> setStatements,
                                     String variable,
                                     ActionFieldList action,
                                     boolean isJavaDialect ) {
        addSettersToAction( setStatements.remove( variable ), action, isJavaDialect );
    }

    private void addSettersToAction( List<String> setters,
                                     ActionFieldList action,
                                     boolean isJavaDialect ) {
        if ( setters != null ) {
            for ( String statement : setters ) {
                int dotPos = statement.indexOf( '.' );
                int argStart = statement.indexOf( '(' );
                String methodName = statement.substring( dotPos + 1, argStart ).trim();
                String field = getSettedField( methodName );
                String value = unwrapParenthesis( statement );
                String dataType = inferDataType( value, isJavaDialect );
                action.addFieldValue( buildFieldValue( isJavaDialect, field, value, dataType ) );
            }
        }
    }

    private ActionFieldValue buildFieldValue( boolean isJavaDialect,
                                              String field,
                                              String value,
                                              String dataType ) {
        if ( value.contains( "wiWorkItem.getResult" ) ) {
            field = field.substring( 0, 1 ).toUpperCase() + field.substring( 1 );
            String wiParam = field.substring( "Results".length() );
            if ( wiParam.equals( "BooleanResult" ) ) {
                return new ActionWorkItemFieldValue( field, DataType.TYPE_BOOLEAN, "WorkItem", wiParam, Boolean.class.getName() );
            } else if ( wiParam.equals( "StringResult" ) ) {
                return new ActionWorkItemFieldValue( field, DataType.TYPE_STRING, "WorkItem", wiParam, String.class.getName() );
            } else if ( wiParam.equals( "IntegerResult" ) ) {
                return new ActionWorkItemFieldValue( field, DataType.TYPE_NUMERIC_INTEGER, "WorkItem", wiParam, Integer.class.getName() );
            } else if ( wiParam.equals( "FloatResult" ) ) {
                return new ActionWorkItemFieldValue( field, DataType.TYPE_NUMERIC_FLOAT, "WorkItem", wiParam, Float.class.getName() );
            }
        }
        return new ActionFieldValue( field, adjustParam( dataType, value, isJavaDialect ), dataType );
    }

    private boolean isJavaIdentifier( String name ) {
        if ( name == null || name.length() == 0 || !Character.isJavaIdentifierStart( name.charAt( 0 ) ) ) {
            return false;
        }
        for ( int i = 1; i < name.length(); i++ ) {
            if ( !Character.isJavaIdentifierPart( name.charAt( i ) ) ) {
                return false;
            }
        }
        return true;
    }

    private String inferDataType( String param,
                                  boolean isJavaDialect ) {
        if ( param.startsWith( "sdf.parse(\"" ) ) {
            return DataType.TYPE_DATE;
        } else if ( param.startsWith( "\"" ) ) {
            return DataType.TYPE_STRING;
        } else if ( param.equals( "true" ) || param.equals( "false" ) ) {
            return DataType.TYPE_BOOLEAN;
        } else if ( param.endsWith( "B" ) || ( isJavaDialect && param.startsWith( "new java.math.BigDecimal" ) ) ) {
            return DataType.TYPE_NUMERIC_BIGDECIMAL;
        } else if ( param.endsWith( "I" ) || ( isJavaDialect && param.startsWith( "new java.math.BigInteger" ) ) ) {
            return DataType.TYPE_NUMERIC_BIGINTEGER;
        }
        return DataType.TYPE_NUMERIC;
    }

    private String adjustParam( String dataType,
                                String param,
                                boolean isJavaDialect ) {
        if ( dataType == DataType.TYPE_DATE ) {
            return param.substring( "sdf.parse(\"".length(), param.length() - 2 );
        } else if ( dataType == DataType.TYPE_STRING ) {
            return param.substring( 1, param.length() - 1 );
        } else if ( dataType == DataType.TYPE_NUMERIC_BIGDECIMAL || dataType == DataType.TYPE_NUMERIC_BIGINTEGER ) {
            if ( isJavaDialect ) {
                return param.substring( "new java.math.BigDecimal(\"".length(), param.length() - 2 );
            } else {
                return param.substring( 0, param.length() - 1 );
            }
        }
        return param;
    }

    private String getSettedField( String methodName ) {
        if ( methodName.length() > 3 && methodName.startsWith( "set" ) ) {
            String field = methodName.substring( 3 );
            if ( Character.isUpperCase( field.charAt( 0 ) ) ) {
                return field.substring( 0, 1 ).toLowerCase() + field.substring( 1 );
            }
        }
        return null;
    }

    private static String unwrapParenthesis( String s ) {
        int start = s.indexOf( '(' );
        int end = s.lastIndexOf( ')' );
        return s.substring( start + 1, end ).trim();
    }

    private String getStatementType( String fact,
                                     Map<String, String> factsType ) {
        String type = null;
        if ( fact.startsWith( "new " ) ) {
            String inserted = fact.substring( 4 ).trim();
            if ( inserted.endsWith( "()" ) ) {
                type = inserted.substring( 0, inserted.length() - 2 ).trim();
            }
        } else {
            type = factsType.get( fact );
        }
        return type;
    }

    private Expr parseExpr( String expr ) {
        List<String> splittedExpr = splitExpression( expr );
        if ( splittedExpr.size() == 1 ) {
            String singleExpr = splittedExpr.get( 0 );
            if ( singleExpr.startsWith( "(" ) ) {
                return parseExpr( singleExpr.substring( 1 ) );
            } else if ( singleExpr.startsWith( "eval" ) ) {
                return new EvalExpr( unwrapParenthesis( singleExpr ) );
            } else {
                return new SimpleExpr( singleExpr );
            }
        }
        ComplexExpr complexExpr = new ComplexExpr( splittedExpr.get( 1 ) );
        for ( int i = 0; i < splittedExpr.size(); i += 2 ) {
            complexExpr.subExprs.add( parseExpr( splittedExpr.get( i ) ) );
        }
        return complexExpr;
    }

    private enum SplitterState {
        START, EXPR, PIPE, OR, AMPERSAND, AND, NESTED
    }

    private List<String> splitExpression( String expr ) {
        List<String> splittedExpr = new ArrayList<String>();
        int nestingLevel = 0;
        SplitterState status = SplitterState.START;

        StringBuilder sb = new StringBuilder();
        for ( char ch : expr.toCharArray() ) {
            switch ( status ) {
                case START:
                    if ( ch == '(' ) {
                        status = SplitterState.NESTED;
                        nestingLevel++;
                    } else {
                        status = SplitterState.EXPR;
                        sb.append( ch );
                    }
                    break;
                case EXPR:
                    if ( ch == '|' ) {
                        status = SplitterState.PIPE;
                    } else if ( ch == '&' ) {
                        status = SplitterState.AMPERSAND;
                    } else {
                        sb.append( ch );
                    }
                    break;
                case PIPE:
                    if ( ch == '|' ) {
                        status = SplitterState.OR;
                    } else {
                        status = SplitterState.EXPR;
                        sb.append( '|' ).append( ch );
                    }
                    break;
                case AMPERSAND:
                    if ( ch == '&' ) {
                        status = SplitterState.AND;
                    } else {
                        status = SplitterState.EXPR;
                        sb.append( '&' ).append( ch );
                    }
                    break;
                case OR:
                case AND:
                    if ( ch == '=' || ch == '!' || ch == '<' || ch == '>' ) {
                        status = SplitterState.EXPR;
                        sb.append( status == SplitterState.AND ? "&& " : "|| " );
                        sb.append( ch );
                    } else if ( Character.isJavaIdentifierStart( ch ) ) {
                        String currentExpr = sb.toString().trim();
                        if ( currentExpr.length() > 0 ) {
                            splittedExpr.add( currentExpr );
                        }
                        splittedExpr.add( status == SplitterState.AND ? "&&" : "||" );
                        status = SplitterState.EXPR;
                        sb = new StringBuilder();
                        sb.append( ch );
                    } else if ( ch == '(' ) {
                        String currentExpr = sb.toString().trim();
                        if ( currentExpr.length() > 0 ) {
                            splittedExpr.add( currentExpr );
                        }
                        splittedExpr.add( status == SplitterState.AND ? "&&" : "||" );
                        status = SplitterState.NESTED;
                        nestingLevel++;
                        sb = new StringBuilder();
                        sb.append( ch );
                    }
                    break;
                case NESTED:
                    if ( ch == '(' ) {
                        nestingLevel++;
                        sb.append( ch );
                    } else if ( ch == ')' ) {
                        nestingLevel--;
                        if ( nestingLevel == 0 ) {
                            String currentExpr = sb.toString().trim();
                            if ( currentExpr.length() > 0 ) {
                                splittedExpr.add( "(" + currentExpr );
                            }
                            status = SplitterState.EXPR;
                            sb = new StringBuilder();
                        } else {
                            sb.append( ch );
                        }
                    } else {
                        sb.append( ch );
                    }
                    break;
            }
        }
        String currentExpr = sb.toString().trim();
        if ( currentExpr.length() > 0 ) {
            splittedExpr.add( currentExpr );
        }
        return splittedExpr;
    }

    private interface Expr {

        FieldConstraint asFieldConstraint( FactPattern factPattern );
    }

    private static class SimpleExpr implements Expr {

        private final String expr;

        private SimpleExpr( String expr ) {
            this.expr = expr;
        }

        public FieldConstraint asFieldConstraint( FactPattern factPattern ) {
            String fieldName = expr;

            String operator = findOperator( expr );
            String value = null;
            if ( operator != null ) {
                int opPos = expr.indexOf( operator );
                fieldName = expr.substring( 0, opPos ).trim();
                value = expr.substring( opPos + operator.length(), expr.length() ).trim();
            }

            return fieldName.contains( "." ) ?
                    asExpressionBuilderConstraint( factPattern, fieldName, operator, value ) :
                    asSingleFieldConstraint( fieldName, operator, value );
        }

        private FieldConstraint asSingleFieldConstraint( String fieldName,
                                                         String operator,
                                                         String value ) {
            SingleFieldConstraint con = new SingleFieldConstraint();
            fieldName = setFieldBindingOnContraint( fieldName, con );
            con.setFieldName( fieldName );
            setOperatorAndValueOnConstraint( operator, value, con );
            return con;
        }

        private FieldConstraint asExpressionBuilderConstraint( FactPattern factPattern,
                                                               String fieldName,
                                                               String operator,
                                                               String value ) {
            SingleFieldConstraintEBLeftSide con = new SingleFieldConstraintEBLeftSide();
            fieldName = setFieldBindingOnContraint( fieldName, con );
            con.getExpressionLeftSide().appendPart( new ExpressionUnboundFact( factPattern ) );

            String type = setOperatorAndValueOnConstraint( operator, value, con );
            String[] splits = fieldName.split( "\\." );
            for ( int i = 0; i < splits.length - 1; i++ ) {
                con.getExpressionLeftSide().appendPart( new ExpressionField( splits[ i ].trim(), "", DataType.TYPE_OBJECT ) );
            }
            con.getExpressionLeftSide().appendPart( new ExpressionField( splits[ splits.length - 1 ].trim(), "", type ) );
            return con;
        }

        private String setFieldBindingOnContraint( String fieldName,
                                                   SingleFieldConstraint con ) {
            int colonPos = fieldName.indexOf( ':' );
            if ( colonPos > 0 ) {
                String fieldBinding = fieldName.substring( 0, colonPos ).trim();
                con.setFieldBinding( fieldBinding );
                fieldName = fieldName.substring( colonPos + 1 ).trim();
            }
            return fieldName;
        }

        private String setOperatorAndValueOnConstraint( String operator,
                                                        String value,
                                                        SingleFieldConstraint con ) {
            if ( operator != null ) {
                con.setOperator( operator );
                boolean isAnd = value.contains( "&&" );
                String[] splittedValue = isAnd ? value.split( "\\&\\&" ) : value.split( "\\|\\|" );
                String type = setValueOnConstraint( operator, con, splittedValue[ 0 ].trim() );

                if ( splittedValue.length > 1 ) {
                    ConnectiveConstraint[] connectiveConstraints = new ConnectiveConstraint[ splittedValue.length - 1 ];
                    for ( int i = 0; i < connectiveConstraints.length; i++ ) {
                        String constraint = splittedValue[ i + 1 ].trim();
                        String connectiveOperator = findOperator( constraint );
                        String connectiveValue = constraint.substring( connectiveOperator.length() ).trim();

                        connectiveConstraints[ i ] = new ConnectiveConstraint();
                        connectiveConstraints[ i ].setOperator( ( isAnd ? "&& " : "|| " ) + connectiveOperator );
                        setValueOnConstraint( operator, connectiveConstraints[ i ], connectiveValue );
                    }
                    con.setConnectives( connectiveConstraints );
                }
                return type;
            }
            return null;
        }

        private String setValueOnConstraint( String operator,
                                             BaseSingleFieldConstraint con,
                                             String value ) {
            String type = null;
            if ( value.startsWith( "\"" ) ) {
                type = DataType.TYPE_STRING;
                con.setConstraintValueType( SingleFieldConstraint.TYPE_LITERAL );
                con.setValue( value.substring( 1, value.length() - 1 ) );
            } else if ( value.startsWith( "(" ) ) {
                if ( operator.contains( "in" ) ) {
                    value = unwrapParenthesis( value );
                    type = value.startsWith( "\"" ) ? DataType.TYPE_STRING : DataType.TYPE_NUMERIC_INTEGER;
                    con.setConstraintValueType( SingleFieldConstraint.TYPE_LITERAL );
                    con.setValue( value );
                } else {
                    con.setConstraintValueType( SingleFieldConstraint.TYPE_RET_VALUE );
                    con.setValue( unwrapParenthesis( value ) );
                }
            } else {
                if ( !Character.isDigit( value.charAt( 0 ) ) ) {
                    if ( value.equals( "true" ) || value.equals( "false" ) ) {
                        type = DataType.TYPE_BOOLEAN;
                        con.setConstraintValueType( BaseSingleFieldConstraint.TYPE_ENUM );
                    } else {
                        con.setConstraintValueType( SingleFieldConstraint.TYPE_VARIABLE );
                    }
                } else {
                    if ( value.endsWith( "I" ) ) {
                        type = DataType.TYPE_NUMERIC_BIGINTEGER;
                        value = value.substring( 0, value.length() - 1 );
                    } else if ( value.endsWith( "B" ) ) {
                        type = DataType.TYPE_NUMERIC_BIGDECIMAL;
                        value = value.substring( 0, value.length() - 1 );
                    } else {
                        type = DataType.TYPE_NUMERIC_INTEGER;
                    }
                    con.setConstraintValueType( SingleFieldConstraint.TYPE_LITERAL );
                }
                con.setValue( value );
            }
            if ( con instanceof SingleFieldConstraint ) {
                ( (SingleFieldConstraint) con ).setFieldType( type );
            } else if ( con instanceof ConnectiveConstraint ) {
                ( (ConnectiveConstraint) con ).setFieldType( type );
            }
            return type;
        }
    }

    private static class ComplexExpr implements Expr {

        private final List<Expr> subExprs = new ArrayList<Expr>();
        private final String connector;

        private ComplexExpr( String connector ) {
            this.connector = connector;
        }

        public FieldConstraint asFieldConstraint( FactPattern factPattern ) {
            CompositeFieldConstraint comp = new CompositeFieldConstraint();
            comp.setCompositeJunctionType( connector.equals( "&&" ) ? CompositeFieldConstraint.COMPOSITE_TYPE_AND : CompositeFieldConstraint.COMPOSITE_TYPE_OR );
            for ( Expr expr : subExprs ) {
                comp.addConstraint( expr.asFieldConstraint( factPattern ) );
            }
            return comp;
        }
    }

    private static class EvalExpr implements Expr {

        private final String expr;

        private EvalExpr( String expr ) {
            this.expr = expr;
        }

        public FieldConstraint asFieldConstraint( FactPattern factPattern ) {
            SingleFieldConstraint con = new SingleFieldConstraint();
            con.setConstraintValueType( SingleFieldConstraint.TYPE_PREDICATE );
            con.setValue( expr );
            return con;
        }
    }
}
