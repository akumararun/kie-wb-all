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

package org.kie.workbench.common.screens.projecteditor.client.forms;

import com.github.gwtbootstrap.client.ui.ListBox;
import com.github.gwtbootstrap.client.ui.PageHeader;
import com.github.gwtbootstrap.client.ui.RadioButton;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import org.guvnor.common.services.project.model.KSessionModel;
import org.kie.workbench.common.screens.projecteditor.client.resources.i18n.ProjectEditorConstants;

import javax.enterprise.inject.New;
import javax.inject.Inject;
import java.util.Map;

public class KBaseFormViewImpl
        extends Composite
        implements KBaseFormView {

    private Presenter presenter;

    interface KnowledgeBaseConfigurationFormViewImplBinder
            extends
            UiBinder<Widget, KBaseFormViewImpl> {

    }

    private static KnowledgeBaseConfigurationFormViewImplBinder uiBinder = GWT.create(KnowledgeBaseConfigurationFormViewImplBinder.class);

    @UiField(provided = true)
    CRUDListBox packagesListBox;

    @UiField
    PageHeader nameLabel;

    @UiField
    RadioButton equalsBehaviorIdentity;

    @UiField
    RadioButton equalsBehaviorEquality;

    @UiField
    RadioButton eventProcessingModeStream;

    @UiField
    RadioButton eventProcessingModeCloud;

    @UiField(provided = true)
    KSessionsPanel statefulSessionsPanel;

    @UiField(provided = true)
    KSessionsPanel statelessSessionsPanel;

    @Inject
    public KBaseFormViewImpl(@New KSessionsPanel statefulSessionsPanel,
                             @New KSessionsPanel statelessSessionsPanel,
                             CRUDListBox packagesListBox) {
        this.statefulSessionsPanel = statefulSessionsPanel;
        this.statelessSessionsPanel = statelessSessionsPanel;
        this.packagesListBox = packagesListBox;

        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setName(String name) {
        nameLabel.setText(name);
    }

    @Override
    public String getSelectedPackageName() {
        return null;
//                packagesListBox.getValue(packagesListBox.getSelectedIndex());
    }

    @Override
    public void removePackageName(String selectedPackageName) {
//        for (int i = 0; i < packagesListBox.getItemCount(); i++) {
//            if (packagesListBox.getValue(i).equals(selectedPackageName)) {
//                packagesListBox.removeItem(i);
//            }
//        }
    }

    @Override
    public void setDefault(boolean aDefault) {
        if (aDefault) {
            nameLabel.setSubtext(ProjectEditorConstants.INSTANCE.BracketDefaultBracket());
        } else {
            nameLabel.setSubtext("");
        }
    }

    @Override
    public void setEqualsBehaviorEquality() {
        equalsBehaviorEquality.setValue(true);
    }

    @Override
    public void setEqualsBehaviorIdentity() {
        equalsBehaviorIdentity.setValue(true);
    }

    @Override
    public void setEventProcessingModeStream() {
        eventProcessingModeStream.setValue(true);
    }

    @Override
    public void setEventProcessingModeCloud() {
        eventProcessingModeCloud.setValue(true);
    }

    @Override
    public void setStatefulSessions(Map<String, KSessionModel> statefulSessions) {
        statefulSessionsPanel.setItems(statefulSessions);
    }

    @Override
    public void setStatelessSessions(Map<String, KSessionModel> statefulSessions) {
        statelessSessionsPanel.setItems(statefulSessions);
    }

    @Override
    public void setReadOnly() {
        equalsBehaviorIdentity.setEnabled(false);
        equalsBehaviorEquality.setEnabled(false);
        eventProcessingModeStream.setEnabled(false);
        eventProcessingModeCloud.setEnabled(false);
        statefulSessionsPanel.makeReadOnly();
        statelessSessionsPanel.makeReadOnly();
    }

    @Override
    public void addPackageName(String name) {
//        packagesListBox.addItem(name);
    }

    @UiHandler("addButton")
    public void onAdd(ClickEvent clickEvent) {
        presenter.onAddPackage();
    }

    @UiHandler("deleteButton")
    public void onDelete(ClickEvent clickEvent) {
        presenter.onDeletePackage();
    }

    @UiHandler("equalsBehaviorIdentity")
    public void onEqualsBehaviorIdentityChange(ValueChangeEvent<Boolean> valueChangeEvent) {
        if (equalsBehaviorIdentity.getValue()) {
            presenter.onEqualsBehaviorIdentitySelect();
        }
    }

    @UiHandler("equalsBehaviorEquality")
    public void onEqualsBehaviorEqualityChange(ValueChangeEvent<Boolean> valueChangeEvent) {
        if (equalsBehaviorEquality.getValue()) {
            presenter.onEqualsBehaviorEqualitySelect();
        }
    }

    @UiHandler("eventProcessingModeStream")
    public void onEventProcessingModeStreamChange(ValueChangeEvent<Boolean> valueChangeEvent) {
        if (eventProcessingModeStream.getValue()) {
            presenter.onEventProcessingModeStreamSelect();
        }
    }

    @UiHandler("eventProcessingModeCloud")
    public void onEventProcessingModeCloudChange(ValueChangeEvent<Boolean> valueChangeEvent) {
        if (eventProcessingModeCloud.getValue()) {
            presenter.onEventProcessingModeCloudSelect();
        }
    }
}
