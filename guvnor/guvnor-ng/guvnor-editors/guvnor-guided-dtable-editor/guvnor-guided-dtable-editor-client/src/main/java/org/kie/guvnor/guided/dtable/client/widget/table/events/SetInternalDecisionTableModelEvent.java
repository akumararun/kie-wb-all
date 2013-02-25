/*
 * Copyright 2011 JBoss Inc
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.kie.guvnor.guided.dtable.client.widget.table.events;

import com.google.gwt.event.shared.GwtEvent;
import org.kie.guvnor.decoratedgrid.client.widget.DynamicColumn;
import org.kie.guvnor.decoratedgrid.client.widget.data.DynamicData;
import org.kie.guvnor.decoratedgrid.client.widget.events.SetInternalModelEvent;
import org.drools.guvnor.models.guided.dtable.shared.model.BaseColumn;
import org.drools.guvnor.models.guided.dtable.shared.model.GuidedDecisionTable52;

import java.util.List;

/**
 * An event to set the internal model for a Guided Decision Table
 */
public class SetInternalDecisionTableModelEvent extends SetInternalModelEvent<GuidedDecisionTable52, BaseColumn> {

    public static GwtEvent.Type<SetInternalModelEvent.Handler<GuidedDecisionTable52, BaseColumn>> TYPE = new GwtEvent.Type<SetInternalModelEvent.Handler<GuidedDecisionTable52, BaseColumn>>();

    public SetInternalDecisionTableModelEvent( GuidedDecisionTable52 model,
                                               DynamicData data,
                                               List<DynamicColumn<BaseColumn>> columns ) {
        super( model,
               data,
               columns );
    }

    @Override
    public GwtEvent.Type<SetInternalModelEvent.Handler<GuidedDecisionTable52, BaseColumn>> getAssociatedType() {
        return TYPE;
    }

}
