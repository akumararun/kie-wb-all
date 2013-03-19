/**
 * Copyright (C) 2012 JBoss Inc
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
package org.jboss.dashboard.security.principals;

import org.jboss.dashboard.LocaleManager;
import org.jboss.dashboard.users.Role;

import java.util.ResourceBundle;

public class ComplementaryRolePrincipal extends DefaultPrincipal {

    private  transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(ComplementaryRolePrincipal.class.getName());

    public ComplementaryRolePrincipal(String name) {
        super(name);
    }

    public ComplementaryRolePrincipal(Role role) {
        super("!role-" + role.getName());
    }

    public String toString() {
        ResourceBundle rb = ResourceBundle.getBundle("org.jboss.dashboard.ui.components.permissions.messages", LocaleManager.currentLocale());
        return rb.getString("permissions.inverted.role") + " " + super.toString();
    }
}
