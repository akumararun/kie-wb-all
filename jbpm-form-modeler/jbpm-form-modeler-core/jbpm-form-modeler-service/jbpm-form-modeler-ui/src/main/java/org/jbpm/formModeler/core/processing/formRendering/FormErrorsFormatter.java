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

import org.jbpm.formModeler.api.util.helpers.CDIHelper;
import org.jbpm.formModeler.service.bb.commons.config.LocaleManager;
import org.jbpm.formModeler.service.bb.mvc.taglib.formatter.Formatter;
import org.jbpm.formModeler.service.bb.mvc.taglib.formatter.FormatterException;
import org.jbpm.formModeler.core.config.FormManagerImpl;
import org.jbpm.formModeler.api.model.Form;
import org.jbpm.formModeler.api.model.Field;
import org.jbpm.formModeler.api.processing.FormProcessor;
import org.jbpm.formModeler.api.processing.FormStatusData;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class FormErrorsFormatter extends Formatter {
    private static transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(FormErrorsFormatter.class.getName());

    private LocaleManager localeManager;
    private FormManagerImpl formManagerImpl;
    private FormProcessor defaultFormProcessor = (FormProcessor) CDIHelper.getBeanByType(FormProcessor.class);

    private int maxVisibleErrors = 5;

    @Override
    public void start() throws Exception {
        super.start();
        formManagerImpl = FormManagerImpl.lookup();
    }

    public LocaleManager getLocaleManager() {
        return localeManager;
    }

    public void setLocaleManager(LocaleManager localeManager) {
        this.localeManager = localeManager;
    }

    public FormManagerImpl getFormManager() {
        return formManagerImpl;
    }

    public void setFormManager(FormManagerImpl formManagerImpl) {
        this.formManagerImpl = formManagerImpl;
    }

    public int getMaxVisibleErrors() {
        return maxVisibleErrors;
    }

    public void setMaxVisibleErrors(int maxVisibleErrors) {
        this.maxVisibleErrors = maxVisibleErrors;
    }

    public void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws FormatterException {

        String namespace = httpServletRequest.getAttribute("namespace") != null ? (String) httpServletRequest.getAttribute("namespace") : "";

        Form formToPaint = null;

        Object formObject = httpServletRequest.getAttribute("form");
        if (formObject != null) formToPaint = (Form) formObject;
        else {
            Object formIdObject = httpServletRequest.getAttribute("formId");
            Long formId = Long.decode(String.valueOf(formIdObject));
            formToPaint = formManagerImpl.getFormById(formId);
        }

        List errorsToShow = getFormFieldErrors(namespace, formToPaint);

        if (errorsToShow.size() > 0) {
            renderFragment("outputStart");
            renderFragment("outputErrorsStart");

            for (int i = 0; i < errorsToShow.size(); i++) {
                setAttribute("errorMsg", errorsToShow.get(i));
                setAttribute("namespace", namespace);
                setAttribute("index", i);
                setAttribute("display", i < getMaxVisibleErrors() ? "" : "none");
                renderFragment("outputError");
            }
            renderFragment("outputErrorsEnd");

            if (errorsToShow.size() > getMaxVisibleErrors()) {
                setAttribute("namespace", namespace);
                setAttribute("min", getMaxVisibleErrors());
                setAttribute("max", errorsToShow.size());
                renderFragment("outputDisplayLinks");
            }
            renderFragment("outputEnd");
        }
    }

    public List getFormFieldErrors(String namespace, Form form) {
        List errorsToShow = new ArrayList();
        if (form != null && namespace != null) {
            try {
                FormStatusData statusData = defaultFormProcessor.read(form, namespace);
                for (int i = 0; i < statusData.getWrongFields().size(); i++) {
                    Field field = form.getField((String) statusData.getWrongFields().get(i));
                    Boolean fieldIsRequired = field.getFieldRequired();
                    boolean fieldRequired = fieldIsRequired != null && fieldIsRequired.booleanValue() && !Form.RENDER_MODE_DISPLAY.equals(fieldIsRequired);
                    String currentValue = statusData.getCurrentInputValue(namespace + FormProcessor.NAMESPACE_SEPARATOR + form.getId() + FormProcessor.NAMESPACE_SEPARATOR + field.getFieldName());
                    if (fieldRequired && (currentValue == null || currentValue.trim().equals(""))) {
                        errorsToShow.clear();
                        ResourceBundle bundle = ResourceBundle.getBundle("org.jbpm.formModeler.core.processing.formRendering.messages", LocaleManager.currentLocale());
                        errorsToShow.add(bundle.getString("errorMessages.required"));
                        break;
                    }
                }
            } catch (Exception e) {
                log.error("Error getting error messages for object " + form.getId() + ": ", e);
            }
        }
        return errorsToShow;
    }

    public FormProcessor getDefaultFormProcessor() {
        return defaultFormProcessor;
    }

    public void setDefaultFormProcessor(FormProcessor defaultFormProcessor) {
        this.defaultFormProcessor = defaultFormProcessor;
    }
}
