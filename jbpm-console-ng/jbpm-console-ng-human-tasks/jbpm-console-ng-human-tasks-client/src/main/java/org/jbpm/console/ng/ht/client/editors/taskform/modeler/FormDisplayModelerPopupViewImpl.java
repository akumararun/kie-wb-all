/*
 * Copyright 2011 JBoss Inc
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

package org.jbpm.console.ng.ht.client.editors.taskform.modeler;

import com.github.gwtbootstrap.client.ui.Label;
import com.github.gwtbootstrap.client.ui.base.UnorderedList;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jbpm.formModeler.api.client.FormRenderContextTO;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.jbpm.formModeler.renderer.client.FormRendererWidget;

import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.workbench.events.NotificationEvent;

/**
 * Main view.
 */
@Dependent
@Templated(value = "FormDisplayModelerPopupViewImpl.html")
public class FormDisplayModelerPopupViewImpl extends Composite implements FormDisplayModelerPopupPresenter.FormDisplayModelerView {

    private FormDisplayModelerPopupPresenter presenter;

    
    @Inject
    @DataField
    public FormRendererWidget formRenderer;

    @Inject
    @DataField
    public Label nameText;

    @Inject
    @DataField
    public Label taskIdText;

    @Inject
    @DataField
    public FlowPanel optionsDiv;

    @Inject
    @DataField
    public UnorderedList navBarUL;

    private long taskId;
    private String domainId;
    private String processId;
    private String action;

    @Inject
    private PlaceManager placeManager;

    @Inject
    private Event<NotificationEvent> notification;

    @Override
    public void init(FormDisplayModelerPopupPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void displayNotification(String text) {
        notification.fire(new NotificationEvent(text));
    }

    @Override
    public long getTaskId() {
        return taskId;
    }

    @Override
    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    @Override
    public String getProcessId() {
        return processId;
    }

    @Override
    public void setProcessId(String processId) {
        this.processId = processId;
    }

    @Override
    public Label getNameText() {
        return nameText;
    }

    @Override
    public Label getTaskIdText() {
        return taskIdText;
    }

    @Override
    public FlowPanel getOptionsDiv() {
        return optionsDiv;
    }

    @Override
    public UnorderedList getNavBarUL() {
        return navBarUL;
    }

    @Override
    public String getDomainId() {
        return domainId;
    }

    @Override
    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }
    
    @Override
    public void loadContext(FormRenderContextTO ctx) {
        if (ctx != null) {
            formRenderer.loadContext(ctx);
        }
    }

    @Override
    public void loadContext(String ctxUID) {
        if (ctxUID != null) {
            formRenderer.loadContext(ctxUID);
        }
    }

    @Override
    public void submitStartProcessForm() {
        submitForm(FormDisplayModelerPopupPresenter.ACTION_START_PROCESS);
    }

    @Override
    public void submitChangeTab(String tab) {
        submitForm(tab);
    }

    @Override
    public void submitSaveTaskStateForm() {
        submitForm(FormDisplayModelerPopupPresenter.ACTION_SAVE_TASK);
    }

    @Override
    public void submitCompleteTaskForm() {
        submitForm(FormDisplayModelerPopupPresenter.ACTION_COMPLETE_TASK);
    }

    @Override
    public void submitForm() {
        formRenderer.submitForm();
    }

    protected void submitForm(String action) {
        this.action = action;
        formRenderer.submitFormAndPersist();
    }

    @Override
    public void setAction(String action) {
        this.action = action;
    }

    @Override
    public String getAction() {
        return action;
    }
}
