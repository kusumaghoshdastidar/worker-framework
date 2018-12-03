package com.hpe.caf.worker.queue.rabbit;

import com.hpe.caf.api.worker.InvalidTaskException;
import com.hpe.caf.api.worker.TaskCallback;
import com.hpe.caf.api.worker.TaskRejectedException;
import com.hpe.caf.util.rabbitmq.QueueConsumer;
import com.rabbitmq.client.ConfirmListener;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.jodah.lyra.event.ConnectionListener;
import net.jodah.lyra.event.DefaultConnectionListener;

public class UnnamedClass
{
    private final Map<String, MyMessage> inProgressTasks;

    public UnnamedClass()
    {
        this.inProgressTasks = new ConcurrentHashMap<>();
    }

    public ConfirmListener hookConfirmListener(final ConfirmListener listener)
    {
        return new MyConfirmListener(listener);
    }

    public ConnectionListener hookConnectionListener(final ConnectionListener listener)
    {
        return new MyConnectionListener(listener);
    }

    public ConnectionListener createConnectionListener()
    {
        return new MyOtherConnectionListener();
    }

    public TaskCallback hookTaskCallback(final TaskCallback callback)
    {
        return new MyTaskCallback(callback);
    }

    public QueueConsumer hookQueueConsumer(final QueueConsumer consumer)
    {
        return new MyQueueConsumer(consumer);
    }

    private class MyConfirmListener extends ProxyConfirmListener
    {
        public MyConfirmListener(final ConfirmListener confirmListener)
        {
            super(confirmListener);
        }

        @Override
        public void handleAck(final long deliveryTag, final boolean multiple) throws IOException
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void handleNack(final long deliveryTag, final boolean multiple) throws IOException
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    // More control - can go before or after in each case
    private class MyConnectionListener extends ProxyConnectionListener
    {
        public MyConnectionListener(final ConnectionListener connectionListener)
        {
            super(connectionListener);
        }
    }

    // Less control - better if we can get away with it
    private class MyOtherConnectionListener extends DefaultConnectionListener
    {
    }

    private class MyTaskCallback extends ProxyTaskCallback
    {
        public MyTaskCallback(final TaskCallback taskCallback)
        {
            super(taskCallback);
        }

        @Override
        public void registerNewTask(final String taskId, final byte[] taskData, final Map<String, Object> headers)
            throws TaskRejectedException, InvalidTaskException
        {
            if (inProgressTasks.putIfAbsent(taskId, new MyMessage(taskId)) != null) {
                throw new RuntimeException("Task Identifier already registered!");
            }

            super.registerNewTask(taskId, taskData, headers);
        }

        @Override
        public void abortTasks()
        {
            super.abortTasks();
            inProgressTasks.clear();
        }
    }

    private class MyQueueConsumer extends ProxyQueueConsumer
    {
        public MyQueueConsumer(final QueueConsumer queueConsumer)
        {
            super(queueConsumer);
        }
    }
}
