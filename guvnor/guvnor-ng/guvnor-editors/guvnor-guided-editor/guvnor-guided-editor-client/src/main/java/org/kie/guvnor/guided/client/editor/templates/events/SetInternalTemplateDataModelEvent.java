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
package org.kie.guvnor.guided.client.editor.templates.events;

import java.util.List;

import com.google.gwt.event.shared.GwtEvent;
import org.kie.guvnor.decoratedgrid.client.widget.DynamicColumn;
import org.kie.guvnor.decoratedgrid.client.widget.data.DynamicData;
import org.kie.guvnor.decoratedgrid.client.widget.events.SetInternalModelEvent;
import org.kie.guvnor.guided.client.editor.templates.TemplateDataColumn;
import org.kie.guvnor.guided.model.templates.TemplateModel;

/**
 * An event to set the internal model for the Template Data grid
 */
public class SetInternalTemplateDataModelEvent
        extends SetInternalModelEvent<TemplateModel, TemplateDataColumn> {

    public static GwtEvent.Type<SetInternalModelEvent.Handler<TemplateModel, TemplateDataColumn>> TYPE = new GwtEvent.Type<SetInternalModelEvent.Handler<TemplateModel, TemplateDataColumn>>();

    public SetInternalTemplateDataModelEvent( TemplateModel model,
                                              DynamicData data,
                                              List<DynamicColumn<TemplateDataColumn>> columns ) {
        super( model,
               data,
               columns );
    }

    @Override
    public GwtEvent.Type<SetInternalModelEvent.Handler<TemplateModel, TemplateDataColumn>> getAssociatedType() {
        return TYPE;
    }

}
