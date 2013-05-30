/**
 * Copyright (C) 2012 JBoss Inc
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
package org.jbpm.formModeler.editor.client.handlers;


import com.google.gwt.user.client.ui.IsWidget;
import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.api.Caller;
import org.jbpm.formModeler.editor.client.type.FormDefinitionResourceType;
import org.jbpm.formModeler.editor.service.FormModelerService;
import org.kie.workbench.common.widgets.client.handlers.DefaultNewResourceHandler;
import org.kie.workbench.common.widgets.client.handlers.NewResourcePresenter;
import org.uberfire.backend.vfs.Path;
import org.uberfire.client.common.BusyPopup;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.impl.PathPlaceRequest;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class NewFormDefinitionlHandler extends DefaultNewResourceHandler {

    @Inject
    private Caller<FormModelerService> modelerService;

    @Inject
    private PlaceManager placeManager;

    @Inject
    private FormDefinitionResourceType resourceType;

    @Override
    public String getDescription() {
        return "New Form";
    }

    @Override
    public IsWidget getIcon() {
        return null;
    }

    @Override
    public void create(final Path context,
                       final String baseFileName,
                       final NewResourcePresenter presenter) {

        BusyPopup.showMessage("Creating New Form");
        modelerService.call( new RemoteCallback<Path>() {
            @Override
            public void callback( final Path path ) {
                BusyPopup.close();
                presenter.complete();
                notifySuccess();
                PlaceRequest place = new PathPlaceRequest(path, "FormModelerEditor");
                placeManager.goTo(place);
            }
        } ).createForm(context, buildFileName(resourceType, baseFileName));
    }
}
