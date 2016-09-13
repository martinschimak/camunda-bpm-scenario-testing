package org.example.camunda.bpm.scenario;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.scenario.ProcessScenario;
import org.junit.Rule;
import org.junit.Test;

import static org.camunda.bpm.scenario.Scenario.*;
import static org.mockito.Mockito.*;

@Deployment(resources = "ReadmeProcess.bpmn")
public class ReadmeProcessTest {

  @Rule
  public ProcessEngineRule rule = new ProcessEngineRule();

  private ProcessScenario process = mock(ProcessScenario.class);

  @Test
  public void testHappyPath() {
    when(process.waitsAtUserTask("CompleteWork")).thenReturn((task) ->
        task.complete()
    );
    run(process).startByKey("ReadmeProcess").execute();
    verify(process).hasFinished("WorkFinished");
  }

  @Test
  public void testSlowPath() {
    when(process.waitsAtUserTask("CompleteWork")).thenReturn((task) ->
        task.defer("P2DT12H", () -> task.complete())
    );
    when(process.waitsAtUserTask("RemindColleague")).thenReturn((task) ->
        task.complete()
    );
    run(process).startByKey("ReadmeProcess").execute();
    verify(process).hasFinished("WorkFinished");
    verify(process, times(2)).hasFinished("ColleagueReminded");
  }

}
