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
import org.kie.guvnor.datamodel.model.auditlog.AuditLogEntry;
import org.kie.guvnor.datamodel.model.auditlog.DefaultAuditLogEntry;
import org.kie.guvnor.guided.dtable.model.AttributeCol52;
import org.kie.guvnor.guided.dtable.model.BaseColumn;
import org.kie.guvnor.guided.dtable.model.MetadataCol52;

/**
 * An Audit Event for when a column is deleted
 */
@Portable
public class DeleteColumnAuditLogEntry extends DefaultAuditLogEntry {

    private static final long serialVersionUID = 2118763458557017503L;

    private static final String TYPE = DecisionTableAuditEvents.DELETE_COLUMN.name();

    private String columnHeader;

    public DeleteColumnAuditLogEntry() {
    }

    public DeleteColumnAuditLogEntry( final String userName ) {
        super( userName );
    }

    public DeleteColumnAuditLogEntry( final String userName,
                                      final BaseColumn column ) {
        super( userName );
        if ( column instanceof MetadataCol52 ) {
            this.columnHeader = ( (MetadataCol52) column ).getMetadata();
        } else if ( column instanceof AttributeCol52 ) {
            this.columnHeader = ( (AttributeCol52) column ).getAttribute();
        } else {
            this.columnHeader = column.getHeader();
        }
    }

    @Override
    public String getGenericType() {
        return TYPE;
    }

    public String getColumnHeader() {
        return this.columnHeader;
    }

}
