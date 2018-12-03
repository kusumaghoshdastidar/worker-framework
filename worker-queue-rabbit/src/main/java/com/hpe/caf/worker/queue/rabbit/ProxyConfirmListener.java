package com.hpe.caf.worker.queue.rabbit;

import com.rabbitmq.client.ConfirmListener;
import java.io.IOException;

public class ProxyConfirmListener implements ConfirmListener
{
    private final ConfirmListener confirmListener;

    public ProxyConfirmListener(final ConfirmListener confirmListener)
    {
        this.confirmListener = confirmListener;
    }

    @Override
    public void handleAck(final long deliveryTag, final boolean multiple) throws IOException
    {
        confirmListener.handleAck(deliveryTag, multiple);
    }

    @Override
    public void handleNack(final long deliveryTag, final boolean multiple) throws IOException
    {
        confirmListener.handleNack(deliveryTag, multiple);
    }
}
