/*
 * Copyright 2011 JBoss Inc
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
package org.drools.guvnor.client.decisiontable.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;
import org.drools.guvnor.client.messages.Constants;

/**
 * Simple container for controls to manipulate a Decision Table
 */
public class DecisionTableControlsWidget extends Composite {

    private Button btnOtherwise;
    private AbstractDecisionTableWidget dtable;

    // Resources
    protected static final Constants messages = GWT.create(Constants.class);

    public DecisionTableControlsWidget() {

        // Add row button
        Button btnAddRow = new Button(messages.AddRow(),
                new ClickHandler() {

                    public void onClick(ClickEvent event) {
                        if (dtable != null) {
                            dtable.appendRow();
                        }
                    }
                });
        Panel panel = new HorizontalPanel();
        panel.add(btnAddRow);

        btnOtherwise = new Button("Otherwise",
                new ClickHandler() {

                    public void onClick(ClickEvent event) {
                        if (dtable != null) {
                            dtable.makeOtherwiseCell();
                        }
                    }
                });
        btnOtherwise.setEnabled(false);

        panel.add(btnOtherwise);

        initWidget(panel);

    }

    /**
     * Retrieve "otherwise" button
     *
     * @return
     */
    Button getOtherwiseButton() {
        return this.btnOtherwise;
    }

    /**
     * Inject DecisionTable to which these controls relate
     *
     * @param dtable
     */
    void setDecisionTableWidget(AbstractDecisionTableWidget dtable) {
        this.dtable = dtable;
    }

}
