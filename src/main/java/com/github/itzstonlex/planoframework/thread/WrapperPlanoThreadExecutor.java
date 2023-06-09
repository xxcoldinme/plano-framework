package com.github.itzstonlex.planoframework.thread;

import com.github.itzstonlex.planoframework.TaskPlan;
import com.github.itzstonlex.planoframework.exception.PlanoParamNotFoundException;
import com.github.itzstonlex.planoframework.param.TaskParamKey;
import com.github.itzstonlex.planoframework.param.TaskParams;
import com.github.itzstonlex.planoframework.task.WrapperScheduledFuture;
import com.github.itzstonlex.planoframework.task.process.TaskProcess;
import com.github.itzstonlex.planoframework.task.process.response.CompletableResponse;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class WrapperPlanoThreadExecutor {

  private final ScheduledExecutorService impl;

  @NotNull
  public final List<Runnable> shutdown() {
    return impl.shutdownNow();
  }

  @NotNull
  protected final <V> V getValidatedParameterValue(@NotNull TaskPlan plan, @NotNull TaskParamKey<V> key) {
    V value = plan.getParameter(key);
    if (value == null) {
      throw new PlanoParamNotFoundException(key);
    }

    return value;
  }

  @SuppressWarnings("unchecked")
  public WrapperScheduledFuture schedule(@NotNull TaskPlan plan, @NotNull TaskProcess process) {
    CompletableResponse<Object> response = new CompletableResponse<>();

    WrapperPlanoExecutorRunnable runnableImpl =
        new WrapperPlanoExecutorRunnable(plan, process, response);

    long delay = getValidatedParameterValue(plan, TaskParams.TASK_DELAY);
    TimeUnit unit = getValidatedParameterValue(plan, TaskParams.TASK_TIME_UNIT);

    ScheduledFuture<Object> scheduledFuture;

    if (plan.isRepeatable()) {
      long repeatDelay = getValidatedParameterValue(plan, TaskParams.TASK_REPEAT_DELAY);
      boolean repeatWaitProcessEnd = getValidatedParameterValue(plan, TaskParams.TASK_REPEAT_WAIT_PROCESS_END);

      if (repeatWaitProcessEnd) {
        scheduledFuture = (ScheduledFuture<Object>) impl.scheduleWithFixedDelay(runnableImpl, delay, repeatDelay, unit);
      } else {
        scheduledFuture = (ScheduledFuture<Object>) impl.scheduleAtFixedRate(runnableImpl, delay, repeatDelay, unit);
      }
    } else {
      scheduledFuture = (ScheduledFuture<Object>) impl.schedule(runnableImpl, delay, unit);
    }

    WrapperScheduledFuture wrapper = new WrapperScheduledFuture(scheduledFuture, response);
    runnableImpl.addActionAfterTaskProcess(wrapper::fireAfterProcesses);

    return wrapper;
  }

}
