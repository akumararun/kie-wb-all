/*
 * Copyright 2010 JBoss Inc
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

package org.kie.guvnor.guided.template.server.util;

import org.drools.guvnor.models.commons.backend.rule.BRLPersistence;
import org.drools.guvnor.models.commons.shared.rule.RuleModel;
import org.kie.guvnor.guided.rule.backend.server.util.BRXMLPersistence;
import org.kie.guvnor.guided.template.model.TemplateModel;
import org.kie.guvnor.guided.template.server.util.upgrade.TemplateModelUpgradeHelper1;

import java.util.List;

/**
 * This class persists the template rule model to XML and back. This is the
 * 'brl' xml format (Business Rule Language).
 */
public class BRDRTXMLPersistence extends BRXMLPersistence {

    private static final BRLPersistence INSTANCE = new BRDRTXMLPersistence();

    private static final TemplateModelUpgradeHelper1 upgrader1 = new TemplateModelUpgradeHelper1();

    private BRDRTXMLPersistence() {
        super();
        this.xt.alias( "org.drools.guvnor.client.modeldriven.dt.TemplateModel",
                       TemplateModel.class );
    }

    public static BRLPersistence getInstance() {
        return INSTANCE;
    }

    @Override
    public String marshal( final RuleModel model ) {
        ( (TemplateModel) model ).putInSync();
        return super.marshal( model );
    }

    @Override
    public TemplateModel unmarshal( final String xml ) {
        TemplateModel model = (TemplateModel) super.unmarshal( xml );
        model.putInSync();

        //Upgrade model changes to legacy artifacts
        upgrader1.upgrade( model );

        return model;
    }

    @Override
    public RuleModel unmarshalUsingDSL( final String str, final List<String> globals, final String... dsls ) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected RuleModel createEmptyModel() {
        return new TemplateModel();
    }
}
