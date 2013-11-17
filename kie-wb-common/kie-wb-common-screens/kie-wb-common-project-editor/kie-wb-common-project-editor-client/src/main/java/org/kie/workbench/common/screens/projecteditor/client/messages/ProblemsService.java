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

package org.kie.workbench.common.screens.projecteditor.client.messages;

import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import org.guvnor.common.services.project.builder.model.BuildMessage;
import org.guvnor.common.services.project.builder.model.BuildResults;
import org.guvnor.common.services.project.builder.model.IncrementalBuildResults;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.workbench.PanelManager;

/**
 * Service for Message Console, the Console is a screen that shows compile time errors.
 * This listens to Messages and if the Console is not open it opens it.
 */
@ApplicationScoped
public class ProblemsService {

    private static final String HOME_PERSPECTIVE = "Home";
    private static final String ARTIFACT_PERSPECTIVE = "org.guvnor.m2repo.client.perspectives.GuvnorM2RepoPerspective";

    private final PlaceManager placeManager;
    private final PanelManager panelManager;
    private final ListDataProvider<BuildMessage> dataProvider = new ListDataProvider<BuildMessage>();

    @Inject
    public ProblemsService( final PlaceManager placeManager,
                            final PanelManager panelManager ) {
        this.placeManager = placeManager;
        this.panelManager = panelManager;
    }

    public void addBuildMessages( final @Observes BuildResults results ) {
        List<BuildMessage> list = dataProvider.getList();
        list.clear();
        for ( BuildMessage buildMessage : results.getMessages() ) {
            list.add( buildMessage );
        }
        if ( !panelManager.getPerspective().getName().equals( HOME_PERSPECTIVE ) ) {
            placeManager.goTo( "org.kie.guvnor.Problems" );
        }
    }

    public void addIncrementalBuildMessages( final @Observes IncrementalBuildResults results ) {
        final List<BuildMessage> addedMessages = results.getAddedMessages();
        final List<BuildMessage> removedMessages = results.getRemovedMessages();

        List<BuildMessage> list = dataProvider.getList();
        for ( BuildMessage buildMessage : removedMessages ) {
            list.remove( buildMessage );
        }
        for ( BuildMessage buildMessage : addedMessages ) {
            list.add( buildMessage );
        }
        if ( !panelManager.getPerspective().getName().equals( HOME_PERSPECTIVE ) &&
                !panelManager.getPerspective().getName().equals( ARTIFACT_PERSPECTIVE ) ) {
            placeManager.goTo( "org.kie.guvnor.Problems" );
        }
    }

    public void addDataDisplay( HasData<BuildMessage> display ) {
        dataProvider.addDataDisplay( display );
    }
}
