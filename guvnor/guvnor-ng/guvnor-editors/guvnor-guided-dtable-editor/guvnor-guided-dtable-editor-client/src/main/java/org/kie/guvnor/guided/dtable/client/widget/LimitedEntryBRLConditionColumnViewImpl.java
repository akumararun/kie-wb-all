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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gwt.event.shared.EventBus;
import org.kie.guvnor.datamodel.model.IPattern;
import org.kie.guvnor.datamodel.oracle.DataModelOracle;
import org.kie.guvnor.guided.dtable.client.resources.i18n.Constants;
import org.kie.guvnor.guided.dtable.model.BRLColumn;
import org.kie.guvnor.guided.dtable.model.BRLConditionVariableColumn;
import org.kie.guvnor.guided.dtable.model.BRLRuleModel;
import org.kie.guvnor.guided.dtable.model.CompositeColumn;
import org.kie.guvnor.guided.dtable.model.GuidedDecisionTable52;
import org.kie.guvnor.guided.dtable.model.LimitedEntryBRLConditionColumn;
import org.kie.guvnor.guided.rule.client.editor.RuleModellerConfiguration;
import org.kie.guvnor.guided.rule.model.RuleModel;
import org.kie.guvnor.guided.rule.model.templates.RuleModelCloneVisitor;
import org.uberfire.backend.vfs.Path;

/**
 * An editor for a Limited Entry BRL Condition Columns
 */
public class LimitedEntryBRLConditionColumnViewImpl extends AbstractLimitedEntryBRLColumnViewImpl<IPattern, BRLConditionVariableColumn>
        implements
        LimitedEntryBRLConditionColumnView {

    private Presenter presenter;

    public LimitedEntryBRLConditionColumnViewImpl( final Path path,
                                                   final DataModelOracle oracle,
                                                   final GuidedDecisionTable52 model,
                                                   final boolean isNew,
                                                   final LimitedEntryBRLConditionColumn column,
                                                   final EventBus eventBus ) {
        super( path,
               oracle,
               model,
               isNew,
               column,
               eventBus );

        setTitle( Constants.INSTANCE.ConditionBRLFragmentConfiguration() );
    }

    protected boolean isHeaderUnique( String header ) {
        for ( CompositeColumn<?> cc : model.getConditions() ) {
            for ( int iChild = 0; iChild < cc.getChildColumns().size(); iChild++ ) {
                if ( cc.getChildColumns().get( iChild ).getHeader().equals( header ) ) {
                    return false;
                }
            }
        }
        return true;
    }

    public BRLRuleModel getRuleModel( BRLColumn<IPattern, BRLConditionVariableColumn> column ) {
        BRLRuleModel ruleModel = new BRLRuleModel( model );
        List<IPattern> definition = column.getDefinition();
        ruleModel.lhs = definition.toArray( new IPattern[ definition.size() ] );
        return ruleModel;
    }

    public RuleModellerConfiguration getRuleModellerConfiguration() {
        return new RuleModellerConfiguration( false,
                                              true,
                                              true );
    }

    public void setPresenter( Presenter presenter ) {
        this.presenter = presenter;
    }

    @Override
    protected void doInsertColumn() {
        this.editingCol.setDefinition( Arrays.asList( this.ruleModel.lhs ) );
        presenter.insertColumn( (LimitedEntryBRLConditionColumn) this.editingCol );
    }

    @Override
    protected void doUpdateColumn() {
        this.editingCol.setDefinition( Arrays.asList( this.ruleModel.lhs ) );
        presenter.updateColumn( (LimitedEntryBRLConditionColumn) this.originalCol,
                                (LimitedEntryBRLConditionColumn) this.editingCol );
    }

    @Override
    protected BRLColumn<IPattern, BRLConditionVariableColumn> cloneBRLColumn( BRLColumn<IPattern, BRLConditionVariableColumn> col ) {
        LimitedEntryBRLConditionColumn clone = new LimitedEntryBRLConditionColumn();
        clone.setHeader( col.getHeader() );
        clone.setHideColumn( col.isHideColumn() );
        clone.setDefinition( cloneDefinition( col.getDefinition() ) );
        return clone;
    }

    @Override
    protected boolean isDefined() {
        return this.ruleModel.lhs.length > 0;
    }

    private List<IPattern> cloneDefinition( List<IPattern> definition ) {
        RuleModelCloneVisitor visitor = new RuleModelCloneVisitor();
        RuleModel rm = new RuleModel();
        for ( IPattern pattern : definition ) {
            rm.addLhsItem( pattern );
        }
        RuleModel rmClone = visitor.visitRuleModel( rm );
        List<IPattern> clone = new ArrayList<IPattern>();
        for ( IPattern pattern : rmClone.lhs ) {
            clone.add( pattern );
        }
        return clone;
    }

}
