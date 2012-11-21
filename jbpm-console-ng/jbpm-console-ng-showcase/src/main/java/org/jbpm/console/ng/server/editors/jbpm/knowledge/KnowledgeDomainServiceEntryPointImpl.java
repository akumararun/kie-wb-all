/*
 * Copyright 2012 JBoss by Red Hat.
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
package org.jbpm.console.ng.server.editors.jbpm.knowledge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.droolsjbpm.services.api.KnowledgeDataService;
import org.droolsjbpm.services.api.KnowledgeDomainService;
import org.droolsjbpm.services.api.bpmn2.BPMN2DataService;
import org.jboss.errai.bus.server.annotations.Service;
import org.jbpm.console.ng.shared.model.NodeInstanceSummary;
import org.jbpm.console.ng.shared.model.ProcessInstanceSummary;
import org.jbpm.console.ng.shared.model.ProcessSummary;
import org.jbpm.console.ng.shared.model.StatefulKnowledgeSessionSummary;
import org.jbpm.console.ng.shared.model.TaskDefSummary;
import org.jbpm.console.ng.shared.model.VariableSummary;
import org.jbpm.console.ng.shared.KnowledgeDomainServiceEntryPoint;
import org.jbpm.process.instance.impl.ProcessInstanceImpl;
import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.jbpm.workflow.instance.node.CompositeNodeInstance;
import org.jbpm.workflow.instance.node.EventNodeInstance;
import org.kie.runtime.StatefulKnowledgeSession;
import org.kie.runtime.process.NodeInstance;
import org.kie.runtime.process.ProcessInstance;

/**
 *
 * @author salaboy
 */
@Service
@ApplicationScoped
public class KnowledgeDomainServiceEntryPointImpl implements KnowledgeDomainServiceEntryPoint{

    @Inject
    KnowledgeDomainService knowledgeService;
    
    @Inject
    KnowledgeDataService dataService;

    @Inject 
    BPMN2DataService bpmn2Service;

    public KnowledgeDomainServiceEntryPointImpl() {
    }
    

    public StatefulKnowledgeSessionSummary getSession(long sessionId) {
        return StatefulKnowledgeSessionHelper.adapt(knowledgeService.getSession(sessionId));
    }

    public StatefulKnowledgeSessionSummary getSessionSummaryByBusinessKey(String businessKey) {
        return StatefulKnowledgeSessionHelper.adapt(knowledgeService.getSessionByBusinessKey(businessKey));
    }

    public Collection<String> getSessionsNames() {
        return knowledgeService.getSessionsNames();
    }

    public int getAmountOfSessions() {
        return knowledgeService.getAmountOfSessions();
    }

    public Collection<ProcessInstanceSummary> getProcessInstances() {
        return ProcessInstanceHelper.adaptCollection(dataService.getProcessInstances());
    }

    public Collection<ProcessInstanceSummary> getProcessInstancesBySessionId(String sessionId) {
        return ProcessInstanceHelper.adaptCollection(dataService.getProcessInstancesBySessionId(sessionId));
    }

    public Collection<ProcessSummary> getProcessesBySessionId(String sessionId) {
        return ProcessHelper.adaptCollection(dataService.getProcessesByDomainName(sessionId));
    }

    public Collection<ProcessSummary> getProcesses() {
        return ProcessHelper.adaptCollection(dataService.getProcesses());
    }

   

    public List<String> getAssociatedDomainObjects(String bpmn2Content) {
        return bpmn2Service.getAssociatedDomainObjects(bpmn2Content);
    }

    public Map<String, String> getRequiredInputData(String bpmn2Content) {
        return bpmn2Service.getProcessData(bpmn2Content);
    }

    public List<String> getAssociatedForms(String bpmn2Content) {
        return bpmn2Service.getAssociatedForms(bpmn2Content);
    }

    public Collection<TaskDefSummary> getAllTasksDef(String bpmn2Content) {
        return TaskDefHelper.adaptCollection(bpmn2Service.getAllTasksDef(bpmn2Content));
    }

    public String getDomainName() {
        return knowledgeService.getDomainName();
    }

    public Map<String, String> getAvailableProcesses() {
        return knowledgeService.getAvailableProcesses();
    }

    public Map<String, String> getAssociatedEntities(String bpmn2Content) {
        return bpmn2Service.getAssociatedEntities(bpmn2Content);
    }

    public Collection<NodeInstanceSummary> getProcessInstanceHistory(int sessionId, long id) {
        return NodeInstanceHelper.adaptCollection(dataService.getProcessInstanceHistory(sessionId, id));
    }

    public Collection<NodeInstanceSummary> getProcessInstanceHistory(int sessionId, long processId, boolean completed) {
        return NodeInstanceHelper.adaptCollection(dataService.getProcessInstanceHistory(sessionId, processId, completed));
    }

    public Collection<NodeInstanceSummary> getProcessInstanceFullHistory(int sessionId, long processId) {
        return NodeInstanceHelper.adaptCollection(dataService.getProcessInstanceFullHistory(sessionId, processId));
    }

    public Collection<NodeInstanceSummary> getProcessInstanceActiveNodes(int sessionId, long processId) {
        return NodeInstanceHelper.adaptCollection(dataService.getProcessInstanceActiveNodes(sessionId, processId));
    }

    public ProcessSummary getProcessDesc(String bpmn2Content) {
        return ProcessHelper.adapt(bpmn2Service.getProcessDesc(bpmn2Content));
    } 

    public Collection<VariableSummary> getVariablesCurrentState(long processInstanceId, String processId) {
        String processString = knowledgeService.getAvailableProcesses().get(processId);
        Map<String, String> properties = bpmn2Service.getProcessData(processString);
        return VariableHelper.adaptCollection(dataService.getVariablesCurrentState(processInstanceId), properties, processInstanceId);
    }

    public Map<String, String> getTaskInputMappings(String bpmn2Content, String taskName) {
        return bpmn2Service.getTaskInputMappings(bpmn2Content, taskName);
    }

    public Map<String, String> getTaskOutputMappings(String bpmn2Content, String taskName) {
        return bpmn2Service.getTaskOutputMappings(bpmn2Content, taskName);
    }


    @Override
    public void abortProcessInstance(String businessKey, long processInstanceId) {
        knowledgeService.getSessionByBusinessKey(businessKey).abortProcessInstance(processInstanceId);
        
    }


    @Override
    public Collection<ProcessInstanceSummary> getProcessInstances(List<Integer> states) {
        return ProcessInstanceHelper.adaptCollection(dataService.getProcessInstances(states));
    }


    @Override
    public void signalProcessInstance(String businessKey, String signalName, Object event, long processInstanceId) {
        StatefulKnowledgeSession ksession = knowledgeService.getSessionByBusinessKey(businessKey);
        if (processInstanceId == -1) {
            ksession.signalEvent(signalName, event);
        } else {
            ksession.signalEvent(signalName, event, processInstanceId);
        }
        
    }


    @Override
    public Collection<String> getAvailableSignals(String businessKey, long processInstanceId) {
        StatefulKnowledgeSession ksession = knowledgeService.getSessionByBusinessKey(businessKey);
        ProcessInstance processInstance = ksession.getProcessInstance(processInstanceId);
        Collection<String> activeSignals = new ArrayList<String>();
        
        if (processInstance != null){
            ((ProcessInstanceImpl)processInstance).setProcess(ksession.getKnowledgeBase().getProcess(processInstance.getProcessId()));
            Collection<NodeInstance> activeNodes = ((WorkflowProcessInstance)processInstance).getNodeInstances();
            
            activeSignals.addAll(ProcessInstanceHelper.collectActiveSignals(activeNodes));
        }
        
        return activeSignals;
    }


    @Override
    public void setProcessVariable(String businessKey, long processInstanceId, String variableId, Object value) {
        StatefulKnowledgeSession ksession = knowledgeService.getSessionByBusinessKey(businessKey);
        ProcessInstance processInstance = ksession.getProcessInstance(processInstanceId);
        
        ((WorkflowProcessInstance)processInstance).setVariable(variableId, value);
        
    }


    @Override
    public Collection<VariableSummary> getVariableHistory(long processInstanceId, String variableId) {
        return VariableHelper.adaptCollection(dataService.getVariableHistory(processInstanceId, variableId));
    }

    
}
