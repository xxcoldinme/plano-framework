package com.github.itzstonlex.planoframework.test;

import com.github.itzstonlex.planoframework.PlanoCalendar;
import com.github.itzstonlex.planoframework.factory.PlanoCalendars;
import com.github.itzstonlex.planoframework.PlanoScheduler;
import com.github.itzstonlex.planoframework.PlanoTask;
import com.github.itzstonlex.planoframework.TaskPlan;
import com.github.itzstonlex.planoframework.param.TaskParams;
import com.github.itzstonlex.planoframework.param.cache.TaskParamCacheBuilder;
import com.github.itzstonlex.planoframework.task.process.TaskProcess;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;

public class PlanoSimpleTest {

  private PlanoScheduler scheduler;

  private TaskPlan plan;

  @Before
  public void setUp() {
    PlanoCalendar calendar = PlanoCalendars.createSingleThreadCalendar();

    scheduler = calendar.getScheduler();

    plan = scheduler.configurePlan(
        TaskParamCacheBuilder.create()
            .set(TaskParams.TASK_TIME_UNIT, TimeUnit.SECONDS)
            .set(TaskParams.TASK_DELAY, 3L)
            .set(TaskParams.INTERRUPT_ON_CANCEL, false)
            .build());
  }

  @Test
  public void testSchedule() {
    PlanoTask<?> task = scheduler.schedule(plan, new GreetingScheduledTask());
    task.awaitTermination();
  }

  private static class GreetingScheduledTask implements TaskProcess {

    @Override
    public void processAction() {
      System.out.println("Hello world!");
    }
  }
}
