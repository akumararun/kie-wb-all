/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
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

package org.guvnor.client.perspectives;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.guvnor.inbox.client.InboxPresenter;
import org.uberfire.client.annotations.Perspective;
import org.uberfire.client.annotations.WorkbenchPerspective;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.workbench.panels.impl.MultiListWorkbenchPanelPresenter;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.workbench.model.PerspectiveDefinition;
import org.uberfire.workbench.model.impl.PartDefinitionImpl;
import org.uberfire.workbench.model.impl.PerspectiveDefinitionImpl;

/**
 * A Perspective to show Inbox messages
 */
@ApplicationScoped
@WorkbenchPerspective(identifier = "InboxPerspective")
public class InboxPerspective {

    @Inject
    private PlaceManager placeManager;

    @Inject
    private ProjectMenu projectMenu;

    @Perspective
    public PerspectiveDefinition getPerspective() {
        final PlaceRequest recentEdit = new DefaultPlaceRequest( "Inbox" );
        recentEdit.addParameter( "inboxname", InboxPresenter.RECENT_EDITED_ID );

        final PlaceRequest recentView = new DefaultPlaceRequest( "Inbox" );
        recentView.addParameter( "inboxname", InboxPresenter.RECENT_VIEWED_ID );

        final PerspectiveDefinition perspective = new PerspectiveDefinitionImpl(MultiListWorkbenchPanelPresenter.class.getName());
        perspective.getRoot().getParts().add( new PartDefinitionImpl( new DefaultPlaceRequest( "Inbox" ) ) );
        perspective.getRoot().getParts().add( new PartDefinitionImpl( recentEdit ) );
        perspective.getRoot().getParts().add( new PartDefinitionImpl( recentView ) );

        return perspective;
    }

}
