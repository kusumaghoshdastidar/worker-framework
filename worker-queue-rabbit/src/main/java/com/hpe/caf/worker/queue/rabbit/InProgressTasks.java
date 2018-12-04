package com.hpe.caf.worker.queue.rabbit;

import com.hpe.caf.api.worker.InvalidTaskException;
import com.hpe.caf.api.worker.TaskCallback;
import com.hpe.caf.api.worker.TaskRejectedException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class InProgressTasks
{
    private static final Logger LOG = LoggerFactory.getLogger(InProgressTasks.class);

    private final Map<String, MyTask> tasks;

    public InProgressTasks()
    {
        this.tasks = new ConcurrentHashMap<>();
    }

    /**
     * Causes the internal task map to be automatically updated as new tasks are passed to the worker.
     *
     * @param callback the worker's callback interface
     * @return a wrapped callback interface that will update the internal task list and also pass through to the worker
     */
    public TaskCallback hookTaskCallback(final TaskCallback callback)
    {
        return new TaskCallback()
        {
            @Override
            public void registerNewTask(final String taskId, final byte[] taskData, final Map<String, Object> headers)
                throws TaskRejectedException, InvalidTaskException
            {
                if (tasks.putIfAbsent(taskId, new MyTask(taskId)) != null) {
                    throw new RuntimeException("Task Identifier already registered!");
                }

                callback.registerNewTask(taskId, taskData, headers);
            }

            @Override
            public void abortTasks()
            {
                callback.abortTasks();
                tasks.clear();
            }
        };
    }

    public MyTask retrieve(final String taskId, final boolean remove)
    {
        final MyTask task = remove
            ? tasks.remove(taskId)
            : tasks.get(taskId);

        if (task == null) {
            throw new RuntimeException("Task Identifier not recognised!");
        }

        return task;
    }

    public void remove(final String taskId)
    {
        final MyTask task = tasks.remove(taskId);

        if (task == null) {
            LOG.warn("Ignoring request to remove task since it wasn't found: {}", taskId);
        }
    }
}
