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

package org.kie.guvnor.globals.model;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.kie.commons.validation.PortablePreconditions;
import org.kie.guvnor.datamodel.oracle.DataModelOracle;

@Portable
public class GlobalsEditorContent {

    private GlobalsModel model;
    private DataModelOracle oracle;

    public GlobalsEditorContent() {
    }

    public GlobalsEditorContent( final GlobalsModel model,
                                 final DataModelOracle oracle ) {
        this.model = PortablePreconditions.checkNotNull( "model",
                                                         model );
        this.oracle = PortablePreconditions.checkNotNull( "oracle",
                                                          oracle );
    }

    public GlobalsModel getModel() {
        return this.model;
    }

    public DataModelOracle getDataModel() {
        return this.oracle;
    }

}
