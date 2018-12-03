package com.hpe.caf.worker.queue.rabbit;

import com.hpe.caf.util.rabbitmq.Delivery;
import com.hpe.caf.util.rabbitmq.QueueConsumer;

public class ProxyQueueConsumer implements QueueConsumer
{
    private final QueueConsumer queueConsumer;

    public ProxyQueueConsumer(final QueueConsumer queueConsumer)
    {
        this.queueConsumer = queueConsumer;
    }

    @Override
    public void processDelivery(final Delivery delivery)
    {
        queueConsumer.processDelivery(delivery);
    }

    @Override
    public void processAck(final long tag)
    {
        queueConsumer.processAck(tag);
    }

    @Override
    public void processReject(final long tag)
    {
        queueConsumer.processReject(tag);
    }

    @Override
    public void processDrop(final long tag)
    {
        queueConsumer.processDrop(tag);
    }
}
