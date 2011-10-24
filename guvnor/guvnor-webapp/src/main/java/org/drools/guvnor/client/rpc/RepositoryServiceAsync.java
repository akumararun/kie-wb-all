/*
 * Copyright 2011 JBoss Inc
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

package org.drools.guvnor.client.rpc;

import java.util.List;

import org.drools.ide.common.shared.workitems.WorkDefinition;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
*
* This is what the remote service will implement, as a servlet.
*
* (in hosted/debug mode, you could also use an implementation that was
* in-process).
*/
public interface RepositoryServiceAsync {

    /** PLACE THE FOLLOWING IN RepositoryServiceAsync.java **/

    /** Generated by AsyncInterfaceGenerator hackery */
   
    public void loadRuleListForState(org.drools.guvnor.client.rpc.StatePageRequest p0, AsyncCallback<org.drools.guvnor.client.rpc.PageResponse<org.drools.guvnor.client.rpc.StatePageRow>> cb);
    public void loadRuleListForState(java.lang.String p0, int p1, int p2, java.lang.String p3, AsyncCallback<org.drools.guvnor.client.rpc.TableDataResult> cb);
    public void loadTableConfig(java.lang.String p0, AsyncCallback<org.drools.guvnor.client.rpc.TableConfig> cb);
    public void createNewRule(java.lang.String p0, java.lang.String p1, java.lang.String p2, java.lang.String p3, java.lang.String p4, AsyncCallback<java.lang.String> cb);
    public void createNewRule(org.drools.guvnor.client.rpc.NewAssetConfiguration p0, AsyncCallback<java.lang.String> cb);
    public void createNewRule(org.drools.guvnor.client.rpc.NewGuidedDecisionTableAssetConfiguration p0, AsyncCallback<java.lang.String> cb);
    public void doesAssetExistInPackage(java.lang.String p0, java.lang.String p1, AsyncCallback<java.lang.Boolean> cb);
    public void createNewImportedRule(java.lang.String p0, java.lang.String p1, AsyncCallback<java.lang.String> cb);
    public void deleteUncheckedRule(java.lang.String p0, AsyncCallback cb);
    public void clearRulesRepository(AsyncCallback cb);
    public void listWorkspaces(AsyncCallback<java.lang.String[]> cb);
    public void createWorkspace(java.lang.String p0, AsyncCallback cb);
    public void removeWorkspace(java.lang.String p0, AsyncCallback cb);
    public void updateWorkspace(java.lang.String p0, java.lang.String[] p1, java.lang.String[] p2, AsyncCallback cb);
    public void listStates(AsyncCallback<java.lang.String[]> cb);
    public void createState(java.lang.String p0, AsyncCallback<java.lang.String> cb);
    public void renameState(java.lang.String p0, java.lang.String p1, AsyncCallback cb);
    public void removeState(java.lang.String p0, AsyncCallback cb);
    public void loadSuggestionCompletionEngine(java.lang.String p0, AsyncCallback<org.drools.ide.common.client.modeldriven.SuggestionCompletionEngine> cb);
    public void getCustomSelectors(AsyncCallback<java.lang.String[]> cb);
    public void showLog(org.drools.guvnor.client.rpc.PageRequest p0, AsyncCallback<org.drools.guvnor.client.rpc.PageResponse<org.drools.guvnor.client.rpc.LogPageRow>> cb);
    public void showLog(AsyncCallback<org.drools.guvnor.client.rpc.LogEntry[]> cb);
    public void cleanLog(AsyncCallback cb);
    public void loadDropDownExpression(java.lang.String[] p0, java.lang.String p1, AsyncCallback<java.lang.String[]> cb);
    public void queryFullText(org.drools.guvnor.client.rpc.QueryPageRequest p0, AsyncCallback<org.drools.guvnor.client.rpc.PageResponse<org.drools.guvnor.client.rpc.QueryPageRow>> cb);
    public void queryMetaData(org.drools.guvnor.client.rpc.QueryMetadataPageRequest p0, AsyncCallback<org.drools.guvnor.client.rpc.PageResponse<org.drools.guvnor.client.rpc.QueryPageRow>> cb);
    public void queryMetaData(org.drools.guvnor.client.rpc.MetaDataQuery[] p0, java.util.Date p1, java.util.Date p2, java.util.Date p3, java.util.Date p4, boolean p5, int p6, int p7, AsyncCallback<org.drools.guvnor.client.rpc.TableDataResult> cb);
    public void listUserPermissions(org.drools.guvnor.client.rpc.PageRequest p0, AsyncCallback<org.drools.guvnor.client.rpc.PageResponse<org.drools.guvnor.client.rpc.PermissionsPageRow>> cb);
    public void listUserPermissions(AsyncCallback cb);
    public void retrieveUserPermissions(java.lang.String p0, AsyncCallback cb);
    public void updateUserPermissions(java.lang.String p0, java.util.Map p1, AsyncCallback cb);
    public void listAvailablePermissionTypes(AsyncCallback<java.lang.String[]> cb);
    public void listAvailablePermissionRoleTypes(AsyncCallback<List<String>> callback);
    public void deleteUser(java.lang.String p0, AsyncCallback cb);
    public void createUser(java.lang.String p0, AsyncCallback cb);
    public void subscribe(AsyncCallback cb);
    public void loadInbox(java.lang.String p0, AsyncCallback<org.drools.guvnor.client.rpc.TableDataResult> cb);
    public void loadInbox(org.drools.guvnor.client.rpc.InboxPageRequest p0, AsyncCallback<org.drools.guvnor.client.rpc.PageResponse<org.drools.guvnor.client.rpc.InboxPageRow>> cb);
    public void processTemplate(java.lang.String p0, java.util.Map p1, AsyncCallback<java.lang.String> cb);
    public void loadSpringContextElementData(AsyncCallback cb);
    public void loadWorkitemDefinitionElementData(AsyncCallback cb);
    public void loadWorkItemDefinitions(AsyncCallback<List<WorkDefinition>> cb);
    
}

