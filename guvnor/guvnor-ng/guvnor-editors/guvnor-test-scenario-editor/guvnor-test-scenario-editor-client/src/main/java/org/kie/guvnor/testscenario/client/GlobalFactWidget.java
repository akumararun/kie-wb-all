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

package org.kie.guvnor.testscenario.client;

import org.drools.guvnor.models.testscenarios.shared.ExecutionTrace;
import org.drools.guvnor.models.testscenarios.shared.Fixture;
import org.drools.guvnor.models.testscenarios.shared.FixtureList;
import org.kie.guvnor.commons.ui.client.popups.errors.ErrorPopup;
import org.kie.guvnor.datamodel.oracle.DataModelOracle;
import org.kie.guvnor.testscenario.client.resources.i18n.TestScenarioConstants;
import org.drools.guvnor.models.testscenarios.shared.FactData;
import org.drools.guvnor.models.testscenarios.shared.Scenario;


public class GlobalFactWidget extends FactWidget {

    public GlobalFactWidget(String factType,
                            FixtureList definitionList,
                            Scenario sc,
                            DataModelOracle dmo,
                            ScenarioParentWidget parent,
                            ExecutionTrace executionTrace) {
        super(factType,
                definitionList,
                sc,
                dmo,
                parent,
                executionTrace,
                TestScenarioConstants.INSTANCE.globalForScenario(factType));
    }

    public void onDelete() {
        boolean used = false;

        for (Fixture fixture : definitionList) {
            if (fixture instanceof FactData) {
                final FactData factData = (FactData) fixture;
                if (scenario.isFactDataReferenced(factData)) {
                    used = true;
                    break;
                }
            }
        }

        if (used) {
            ErrorPopup.showMessage(TestScenarioConstants.INSTANCE.CantRemoveThisBlockAsOneOfTheNamesIsBeingUsed());
        } else {
            super.onDelete();
        }
    }

}
