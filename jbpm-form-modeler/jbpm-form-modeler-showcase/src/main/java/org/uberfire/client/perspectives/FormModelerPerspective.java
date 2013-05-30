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
package org.uberfire.client.perspectives;

import org.uberfire.client.annotations.Perspective;
import org.uberfire.client.annotations.WorkbenchPerspective;
import org.uberfire.workbench.model.PanelDefinition;
import org.uberfire.workbench.model.PerspectiveDefinition;
import org.uberfire.workbench.model.Position;
import org.uberfire.workbench.model.impl.PanelDefinitionImpl;
import org.uberfire.workbench.model.impl.PartDefinitionImpl;
import org.uberfire.workbench.model.impl.PerspectiveDefinitionImpl;
import org.uberfire.mvp.impl.DefaultPlaceRequest;

import javax.enterprise.context.ApplicationScoped;

/**
 * A Perspective to show Form Modeler
 */
@ApplicationScoped
@WorkbenchPerspective(identifier = "FormModelerPerspective")
public class FormModelerPerspective {
    @Perspective
    public PerspectiveDefinition buildPerspective() {
        final PerspectiveDefinition p = new PerspectiveDefinitionImpl();
        p.setName("Form Modeler");

        final PanelDefinition north = new PanelDefinitionImpl();
        north.addPart(new PartDefinitionImpl(new DefaultPlaceRequest("FormModelerScreen")));
        north.setWidth(250);
        north.setMinWidth(200);
        p.getRoot().insertChild(Position.NORTH, north);

        final PanelDefinition south = new PanelDefinitionImpl();
        south.addPart(new PartDefinitionImpl(new DefaultPlaceRequest("FormModelerPanel")));
        p.getRoot().insertChild(Position.SOUTH, south);

        return p;
    }
}
