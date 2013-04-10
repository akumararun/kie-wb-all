/*
 * Copyright 2013 JBoss Inc
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

package org.kie.guvnor.guided.scorecard.backend.server;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.drools.guvnor.models.guided.scorecard.backend.GuidedScoreCardDRLPersistence;
import org.drools.guvnor.models.guided.scorecard.shared.ScoreCardModel;
import org.kie.commons.java.nio.file.Path;
import org.kie.guvnor.commons.service.source.BaseSourceService;
import org.kie.guvnor.guided.scorecard.type.GuidedScoreCardResourceTypeDefinition;

@ApplicationScoped
public class GuidedScoreCardSourceService
        extends BaseSourceService<ScoreCardModel> {

    @Inject
    private GuidedScoreCardResourceTypeDefinition resourceType;

    @Override
    public String getPattern() {
        return resourceType.getSuffix();
    }

    @Override
    public String getSource( final Path path,
                             final ScoreCardModel model ) {
        return new StringBuilder().append( GuidedScoreCardDRLPersistence.marshal( model ) ).toString();
    }

}
