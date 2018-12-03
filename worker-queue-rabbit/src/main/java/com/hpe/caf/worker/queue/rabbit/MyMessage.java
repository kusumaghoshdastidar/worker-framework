package com.hpe.caf.worker.queue.rabbit;

public final class MyMessage
{
    private final String messageId;

    public MyMessage(final String messageId)
    {
        this.messageId = messageId;
    }
}
