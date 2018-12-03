!not-ready-for-release!

#### Version Number
${version-number}

#### New Features
 - [SCMOD-4898](https://autjira.microfocus.com/browse/SCMOD-4898): Discard superfluous tracking messages  
    The `CAF_WORKER_DISABLE_ZERO_PROGRESS_REPORTING` environment variable can be set to `true` to cause zero-progress tracking messages not to be sent.

#### Bug Fixes
 - [SCMOD-5410](https://autjira.microfocus.com/browse/SCMOD-5410): Potential for task statuses not to be reported  
	An issue has been fixed whereby task messages may have been acknowledged prematurely.

	This fix has involved a change to the [WorkerQueue](https://github.com/WorkerFramework/worker-framework/blob/v3.1.0/worker-api/src/main/java/com/hpe/caf/api/worker/WorkerQueue.java) interface so **it is a breaking change if a custom non-framework queueing module is in use**.

#### Known Issues
