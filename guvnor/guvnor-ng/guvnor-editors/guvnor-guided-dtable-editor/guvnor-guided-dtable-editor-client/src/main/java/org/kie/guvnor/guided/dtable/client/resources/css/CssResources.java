package org.kie.guvnor.guided.dtable.client.resources.css;

import org.kie.guvnor.decoratedgrid.client.resources.GridResources;

/**
 * General Decision Table CSS
 */
public interface CssResources
        extends
        GridResources.GridStyle {

    String metaColumn();

    String conditionColumn();

    String actionColumn();

    String patternSectionHeader();

    String patternConditionSectionHeader();

    String columnLabelHidden();

}

