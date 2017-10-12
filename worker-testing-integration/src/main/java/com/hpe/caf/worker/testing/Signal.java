/*
 * Copyright 2015-2017 EntIT Software LLC, a Micro Focus company.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hpe.caf.worker.testing;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by ploch on 16/11/2015.
 */
public class Signal
{
    private TestResult testResult;

    private static class MonitorObject
    {
    }

    MonitorObject myMonitorObject = new MonitorObject();
    boolean wasSignalled = false;

    public TestResult doWait()
    {
        synchronized (myMonitorObject) {
            while (!wasSignalled) {
                try {
                    myMonitorObject.wait();
                } catch (InterruptedException e) {
                    return TestResult.createFailed(e.getMessage(), new HashSet<>());
                }
            }
            wasSignalled = false;
            return testResult;
        }
    }

    public void doNotify(TestResult testResult)
    {
        synchronized (myMonitorObject) {
            if (this.testResult == null) {
                this.testResult = testResult;
            }
            wasSignalled = true;
            myMonitorObject.notify();
        }
    }
}