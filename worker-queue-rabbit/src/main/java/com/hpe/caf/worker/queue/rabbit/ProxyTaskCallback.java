package com.hpe.caf.worker.queue.rabbit;

import com.hpe.caf.api.worker.InvalidTaskException;
import com.hpe.caf.api.worker.TaskCallback;
import com.hpe.caf.api.worker.TaskRejectedException;
import java.util.Map;

public class ProxyTaskCallback implements TaskCallback
{
    private final TaskCallback taskCallback;

    public ProxyTaskCallback(final TaskCallback taskCallback)
    {
        this.taskCallback = taskCallback;
    }

    @Override
    public void registerNewTask(final String taskId, final byte[] taskData, final Map<String, Object> headers)
        throws TaskRejectedException, InvalidTaskException
    {
        taskCallback.registerNewTask(taskId, taskData, headers);
    }

    @Override
    public void abortTasks()
    {
        taskCallback.abortTasks();
    }
}
