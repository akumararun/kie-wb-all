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

import javax.inject.Inject;

import org.guvnor.common.services.project.model.ConsoleLogger;
import org.guvnor.common.services.project.model.FileLogger;
import org.guvnor.common.services.project.model.KSessionModel;
import org.guvnor.common.services.project.model.ListenerModel;

public class KSessionModelOptionsPopUp
        implements KSessionModelOptionsPopUpView.Presenter {

    private final KSessionModelOptionsPopUpView view;
    private final ConsoleLoggerEditor consoleLoggerEditor;
    private final FileLoggerEditor fileLoggerEditor;
    private KSessionModel model;

    @Inject
    public KSessionModelOptionsPopUp(
            KSessionModelOptionsPopUpView view,
            ConsoleLoggerEditor consoleLoggerEditor,
            FileLoggerEditor fileLoggerEditor) {
        this.view = view;
        this.consoleLoggerEditor = consoleLoggerEditor;
        this.fileLoggerEditor = fileLoggerEditor;
        view.setPresenter(this);
    }

    public void show(KSessionModel kSessionModel) {
        this.model = kSessionModel;

//        setUpLoggerPanel();
        setUpListenerPanel();

        view.show();
    }

    private void setUpListenerPanel() {
        if (model.getListenerModel() != null) {
            view.enableListenerPanel();
            if (model.getListenerModel().getKind().equals(ListenerModel.Kind.WORKING_MEMORY_EVENT_LISTENER)) {
                view.selectWorkingMemoryEventListener();
            } else if (model.getListenerModel().getKind().equals(ListenerModel.Kind.AGENDA_EVENT_LISTENER)) {
                view.selectAgendaEventListener();
            } else if (model.getListenerModel().getKind().equals(ListenerModel.Kind.PROCESS_EVENT_LISTENER)) {
                view.selectProcessEventListener();
            }
            view.setListenerTypeName(model.getListenerModel().getType());
        }
    }

    private void setUpLoggerPanel() {
        if (model.getLogger() != null) {
            view.enableLoggerPanel();
            if (model.getLogger() instanceof ConsoleLogger) {
                consoleLoggerEditor.setModel((ConsoleLogger) model.getLogger());
                view.setLoggerEditor(consoleLoggerEditor);
            } else if (model.getLogger() instanceof FileLogger) {
                fileLoggerEditor.setModel((FileLogger) model.getLogger());
                view.setLoggerEditor(fileLoggerEditor);
            }
        }
    }

    @Override
    public void onToggleLoggerPanel(Boolean value) {
        if (value) {
            view.enableLoggerPanel();
            onConsoleLoggerSelected();
        } else {
            view.disableLoggerPanel();
            view.clearLoggerEditor();
            model.setLogger(null);
        }
    }

    @Override
    public void onConsoleLoggerSelected() {
        ConsoleLogger consoleLogger = new ConsoleLogger();
        model.setLogger(consoleLogger);
        consoleLoggerEditor.setModel(consoleLogger);
        view.setLoggerEditor(consoleLoggerEditor);
    }

    @Override
    public void onFileLoggerSelected() {
        view.clearLoggerEditor();
        view.setLoggerEditor(fileLoggerEditor);
    }

    @Override
    public void onToggleListenerPanel(Boolean value) {
        if (value) {
            model.setListenerModel(new ListenerModel());
            view.enableListenerPanel();
        } else {
            model.setListenerModel(null);
            view.disableListenerPanel();
        }
    }

    @Override
    public void onWorkingMemoryEventListenerSelected() {
        model.getListenerModel().setKind(ListenerModel.Kind.WORKING_MEMORY_EVENT_LISTENER);
    }

    @Override
    public void onAgendaEventListenerSelected() {
        model.getListenerModel().setKind(ListenerModel.Kind.AGENDA_EVENT_LISTENER);
    }

    @Override
    public void onProcessEventListenerSelected() {
        model.getListenerModel().setKind(ListenerModel.Kind.PROCESS_EVENT_LISTENER);
    }

    @Override
    public void onListenerNameChange(String value) {
        model.getListenerModel().setType(value);
    }
}
