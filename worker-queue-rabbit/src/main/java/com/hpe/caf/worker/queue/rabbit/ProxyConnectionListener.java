package com.hpe.caf.worker.queue.rabbit;

import com.rabbitmq.client.Connection;
import net.jodah.lyra.event.ConnectionListener;

public class ProxyConnectionListener implements ConnectionListener
{
    private final ConnectionListener connectionListener;

    public ProxyConnectionListener(final ConnectionListener connectionListener)
    {
        this.connectionListener = connectionListener;
    }

    @Override
    public void onCreate(final Connection connection)
    {
        connectionListener.onCreate(connection);
    }

    @Override
    public void onCreateFailure(final Throwable failure)
    {
        connectionListener.onCreateFailure(failure);
    }

    @Override
    public void onRecoveryStarted(final Connection connection)
    {
        connectionListener.onRecoveryStarted(connection);
    }

    @Override
    public void onRecovery(final Connection connection)
    {
        connectionListener.onRecovery(connection);
    }

    @Override
    public void onRecoveryCompleted(final Connection connection)
    {
        connectionListener.onRecoveryCompleted(connection);
    }

    @Override
    public void onRecoveryFailure(final Connection connection, final Throwable failure)
    {
        connectionListener.onRecoveryFailure(connection, failure);
    }
}
