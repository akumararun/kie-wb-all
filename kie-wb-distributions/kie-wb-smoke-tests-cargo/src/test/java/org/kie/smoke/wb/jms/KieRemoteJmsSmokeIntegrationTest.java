package org.kie.smoke.wb.jms;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.audit.ProcessInstanceLog;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.TaskSummary;
import org.kie.services.client.api.RemoteJmsRuntimeEngineFactory;
import org.kie.services.client.api.builder.RemoteJmsRuntimeEngineBuilder;
import org.kie.services.client.api.command.RemoteRuntimeEngine;
import org.kie.smoke.wb.AbstractWorkbenchIntegrationTest;
import org.kie.smoke.wb.category.JMSSmoke;
import org.kie.smoke.wb.category.KieWbSmoke;

import javax.jms.Queue;
import javax.naming.InitialContext;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.kie.smoke.wb.util.TestConstants.EVALUTAION_PROCESS_ID;
import static org.kie.smoke.wb.util.TestConstants.HUMAN_TASK_PROCESS_ID;
import static org.kie.smoke.wb.util.TestConstants.JOHN_PASSWORD;
import static org.kie.smoke.wb.util.TestConstants.JOHN_USER;
import static org.kie.smoke.wb.util.TestConstants.KRIS_PASSWORD;
import static org.kie.smoke.wb.util.TestConstants.KRIS_USER;
import static org.kie.smoke.wb.util.TestConstants.MARY_PASSWORD;
import static org.kie.smoke.wb.util.TestConstants.MARY_USER;

@Category({KieWbSmoke.class, JMSSmoke.class})
public class KieRemoteJmsSmokeIntegrationTest extends AbstractWorkbenchIntegrationTest {

//    private static final String KSESSION_QUEUE_NAME = "jms/queue/KIE.SESSION";
//    private static final String TASK_QUEUE_NAME = "jms/queue/KIE.TASK";
//    private static final String RESPONSE_QUEUE_NAME = "jms/queue/KIE.RESPONSE";
//
//    private final String deploymentId;
//    private final InitialContext remoteInitialContext;
//
//    public JmsSmokeIntegrationTestMethods(boolean useSSL) {
//        this.deploymentId = deploymentId;
//        this.remoteInitialContext
//                = RemoteJmsRuntimeEngineFactory.getRemoteJbossInitialContext("localhost", MARY_USER, MARY_PASSWORD);
//    }
//
//    // Helper methods ------------------------------------------------------------------------------------------------------------
//
//    /**
//     * Test to make sure that JMS commands received by the server are associated with an identity
//     * on the server side
//     *
//     * @param user     The username to use
//     * @param password The password associated with the given user name
//     */
//    public void remoteApiInitiatorIdentityTest(String user, String password) {
//        // setup
//        RemoteJmsRuntimeEngineFactory remoteSessionFactory
//                = RemoteJmsRuntimeEngineFactory.newBuilder()
//                .addDeploymentId(deploymentId)
//                .addRemoteInitialContext(remoteInitialContext)
//                .addUserName(user)
//                .addPassword(password)
//                .buildFactory();
//
//        RemoteRuntimeEngine runtimeEngine = remoteSessionFactory.newRuntimeEngine();
//
//        KieSession ksession = runtimeEngine.getKieSession();
//        ProcessInstance procInst = ksession.startProcess(HUMAN_TASK_PROCESS_ID);
//        assertNotNull("Null process instance!", procInst);
//        long procId = procInst.getId();
//
//        List<ProcessInstanceLog> procLogs = (List<ProcessInstanceLog>) runtimeEngine.getAuditLogService().findActiveProcessInstances(HUMAN_TASK_PROCESS_ID);
//        boolean procLogFound = false;
//        for (ProcessInstanceLog log : procLogs) {
//            if (log == null) {
//                continue;
//            }
//            if (log.getProcessInstanceId() == procId) {
//                procLogFound = true;
//                assertNotEquals("The identity should not be unknown!", "unknown", log.getIdentity());
//            }
//        }
//        assertTrue("Process instance log could not be found.", procLogFound);
//    }
//
//    public void remoteApiHumanTaskGroupIdTest(URL deploymentUrl) {
//        RemoteJmsRuntimeEngineBuilder jreBuilder
//                = RemoteJmsRuntimeEngineFactory.newBuilder()
//                .addDeploymentId(deploymentId)
//                .useSsl(true)
//                .addHostName("localhost")
//                .addJmsConnectorPort(5446)
//                .addKeystoreLocation("ssl/client_keystore.jks")
//                .addKeystorePassword("CLIENT_KEYSTORE_PASSWORD")
//                .useKeystoreAsTruststore();
//
//        try {
//            jreBuilder
//                    .addTaskServiceQueue((Queue) remoteInitialContext.lookup(TASK_QUEUE_NAME))
//                    .addKieSessionQueue((Queue) remoteInitialContext.lookup(KSESSION_QUEUE_NAME))
//                    .addResponseQueue((Queue) remoteInitialContext.lookup(RESPONSE_QUEUE_NAME));
//        } catch (Exception e) {
//            String msg = "Unable to lookup queue instances: " + e.getMessage();
//            logger.error(msg, e);
//            fail(msg);
//        }
//
//        RemoteRuntimeEngine krisRuntimeEngine = jreBuilder
//                .addUserName(KRIS_USER)
//                .addPassword(KRIS_PASSWORD)
//                .build();
//
//        RemoteRuntimeEngine maryRuntimeEngine = jreBuilder
//                .addUserName(MARY_USER)
//                .addPassword(MARY_PASSWORD)
//                .build();
//
//        RemoteRuntimeEngine johnRuntimeEngine = jreBuilder
//                .addUserName(JOHN_USER)
//                .addPassword(JOHN_PASSWORD)
//                .build();
//
//        runHumanTaskGroupIdTest(krisRuntimeEngine, johnRuntimeEngine, maryRuntimeEngine);
//    }
//
//    public void runHumanTaskGroupIdTest(RemoteRuntimeEngine krisRuntimeEngine, RemoteRuntimeEngine johnRuntimeEngine,
//                                        RemoteRuntimeEngine maryRuntimeEngine) {
//
//        KieSession ksession = krisRuntimeEngine.getKieSession();
//
//        // start a new process instance
//        Map<String, Object> params = new HashMap<String, Object>();
//        params.put("employee", "krisv");
//        params.put("reason", "Yearly performance evaluation");
//        ProcessInstance processInstance = ksession.startProcess(EVALUTAION_PROCESS_ID, params);
//        assertNotNull("Null process instance!", processInstance);
//        long procInstId = processInstance.getId();
//
//        // complete Self Evaluation
//        {
//            String user = "krisv";
//            TaskService taskService = krisRuntimeEngine.getTaskService();
//            List<TaskSummary> tasks = taskService.getTasksAssignedAsPotentialOwner(user, "en-UK");
//            TaskSummary task = findTaskSummary(procInstId, tasks);
//            assertNotNull("Unable to find " + user + "'s task", task);
//            taskService.start(task.getId(), user);
//            Map<String, Object> results = new HashMap<String, Object>();
//            results.put("performance", "exceeding");
//            taskService.complete(task.getId(), user, results);
//        }
//
//        // john from HR
//        {
//            String user = "john";
//            TaskService taskService = johnRuntimeEngine.getTaskService();
//            List<TaskSummary> tasks = taskService.getTasksAssignedAsPotentialOwner(user, "en-UK");
//            TaskSummary task = findTaskSummary(procInstId, tasks);
//            assertNotNull("Unable to find " + user + "'s task", task);
//            taskService.start(task.getId(), user);
//            Map<String, Object> results = new HashMap<String, Object>();
//            results.put("performance", "acceptable");
//            taskService.complete(task.getId(), user, results);
//        }
//
//        // mary from PM
//        {
//            String user = "mary";
//            TaskService taskService = maryRuntimeEngine.getTaskService();
//            List<TaskSummary> tasks = taskService.getTasksAssignedAsPotentialOwner(user, "en-UK");
//            TaskSummary task = findTaskSummary(procInstId, tasks);
//            assertNotNull("Unable to find " + user + "'s task", task);
//            taskService.start(task.getId(), user);
//            Map<String, Object> results = new HashMap<String, Object>();
//            results.put("performance", "outstanding");
//            taskService.complete(task.getId(), user, results);
//        }
//
//        ProcessInstanceLog procInstLog = maryRuntimeEngine.getAuditLogService().findProcessInstance(procInstId);
//    }
//        @Test
//        public void testJmsRemoteApiStartProcessInstanceInitiator() throws Exception {
//            Assume.assumeTrue(jmsQueuesAvailable());
//            printTestName();
//            jmsTests.remoteApiInitiatorIdentityTest(MARY_USER, MARY_PASSWORD);
//        }
//
//        @Test
//        public void testJmsRemoteApiHumanTaskGroupId() throws Exception {
//            Assume.assumeTrue(jmsQueuesAvailable());
//            printTestName();
//
//            jmsTests.remoteApiHumanTaskGroupIdTest(deploymentUrl);
//        }
}
