package org.kie.guvnor.guided.dtable.model.adaptors;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.kie.commons.validation.PortablePreconditions;
import org.kie.guvnor.guided.dtable.model.ConditionCol52;
import org.kie.guvnor.guided.rule.model.FieldConstraint;
import org.kie.guvnor.guided.rule.model.SingleFieldConstraint;

/**
 * Adaptor to use RuleModel class in GuidedDecisionTable
 */
@Portable
public class ConditionCol52FieldConstraintAdaptor extends SingleFieldConstraint {

    private static final long serialVersionUID = 540l;

    private ConditionCol52 condition;

    public ConditionCol52FieldConstraintAdaptor() {
    }

    public ConditionCol52FieldConstraintAdaptor( final ConditionCol52 condition ) {
        PortablePreconditions.checkNotNull( "condition",
                                            condition );
        this.condition = condition;
    }

    @Override
    public boolean isBound() {
        return condition.isBound();
    }

    @Override
    public String getFieldBinding() {
        return condition.getBinding();
    }

    @Override
    public String getFieldName() {
        return condition.getFactField();
    }

    @Override
    public String getFieldType() {
        return condition.getFieldType();
    }

    @Override
    public void setFieldBinding( final String fieldBinding ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addNewConnective() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeConnective( final int index ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFieldName( final String fieldName ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFieldType( final String fieldType ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setParent( final FieldConstraint parent ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setId( final String id ) {
        throw new UnsupportedOperationException();
    }

}