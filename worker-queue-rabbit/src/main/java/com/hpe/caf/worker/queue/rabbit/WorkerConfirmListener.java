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

import com.hpe.caf.util.rabbitmq.ConsumerAckEvent;
import com.hpe.caf.util.rabbitmq.ConsumerRejectEvent;
import com.hpe.caf.util.rabbitmq.Event;
import com.hpe.caf.util.rabbitmq.QueueConsumer;
import com.rabbitmq.client.ConfirmListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Ack incoming task messages when the outgoing response has been confirmed by the RabbitMQ broker.
 */
class WorkerConfirmListener implements ConfirmListener
{
    private static class MessageInfo
    {
        public final long requestMsgId;
        public final boolean isFinalResponse;

        public MessageInfo(final long requestMsgId, final boolean isFinalResponse)
        {
            this.requestMsgId = requestMsgId;
            this.isFinalResponse = isFinalResponse;
        }
    }

    private final SortedMap<Long, MessageInfo> confirmMap = Collections.synchronizedSortedMap(new TreeMap<>());
    private final BlockingQueue<Event<QueueConsumer>> consumerEvents;
    private static final Logger LOG = LoggerFactory.getLogger(WorkerConfirmListener.class);

    WorkerConfirmListener(BlockingQueue<Event<QueueConsumer>> events)
    {
        this.consumerEvents = Objects.requireNonNull(events);
    }

    /**
     * Tell the listener to keep track of a published response and its associated input task message, to ack or reject the input task
     * message as appropriate when RabbitMQ calls back.
     *
     * @param publishSequence the published sequence ID of the Worker response message
     * @param ackId the incoming task message ID to ack when the published response is confirmed
     * @param isFinalResponse true if the message is the final response to the {@code ackId} message
     */
    public void registerResponseSequence(long publishSequence, long ackId, boolean isFinalResponse)
    {
        final MessageInfo messageInfo = new MessageInfo(ackId, isFinalResponse);

        if (confirmMap.putIfAbsent(publishSequence, messageInfo) != null) {
            throw new IllegalStateException("Sequence id " + publishSequence + " already present in confirmations map");
        }

        LOG.debug("Listening for confirmation of publish sequence {} (ack message: {}, is final response: {})",
                  publishSequence, ackId, isFinalResponse);

        confirmMap.put(publishSequence, messageInfo);
    }

    /**
     * Forget all currently monitored sequence numbers, for use when the RabbitMQ connection drops.
     */
    public void clearConfirmations()
    {
        LOG.info("Clearing confirmations map");
        confirmMap.clear();
    }

    @Override
    public void handleAck(long sequenceNo, boolean multiple)
        throws IOException
    {
        LOG.debug("RabbitMQ broker ACKed published sequence id {} (multiple: {})", sequenceNo, multiple);
        handle(sequenceNo, multiple, ConsumerAckEvent::new);
    }

    @Override
    public void handleNack(long sequenceNo, boolean multiple)
        throws IOException
    {
        LOG.warn("RabbitMQ broker NACKed published sequence id {} (multiple: {})", sequenceNo, multiple);
        handle(sequenceNo, multiple, ConsumerRejectEvent::new);
    }

    private void handle(long sequenceNo, boolean multiple, Function<Long, Event<QueueConsumer>> eventSource)
    {
        if (multiple) {
            final Map<Long, MessageInfo> ackMap = confirmMap.headMap(sequenceNo + 1);
            synchronized (confirmMap) {
                consumerEvents.addAll(ackMap.values().stream()
                    .filter(messageInfo -> messageInfo.isFinalResponse)
                    .map(messageInfo -> eventSource.apply(messageInfo.requestMsgId))
                    .collect(Collectors.toList()));
            }
            ackMap.clear(); // clear all entries up to this (n)acked sequence number
        } else {
            final MessageInfo messageInfo = confirmMap.remove(sequenceNo);
            if (messageInfo == null) {
                LOG.error("RabbitMQ broker sent confirm for sequence number {}, which is not registered", sequenceNo);
                throw new IllegalStateException("Sequence number " + sequenceNo + " not found in WorkerConfirmListener");
            } else if (messageInfo.isFinalResponse) {
                consumerEvents.add(eventSource.apply(messageInfo.requestMsgId));
            }
        }
    }
}
