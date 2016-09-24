package org.example.camunda.bpm.scenario;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.assertions.ProcessEngineAssertions;
import org.camunda.bpm.extension.process_test_coverage.junit.rules.TestCoverageProcessEngineRuleBuilder;
import org.camunda.bpm.scenario.ProcessScenario;
import org.camunda.bpm.scenario.Scenario;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;
import static org.camunda.bpm.scenario.Scenario.*;
import static org.mockito.Mockito.*;

@Deployment(resources = "CommunityDay2016Process.bpmn")
public class CommunityDay2016Process {

  public static final String COMMUNITY_DAY_2016 = "CommunityDay2016Process";

  @Rule
  @ClassRule
  public static ProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().assertClassCoverageAtLeast(1.0).build();

  public ProcessScenario process = mock(ProcessScenario.class);

  @Before
  public void defineDefaultActions() {
    when(process.waitsAtUserTask("CompleteWork")).thenReturn((task) -> {
      task.complete();
    });
    when(process.waitsAtUserTask("RemindColleague")).thenReturn((task) -> {
      assertThat(task).hasCandidateGroup("TheWaitingColleagues"); // 'Task' object, use classic "assert" here
      task.complete(withVariables("comment", "Oh please!")); // Delegate Task has a few convenience methods
    });
  }

  @Test
  public void testHappyPath() {
    Scenario scenario = run(process).startByKey(COMMUNITY_DAY_2016).execute(); // this is now "scenario", just execute it
    verify(process).hasFinished("WorkFinished");      // this is plain mockito, (you could use assert here, too)
    ProcessEngineAssertions.assertThat(scenario.instance(process)).hasPassed("WorkFinished"); // does the same as the line before by means of assertions.
    verify(process, never()).hasStarted("RemindColleague");
  }

  @Test
  public void testSlowPath() {
    when(process.waitsAtUserTask("CompleteWork")).thenReturn((task) ->
        task.defer("P2DT12H", () -> task.complete())
    );
    run(process).startBy(() -> runtimeService().startProcessInstanceByKey(COMMUNITY_DAY_2016)).execute();
    verify(process).hasFinished("WorkFinished");
    verify(process, times(2)).hasFinished("RemindColleague");
  }

}
