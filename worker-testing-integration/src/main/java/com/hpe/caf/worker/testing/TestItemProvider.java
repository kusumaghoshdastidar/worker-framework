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

/**
 * Represents the contract for a class that provides {@link TestItem} instances describing execution and validation of tests.
 */
public interface TestItemProvider
{
    /**
     * Gets items which are then used to create messages for worker-under-test and validate produced result(s).
     *
     * @return the items
     * @throws Exception the exception
     */
    Collection<TestItem> getItems() throws Exception;
}
