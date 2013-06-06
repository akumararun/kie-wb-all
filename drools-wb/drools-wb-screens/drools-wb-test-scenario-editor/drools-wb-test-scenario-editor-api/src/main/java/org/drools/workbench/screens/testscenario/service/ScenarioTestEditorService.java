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

package org.drools.workbench.screens.testscenario.service;

import org.drools.workbench.models.testscenarios.shared.Scenario;
import org.drools.workbench.screens.testscenario.model.TestScenarioModelContent;
import org.jboss.errai.bus.server.annotations.Remote;
import org.kie.workbench.services.shared.file.SupportsCopy;
import org.kie.workbench.services.shared.file.SupportsCreate;
import org.kie.workbench.services.shared.file.SupportsDelete;
import org.kie.workbench.services.shared.file.SupportsRead;
import org.kie.workbench.services.shared.file.SupportsRename;
import org.kie.workbench.services.shared.file.SupportsUpdate;
import org.uberfire.backend.vfs.Path;

/**
 * Service definition for Globals editor
 */
@Remote
public interface ScenarioTestEditorService
        extends
        SupportsCreate<Scenario>,
        SupportsRead<Scenario>,
        SupportsUpdate<Scenario>,
        SupportsDelete,
        SupportsCopy,
        SupportsRename {

    TestScenarioModelContent loadContent(Path path);

    void runScenario(Path path, Scenario scenario, String sessoinName);
    void runAllScenarios(Path path, String sessoinName);
}
