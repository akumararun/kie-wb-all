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
package org.drools.ide.common.client.modeldriven.dt52.auditlog;

import org.drools.ide.common.client.modeldriven.auditlog.AuditLogEntry;

/**
 * An Audit Event when a row is inserted
 */
public class InsertRowAuditLogEntry extends AuditLogEntry {

    private static final long serialVersionUID = 8049692773593046770L;

    public int                rowIndex;

    public InsertRowAuditLogEntry() {
    }

    public InsertRowAuditLogEntry(final int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public int getRowIndex() {
        return this.rowIndex;
    }

}
