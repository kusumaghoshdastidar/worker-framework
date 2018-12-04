package com.hpe.caf.api.worker;

public interface ManagedWorkerQueue2
{
    /**
     * Open queues to start accepting tasks and results.
     *
     * @param callback the callback to use when registering or aborting tasks
     * @throws QueueException if the queue cannot be started
     */
    void start(TaskCallback2 callback)  // an alternate interface that passes objects to allow better callback of the WorkerQ
        throws QueueException;
}
