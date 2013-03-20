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
package org.jbpm.formModeler.service.bb.mvc.taglib.factory;

import org.jbpm.formModeler.service.bb.commons.config.componentsFactory.Factory;
import org.jbpm.formModeler.service.bb.mvc.components.HandlerMarkupGenerator;

import javax.servlet.jsp.JspTagException;


public class HandlerTag extends GenericFactoryTag {
    private static transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(HandlerTag.class.getName());

    /**
     * @see javax.servlet.jsp.tagext.TagSupport
     */
    public int doEndTag() throws JspTagException {
        HandlerMarkupGenerator markupGenerator = (HandlerMarkupGenerator) Factory.lookup("org.jbpm.formModeler.service.mvc.components.HandlerMarkupGenerator");
        String textToWrite = markupGenerator.getMarkup(getBean(), getAction());
        try {
            pageContext.getOut().print(textToWrite);
        } catch (java.io.IOException ex) {
            log.error(ex);
        }
        return EVAL_PAGE;
    }


    /**
     * @see javax.servlet.jsp.tagext.TagSupport
     */
    public int doStartTag() throws JspTagException {
        return SKIP_BODY;
    }

}
