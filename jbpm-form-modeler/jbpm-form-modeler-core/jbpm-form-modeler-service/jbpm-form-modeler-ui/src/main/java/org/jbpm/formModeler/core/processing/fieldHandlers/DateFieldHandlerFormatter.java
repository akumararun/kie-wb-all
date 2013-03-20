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

import org.jbpm.formModeler.service.bb.commons.config.LocaleManager;
import org.jbpm.formModeler.service.bb.mvc.taglib.formatter.FormatterException;
import org.jbpm.formModeler.api.model.Field;
import org.jbpm.formModeler.api.model.Form;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;

public class DateFieldHandlerFormatter extends DefaultFieldHandlerFormatter {
    
    private DateFieldHandler dateFieldHandler;
    
    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws FormatterException {
        FieldHandlerParametersReader paramsReader = new FieldHandlerParametersReader(request);

        Field field = paramsReader.getCurrentField();
        Form form = paramsReader.getCurrentForm();
        String namespace = paramsReader.getCurrentNamespace();
        Object value = paramsReader.getCurrentFieldValue();
        String fieldName = paramsReader.getCurrentFieldName();

        setDefaultAttributes(field, form, namespace);

        String inputPattern = dateFieldHandler.getDefaultPattern();

        if (!StringUtils.isEmpty(dateFieldHandler.getDefaultPatterTimeSuffix())) inputPattern += " " + dateFieldHandler.getDefaultPatterTimeSuffix();

        SimpleDateFormat sdf = new SimpleDateFormat(inputPattern,  LocaleManager.currentLocale());

        String dateValue = "";

        if (value != null) dateValue = sdf.format(value);

        setAttribute("name", fieldName);
        setAttribute("value", dateValue);
        setAttribute("inputPattern", dateFieldHandler.getDefaultPattern());
        setAttribute("timePattern", dateFieldHandler.getDefaultPatterTimeSuffix());
        setAttribute("uid", getFormManager().getUniqueIdentifier(form, namespace, field, fieldName));
        renderFragment("output");
    }

    public DateFieldHandler getDateFieldHandler() {
        return dateFieldHandler;
    }

    public void setDateFieldHandler(DateFieldHandler dateFieldHandler) {
        this.dateFieldHandler = dateFieldHandler;
    }
}
