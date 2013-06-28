/*
 * Copyright 2012 JBoss Inc
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

package org.guvnor.common.services.project.model;

import org.jboss.errai.common.client.api.annotations.Portable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Portable
public class KModuleModel
        implements Iterable<KBaseModel> {

    private final Map<String, KBaseModel> kBases = new HashMap<String, KBaseModel>();
    private String kBasesPath;
    private String kModulePath;
    private POM groupArtifactVersion;

    @Override
    public Iterator<KBaseModel> iterator() {
        return kBases.values().iterator();
    }

    public void add(KBaseModel kBase) {
        kBases.put(kBase.getName(), kBase);
    }

    public KBaseModel get(String name) {
        return kBases.get(name);
    }

    public Map<String, KBaseModel> getKBases() {
        return kBases;
    }

    public void remove(String fullName) {
        kBases.remove(fullName);
    }

    public String getKBasesPath() {
        return kBasesPath;
    }

    public String getKModulePath() {
        return kModulePath;
    }

    public POM getGroupArtifactVersion() {
        return groupArtifactVersion;
    }

    public void setKBasesPath(String kBasesPath) {
        this.kBasesPath = kBasesPath;
    }

    public void setKModulePath(String kModulePath) {
        this.kModulePath = kModulePath;
    }

    public void setGroupArtifactVersion(POM groupArtifactVersion) {
        this.groupArtifactVersion = groupArtifactVersion;
    }
}
