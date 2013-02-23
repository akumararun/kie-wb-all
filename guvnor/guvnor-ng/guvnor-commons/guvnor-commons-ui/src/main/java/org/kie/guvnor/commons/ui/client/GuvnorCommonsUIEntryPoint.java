/*
 * Copyright 2012 JBoss Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.kie.guvnor.commons.ui.client;

import org.jboss.errai.ioc.client.api.AfterInitialization;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.kie.guvnor.commons.ui.client.resources.CommonsResources;
import org.kie.guvnor.commons.ui.client.resources.GuvnorSimplePagerResources;
import org.kie.guvnor.commons.ui.client.resources.RoundedCornersResource;
import org.kie.guvnor.commons.ui.client.resources.WizardResources;
import org.kie.guvnor.commons.ui.client.resources.TableResources;

@EntryPoint
public class GuvnorCommonsUIEntryPoint {

    @AfterInitialization
    public void startApp() {
        RoundedCornersResource.INSTANCE.roundCornersCss().ensureInjected();
        CommonsResources.INSTANCE.css().ensureInjected();
        WizardResources.INSTANCE.css().ensureInjected();
        TableResources.INSTANCE.titledTextCellCss().ensureInjected();
    }

}