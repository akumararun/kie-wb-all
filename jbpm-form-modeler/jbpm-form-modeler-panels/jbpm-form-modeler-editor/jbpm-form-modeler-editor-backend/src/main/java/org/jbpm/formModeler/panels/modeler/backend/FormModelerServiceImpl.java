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
package org.jbpm.formModeler.panels.modeler.backend;

import org.apache.commons.lang.StringUtils;
import org.jboss.errai.bus.server.annotations.Service;
import org.jbpm.formModeler.api.model.DataHolder;
import org.jbpm.formModeler.api.model.Field;
import org.jbpm.formModeler.core.config.FormManager;
import org.jbpm.formModeler.core.config.FormSerializationManager;
import org.jbpm.formModeler.api.client.FormEditorContextTO;
import org.jbpm.formModeler.api.model.Form;
import org.jbpm.formModeler.api.client.FormRenderContext;
import org.jbpm.formModeler.api.client.FormRenderContextManager;
import org.jbpm.formModeler.api.client.FormEditorContext;
import org.jbpm.formModeler.core.processing.FormProcessor;
import org.jbpm.formModeler.editor.service.FormModelerService;
import org.kie.commons.io.IOService;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.backend.vfs.FileSystem;
import org.uberfire.backend.vfs.Path;
import org.uberfire.backend.vfs.PathFactory;
import org.uberfire.workbench.events.ResourceAddedEvent;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@ApplicationScoped
public class FormModelerServiceImpl implements FormModelerService {
    public static final String EDIT_FIELD_LITERAL = "editingFormFieldId";

    @Inject
    @Named("ioStrategy")
    IOService ioService;

    @Inject
    private Paths paths;

    @Inject
    private Event<ResourceAddedEvent> resourceAddedEvent;

    @Inject
    private FormManager formManager;

    @Inject
    private FormSerializationManager formSerializationManager;

    @Inject
    private FormRenderContextManager formRenderContextManager;

    protected Map<String, FormEditorContext> formEditorContextMap = new HashMap<String, FormEditorContext>();

    @Override
    public FormEditorContextTO setFormFocus(String ctxUID) {
        if (StringUtils.isEmpty(ctxUID)) return null;
        return getFormEditorContext(ctxUID).getFormEditorContextTO();
    }

    @Override
    public void removeEditingForm(String ctxUID) {
        formEditorContextMap.remove(ctxUID);
        formRenderContextManager.removeContext(ctxUID);
    }

    @Override
    public FormEditorContextTO loadForm(Path context) {
        try {
            org.kie.commons.java.nio.file.Path kiePath = paths.convert( context );

            String xml = ioService.readAllString(kiePath).trim();
            Form form = formSerializationManager.loadFormFromXML(xml, context);

            return newContext(form, context).getFormEditorContextTO();
        } catch (Exception e) {
            Logger.getLogger(FormModelerServiceImpl.class.getName()).log(Level.WARNING, null, e);
            return null;
        }
    }

    @Override
    public FormEditorContext newContext(Form form, Object path) {
        FormRenderContext ctx = formRenderContextManager.newContext(form, new HashMap<String, Object>());
        FormEditorContext formEditorContext = new FormEditorContext(ctx, path);
        formEditorContextMap.put(ctx.getUID(), formEditorContext);
        return formEditorContext;
    }

    @Override
    public FormEditorContext getFormEditorContext(String UID) {
        return formEditorContextMap.get(UID);
    }

    @Override
    public String generateFieldEditionNamespace(String UID, Field field) {
        return UID + FormProcessor.NAMESPACE_SEPARATOR + EDIT_FIELD_LITERAL + FormProcessor.CUSTOM_NAMESPACE_SEPARATOR + field.getId();
    }

    @Override
    public FormEditorContext getRootEditorContext(String UID) {
        if (StringUtils.isEmpty(UID)) return null;
        int separatorIndex = UID.indexOf(FormProcessor.NAMESPACE_SEPARATOR);
        if (separatorIndex != -1) UID = UID.substring(0, separatorIndex);
        return formEditorContextMap.get(UID);
    }

    @Override
    public void saveForm(String ctxUID) {
        saveContext(ctxUID);
    }

    @Override
    public void saveContext(String ctxUID) {
        FormEditorContext ctx = getFormEditorContext(ctxUID);
        formManager.replaceForm(ctx.getOriginalForm(), ctx.getForm());
        org.kie.commons.java.nio.file.Path kiePath = paths.convert((Path)ctx.getPath());
        ioService.write(kiePath, formSerializationManager.generateFormXML(ctx.getForm()));
    }

    @Override
    public Path createForm(Path context, String formName) {
        org.kie.commons.java.nio.file.Path kiePath = paths.convert(context ).resolve(formName);

        ioService.createFile(kiePath);

        Form form = formManager.createForm(formName);

        ioService.write(kiePath, formSerializationManager.generateFormXML(form));

        resourceAddedEvent.fire(new ResourceAddedEvent(context));

        final Path path = paths.convert(kiePath, false);

        return path;
    }
}
