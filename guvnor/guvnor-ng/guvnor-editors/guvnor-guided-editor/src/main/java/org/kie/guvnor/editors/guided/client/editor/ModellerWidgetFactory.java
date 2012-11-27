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

package org.kie.guvnor.editors.guided.client.editor;

import com.google.web.bindery.event.shared.EventBus;
import org.kie.guvnor.datamodel.api.shared.IAction;
import org.kie.guvnor.datamodel.api.shared.IPattern;
import org.kie.guvnor.editors.guided.client.widget.RuleModellerWidget;

public interface ModellerWidgetFactory {

    /**
     * Used for get widgets for RHS
     * @param ruleModeller
     * @param eventBus
     * @param action
     * @param readOnly
     * @return
     */
    public RuleModellerWidget getWidget( RuleModeller ruleModeller,
                                         EventBus eventBus,
                                         IAction action,
                                         Boolean readOnly );

    /**
     * Used for get widgets for LHS
     * @param ruleModeller
     * @param eventBus
     * @param pattern
     * @param readOnly
     * @return
     */
    public RuleModellerWidget getWidget( RuleModeller ruleModeller,
                                         EventBus eventBus,
                                         IPattern pattern,
                                         Boolean readOnly );

    public boolean isTemplate();

}
