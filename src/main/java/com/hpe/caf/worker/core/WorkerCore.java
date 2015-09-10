package com.hpe.caf.worker.core;


import com.hpe.caf.api.Codec;
import com.hpe.caf.api.CodecException;
import com.hpe.caf.api.ServicePath;
import com.hpe.caf.api.worker.NewTaskCallback;
import com.hpe.caf.api.worker.QueueException;
import com.hpe.caf.api.worker.TaskMessage;
import com.hpe.caf.api.worker.TaskStatus;
import com.hpe.caf.api.worker.WorkerException;
import com.hpe.caf.api.worker.WorkerFactory;
import com.hpe.caf.api.worker.WorkerQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * WorkerCore represents the main logic of the microservice worker. It is responsible for accepting
 * new tasks from a WorkerQueue, handing them off to a backend Worker and executing them upon a thread pool.
 * It will then accept a result from the Worker it executed and hand the TaskResult back to the WorkerQueue
 * for publishing.
 */
public class WorkerCore
{
    private final ThreadPoolExecutor threadPool;
    private final WorkerQueue workerQueue;
    private final WorkerStats stats = new WorkerStats();
    private final NewTaskCallback callback;
    private static final Logger LOG = LoggerFactory.getLogger(WorkerCore.class);


    public WorkerCore(final Codec codec, final ThreadPoolExecutor pool, final WorkerQueue queue, final WorkerFactory factory, final ServicePath path)
    {
        CompleteTaskCallback taskCallback =  new ApplicationTaskCallback(codec, queue, stats);
        this.threadPool = Objects.requireNonNull(pool);
        this.callback = new ApplicationQueueCallback(codec, stats, threadPool, new WorkerWrapperFactory(path, taskCallback, factory));
        this.workerQueue = Objects.requireNonNull(queue);
    }


    /**
     * Begin accepting tasks to process.
     * @throws QueueException if the queues cannot be started
     */
    public void start()
        throws QueueException
    {
        workerQueue.start(callback);
    }


    /**
     * Close the incoming queues so no more jobs are taken, signal the thread pool to shut down and wait
     * a while to allow any active work to complete, before shutting down the queue completely.
     */
    public void shutdown()
    {
        LOG.debug("Shutting down");
        workerQueue.shutdownIncoming();
        threadPool.shutdown();
        try {
            threadPool.awaitTermination(300_000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            LOG.warn("Shutdown interrupted", e);
            Thread.currentThread().interrupt();
        }
        workerQueue.shutdown();
    }


    /**
     * The current idle time for the worker. If there are any active threads, this is 0. Otherwise it is the
     * difference between the current time and completion time of the last completed task.
     * @return the current idle time in milliseconds
     */
    public long getCurrentIdleTime()
    {
        if ( threadPool.getActiveCount() == 0 ) {
            return System.currentTimeMillis() - stats.getLastTaskFinishedTime();
        } else {
            return 0;   // if we are working, then we are not idle
        }
    }


    public WorkerQueue getWorkerQueue()
    {
        return this.workerQueue;
    }


    /**
     * @return the current number of tasks accepted by the worker but are not in progress
     */
    public int getBacklogSize()
    {
        return threadPool.getQueue().size();
    }


    public WorkerStats getStats()
    {
        return this.stats;
    }


    /**
     * Called by the queue component to register a new task incoming.
     */
    private static class ApplicationQueueCallback implements NewTaskCallback
    {
        private final Codec codec;
        private final WorkerStats stats;
        private final ThreadPoolExecutor threadPool;
        private final WorkerWrapperFactory wrapperFactory;


        public ApplicationQueueCallback(final Codec codec, final WorkerStats stats, final ThreadPoolExecutor pool, final WorkerWrapperFactory factory)
        {
            this.codec = Objects.requireNonNull(codec);
            this.stats = Objects.requireNonNull(stats);
            this.threadPool = Objects.requireNonNull(pool);
            this.wrapperFactory = Objects.requireNonNull(factory);
        }


        /**
         * {@inheritDoc}
         *
         * Use the factory to get a new worker to handle the task, wrap this in a handler
         * and hand it off to the thread pool.
         */
        @Override
        public void registerNewTask(final String queueMsgId, final byte[] taskMessage)
            throws WorkerException
        {
            Objects.requireNonNull(queueMsgId);
            try {
                stats.incrementTasksReceived();
                TaskMessage tm = codec.deserialise(taskMessage, TaskMessage.class);
                LOG.debug("Received task {} (message id: {})", tm.getTaskId(), queueMsgId);
                execute(wrapperFactory.getWorkerWrapper(tm, queueMsgId));
            } catch (WorkerException e) {
                stats.incrementTasksRejected();
                throw e;
            } catch (CodecException e) {
                stats.incrementTasksRejected();
                throw new WorkerException("Queue data did not deserialise to a TaskMessage", e);
            }
        }


        /**
         * Pass off a runnable task to the backend, considering a hard upper bound to the internal backlog.
         * @param wrapper the new task to run
         * @throws WorkerException if no more tasks can be added to the internal backlog
         */
        private void execute(final Runnable wrapper)
            throws WorkerException
        {
            if ( threadPool.getQueue().size() < threadPool.getCorePoolSize() * 10 ) {
                threadPool.execute(wrapper);
            } else {
                throw new WorkerException("Maximum internal task backlog exceeded");
            }
        }
    }


    /**
     * Called by a WorkerWrapper to indicate a task was completed by a worker.
     */
    private static class ApplicationTaskCallback implements CompleteTaskCallback
    {
        private final Codec codec;
        private final WorkerQueue workerQueue;
        private final WorkerStats stats;


        public ApplicationTaskCallback(final Codec codec, final WorkerQueue queue, final WorkerStats stats)
        {
            this.codec = Objects.requireNonNull(codec);
            this.workerQueue = Objects.requireNonNull(queue);
            this.stats = Objects.requireNonNull(stats);
        }


        /**
         * {@inheritDoc}
         *
         * Hand off the serialised result from a worker with its status to the queue. If the result cannot
         * be serialised for any reason, we reject the task.
         */
        @Override
        public void complete(final String queueMsgId, final String queue, final TaskMessage responseMessage)
        {
            Objects.requireNonNull(queueMsgId);
            Objects.requireNonNull(queue);
            Objects.requireNonNull(responseMessage);
            LOG.debug("Task {} complete (message id: {})", responseMessage.getTaskId(), queueMsgId);
            try {
                workerQueue.publish(queueMsgId, codec.serialise(responseMessage), queue);
                stats.updatedLastTaskFinishedTime();
                if ( TaskStatus.isSuccessfulResponse(responseMessage.getTaskStatus()) ) {
                    stats.incrementTasksSucceeded();
                } else {
                    stats.incrementTasksFailed();
                }
            } catch (CodecException | QueueException e) {
                LOG.error("Cannot publish data for task {} (message id: {}), rejecting", responseMessage.getTaskId(), queueMsgId, e);
                workerQueue.rejectTask(queueMsgId);
                stats.incrementTasksRejected();
            }
        }
    }
}
