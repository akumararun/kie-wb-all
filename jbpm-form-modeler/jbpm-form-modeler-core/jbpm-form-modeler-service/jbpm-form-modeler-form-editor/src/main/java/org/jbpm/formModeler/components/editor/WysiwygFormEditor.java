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
package org.jbpm.formModeler.components.editor;

import org.jbpm.formModeler.service.bb.commons.config.LocaleManager;
import org.jbpm.formModeler.service.bb.mvc.components.handling.BaseUIComponent;
import org.jbpm.formModeler.service.bb.mvc.controller.CommandRequest;
import org.jbpm.formModeler.service.bb.mvc.controller.CommandResponse;

import org.apache.commons.logging.Log;
import org.jbpm.formModeler.core.config.FieldTypeManagerImpl;
import org.jbpm.formModeler.core.config.FormManagerImpl;
import org.jbpm.formModeler.api.util.helpers.EditorHelper;
import org.jbpm.formModeler.api.model.Form;
import org.jbpm.formModeler.api.model.i18n.I18nSet;
import org.apache.commons.lang.StringUtils;
import org.jbpm.formModeler.api.model.Field;
import org.jbpm.formModeler.api.model.FieldType;
import org.jbpm.formModeler.api.processing.FormProcessor;
import org.jbpm.formModeler.api.processing.FormStatusData;

import java.lang.reflect.Method;
import java.util.*;

public class WysiwygFormEditor extends BaseUIComponent {
    private static transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(WysiwygFormEditor.class.getName());

    private String componentIncludeJSP;
    private String baseComponentJSP;

    public static final String TOP_FIELD_MODIFIER = "topModifier";
    public static final String LEFT_FIELD_MODIFIER = "leftModifier";
    public static final String RIGHT_FIELD_MODIFIER = "rightModifier";
    public static final String BOTTOM_FIELD_MODIFIER = "bottomModifier";

    public static final String EDITION_OPTION_FIELDTYPES = "fieldTypes";
    public static final String EDITION_OPTION_ENTITYFIELDS = "entityFields";
    public static final String EDITION_OPTION_FORM_PROPERTIES = "formProperties";
    public static final String EDITION_OPTION_FORM_EDITION_PROPERTIES = "formEditionProperties";

    public static final String ACTION_TO_DO = "actionToDo";
    public static final String ACTION_CHANGE_FIELD_TYPE = "changeFieldType";
    public static final String ACTION_SAVE_FIELD_PROPERTIES = "saveFieldProperties";
    public static final String ACTION_CANCEL_FIELD_EDITION = "cancelFieldEdition";

    private String namespace;
    private Form currentForm;
    private int currentEditFieldPosition = -1;
    private FormProcessor defaultFormProcessor;
    private FormManagerImpl formManagerImpl;
    private FieldTypeManagerImpl fieldTypeManagerImpl;
    private LocaleManager localeManager;
    private boolean swapFields = true;
    private String fieldTypeToView = null;
    private String currentEditionOption = EDITION_OPTION_FIELDTYPES;
    private FormTemplateEditor formTemplateEditor;
    private int lastMovedFieldPosition = -1;
    private boolean showReturnButton = false;
    private String renderMode = Form.RENDER_MODE_WYSIWYG_FORM;
    private FieldType originalFieldType;

    @Override
    public void start() throws Exception {
        super.start();
        formManagerImpl = FormManagerImpl.lookup();
        fieldTypeManagerImpl = FieldTypeManagerImpl.lookup();
    }

    public boolean isShowReturnButton() {
        return showReturnButton;
    }

    public void setShowReturnButton(boolean showReturnButton) {
        this.showReturnButton = showReturnButton;
    }

    public Form getCurrentForm() {
        return currentForm;
    }

    public void setCurrentForm(Form currentForm) {
        this.currentForm = currentForm;
        lastMovedFieldPosition = -1;
        setCurrentEditFieldPosition(-1);
        setFieldTypeToView(null);
    }

    public FormTemplateEditor getFormTemplateEditor() {
        return formTemplateEditor;
    }

    public void setFormTemplateEditor(FormTemplateEditor formTemplateEditor) {
        this.formTemplateEditor = formTemplateEditor;
    }

    public String getCurrentEditionOption() {
        return currentEditionOption;
    }

    public void setCurrentEditionOption(String currentEditionOption) {
        this.currentEditionOption = currentEditionOption;
    }

    public int getCurrentEditFieldPosition() {
        return currentEditFieldPosition;
    }

    public void setCurrentEditFieldPosition(int currentEditFieldPosition)  {
        this.currentEditFieldPosition = currentEditFieldPosition;
        Field field = getCurrentEditField();
        if (field != null) setFieldTypeToView(field.getFieldType().getCode());
    }

    public String getComponentIncludeJSP() {
        return componentIncludeJSP;
    }

    public void setComponentIncludeJSP(String componentIncludeJSP) {
        this.componentIncludeJSP = componentIncludeJSP;
    }

    public FieldTypeManagerImpl getFieldTypesManager() {
        return fieldTypeManagerImpl;
    }

    public void setFieldTypesManager(FieldTypeManagerImpl FieldTypeManagerImpl) {
        this.fieldTypeManagerImpl = FieldTypeManagerImpl;
    }

    public LocaleManager getLocaleManager() {
        return localeManager;
    }

    public void setLocaleManager(LocaleManager localeManager) {
        this.localeManager = localeManager;
    }

    public boolean isSwapFields() {
        return swapFields;
    }

    public void setSwapFields(boolean swapFields) {
        this.swapFields = swapFields;
    }

    public String getFieldTypeToView() {
        return fieldTypeToView;
    }

    public void setFieldTypeToView(String fieldTypeToView) {
        this.fieldTypeToView = fieldTypeToView;
    }

    public int getLastMovedFieldPosition() {
        return lastMovedFieldPosition;
    }

    public void setLastMovedFieldPosition(int lastMovedFieldPosition) {
        this.lastMovedFieldPosition = lastMovedFieldPosition;
    }

    public CommandResponse handle(CommandRequest commandRequest, String string) throws Exception {
        setLastMovedFieldPosition(-1);
        return super.handle(commandRequest, string);
    }

    public String getBeanName() {
        return super.getComponentName();
    }

    public Form getCurrentEditForm() {
        return currentForm;
    }

    public Field getCurrentEditField() {
        return getFieldInPosition(getCurrentEditFieldPosition());
    }

    protected Field getFieldInPosition(int position)  {
        if (position != -1) {
            Form form = getCurrentEditForm();
            if (form != null) {
                for (Field field : form.getFormFields()) {
                    if (field.getPosition() == position) {
                        return field;
                    }
                }
            }
        }
        return null;
    }

    public void actionEditForm(CommandRequest commandRequest) throws Exception {

        EditorHelper helper = (EditorHelper) commandRequest.getSessionObject().getAttribute("EditorHelper");

        if (helper != null) setCurrentForm(helper.getFormToEdit());
        else {
            String formId = commandRequest.getRequestObject().getParameter("formId");
            setCurrentForm(formManagerImpl.getFormById(Long.decode(formId)));
        }

    }

    public void actionDelete(CommandRequest request) throws Exception {
        Long pos = Long.decode(request.getParameter("position"));
        Form form = getCurrentEditForm();
        if (form == null) {
            log.error("Cannot modify unexistant form.");
        } else {
            Field fieldNextToDeleted = getFieldInPosition(pos.intValue() + 1);
            if (fieldNextToDeleted != null) {
                Field fieldToDelete = getFieldInPosition(pos.intValue());
                if (!Boolean.TRUE.equals(fieldToDelete.getGroupWithPrevious())) {
                    fieldNextToDeleted.setGroupWithPrevious(fieldToDelete.getGroupWithPrevious());
                }
            }
            formManagerImpl.deleteField(form, pos.intValue());
        }
    }

    public void actionStartEdit(CommandRequest request) throws Exception {
        Integer pos = Integer.decode(request.getParameter("position"));
        setCurrentEditFieldPosition(pos.intValue());

        Form formToEdit = getFormularyForFieldEdition(getCurrentEditField());
        if (formToEdit != null) {
            defaultFormProcessor.clear(formToEdit.getId(), "edit_" + getCurrentEditField().getId());
        }
        originalFieldType = getCurrentEditField().getFieldType();
    }

    public void actionSwapUp(CommandRequest request) throws Exception {
        lastMovedFieldPosition = Integer.decode(request.getParameter("position")).intValue();
        Form form = getCurrentEditForm();
        if (form == null) {
            log.error("Cannot modify unexistant form.");
        } else {
            Field fieldToMove = getFieldInPosition(lastMovedFieldPosition);
            if (fieldToMove != null) {
                Field previousField = getFieldInPosition(lastMovedFieldPosition - 1);
                if (previousField != null) {
                    Boolean b = previousField.getGroupWithPrevious();
                    previousField.setGroupWithPrevious(fieldToMove.getGroupWithPrevious());
                    fieldToMove.setGroupWithPrevious(b);
                    formManagerImpl.moveUp(form, lastMovedFieldPosition);
                } else {
                    fieldToMove.setGroupWithPrevious(Boolean.FALSE);
                }
                lastMovedFieldPosition--;
            } else {
                log.error("Cannot swap up unexistant field");
            }
        }
    }

    public void actionSwapDown(CommandRequest request) throws Exception {
        lastMovedFieldPosition = Integer.decode(request.getParameter("position")).intValue();
        Form form = getCurrentEditForm();
        if (form == null) {
            log.error("Cannot modify unexistant form.");
        } else {
            Field fieldToMove = getFieldInPosition(lastMovedFieldPosition);
            if (fieldToMove != null) {
                Field nextField = getFieldInPosition(lastMovedFieldPosition + 1);
                if (nextField == null) {
                    fieldToMove.setGroupWithPrevious(Boolean.FALSE);
                } else {
                    Boolean b = nextField.getGroupWithPrevious();
                    nextField.setGroupWithPrevious(fieldToMove.getGroupWithPrevious());
                    fieldToMove.setGroupWithPrevious(b);
                    formManagerImpl.moveDown(form, lastMovedFieldPosition);
                }
                lastMovedFieldPosition++;
            } else {
                log.error("Cannot swap down unexistant field");
            }
        }
    }


    public synchronized void actionMoveUp(CommandRequest request) throws Exception {
        lastMovedFieldPosition = Integer.decode(request.getParameter("position")).intValue();
        Form form = getCurrentEditForm();
        if (form == null) {
            log.error("Cannot modify unexistant form.");
        } else {
            Field fieldToMove = getFieldInPosition(lastMovedFieldPosition);
            if (fieldToMove != null) {
                if (Boolean.TRUE.equals(fieldToMove.getGroupWithPrevious())) {
                    Field previousField = getFieldInPosition(lastMovedFieldPosition - 1);
                    fieldToMove.setGroupWithPrevious(previousField.getGroupWithPrevious());
                    previousField.setGroupWithPrevious(Boolean.TRUE);
                    formManagerImpl.moveUp(form, lastMovedFieldPosition);
                } else {
                    Field nextField = getFieldInPosition(lastMovedFieldPosition + 1);
                    if (nextField != null) {
                        nextField.setGroupWithPrevious(Boolean.FALSE);
                    }
                    fieldToMove.setGroupWithPrevious(Boolean.TRUE);
                }
                lastMovedFieldPosition--;
            } else {
                log.error("Cannot move up unexistant field");
            }
        }
    }

    public synchronized void actionMoveDown(CommandRequest request) throws Exception {
        lastMovedFieldPosition = Integer.decode(request.getParameter("position")).intValue();
        Form form = getCurrentEditForm();
        if (form == null) {
            log.error("Cannot modify unexistant form.");
        } else {
            Field fieldToMove = getFieldInPosition(lastMovedFieldPosition);
            if (fieldToMove != null) {
                Field nextField = getFieldInPosition(lastMovedFieldPosition + 1);
                if (nextField == null) {
                    fieldToMove.setGroupWithPrevious(Boolean.FALSE);
                } else if (Boolean.TRUE.equals(nextField.getGroupWithPrevious())) {
                    nextField.setGroupWithPrevious(fieldToMove.getGroupWithPrevious());
                    fieldToMove.setGroupWithPrevious(Boolean.TRUE);
                    formManagerImpl.moveDown(form, lastMovedFieldPosition);
                } else {
                    nextField.setGroupWithPrevious(Boolean.TRUE);
                    fieldToMove.setGroupWithPrevious(Boolean.FALSE);
                }
                lastMovedFieldPosition++;
            } else {
                log.error("Cannot move down unexistant field");
            }
        }
    }

    public synchronized void actionPutInNewLine(CommandRequest request) throws Exception {
        final Integer pos = Integer.decode(request.getParameter("position"));
        Form form = getCurrentEditForm();
        if (form == null) {
            log.error("Cannot modify unexistant form.");
        } else {
            formManagerImpl.groupWithPrevious(form, pos.intValue(), false);
        }
    }

    public synchronized void actionPutInPreviousLine(CommandRequest request) throws Exception {
        final Integer pos = Integer.decode(request.getParameter("position"));
        Form form = getCurrentEditForm();
        if (form == null) {
            log.error("Cannot modify unexistant form.");
        } else {
            formManagerImpl.groupWithPrevious(form, pos.intValue(), true);
        }
    }


    public void actionEnd(CommandRequest request) throws Exception {
        setCurrentForm(null);
    }

    public boolean isActive() {
        return currentForm != null;
    }

    protected void addFieldToForm(Form form, String typeId) throws Exception {
        if (form == null) {
            log.error("Cannot modify unexistant form.");
        } else {
            FieldType fType = fieldTypeManagerImpl.getTypeByCode(typeId);
            Field field = formManagerImpl.addFieldToForm(form, fType);
            setCurrentEditFieldPosition(field.getPosition());
            originalFieldType = fType;
        }
    }

    public void actionAddFieldToFormulary(CommandRequest request) throws Exception {
        final String fieldType = request.getParameter("fieldType");
        Form form = getCurrentEditForm();
        addFieldToForm(form, fieldType);
    }

    public void actionAddDecoratorToFormulary(CommandRequest request) throws Exception {
        final String fieldType = request.getParameter("fieldType");
        Form form = getCurrentEditForm();
        if (form == null) {
            log.error("Cannot modify unexistant form.");
        } else {
            final String name = generateDecoratorName(form);
            I18nSet label = new I18nSet();
            String lang = getLocaleManager().getDefaultLang();
            FieldType fType = fieldTypeManagerImpl.getTypeByCode(fieldType);
            Field formField = formManagerImpl.addFieldToForm(form, name, fType, label);

            /*

            TODO: fix that
                    if ("HTMLLabel".equals(fType.getCode()) && field.hasProperty("htmlContent")) {
                        HTMLi18n val = new HTMLi18n();
                        val.setValue(lang, "HTML");
                        field.setPropertyValue("htmlContent", val);
                    }                                              */
        }
    }

    protected synchronized String generateDecoratorName(Form form) {
        TreeSet names = new TreeSet();
        for (Field pff : form.getFormFields()) {
            names.add(pff.getFieldName());
        }

        String name = ":decorator_0";
        for (int i = 1; names.contains(name); name = ":decorator_" + (i++))
            ;
        return name;
    }

    public Form getFormularyForFieldEdition(Field field) throws Exception {
        if (getFieldTypeToView() != null) {
            return formManagerImpl.getFormForFieldEdition(fieldTypeManagerImpl.getTypeByCode(getFieldTypeToView()));
        }
        return formManagerImpl.getFormForFieldEdition(field.getFieldType());
    }

    public void actionSaveFieldProperties(final CommandRequest request) throws Exception {

        Field pField = getCurrentEditField();
        Map parameterMap = request.getRequestObject().getParameterMap();
        Map filesMap = request.getFilesByParamName();
        String action = request.getRequestObject().getParameter(ACTION_TO_DO);
        if (pField == null) {
            log.error("Cannot update unexistant field.");
        } else {
            fieldTypeToView = ((String[]) parameterMap.get("fieldType"))[0];

            Form editForm = getFormularyForFieldEdition(pField);

            if (ACTION_CHANGE_FIELD_TYPE.equals(action)) {
                pField.setFieldType(fieldTypeManagerImpl.getTypeByCode(getFieldTypeToView()));
            } else if (ACTION_CANCEL_FIELD_EDITION.equals(action)) {
                pField.setFieldType(originalFieldType);
                defaultFormProcessor.clear(editForm.getId(), "edit_" + pField.getId());
                originalFieldType = null;
                currentEditFieldPosition = -1;
            } else {
                //Use custom edit form
                defaultFormProcessor.setValues(editForm, "edit_" + pField.getId(), parameterMap, filesMap);
                FormStatusData data = defaultFormProcessor.read(editForm.getId(), "edit_" + pField.getId());
                if (data.isValid()) {

                    /*
                     * TODO: fix that
                     */
                    Set names = pField.getPropertyNames();

                    for (Iterator it = data.getCurrentValues().keySet().iterator(); it.hasNext(); ) {
                        String propertyName = (String) it.next();
                        if (names.contains(propertyName)) {

                            Object value = data.getCurrentValue(propertyName);

                            Method setterMethod = pField.getClass().getMethod("set" + StringUtils.capitalize(propertyName), new Class[]{Class.forName(editForm.getField(propertyName).getFieldType().getFieldClass())});
                            try {
                                setterMethod.invoke(pField, new Object[]{value});
                            } catch (IllegalArgumentException iae) {
                                log.error("Error calling " + setterMethod, iae);

                            }

                        }
                    }

                    pField.setFieldType(fieldTypeManagerImpl.getTypeByCode(getFieldTypeToView()));
                    currentEditFieldPosition = -1;
                }
            }

        }
    }

    public synchronized void actionMoveField(CommandRequest request) throws Exception {
        String selectedField = request.getRequestObject().getParameter("selectedField");
        String newPosition = request.getRequestObject().getParameter("newPosition");
        String modifier = request.getRequestObject().getParameter("modifier");
        String promote = request.getRequestObject().getParameter("promote");

        if (StringUtils.isEmpty(selectedField) || StringUtils.isEmpty(newPosition) || StringUtils.isEmpty(modifier) || StringUtils.isEmpty(promote))
            return;

        Form form = getCurrentEditForm();
        if (form == null) {
            log.error("Cannot modify unexistant form.");
        } else {

            int origPosition = Integer.parseInt(selectedField);
            int destPosition = Integer.parseInt(newPosition);

            boolean groupWithPrevious = RIGHT_FIELD_MODIFIER.equals(modifier);
            boolean nextGrouped = LEFT_FIELD_MODIFIER.equals(modifier);

            lastMovedFieldPosition = destPosition;

            if (Boolean.parseBoolean(promote))
                formManagerImpl.promoteField(form, origPosition, destPosition, groupWithPrevious, nextGrouped);
            else formManagerImpl.degradeField(form, origPosition, destPosition, groupWithPrevious, nextGrouped);
        }
    }

    public synchronized void actionMoveFirst(CommandRequest request) throws Exception {
        lastMovedFieldPosition = Integer.decode(request.getParameter("position")).intValue();
        Form form = getCurrentEditForm();
        if (form == null) {
            log.error("Cannot modify unexistant form.");
        } else {
            formManagerImpl.moveTop(form, lastMovedFieldPosition);
            lastMovedFieldPosition = 0;
        }
    }

    public synchronized void actionMoveLast(CommandRequest request) throws Exception {
        lastMovedFieldPosition = Integer.decode(request.getParameter("position")).intValue();
        Form form = getCurrentEditForm();
        if (form == null) {
            log.error("Cannot modify unexistant form.");
        } else {
            formManagerImpl.moveBottom(form, lastMovedFieldPosition);
            lastMovedFieldPosition = form.getFormFields().size() - 1;
        }

    }

    /*
  public void actionAddComplexFieldToFormulary(CommandRequest request) throws Exception {
      String managerClass = request.getParameter("fieldClass");
      String name = request.getParameter("name");
      String label = request.getParameter("label");
      Long typeDbid = null;
      Form editForm = getCurrentEditForm();
      PropertyDefinition propDef = ddmManager.getPropertyType(name, editForm.getSubject());
      List fieldTypes = getFieldTypesManager().getSuitableFieldTypes(name, propDef);
      for (int i = 0; i < fieldTypes.size(); i++) {
          FieldType type = (FieldType) fieldTypes.get(i);
          if (type.getManagerClass().equals(managerClass)) {
              typeDbid = type.getDbid();
              break;
          }
      }
      if (typeDbid != null)
          addFieldToForm(editForm, typeDbid, label, name);
      else
          log.error("Could not add " + name + " field to form. Not found type for manager " + managerClass);
  }
    */
    public void setNamespace(String paramNamespace) {
        this.namespace = paramNamespace;
    }

    public String getNamespace() {
        return namespace;
    }

    public synchronized void actionSaveCurrentForm(CommandRequest request) throws Exception {
        saveCurrentForm(request.getRequestObject().getParameterMap());
    }

    public synchronized void actionSwitchRenderMode(CommandRequest request) throws Exception {
        String renderMode = request.getRequestObject().getParameter("renderMode");
        if (Form.RENDER_MODE_WYSIWYG_DISPLAY.equals(renderMode) || Form.RENDER_MODE_WYSIWYG_FORM.equals(renderMode))
            setRenderMode(renderMode);
    }

    public void saveCurrentForm(Map parameterMap) throws Exception {
        Map map = fillFormFromForm(parameterMap);
        String name = (String) map.get("name");
        String displayMode = (String) map.get("displayMode");
        String labelMode = (String) map.get("labelMode");
        Long status = (Long) map.get("status");
        Boolean isDefault = (Boolean) map.get("default");
        Boolean isDefaultView = (Boolean) map.get("defaultView");
        Boolean isShortView = (Boolean) map.get("shortView");
        Boolean isCreationView = (Boolean) map.get("creationView");
        Boolean isSearchView = (Boolean) map.get("searchView");
        Boolean isResultView = (Boolean) map.get("resultView");
        String customJsp = (String) map.get("customJsp");
        while (customJsp.indexOf("..") != -1) {
            customJsp = customJsp.substring(0, customJsp.indexOf("..")) + customJsp.substring(customJsp.indexOf("..") + 2, customJsp.length());
        }

        Form form = getCurrentEditForm();
        form.setName(name);
        form.setDisplayMode(displayMode);
        form.setLabelMode(labelMode);
        form.setStatus(status);
        form.setCustomRenderPage(customJsp);
        form.setDefault(isDefault);
        form.setDefaultView(isDefaultView);
        form.setCreationView(isCreationView);
        form.setShortView(isShortView);
        form.setSearchView(isSearchView);
        form.setResultView(isResultView);

        String[] editTemplateParams = (String[]) parameterMap.get("editTemplate");
        if (editTemplateParams != null && editTemplateParams.length > 0 && "true".equals(editTemplateParams[0])) {
            getFormTemplateEditor().setFormId(currentForm.getId());
            Long formId = currentForm.getId();
            if (formId != null) getFormTemplateEditor().setFormId(formId);
        }
    }

    protected Map fillFormFromForm(Map parameterMap) {
        Map m = new HashMap();
        String[] name = (String[]) parameterMap.get("name");
        String[] displayMode = (String[]) parameterMap.get("displayMode");
        String[] labelMode = (String[]) parameterMap.get("labelMode");
        String[] status = (String[]) parameterMap.get("status");
        String[] isDefault = (String[]) parameterMap.get("default");
        String[] isDefaultView = (String[]) parameterMap.get("defaultView");
        String[] isShortView = (String[]) parameterMap.get("shortView");
        String[] isCreationView = (String[]) parameterMap.get("creationView");
        String[] isSearchView = (String[]) parameterMap.get("searchView");
        String[] isResultView = (String[]) parameterMap.get("resultView");
        String[] copyingFrom = (String[]) parameterMap.get("copyingFrom");
        String[] customJsp = (String[]) parameterMap.get("customJsp");

        if (status == null || status.length == 0) {
            status = new String[]{String.valueOf(FormManagerImpl.FORMSTATUS_NORMAL)};
        }

        m.put("name", (name != null && name.length > 0) ? name[0] : null);
        m.put("displayMode", (displayMode != null && displayMode.length > 0) ? displayMode[0] : "default");
        m.put("labelMode", (labelMode != null && labelMode.length > 0) ? labelMode[0] : "undefined");
        m.put("status", Long.decode(status[0]));
        m.put("default", isDefault != null ? (Boolean.valueOf(isDefault[0])) : (Boolean.FALSE));
        m.put("defaultView", isDefaultView != null ? (Boolean.valueOf(isDefaultView[0])) : (Boolean.FALSE));
        m.put("shortView", isShortView != null ? (Boolean.valueOf(isShortView[0])) : (Boolean.FALSE));
        m.put("creationView", isCreationView != null ? (Boolean.valueOf(isCreationView[0])) : (Boolean.FALSE));
        m.put("searchView", isSearchView != null ? (Boolean.valueOf(isSearchView[0])) : (Boolean.FALSE));
        m.put("resultView", isResultView != null ? (Boolean.valueOf(isResultView[0])) : (Boolean.FALSE));
        m.put("copyingFrom", (copyingFrom != null && !"".equals(copyingFrom[0].trim())) ? (Long.decode(copyingFrom[0])) : null);
        m.put("customJsp", customJsp != null && customJsp.length == 1 ? customJsp[0] : null);
        return m;
    }

    public void createForm(Map parameterMap) {
        Map map = fillFormFromForm(parameterMap);
        String name = (String) map.get("name");
        String displayMode = (String) map.get("displayMode");
        Long copyingFrom = (Long) map.get("copyingFrom");
        Long status = (Long) map.get("status");
        try {
            Form form = null;
            if (copyingFrom == null) {
                form = formManagerImpl.createForm("", name, displayMode, status);
            } else {
                form = formManagerImpl.duplicateForm(copyingFrom, name, displayMode, status);
            }
            setCurrentForm(form);
        } catch (Exception e) {
            log.error("Error:", e);
        }
    }
    /*
   public boolean isShowingTemplateEdition() {
       if (getFormTemplateEditor() != null) {
           return getFormTemplateEditor().isOn();
       }
       return false;
   } */

    public boolean isShowingFormsList() {
        return !isActive();
    }

    public void actionUnGroupWithPrevious(CommandRequest request) throws Exception {
        groupField(request, false);
    }

    public void actionGroupWithPrevious(CommandRequest request) throws Exception {
        groupField(request, true);
    }

    protected void groupField(CommandRequest request, final boolean groupIt) throws Exception {
        lastMovedFieldPosition = Integer.decode(request.getParameter("position")).intValue();
        Form form = getCurrentEditForm();
        if (form == null) {
            log.error("Cannot modify unexistant form.");
        } else {
            Field fieldToMove = getFieldInPosition(lastMovedFieldPosition);
            if (fieldToMove != null) {
                fieldToMove.setGroupWithPrevious(Boolean.valueOf(groupIt));
            } else {
                log.error("Cannot modify unexistant field");
            }
        }
    }

    public String getRenderMode() {
        return renderMode;
    }

    public void setRenderMode(String renderMode) {
        this.renderMode = renderMode;
    }

    public FormManagerImpl getFormManager() {
        return formManagerImpl;
    }

    public FormProcessor getDefaultFormProcessor() {
        return defaultFormProcessor;
    }

    public void setDefaultFormProcessor(FormProcessor defaultFormProcessor) {
        this.defaultFormProcessor = defaultFormProcessor;
    }

    public String getBaseComponentJSP() {
        return baseComponentJSP;
    }

    public void setBaseComponentJSP(String baseComponentJSP) {
        this.baseComponentJSP = baseComponentJSP;
    }

    public static Log getLog() {
        return log;
    }

    public static void setLog(Log log) {
        WysiwygFormEditor.log = log;
    }
}
