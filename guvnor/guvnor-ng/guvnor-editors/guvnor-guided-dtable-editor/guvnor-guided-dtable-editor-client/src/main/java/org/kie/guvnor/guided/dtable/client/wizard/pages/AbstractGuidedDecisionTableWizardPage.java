/*
 * Copyright 2011 JBoss Inc
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
package org.kie.guvnor.guided.dtable.client.wizard.pages;

import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import org.drools.guvnor.models.guided.dtable.shared.model.GuidedDecisionTable52;
import org.kie.guvnor.datamodel.oracle.DataModelOracle;
import org.kie.guvnor.guided.dtable.client.widget.Validator;
import org.kie.guvnor.guided.dtable.client.widget.table.DTCellValueUtilities;
import org.kie.guvnor.guided.dtable.client.wizard.NewAssetWizardContext;
import org.kie.guvnor.guided.dtable.client.wizard.NewGuidedDecisionTableAssetWizardContext;
import org.kie.guvnor.guided.dtable.model.GuidedDecisionTableUtils;
import org.uberfire.client.wizards.WizardPage;

/**
 * Base page for the guided Decision Table Wizard
 */
public abstract class AbstractGuidedDecisionTableWizardPage
        implements
        WizardPage {

    protected static final String NEW_FACT_PREFIX = "f";

    protected final SimplePanel content = new SimplePanel();

    protected NewAssetWizardContext context;
    protected GuidedDecisionTable52 model;
    protected Validator validator;

    protected GuidedDecisionTableUtils modelUtils;
    protected DTCellValueUtilities cellUtils;
    protected DataModelOracle oracle;

    @Override
    public Widget asWidget() {
        return content;
    }

    public void setContent( final NewGuidedDecisionTableAssetWizardContext context,
                            final DataModelOracle oracle,
                            final GuidedDecisionTable52 model,
                            final Validator validator ) {
        this.context = context;
        this.oracle = oracle;
        this.model = model;
        this.validator = validator;
        this.cellUtils = new DTCellValueUtilities( this.model,
                                                   this.oracle );
        this.modelUtils = new GuidedDecisionTableUtils( this.oracle,
                                                        this.model );
    }

    public Validator getValidator() {
        return this.validator;
    }

    /**
     * When the Widget is finished a GuidedDecisionTable52 instance is passed to
     * each page for enrichment. Some pages are able to work on this instance
     * directly (i.e. the model is suitable for direct use in the page, such as
     * FactPatternsPage) however others maintain their own representation of the
     * model that must be copied into the GuidedDecisionTable52.
     * @param model
     */
    public void makeResult( final GuidedDecisionTable52 model ) {
        //Default implementation does nothing
    }

    /**
     * Check whether empty values are permitted
     * @return True if empty values are permitted
     */
    protected boolean allowEmptyValues() {
        return this.model.getTableFormat() == GuidedDecisionTable52.TableFormat.EXTENDED_ENTRY;
    }

}
