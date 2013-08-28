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

package org.kie.workbench.common.screens.projecteditor.client.forms;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.Container;
import com.github.gwtbootstrap.client.ui.ListBox;
import com.github.gwtbootstrap.client.ui.base.Style;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import org.kie.workbench.common.screens.projecteditor.client.resources.ProjectEditorResources;
import org.kie.workbench.common.screens.projecteditor.client.resources.i18n.ProjectEditorConstants;
import org.uberfire.client.common.Popup;

public class KSessionModelOptionsPopUpViewImpl
        extends Popup
        implements KSessionModelOptionsPopUpView {

    private static final Style PANEL_ENABLED = new Style() {
        @Override
        public String get() {
            return ProjectEditorResources.INSTANCE.mainCss().panelEnabled();
        }
    };

    private static final Style PANEL_DISABLED = new Style() {
        @Override
        public String get() {
            return ProjectEditorResources.INSTANCE.mainCss().panelDisabled();
        }
    };

    private final Widget content;
    private final String CONSOLE_LOGGER = ProjectEditorConstants.INSTANCE.ConsoleLogger();
    private final String FILE_LOGGER = ProjectEditorConstants.INSTANCE.FileLogger();
    private Presenter presenter;

    interface Binder
            extends
            UiBinder<Widget, KSessionModelOptionsPopUpViewImpl> {

    }

    private static Binder uiBinder = GWT.create(Binder.class);

    @UiField
    CheckBox loggerCheckBox;

    @UiField
    ListBox loggerTypeListBox;

    @UiField
    Container loggerEditorPanel;

    @UiField
    Container loggerContainer;

    public KSessionModelOptionsPopUpViewImpl() {
        content = uiBinder.createAndBindUi(this);
        loggerTypeListBox.addItem(CONSOLE_LOGGER);
        loggerTypeListBox.addItem(FILE_LOGGER);
    }

    @Override
    public Widget getContent() {
        return content;
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setLoggerEditor(LoggerEditorPanel loggerEditor) {
        clearLoggerEditor();
        loggerEditorPanel.add(loggerEditor);
    }

    @Override
    public void clearLoggerEditor() {
        loggerEditorPanel.clear();
    }

    @Override
    public void enableLoggerPanel() {
        loggerCheckBox.setValue(true);
        loggerTypeListBox.setEnabled(true);
        loggerContainer.setStyle(PANEL_ENABLED);
    }

    @Override
    public void disableLoggerPanel() {
        loggerCheckBox.setValue(false);
        loggerTypeListBox.setEnabled(false);
        loggerContainer.setStyle(PANEL_DISABLED);
    }

    @UiHandler("loggerCheckBox")
    public void onLoggerPanelToggle(ValueChangeEvent<Boolean> event) {
        presenter.onToggleLoggerPanel(event.getValue());
    }

    @UiHandler("loggerTypeListBox")
    public void onLoggerTypeSelected(ChangeEvent event) {

        if (loggerTypeListBox.getValue().equals(FILE_LOGGER)) {
            presenter.onFileLoggerSelected();
        } else if (loggerTypeListBox.getValue().equals(CONSOLE_LOGGER)) {
            presenter.onConsoleLoggerSelected();
        }
    }
}
