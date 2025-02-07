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
package com.hpe.caf.worker.queue.rabbit;

import com.hpe.caf.api.worker.MessagePriorityManager;
import com.hpe.caf.api.worker.TaskMessage;

import java.util.Objects;

/**
 * Implementation of MessagePriorityManager which increases a message priority on each response.
 */
public class IncreasingMessagePriorityManager implements MessagePriorityManager
{
    @Override
    public Integer getResponsePriority(final TaskMessage originalTaskMessage)
    {
        Objects.requireNonNull(originalTaskMessage);
        return originalTaskMessage.getPriority() == null ? 1 : originalTaskMessage.getPriority() + 1;
    }
}
