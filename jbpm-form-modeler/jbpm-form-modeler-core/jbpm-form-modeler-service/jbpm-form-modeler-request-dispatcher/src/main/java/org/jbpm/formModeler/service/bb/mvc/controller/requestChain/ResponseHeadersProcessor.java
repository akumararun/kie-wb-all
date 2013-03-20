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
package org.jbpm.formModeler.service.bb.mvc.controller.requestChain;

import org.jbpm.formModeler.service.bb.mvc.Framework;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 *
 */
public class ResponseHeadersProcessor extends RequestChainProcessor {
    private static transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(ResponseHeadersProcessor.class.getName());

    private boolean useRefreshHeader = false;
    private String responseContentType = "text/html";
    private Framework framework;


    public boolean isUseRefreshHeader() {
        return useRefreshHeader;
    }

    public void setUseRefreshHeader(boolean useRefreshHeader) {
        this.useRefreshHeader = useRefreshHeader;
    }

    public String getResponseContentType() {
        return responseContentType;
    }

    public void setResponseContentType(String responseContentType) {
        this.responseContentType = responseContentType;
    }

    public Framework getFramework() {
        return framework;
    }

    public void setFramework(Framework framework) {
        this.framework = framework;
    }

    /**
     * Make required processing of request.
     *
     * @return true if processing must continue, false otherwise.
     */
    protected boolean processRequest() {
        if (responseContentType != null && !"".equals(responseContentType)) {
            getResponse().setContentType(responseContentType);
            getResponse().setHeader("Content-Type", responseContentType + "; charset=" + framework.getFrameworkEncoding());
        }
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss", Locale.US);
        getResponse().setHeader("Expires", "Mon, 06 Jan 2003 21:29:02 GMT");
        getResponse().setHeader("Last-Modified", sdf.format(new Date()) + " GMT");
        getResponse().setHeader("Cache-Control", "no-cache, must-revalidate");
        getResponse().setHeader("Pragma", "no-cache");
        if (useRefreshHeader)
            getResponse().setHeader("Refresh", String.valueOf(getRequest().getSession().getMaxInactiveInterval() + 61));
        return true;
    }
}
