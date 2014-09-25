package org.kie.smoke.wb.rest;

import static org.junit.Assert.assertEquals;
import static org.kie.smoke.wb.util.RestUtil.get;
import static org.kie.smoke.wb.util.RestUtil.post;
import static org.kie.smoke.wb.util.TestConstants.*;

import javax.ws.rs.core.MediaType;

import org.apache.commons.net.util.Base64;
import org.jboss.resteasy.client.ClientRequest;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.audit.AuditService;
import org.kie.api.runtime.manager.audit.ProcessInstanceLog;
import org.kie.api.runtime.manager.audit.VariableInstanceLog;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.runtime.conf.RuntimeStrategy;
import org.kie.remote.client.jaxb.JaxbTaskSummaryListResponse;
import org.kie.services.client.api.RemoteRestRuntimeEngineFactory;
import org.kie.services.client.api.command.RemoteRuntimeEngine;
import org.kie.services.client.serialization.JaxbSerializationProvider;
import org.kie.services.client.serialization.JsonSerializationProvider;
import org.kie.services.client.serialization.jaxb.impl.audit.AbstractJaxbHistoryObject;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbHistoryLogList;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbProcessInstanceLog;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbVariableInstanceLog;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnit;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnitList;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessDefinition;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceResponse;
import org.kie.smoke.wb.AbstractWorkbenchIntegrationTest;
import org.kie.smoke.wb.category.KieWbSmoke;
import org.kie.smoke.wb.util.RestRepositoryDeploymentUtil;
import org.kie.smoke.wb.util.RestRequestHelper;
import org.kie.tests.MyType;
import org.kie.tests.Person;
import org.kie.tests.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@Category(KieWbSmoke.class)
public class KieRemoteRestSmokeIntegrationTest extends AbstractWorkbenchIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(KieRemoteRestSmokeIntegrationTest.class);

//    public abstract MediaType getMediaType();
//    public abstract boolean jmsQueuesAvailable();
//    public abstract RuntimeStrategy getStrategy();
//    public abstract int getTimeout();

    private static final String taskUserId = "salaboy";

    private final String deploymentId;
    private final KModuleDeploymentUnit deploymentUnit;
    private RuntimeStrategy strategy = RuntimeStrategy.SINGLETON;

    private MediaType mediaType;
    private int timeout;
    private static final int DEFAULT_TIMEOUT = 10;

    public KieRemoteRestSmokeIntegrationTest() {
        this.deploymentId = KJAR_DEPLOYMENT_ID;
        this.mediaType = MediaType.APPLICATION_XML_TYPE;
        this.strategy = RuntimeStrategy.SINGLETON;
        this.timeout = 1000;
        this.deploymentUnit = new KModuleDeploymentUnit(GROUP_ID, ARTIFACT_ID, VERSION);
        assertEquals("Deployment unit information", deploymentId, deploymentUnit.getIdentifier());
    }

    private final static int SETUP = 0;

    private final static int REST_SUCCEEDING = 1;

    private final static int JMS_SUCCEEDING = 2;

    private static JaxbSerializationProvider jaxbSerializationProvider;

    {
        Class<?>[] extraClasses = {MyType.class, Person.class, Request.class};
        jaxbSerializationProvider = JaxbSerializationProvider.clientSideInstance(Arrays.asList(extraClasses));

    }

    private JsonSerializationProvider jsonSerializationProvider = new JsonSerializationProvider();

    /**
     * Helper methods
     */

    private RestRequestHelper getRestRequestHelper(URL deploymentUrl, String user, String password) {
        return RestRequestHelper.newInstance(deploymentUrl, user, password, timeout, mediaType);
    }

    private RemoteRuntimeEngine getRemoteRuntime(URL deploymentUrl, String user, String password) {
        // @formatter:off
        return RemoteRestRuntimeEngineFactory.newBuilder()
                .addDeploymentId(deploymentId)
                .addUrl(deploymentUrl)
                .addUserName(user)
                .addPassword(password)
                .addExtraJaxbClasses(MyType.class, Person.class, Request.class)
                .build();
        // @formatter:on
    }

    @BeforeClass
    public static void waitForDeployedKmodulesToLoad() throws InterruptedException {
        long sleep = 2;
        logger.info("Waiting " + sleep + " secs for server to finish starting up.");
        Thread.sleep(sleep * 1000);
    }

    @AfterClass
    public static void waitForTxOnServer() throws InterruptedException {
        long sleep = 1;
        logger.info("Waiting " + sleep + " secs for tx's on server to close.");
        Thread.sleep(sleep * 1000);
    }

    @Before
    public void printTestName() {
        String testName = Thread.currentThread().getStackTrace()[2].getMethodName();
        System.out.println("-=> " + testName);
    }

    /**
     * Clone, build and deploy the test deployment unit.
     */
    @BeforeClass
    public static void setupDeployment() throws Exception {
        RestRepositoryDeploymentUtil deployUtil = new RestRepositoryDeploymentUtil(deploymentUrl, MARY_USER, MARY_PASSWORD, RuntimeStrategy.SINGLETON);
        deployUtil.setSleepSeconds(5);
        deployUtil.setTotalTries(6);

        String repoUrl = "https://github.com/droolsjbpm/jbpm-playground.git";
        String repositoryName = "playground";
        String project = "integration-tests";
        String deploymentId = "org.test:kjar:1.0";
        String orgUnit = "integTestUser";
        deployUtil.createAndDeployRepository(repoUrl, repositoryName, project, deploymentId, orgUnit, MARY_USER);
        Thread.sleep(5000);
    }

    @Test
    public void testUrlsGetDeployments() throws Exception {
        printTestName();
        // test with normal RestRequestHelper
        String user = MARY_USER;
        String password = MARY_PASSWORD;
        RestRequestHelper requestHelper = getRestRequestHelper(deploymentUrl, user, password);

        ClientRequest httpRequest = requestHelper.createRequest("deployment/");
        JaxbDeploymentUnitList depList = get(httpRequest, mediaType, JaxbDeploymentUnitList.class);
        assertNotNull("Null answer!", depList);
        assertNotNull("Null deployment list!", depList.getDeploymentUnitList());
        assertTrue("Empty deployment list!", depList.getDeploymentUnitList().size() > 0);

        String deploymentId = depList.getDeploymentUnitList().get(0).getIdentifier();
        httpRequest = requestHelper.createRequest("deployment/" + deploymentId);
        JaxbDeploymentUnit dep = get(httpRequest, mediaType, JaxbDeploymentUnit.class);

        assertNotNull("Null answer!", dep);
        assertNotNull("Null deployment list!", dep);
        assertEquals("Empty status!", JaxbDeploymentUnit.JaxbDeploymentStatus.DEPLOYED, dep.getStatus());

        // test with HttpURLConnection
        URL url = new URL(deploymentUrl, deploymentUrl.getPath() + "rest/deployment/");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        String authString = user + ":" + password;
        byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
        String authStringEnc = new String(authEncBytes);
        connection.setRequestProperty("Authorization", "Basic " + authStringEnc);
        connection.setRequestMethod("GET");

        logger.debug(">> [GET] " + url.toExternalForm());
        connection.connect();
        int respCode = connection.getResponseCode();
        if (200 != respCode) {
            logger.warn(connection.getContent().toString());
        }
        assertEquals(200, respCode);

        JaxbSerializationProvider jaxbSerializer = JaxbSerializationProvider.clientSideInstance();
        String xmlStrObj = getConnectionContent(connection.getContent());
        depList = (JaxbDeploymentUnitList) jaxbSerializer.deserialize(xmlStrObj);

        assertNotNull("Null answer!", depList);
        assertNotNull("Null deployment list!", depList.getDeploymentUnitList());
        assertTrue("Empty deployment list!", depList.getDeploymentUnitList().size() > 0);
    }

    @Test
    public void testRestHistoryLogs() throws Exception {
        //restTests.urlsHistoryLogs(deploymentUrl, MARY_USER, MARY_PASSWORD);
        String user = MARY_USER;
        String password = MARY_PASSWORD;
        RestRequestHelper helper = getRestRequestHelper(deploymentUrl, user, password);
        {
            // Start process
            ClientRequest httpRequest = helper.createRequest("runtime/" + deploymentId + "/process/" + SCRIPT_TASK_VAR_PROCESS_ID + "/start?map_x=initVal");
            JaxbProcessInstanceResponse processInstance = post(httpRequest, mediaType, 200, JaxbProcessInstanceResponse.class);
            long scriptTaskVarProcInstId = processInstance.getId();

            // instances/
            httpRequest = helper.createRequest("history/instances");
            JaxbHistoryLogList historyLogList = get(httpRequest, mediaType, JaxbHistoryLogList.class);
            List<Object> auditEventList = historyLogList.getResult();

            assertFalse("Empty list of audit events.", auditEventList.isEmpty());
            for (Object event : auditEventList) {
                assertTrue("ProcessInstanceLog", event instanceof ProcessInstanceLog);
                ProcessInstanceLog procLog = (ProcessInstanceLog) event;
                // @formatter:off
                Object[][] out = {
                        {procLog.getDuration(), "duration"},
                        {procLog.getEnd(), "end date"},
                        {procLog.getIdentity(), "identity"},
                        {procLog.getOutcome(), "outcome"},
                        {procLog.getParentProcessInstanceId(), "parent proc id"},
                        {procLog.getProcessId(), "process id"},
                        {procLog.getProcessInstanceId(), "process instance id"},
                        {procLog.getProcessName(), "process name"},
                        {procLog.getProcessVersion(), "process version"},
                        {procLog.getStart(), "start date"},
                        {procLog.getStatus(), "status"}};
                // @formatter:on
                for (int i = 0; i < out.length; ++i) {
                    // System.out.println(out[i][1] + ": " + out[i][0]);
                }
            }

            // instance/{procInstId}
            ProcessInstanceLog origProcInstLog = ((ProcessInstanceLog) auditEventList.get(0));
            long procInstId = origProcInstLog.getProcessInstanceId();
            httpRequest = helper.createRequest("history/instance/" + origProcInstLog.getProcessInstanceId());
            JaxbProcessInstanceLog procInstLog = get(httpRequest, mediaType, JaxbProcessInstanceLog.class);
            assertNotNull("Null process instance log!", procInstLog);
            assertEquals("Log process instance id",
                    procInstId, procInstLog.getProcessInstanceId().longValue());
            assertEquals("Process instance status",
                    origProcInstLog.getStatus(), procInstLog.getStatus());

            // TODO: instance/{procInstId}/child

            // instance/{procInstId}/node
            httpRequest = helper.createRequest("history/instance/" + procInstId + "/node");
            historyLogList = get(httpRequest, mediaType, JaxbHistoryLogList.class);
            assertNotNull("Null process instance log!", historyLogList);
            auditEventList = historyLogList.getResult();
            assertTrue("Empty audit event list!", auditEventList != null && !auditEventList.isEmpty());

            // TODO: instance/{procInstId}/variable

            // TODO: instance/{procInstId}/node/{nodeId}

            // instance/{procInstId}/variable/{variable}
            httpRequest = helper.createRequest("history/instance/" + scriptTaskVarProcInstId + "/variable/x");
            logger.debug(">> [runtime]" + httpRequest.getUri());
            historyLogList = get(httpRequest, mediaType, JaxbHistoryLogList.class);
            List<AbstractJaxbHistoryObject> historyVarLogList = historyLogList.getHistoryLogList();

            for (int i = 0; i < historyVarLogList.size(); ++i) {
                JaxbVariableInstanceLog varLog = (JaxbVariableInstanceLog) historyVarLogList.get(i);
                JaxbVariableInstanceLog historyVarLog = (JaxbVariableInstanceLog) historyVarLogList.get(i);
                assertEquals(historyVarLog.getValue(), varLog.getValue());
                assertEquals("Incorrect variable id", "x", varLog.getVariableId());
                assertEquals("Incorrect process id", SCRIPT_TASK_VAR_PROCESS_ID, varLog.getProcessId());
                assertEquals("Incorrect process instance id", scriptTaskVarProcInstId, varLog.getProcessInstanceId().longValue());
            }
        }

        // process/{procDefId}
        {
            ClientRequest httpRequest
                    = helper.createRequest("runtime/" + deploymentId + "/process/" + OBJECT_VARIABLE_PROCESS_ID);
            JaxbProcessDefinition procDef = get(httpRequest, mediaType, JaxbProcessDefinition.class);
            assertNotNull("Empty process definition!", procDef);
            assertEquals("Process definition id", OBJECT_VARIABLE_PROCESS_ID, procDef.getId());
        }

        {
            String varId = "myobject";
            String varVal = "10";
            ClientRequest httpRequest
                    = helper.createRequest("runtime/" + deploymentId + "/process/" + OBJECT_VARIABLE_PROCESS_ID + "/start?map_" + varId + "=" + varVal);
            JaxbProcessInstanceResponse procInstResp = post(httpRequest, mediaType, 200, JaxbProcessInstanceResponse.class);
            long objVarProcInstId = procInstResp.getResult().getId();

            // variable/{varId}

            httpRequest = helper.createRequest("history/variable/" + varId);
            JaxbHistoryLogList jhll = get(httpRequest, mediaType, JaxbHistoryLogList.class);
            List<VariableInstanceLog> viLogs = new ArrayList<VariableInstanceLog>();
            if (jhll != null) {
                List<Object> history = jhll.getResult();
                for (Object ae : history) {
                    VariableInstanceLog viLog = (VariableInstanceLog) ae;
                    if (viLog.getProcessInstanceId() == objVarProcInstId) {
                        viLogs.add(viLog);
                    }
                }
            }

            assertNotNull("Empty VariableInstanceLog list.", viLogs);
            assertEquals("VariableInstanceLog list size", 1, viLogs.size());
            VariableInstanceLog vil = viLogs.get(0);
            assertNotNull("Empty VariableInstanceLog instance.", vil);
            assertEquals("Process instance id", vil.getProcessInstanceId().longValue(), objVarProcInstId);
            assertEquals("Variable id", vil.getVariableId(), "myobject");
            assertEquals("Variable value", vil.getValue(), varVal);

            // TODO: variable/{varId}/{value}

            // history/variable/{varId}/instances

            httpRequest = helper.createRequest("history/variable/" + varId + "/instances");
            jhll = get(httpRequest, mediaType, JaxbHistoryLogList.class);

            assertNotNull("Empty ProcesInstanceLog list", jhll);
            List<ProcessInstanceLog> piLogs = new ArrayList<ProcessInstanceLog>();
            if (jhll != null) {
                List<Object> history = jhll.getResult();
                for (Object ae : history) {
                    piLogs.add((ProcessInstanceLog) ae);
                }
            }
            assertNotNull("Empty ProcesInstanceLog list", piLogs);
            assertEquals("ProcessInstanceLog list size", piLogs.size(), 1);
            ProcessInstanceLog pi = piLogs.get(0);
            assertNotNull(pi);

            // TODO: history/variable/{varId}/value/{val}/instances
        }
    }

    @Test
    public void testRestRemoteApiHumanTaskProcess() throws Exception {
        String user = MARY_USER;
        String password = MARY_PASSWORD;
        // create REST request
        RemoteRuntimeEngine engine = getRemoteRuntime(deploymentUrl, user, password);
        KieSession ksession = engine.getKieSession();
        ProcessInstance processInstance = ksession.startProcess(HUMAN_TASK_PROCESS_ID);
        assertNotNull("Null ProcessInstance!", processInstance);
        long procInstId = processInstance.getId();

        logger.debug("Started process instance: " + processInstance + " " + procInstId);

        TaskService taskService = engine.getTaskService();
        List<TaskSummary> tasks = taskService.getTasksAssignedAsPotentialOwner(taskUserId, "en-UK");
        long taskId = findTaskId(procInstId, tasks);

        logger.debug("Found task " + taskId);
        Task task = taskService.getTaskById(taskId);
        logger.debug("Got task " + taskId + ": " + task);
        taskService.start(taskId, taskUserId);
        taskService.complete(taskId, taskUserId, null);

        logger.debug("Now expecting failure");
        try {
            taskService.complete(taskId, taskUserId, null);
            fail("Should not be able to complete task " + taskId + " a second time.");
        } catch (Throwable t) {
            logger.info("The above exception was an expected part of the test.");
            // do nothing
        }

        List<Status> statuses = new ArrayList<Status>();
        statuses.add(Status.Reserved);
        List<TaskSummary> taskIds = taskService.getTasksByStatusByProcessInstanceId(procInstId, statuses, "en-UK");
        assertEquals("Expected 2 tasks.", 2, taskIds.size());
    }

    protected long findTaskId(Long procInstId, List<TaskSummary> taskSumList) {
        long taskId = -1;
        TaskSummary task = findTaskSummary(procInstId, taskSumList);
        if (task != null) {
            taskId = task.getId();
        }
        assertNotEquals("Could not determine taskId!", -1, taskId);
        return taskId;
    }

    protected TaskSummary findTaskSummary(Long procInstId, List<TaskSummary> taskSumList) {
        for (TaskSummary task : taskSumList) {
            if (procInstId.equals(task.getProcessInstanceId())) {
                return task;
            }
        }
        fail("Unable to find task summary for process instance " + procInstId);
        return null;
    }

    @Test
    public void testRestRemoteApiExtraJaxbClasses() throws Exception {
        String user = MARY_USER;
        String password = MARY_PASSWORD;

        // Remote API setup
        RemoteRuntimeEngine engine = getRemoteRuntime(deploymentUrl, user, password);
        // test

        /**
         * MyType
         */
        testParamSerialization(engine, new MyType("variable", 29));

        /**
         * Float
         */
        testParamSerialization(engine, new Float(23.01));

        /**
         * Float []
         */
        testParamSerialization(engine, new Float[]{39.391f});
    }

    private void testParamSerialization(RemoteRuntimeEngine engine, Object param) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("myobject", param);
        KieSession ksession = engine.getKieSession();
        ProcessInstance procInst = ksession.startProcess(OBJECT_VARIABLE_PROCESS_ID, parameters);
        assertNotNull("No process instance returned!", procInst);
        long procInstId = procInst.getId();

        /**
         * Check that MyType was correctly deserialized on server side
         */
        List<VariableInstanceLog> varLogList = (List<VariableInstanceLog>) engine.getAuditLogService().findVariableInstancesByName("type", false);
        VariableInstanceLog thisProcInstVarLog = null;
        for (VariableInstanceLog varLog : varLogList) {
            if (varLog.getProcessInstanceId() == procInstId) {
                thisProcInstVarLog = varLog;
            }
        }
        assertNotNull("No VariableInstanceLog found!", thisProcInstVarLog);
        assertEquals("type", thisProcInstVarLog.getVariableId());
        assertEquals("De/serialization of Kjar type did not work.", param.getClass().getName(), thisProcInstVarLog.getValue());

        // Double check for BZ-1085267
        varLogList = (List<VariableInstanceLog>) engine.getAuditLogService().findVariableInstances(procInstId, "type");
        assertNotNull("No variable log list retrieved!", varLogList);
        assertTrue("Variable log list is empty!", varLogList.size() > 0);
    }

    @Test
    public void testRestRemoteApiRuleTaskProcess() throws Exception {
        printTestName();
        // Remote API setup
        RemoteRuntimeEngine runtimeEngine = getRemoteRuntime(deploymentUrl, MARY_USER, MARY_PASSWORD);

        KieSession ksession = runtimeEngine.getKieSession();
        AuditService auditService = runtimeEngine.getAuditLogService();

        // Setup facts
        Person person = new Person("guest", "Dluhoslav Chudobny");
        person.setAge(25); // >= 18
        Request request = new Request("1");
        request.setPersonId("guest");
        request.setAmount(500); // < 1000

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("request", request);
        params.put("person", person);

        // Start process
        ProcessInstance pi = ksession.startProcess(RULE_TASK_PROCESS_ID, params);
        assertNotNull("No Process instance returned!", pi);
        ksession.fireAllRules();

        // Check
        // assertEquals("Poor customer", ((Request)ksession.getObject(factHandle)).getInvalidReason());
        assertNull(ksession.getProcessInstance(pi.getId()));

        List<VariableInstanceLog> varLogs = (List<VariableInstanceLog>) auditService.findVariableInstancesByName("requestReason", false);
        for (VariableInstanceLog varLog : varLogs) {
            if (varLog.getProcessInstanceId() == pi.getId()) {
                assertEquals("Poor customer", varLog.getValue());
            }
        }
    }

    @Test
    public void testRestUrlsGroupAssignmentProcess() throws Exception {
        RestRequestHelper maryReqHelper = RestRequestHelper.newInstance(deploymentUrl, MARY_USER, MARY_PASSWORD);
        RestRequestHelper johnReqHelper = RestRequestHelper.newInstance(deploymentUrl, JOHN_USER, JOHN_PASSWORD);

        ClientRequest httpRequest = maryReqHelper.createRequest(
                "runtime/" + deploymentId + "/process/" + GROUP_ASSSIGNMENT_PROCESS_ID + "/start");
        JaxbProcessInstanceResponse procInstResp = post(httpRequest, mediaType, 200, JaxbProcessInstanceResponse.class);
        assertEquals(ProcessInstance.STATE_ACTIVE, procInstResp.getState());
        long procInstId = procInstResp.getId();

        // assert the task
        TaskSummary taskSummary = getTaskSummary(maryReqHelper, procInstId, Status.Ready);
        long taskId = taskSummary.getId();
        assertNull(taskSummary.getActualOwner());
        assertTrue(taskSummary.getPotentialOwners().isEmpty());
        assertEquals("Task 1", taskSummary.getName());

        // complete 'Task 1' as mary
        httpRequest = maryReqHelper.createRequest("task/" + taskId + "/claim");
        post(httpRequest, mediaType, 200);

        httpRequest = maryReqHelper.createRequest("task/" + taskId + "/start");
        post(httpRequest, mediaType, 200);
        httpRequest = maryReqHelper.createRequest("task/" + taskId + "/complete");
        post(httpRequest, mediaType, 200);

        // now make sure that the next task has been assigned to the
        // correct person. it should be mary.
        taskSummary = getTaskSummary(maryReqHelper, procInstId, Status.Reserved);
        assertEquals("Task 2", taskSummary.getName());
        assertEquals(MARY_USER, taskSummary.getActualOwner().getId());
        taskId = taskSummary.getId();

        // complete 'Task 2' as john
        httpRequest = maryReqHelper.createRequest("task/" + taskId + "/release");
        post(httpRequest, mediaType, 200);
        httpRequest = johnReqHelper.createRequest("task/" + taskId + "/start");
        post(httpRequest, mediaType, 200);
        httpRequest = johnReqHelper.createRequest("task/" + taskId + "/complete");
        post(httpRequest, mediaType, 200);

        // now make sure that the next task has been assigned to the
        // correct person. it should be john.
        taskSummary = getTaskSummary(johnReqHelper, procInstId, Status.Reserved);
        assertEquals("Task 3", taskSummary.getName());
        assertEquals(JOHN_USER, taskSummary.getActualOwner().getId());
        taskId = taskSummary.getId();

        // complete 'Task 3' as john
        httpRequest = johnReqHelper.createRequest("task/" + taskId + "/start");
        post(httpRequest, mediaType, 200);
        httpRequest = johnReqHelper.createRequest("task/" + taskId + "/complete");
        post(httpRequest, mediaType, 200);

        // assert process finished
        httpRequest = maryReqHelper.createRequest("history/instance/" + procInstId);
        JaxbProcessInstanceLog jaxbProcInstLog = get(httpRequest, mediaType, JaxbProcessInstanceLog.class);
        ProcessInstanceLog procInstLog = jaxbProcInstLog.getResult();
        assertEquals("Process instance has not completed!", ProcessInstance.STATE_COMPLETED, procInstLog.getStatus().intValue());
    }

    private TaskSummary getTaskSummary(RestRequestHelper requestHelper, long processInstanceId, Status status) throws Exception {
        ClientRequest httpRequest = requestHelper.createRequest(
                "task/query?processInstanceId=" + processInstanceId + "&status=" + status.toString());
        JaxbTaskSummaryListResponse taskSumListResp = get(httpRequest, mediaType, JaxbTaskSummaryListResponse.class);
        List<TaskSummary> taskSumList = taskSumListResp.getResult();
        assertEquals(1, taskSumList.size());
        return taskSumList.get(0);
    }


    private String getConnectionContent(Object content) throws Exception {
        InputStreamReader in = new InputStreamReader((InputStream) content);
        BufferedReader buff = new BufferedReader(in);
        StringBuffer text = new StringBuffer();
        String line = buff.readLine();
        while (line != null) {
            text.append(line);
            line = buff.readLine();
        }
        return text.toString();
    }

}
