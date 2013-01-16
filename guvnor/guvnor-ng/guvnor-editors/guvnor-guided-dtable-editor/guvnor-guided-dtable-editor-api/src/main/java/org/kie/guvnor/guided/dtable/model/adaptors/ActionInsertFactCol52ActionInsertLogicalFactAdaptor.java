package org.kie.guvnor.guided.dtable.model.adaptors;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.kie.commons.validation.PortablePreconditions;
import org.kie.guvnor.guided.dtable.model.ActionInsertFactCol52;
import org.kie.guvnor.guided.rule.model.ActionFieldValue;
import org.kie.guvnor.guided.rule.model.ActionInsertLogicalFact;
import org.kie.guvnor.guided.rule.model.BaseSingleFieldConstraint;

/**
 * Adaptor to use RuleModel class in GuidedDecisionTable
 */
@Portable
public class ActionInsertFactCol52ActionInsertLogicalFactAdaptor extends ActionInsertLogicalFact {

    private static final long serialVersionUID = 540l;

    private ActionInsertFactCol52 action;

    public ActionInsertFactCol52ActionInsertLogicalFactAdaptor() {
    }

    public ActionInsertFactCol52ActionInsertLogicalFactAdaptor( final ActionInsertFactCol52 action ) {
        PortablePreconditions.checkNotNull( "action",
                                            action );
        this.action = action;
        this.setFactType( action.getFactType() );
        final ActionFieldValue afv = new ActionFieldValue();
        afv.setField( action.getFactField() );
        afv.setNature( BaseSingleFieldConstraint.TYPE_LITERAL );
        afv.setType( action.getType() );
        super.addFieldValue( afv );
    }

    @Override
    public boolean isBound() {
        return !( action.getBoundName() == null || "".equals( action.getBoundName() ) );
    }

    @Override
    public String getBoundName() {
        return action.getBoundName();
    }

    @Override
    public void setBoundName( final String boundName ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeField( final int idx ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addFieldValue( final ActionFieldValue val ) {
        throw new UnsupportedOperationException();
    }

}