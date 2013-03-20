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
package org.jbpm.formModeler.service.bb.commons.config.componentsFactory;

/**
 *
 */
public class PropertySetProcessingInstruction extends PropertyChangeProcessingInstruction {
    private static transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(PropertySetProcessingInstruction.class.getName());

    public PropertySetProcessingInstruction(Component component, String propertyName, String propertyValue) {
        super(component, propertyName, propertyValue);
    }


    public Object getValueAfterChange(Object originalValue, Class expectedClass) throws Exception {
        if (log.isDebugEnabled())
            log.debug("Processing instruction " + this + " on value " + originalValue + " (" + expectedClass + ")");
        return getValueForParameter(getPropertyValue(), expectedClass);
    }


    public String toString() {
        return "do{" + getPropertyName() + "=" + getPropertyValue() + "}";
    }
}
