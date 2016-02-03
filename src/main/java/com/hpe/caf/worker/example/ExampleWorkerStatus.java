package com.hpe.caf.worker.example;

/**
 * Enumeration representing the status of the worker result.
 */
public enum ExampleWorkerStatus {

    /**
     * Worker processed task and was successful.
     */
    COMPLETED,

    /**
     * The source data could not be acquired from the DataStore.
     */
    SOURCE_FAILED,

    /**
     * The result could not be stored in the DataStore.
     */
    STORE_FAILED,

    /**
     * The input file could be read but the worker failed in an unexpected way.
     */
    WORKER_EXAMPLE_FAILED
}
