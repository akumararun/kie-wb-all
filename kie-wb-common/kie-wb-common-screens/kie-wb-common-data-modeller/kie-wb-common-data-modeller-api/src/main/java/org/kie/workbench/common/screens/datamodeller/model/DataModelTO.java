/**
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.screens.datamodeller.model;

import org.jboss.errai.common.client.api.annotations.Portable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Portable
public class DataModelTO {

    private String parentProjectName;

    private List<DataObjectTO> dataObjects = new ArrayList<DataObjectTO>();

    private Map<String, String> sources = new HashMap<String, String>( );

    /**
     * List of class names imported by this module.
     */
    private List<DataObjectTO> externalClasses = new ArrayList<DataObjectTO>();

    /**
     * A list to remember data objects that was deleted in memory and has to be removed fisically when the model
     * is saved.
     */
    private List<DataObjectTO> deletedDataObjects = new ArrayList<DataObjectTO>();


    private static int modelIds = 0;

    //only to distinguish models created in memory
    private int id = modelIds++;


    public static enum TOStatus {

        /**
         * An element that was read form persistent status, .java files.
         */
        PERSISTENT,

        /**
         * An element that was created in memory an was not saved to persistent .java file yet.
         */
        VOLATILE,

        /**
         * Data objects that wasn't created by the data modeller, or was modified by an external editor and pushed to
         * the project repository.
         */
        PERSISTENT_EXTERNALLY_MODIFIED

    }


    public DataModelTO() {
    }

    public String getParentProjectName() {
        return parentProjectName;
    }

    public void setParentProjectName(String parentProjectName) {
        this.parentProjectName = parentProjectName;
    }

    public List<DataObjectTO> getDataObjects() {
        return dataObjects;
    }

    public void setDataObjects(List<DataObjectTO> dataObjects) {
        this.dataObjects = dataObjects;
    }

    public DataObjectTO getDataObjectByClassName(String className) {
        for (DataObjectTO dataObject : dataObjects) {
            if (dataObject.getClassName() != null && dataObject.getClassName().equals(className)) return dataObject;
        }
        return null;
    }

    public void removeDataObject(DataObjectTO dataObject) {
        getDataObjects().remove(dataObject);
        deletedDataObjects.add(dataObject);
    }

    public void removeDataObject(String className) {
        DataObjectTO dataObjectTO;
        if ( ( dataObjectTO = getDataObjectByClassName( className )) != null) {
            removeDataObject( dataObjectTO );
        }
    }

    public List<DataObjectTO> getDeletedDataObjects() {
        return deletedDataObjects;
    }

    public void setDeletedDataObjects(List<DataObjectTO> deletedDataObjects) {
        this.deletedDataObjects = deletedDataObjects;
    }

    /**
     * Tag all objects as persisted and clean deleted objects list.
     *
     * @param includeReadonlyObjects true set also readonly objects as PERSISTENT objects, false to skip them.
     */
    public void setPersistedStatus(boolean includeReadonlyObjects) {
        deletedDataObjects.clear();
        for (DataObjectTO dataObjectTO : dataObjects) {
            if (includeReadonlyObjects || !dataObjectTO.isExternallyModified()) {
                dataObjectTO.setOriginalClassName(dataObjectTO.getClassName());
                dataObjectTO.setStatus(TOStatus.PERSISTENT);
                for (ObjectPropertyTO propertyTO : dataObjectTO.getProperties()) {
                    propertyTO.setOriginalName( propertyTO.getName() );
                    propertyTO.setStatus( TOStatus.PERSISTENT );
                }
            }
        }
    }

    public void updateFingerPrints(Map<String, String> fingerPrints) {
        String fingerPrint = null;
        for (DataObjectTO dataObject : getDataObjects()) {
            fingerPrint = fingerPrints.get(dataObject.getClassName());
            if (fingerPrint != null) {
                if (dataObject.isExternallyModified() && !fingerPrint.equals(dataObject.getFingerPrint())) {
                    dataObject.setOriginalClassName(dataObject.getClassName());
                    dataObject.setStatus(TOStatus.PERSISTENT);
                    for (ObjectPropertyTO property : dataObject.getProperties()) {
                        property.setOriginalName( property.getName() );
                        property.setStatus( TOStatus.PERSISTENT );
                    }
                }
                dataObject.setFingerPrint(fingerPrint);
            }
        }
    }

    public List<DataObjectTO> getExternalClasses() {
        return externalClasses;
    }

    public void setExternalClasses(List<DataObjectTO> externalClasses) {
        this.externalClasses = externalClasses;
    }

    public boolean isExternal(String className) {
        if (externalClasses == null || className == null || "".equals(className)) return false;
        for (DataObjectTO externalDataObject : externalClasses) {
            if (className.equals(externalDataObject.getClassName())) return true;
        }
        return false;
    }

    public int getId() {
        return id;
    }

    public Map<String, String> getSources() {
        return sources;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DataModelTO that = (DataModelTO) o;

        if (id != that.id) {
            return false;
        }
        if (dataObjects != null ? !dataObjects.equals(that.dataObjects) : that.dataObjects != null) {
            return false;
        }
        if (deletedDataObjects != null ? !deletedDataObjects.equals(that.deletedDataObjects) : that.deletedDataObjects != null) {
            return false;
        }
        if (externalClasses != null ? !externalClasses.equals(that.externalClasses) : that.externalClasses != null) {
            return false;
        }
        if (parentProjectName != null ? !parentProjectName.equals(that.parentProjectName) : that.parentProjectName != null) {
            return false;
        }
        if (sources != null ? !sources.equals(that.sources) : that.sources != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = parentProjectName != null ? parentProjectName.hashCode() : 0;
        result = ~~result;
        result = 31 * result + (dataObjects != null ? dataObjects.hashCode() : 0);
        result = ~~result;
        result = 31 * result + (sources != null ? sources.hashCode() : 0);
        result = ~~result;
        result = 31 * result + (externalClasses != null ? externalClasses.hashCode() : 0);
        result = ~~result;
        result = 31 * result + (deletedDataObjects != null ? deletedDataObjects.hashCode() : 0);
        result = ~~result;
        result = 31 * result + id;
        result = ~~result;
        return result;
    }
}

