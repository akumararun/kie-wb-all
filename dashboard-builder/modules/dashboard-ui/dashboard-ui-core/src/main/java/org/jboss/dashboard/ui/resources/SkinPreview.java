/**
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
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
package org.jboss.dashboard.ui.resources;

import java.io.File;
import java.io.Serializable;

/**
 *
 */
public class SkinPreview extends GraphicElementPreview implements Serializable, ResourceHolder {
    private static transient org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SkinPreview.class.getName());

    public SkinPreview(File f, String workspaceId, Long sectionId, Long panelId, String id) {
        super(f, workspaceId, sectionId, panelId, id);
    }

    /**
     * Convert this preview into a skin.
     *
     * @return
     */
    public Skin toSkin() {
        return (Skin) toElement();
    }

    protected String getDescriptorFilename() {
        return Skin.DESCRIPTOR_FILENAME;
    }

    protected GraphicElement makeNewElement() {
        return new Skin();
    }
}
