package com.hpe.caf.worker.queue.rabbit;

public final class MyTask
{
    private final String taskId;
    private final Object responseCountLock;
    private volatile int responseCount;
    private volatile boolean isResponseCountFinal;
    private final Object acknowledgementCountLock;
    private volatile int acknowledgementCount;

    public MyTask(final String taskId)
    {
        this.taskId = taskId;
        this.responseCountLock = new Object();
        this.responseCount = 0;
        this.isResponseCountFinal = false;
        this.acknowledgementCountLock = new Object();
        this.acknowledgementCount = 0;
    }

    public void incrementResponseCount(final boolean isFinalResponse)
    {
        synchronized (responseCountLock) {
            if (isResponseCountFinal) {
                throw new RuntimeException("Final response already set!");
            }

            responseCount++;

            if (isFinalResponse) {
                isResponseCountFinal = true;
            }
        }
    }

    /**
     * Indicate that there are no more responses to come.
     *
     * @return true if all the responses have already been acknowledged
     */
    public boolean finalizeResponseCount()
    {
        synchronized (acknowledgementCountLock) {
            synchronized (responseCountLock) {
                isResponseCountFinal = true;
            }

            return responseCount == acknowledgementCount;
        }
    }

    /**
     * Increment the count of acknowledgements.
     *
     * @return true if this increment means that all responses have been acknowledged
     */
    public boolean incrementAcknowledgementCount()
    {
        synchronized (acknowledgementCountLock) {
            final int ackCount = ++acknowledgementCount;

            return isResponseCountFinal
                && (responseCount == ackCount);
        }
    }

    //public boolean areAllResponsesAcknowledged()
    //{
    //}
    private boolean isFinalResponseCountKnown()
    {
        return isResponseCountFinal;
    }

    private int getFinalResponseCount()
    {
        if (!isResponseCountFinal) {
            throw new RuntimeException("Final response count not yet known!");
        }

        return responseCount;
    }
}
