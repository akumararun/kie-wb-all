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
package org.jboss.dashboard.provider;

import org.jboss.dashboard.DataProviderServices;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.parsers.DOMParser;
import org.jboss.dashboard.LocaleManager;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.util.*;
import java.io.*;

import org.jboss.dashboard.dataset.DataSet;
import org.jboss.dashboard.database.hibernate.HibernateTxFragment;
import org.hibernate.*;

public class DataProviderImpl implements DataProvider {

    /** Logger */
    private transient static Log log = LogFactory.getLog(DataProviderImpl.class);

    protected Long id;
    protected String code;
    protected Map descriptions;
    public boolean canEdit;
    public boolean canEditProperties;
    public boolean canDelete;
    protected String dataProviderUid;
    protected String dataProviderXML;
    protected String dataPropertiesXML;
    protected transient DataSet dataSet;
    protected transient DataLoader dataLoader;

    public DataProviderImpl() {
        id = null;
        code = null;
        descriptions = new HashMap();
        dataLoader = null;
        dataSet = null;
        dataProviderUid = null;
        dataProviderXML = null;
        dataPropertiesXML = null;
        canEdit = true;
        canEditProperties = true;
        canDelete = true;
    }

    public int hashCode() {
        return new HashCodeBuilder().append(getId()).toHashCode();
    }

    public boolean equals(Object obj) {
        try {
            if (obj == null) return false;
            if (obj == this) return true;
            if (id == null) return false;

            DataProviderImpl other = (DataProviderImpl) obj;
            return id.equals(other.getId());
        }
        catch (ClassCastException e) {
            return false;
        }
    }

    public boolean isReady() {
        if (getDataLoader() == null) return false;
        return dataLoader.isReady();
    }

    public boolean isCanDelete() {
        return canDelete;
    }

    public void setCanDelete(boolean canDelete) {
        this.canDelete = canDelete;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }

    public boolean isCanEditProperties() {
        return canEditProperties;
    }

    public void setCanEditProperties(boolean canEditProperties) {
        this.canEditProperties = canEditProperties;
    }

    public String toString() {
        return new ToStringBuilder(this).append("id", getId()).toString();
    }

    public boolean isPersistent() {
        return id != null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        if (id != null && (code == null || code.trim().equals(""))) code = "dataprovider_" + id + System.currentTimeMillis();
        if (id == null && (code == null || code.trim().equals(""))) code = "dataprovider_" + System.currentTimeMillis();
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription(Locale l) {
        String result = (String) descriptions.get(l.toString());
        if (result == null) {
            // Try to get the first not null value.
            Iterator it = descriptions.keySet().iterator();
            while (it.hasNext()) {
                String lang = (String) it.next();
                result = (String) descriptions.get(lang);
                if (result != null) return result;
            }
        }
        return result;
    }

    public void setDescription(String descr, Locale l) {
        if (l == null) l = LocaleManager.currentLocale();
        if (descr == null) descriptions.remove(l.toString());
        else descriptions.put(l.toString(), descr);
    }

    public Map getDescriptionI18nMap() {
        Map results = new HashMap();
        Iterator it = descriptions.keySet().iterator();
        while (it.hasNext()) {
            String language = (String) it.next();
            results.put(new Locale(language), descriptions.get(language));
        }
        return results;
    }

    public DataProviderType getDataProviderType() {
        return getDataLoader().getDataProviderType();
    }

    public DataLoader getDataLoader() {
        if (dataLoader == null) deserializeDataLoader();
        return dataLoader;
    }

    public void setDataLoader(DataLoader dataLoader) {
        this.dataLoader = dataLoader;
        if (dataLoader != null) serializeDataLoader();

    }

    // Persistent stuff

    public boolean save() throws Exception {
        final boolean isTransient = !isPersistent();
        new HibernateTxFragment() {
        protected void txFragment(Session session) throws Exception {
            serializeDataLoader();
            if (isTransient) persist(0);
            else persist(1);
        }}.execute();
        return isTransient;
    }

    public boolean delete() throws Exception {
        if (!isPersistent()) return false;
        persist(2);
        return true;
    }

    protected void persist(final int op) throws Exception {
        new HibernateTxFragment() {
        protected void txFragment(Session session) throws Exception {
            switch(op) {
                case 0: session.save(DataProviderImpl.this);
                        break;
                case 1: session.update(DataProviderImpl.this);
                        break;
                case 2: session.delete(DataProviderImpl.this); break;
            }
            session.flush();
        }}.execute();
    }


    // DataProvider implementation.

    public DataSet getDataSet() throws Exception {
        if (dataSet == null && getDataLoader() != null) {
            dataSet = getDataLoader().load(this);
            dataSet.setDataProvider(this);
            deserializeDataProperties();
        }
        return dataSet;
    }

    public void setDataSet(DataSet s) {
        dataSet = s;
    }

    public DataSet refreshDataSet() throws Exception {
        if (getDataLoader() == null) return null;
        dataSet = getDataLoader().load(this);
        return dataSet;
    }

    public DataSet filterDataSet(DataFilter filter) throws Exception {
        getDataSet().filter(filter);
        deserializeDataProperties();
        return getDataSet();
    }

    // Persistence internals.

    protected void serializeDataLoader() {
        try {
            dataProviderUid = null;
            dataProviderXML = null;
            if (dataLoader == null) return;

            DataProviderType type = dataLoader.getDataProviderType();
            dataProviderUid = type.getUid();
            dataProviderXML = type.getXmlFormat().format(dataLoader);
            serializeDataProperties();
        } catch (Exception e) {
            log.error("Error serializing data provider: " + id, e);
        }
    }

    protected void serializeDataProperties() throws Exception {
        StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter(sw);
        getDataSet().formatXMLProperties(out, 0);
        dataPropertiesXML = sw.toString();
    }

    protected void deserializeDataLoader() {
        try {
            if (dataProviderUid == null) return;
            DataProviderManager dataProviderManager = DataProviderServices.lookup().getDataProviderManager();
            DataProviderType type = dataProviderManager.getProviderTypeByUid(dataProviderUid);
            if (dataProviderXML != null) {
                dataLoader = type.getXmlFormat().parse(dataProviderXML);
                dataLoader.setDataProviderType(type);
            }
        } catch (Exception e) {
            log.error("Error deserializing data provider: " + id, e);
        }
    }

    protected void deserializeDataProperties() throws Exception {
        if (dataPropertiesXML == null || dataPropertiesXML.trim().equals("")) return;

        DOMParser parser = new DOMParser();
        parser.parse(new InputSource(new StringReader(dataPropertiesXML)));
        Document doc = parser.getDocument();
        NodeList nodes = doc.getElementsByTagName("dataproperty");
        getDataSet().parseXMLProperties(nodes);
    }

    // For Hibernate
    protected String getDataProviderUid() {
        return dataProviderUid;
    }

    // For Hibernate
    protected void setDataProviderUid(String dataProviderUid) {
        this.dataProviderUid = dataProviderUid;
    }

    // For Hibernate
    protected String getDataProviderXML() {
        return dataProviderXML;
    }

    // For Hibernate
    protected void setDataProviderXML(String dataProviderXML) {
        this.dataProviderXML = dataProviderXML;
    }

    // For Hibernate
    protected String getDataPropertiesXML() {
        return dataPropertiesXML;
    }

    // For Hibernate
    protected void setDataPropertiesXML(String dataPropertiesXML) {
        this.dataPropertiesXML = dataPropertiesXML;
    }

    // For Hibernate
    protected Map getDescriptions() {
        return this.descriptions;
    }

    // For Hibernate
    protected void setDescriptions(Map descriptionI18nMap) {
        this.descriptions = descriptionI18nMap;
    }
}