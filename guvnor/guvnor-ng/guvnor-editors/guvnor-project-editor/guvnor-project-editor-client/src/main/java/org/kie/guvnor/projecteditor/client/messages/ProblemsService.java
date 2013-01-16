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

package org.kie.guvnor.projecteditor.client.messages;

import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import org.kie.guvnor.commons.service.builder.model.Message;
import org.kie.guvnor.commons.service.builder.model.Results;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.workbench.widgets.events.NotificationEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.List;

/**
 * Service for Message Console, the Console is a screen that shows compile time errors.
 * This listens to Messages and if the Console is not open it opens it.
 */
@ApplicationScoped
public class ProblemsService {

    private final PlaceManager placeManager;

    private ListDataProvider<Message> dataProvider = new ListDataProvider<Message>();
    private final Event<NotificationEvent> notificationEvent;
    private final ProblemsServiceView view;

    @Inject
    public ProblemsService(ProblemsServiceView view,
                           PlaceManager placeManager,
                           Event<NotificationEvent> notificationEvent) {
        this.view = view;
        this.placeManager = placeManager;
        this.notificationEvent = notificationEvent;
    }

    public void addMessages(@Observes Results results) {
        if (results.isEmpty()) {
            notificationEvent.fire(new NotificationEvent(view.showBuildSuccessful()));
        }

        List<Message> list = dataProvider.getList();
        list.clear();
        for (Message message : results) {
            list.add(message);
        }

        placeManager.goTo("org.kie.guvnor.Problems");
    }

    public void addDataDisplay( HasData<Message> display ) {
        dataProvider.addDataDisplay( display );
    }
}
