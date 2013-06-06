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

package org.kie.guvnor.guided.dtable.backend.server;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.drools.guvnor.models.guided.dtable.backend.GuidedDTDRLPersistence;
import org.drools.guvnor.models.guided.dtable.shared.model.GuidedDecisionTable52;
import org.kie.commons.java.nio.file.Path;
import org.kie.guvnor.commons.service.backend.BaseSourceService;
import org.kie.guvnor.guided.dtable.type.GuidedDTableResourceTypeDefinition;

@ApplicationScoped
public class GuidedDecisionTableSourceService
        extends BaseSourceService<GuidedDecisionTable52> {

    @Inject
    private GuidedDTableResourceTypeDefinition resourceType;

    @Override
    public String getPattern() {
        return resourceType.getSuffix();
    }

    @Override
    public String getSource( final Path path,
                             final GuidedDecisionTable52 model ) {
        return new StringBuilder().append( GuidedDTDRLPersistence.getInstance().marshal( model ) ).toString();
    }

}
