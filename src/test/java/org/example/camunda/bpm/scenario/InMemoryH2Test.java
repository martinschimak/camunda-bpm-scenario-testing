package org.example.camunda.bpm.scenario;

import org.apache.ibatis.logging.LogFactory;
import org.camunda.bpm.consulting.process_test_coverage.ProcessTestCoverage;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.scenario.ProcessScenario;
import org.camunda.bpm.scenario.Scenario;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;

/**
 * Test case starting an in-memory database-backed Process Engine.
 */
public class InMemoryH2Test {

  @Rule
  public ProcessEngineRule rule = new ProcessEngineRule();

  private static final String PROCESS_DEFINITION_KEY = "camunda-bpm-scenario-testing";

  static {
    LogFactory.useSlf4jLogging(); // MyBatis
  }

  @Before
  public void setup() {
    init(rule.getProcessEngine());
  }

  /**
   * Just tests if the process definition is deployable.
   */
  @Test
  @Deployment(resources = "process.bpmn")
  public void testParsingAndDeployment() {
    // nothing is done here, as we just want to check for exceptions during deployment
  }

  @Test
  @Deployment(resources = "process.bpmn")
  public void testHappyPath() {

    // GIVEN a process scenario... (this one just completes all user tasks)
    ProcessScenario process = Mockito.mock(ProcessScenario.class);
    Mockito.when(process.waitsAtUserTask(Mockito.anyString())).thenReturn((task) -> task.complete());

    // WHEN we run the scenario
    Scenario scenario = Scenario.run(process).startByKey("camunda-bpm-scenario-testing").execute();

    // THEN we assume to reach the end event
    Mockito.verify(process).hasFinished("EndEventProcessEnded");

    // You may want to use the bundled coverage to visualise that the process instance
	  ProcessTestCoverage.calculate(scenario.instance(process), rule.getProcessEngine());

  }

  @After
  public void calculateCoverageForAllTests() throws Exception {
    ProcessTestCoverage.calculate(rule.getProcessEngine());
  }  

}
