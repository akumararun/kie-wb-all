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
package org.jbpm.formModeler.core.processing.fieldHandlers;

import org.apache.commons.logging.Log;
import org.jbpm.formModeler.api.config.FormManager;
import org.jbpm.formModeler.api.model.FieldType;
import org.jbpm.formModeler.api.processing.BindingManager;
import org.jbpm.formModeler.core.FormCoreServices;
import org.jbpm.formModeler.core.processing.FormProcessingServices;
import org.jbpm.formModeler.service.annotation.config.Config;
import org.jbpm.formModeler.service.bb.mvc.taglib.formatter.FormaterTagDynamicAttributesInterpreter;
import org.jbpm.formModeler.service.bb.mvc.taglib.formatter.Formatter;

import org.jbpm.formModeler.core.config.FormManagerImpl;
import org.jbpm.formModeler.api.model.i18n.I18nSet;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.jbpm.formModeler.api.model.Field;
import org.jbpm.formModeler.api.model.Form;
import org.jbpm.formModeler.api.processing.FormProcessor;

import javax.inject.Inject;

public abstract class DefaultFieldHandlerFormatter extends Formatter {

    @Inject
    private Log log;

    @Inject @Config("128")
    protected int defaultMaxLength;

    @Inject @Config("25")
    protected int defaultSize;

    public final FormManager getFormManager() {
        return FormCoreServices.lookup().getFormManager();
    }

    public final FormProcessor getFormProcessor() {
        return FormProcessingServices.lookup().getFormProcessor();
    }

    protected void setDefaultAttributes(final Field field, final Form form, final String namespace) {
        setAttributeInterpreter(new FormaterTagDynamicAttributesInterpreter() {
            private BindingManager bindingManager = FormCoreServices.lookup().getBindingManager();
            public Object getValueForParameter(String parameter) {
                Object value = null;
                if ("form".equals(parameter)) value = form;
                if ("field".equals(parameter)) value = field;
                if (form != null && ("lastParameterMap".equals(parameter))) {
                    value = getFormProcessor().read(form.getId(), namespace).getCurrentInputValues();
                }
                if (field != null && ("name".equals(parameter))) {
                    value = field.getFieldName();
                }

                if (field != null && form != null) try {
                    FieldType fieldType = field.getFieldType();
                    if (bindingManager.hasProperty(fieldType, parameter)) {
                        Object val = bindingManager.getPropertyValue(fieldType, parameter);
                        val = getCustomValueIfApplicable(field, parameter, val, form, namespace);
                        if (val != null)
                            value = val;
                    }

                    if (bindingManager.hasProperty(field, parameter)) {
                        Object val = bindingManager.getPropertyValue(field, parameter);
                        val = getCustomValueIfApplicable(field, parameter, val, form, namespace);
                        if (val != null && !"".equals(val))
                            value = val;
                    }
                } catch (Exception e) {
                    log.error("Error calculating attribute " + parameter + " for field " + field.getFieldName());
                }
                return value;
            }
        });
    }

    protected String getUniqueIdentifier(Form form, String namespace, Field field, String fieldName) {
        return getFormManager().getUniqueIdentifier(form, namespace, field, fieldName);
    }

    /**
     * Some attributes require special treatment.
     *
     * @param propName
     * @param propValue
     */
    protected Object getCustomValueIfApplicable(Field field, String propName, Object propValue, Form form, String namespace) {
        if ("title".equals(propName) || "label".equals(propName)) {
            if (propValue != null && propValue instanceof I18nSet) {
                String value = (String) getLocaleManager().localize((I18nSet) propValue);
                propValue = !"".equals(value) ? value : null;
            }
            if ("title".equals(propName)) {
                propValue = StringEscapeUtils.escapeHtml(StringUtils.defaultString((String) propValue));
            }
        }
        Object overridenValue = getFormProcessor().getAttribute(form, namespace, field.getFieldName() + "." + propName);
        if (overridenValue != null) {
            propValue = overridenValue;
        }
        return propValue;
    }
}
