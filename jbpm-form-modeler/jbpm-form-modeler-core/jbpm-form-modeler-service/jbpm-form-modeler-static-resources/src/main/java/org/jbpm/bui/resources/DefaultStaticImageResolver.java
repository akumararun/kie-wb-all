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
package org.jbpm.bui.resources;

import org.jbpm.formModeler.service.bb.commons.config.componentsFactory.BasicFactoryElement;
import org.apache.commons.lang.StringUtils;

public class DefaultStaticImageResolver extends BasicFactoryElement implements StaticResourceResolver {
    private String basePath = "/formModeler/images/";
    private String separator = "/";

    @Override
    public String getImagePath(String image) {
        if (StringUtils.isEmpty(image)) return null;
        if (!basePath.endsWith(separator) && !image.startsWith(separator)) image = separator + image;

        return basePath + image;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    @Override
    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }
}
