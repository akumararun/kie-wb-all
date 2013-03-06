package org.kie.guvnor.guided.template.client.editor;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.IsWidget;
import org.drools.guvnor.models.guided.template.shared.TemplateModel;
import org.kie.guvnor.datamodel.oracle.DataModelOracle;

/**
 * Guided Rule Template Data View definition
 */
public interface GuidedRuleTemplateDataView extends IsWidget {

    void setContent( final TemplateModel model,
                     final DataModelOracle dataModel,
                     final EventBus eventBus,
                     final boolean isReadOnly );

}
