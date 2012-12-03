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
package org.kie.guvnor.guided.dtable.model.auditlog;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.kie.guvnor.guided.dtable.model.BaseColumn;

/**
 * An Audit Event for when a column is updated
 */
@Portable
public class UpdateColumnAuditLogEntry extends InsertColumnAuditLogEntry {

    private static final long serialVersionUID = -6953659333450748813L;

    private static final String TYPE = DecisionTableAuditLogFilter.DecisionTableAuditEvents.UPDATE_COLUMN.name();

    private ColumnDetails originalDetails;

    public UpdateColumnAuditLogEntry() {
    }

    public UpdateColumnAuditLogEntry( final String userName ) {
        super( userName );
    }

    public UpdateColumnAuditLogEntry( final String userName,
                                      final BaseColumn originalColumn,
                                      final BaseColumn newColumn ) {
        super( userName,
               newColumn );
        this.originalDetails = getDetails( originalColumn );
    }

    @Override
    public String getGenericType() {
        return TYPE;
    }

    public ColumnDetails getOriginalDetails() {
        return originalDetails;
    }

}
