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
package org.jbpm.formModeler.core.processing.formRendering;

import org.jbpm.formModeler.service.bb.commons.config.componentsFactory.BasicFactoryElement;
import org.jbpm.formModeler.api.model.Form;

public class CustomRenderingInfo extends BasicFactoryElement implements Cloneable {
    private static transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(CustomRenderingInfo.class.getName());
    private Form form;
    private String namespace;
    private String renderMode;
    private String labelMode;
    private String displayMode;

    public Form getForm() {
        return form;
    }

    public void setForm(Form form) {
        this.form = form;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(String displayMode) {
        this.displayMode = displayMode;
    }

    public String getLabelMode() {
        return labelMode;
    }

    public void setLabelMode(String labelMode) {
        this.labelMode = labelMode;
    }

    public String getRenderMode() {
        return renderMode;
    }

    public void setRenderMode(String renderMode) {
        this.renderMode = renderMode;
    }

    protected Object clone() throws CloneNotSupportedException {
        CustomRenderingInfo clone = (CustomRenderingInfo) super.clone();
        clone.form = this.form;
        return clone;
    }

    public void copyFrom(CustomRenderingInfo other) {
        if (other != null) {
            form = other.form;
            namespace = other.namespace;
            renderMode = other.renderMode;
            labelMode = other.labelMode;
            displayMode = other.displayMode;
        }
    }
}
