/*
 * Copyright 2015-2018 Micro Focus or one of its affiliates.
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

import java.util.Collection;
import java.util.Set;

/**
 * Created by ploch on 05/02/2016.
 */
public class TestsFailedException extends Exception
{
    private final Collection<TestCaseResult> results;

    public TestsFailedException(String message, Collection<TestCaseResult> results)
    {
        this(message, results, null);
    }

    public TestsFailedException(String message, Collection<TestCaseResult> results, Throwable cause)
    {
        super(message, cause);
        this.results = results;
    }

    /**
     * Getter for property 'results'.
     *
     * @return Value for property 'results'.
     */
    public Collection<TestCaseResult> getResults()
    {
        return results;
    }
}
