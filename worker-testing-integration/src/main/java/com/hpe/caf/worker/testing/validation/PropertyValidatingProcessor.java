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
package com.hpe.caf.worker.testing.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.hpe.caf.api.worker.TaskMessage;
import com.hpe.caf.worker.testing.AbstractResultProcessor;
import com.hpe.caf.worker.testing.TestConfiguration;
import com.hpe.caf.worker.testing.TestItem;
import com.hpe.caf.worker.testing.WorkerServices;
import com.hpe.caf.worker.testing.configuration.ValidationSettings;

import java.util.Map;

/**
 * The {@code PropertyValidatingProcessor} class. Processor validating property maps - objects converted to maps of property names and
 * values. Servers as an entry point for result validation and creates actual validators ({@link PropertyValidator} implementations).
 * depending on property type.
 *
 * @param <TResult> the worker result type parameter
 * @param <TInput> the worker test input type parameter
 * @param <TExpected> the worker test expectation type parameter
 */
public abstract class PropertyValidatingProcessor<TResult, TInput, TExpected> extends AbstractResultProcessor<TResult, TInput, TExpected>
{
    private final ValidatorFactory validatorFactory;

    /**
     * Instantiates a new Property validating processor.
     *
     * @param testConfiguration the test configuration
     * @param workerServices the worker services
     * @param validationSettings the validation settings
     */
    public PropertyValidatingProcessor(TestConfiguration<?, TResult, TInput, TExpected> testConfiguration, WorkerServices workerServices, ValidationSettings validationSettings)
    {
        super(workerServices.getCodec(), testConfiguration.getWorkerResultClass());
        this.validatorFactory = new ValidatorFactory(validationSettings, workerServices.getDataStore(), workerServices.getCodec(), testConfiguration);
    }

    /**
     * Validates worker result. This method acquires a map used for validation from test item expectation and converts validated result
     * into another map that will be validated. {@link ValidatorFactory} creates a concrete {@link PropertyValidator} which then validates
     * the result using expectation.
     *
     * @param testItem the test item which contains test input and expectation
     * @param message the task message retrieved from a queue
     * @param result the worker result deserialized from message body
     * @return the boolean indicating whether validation succeeded.
     * @throws Exception
     */
    @Override
    protected boolean processWorkerResult(final TestItem<TInput, TExpected> testItem, final TaskMessage message, final TResult result) 
        throws Exception
    {

        final Map<String, Object> expectation = getExpectationMap(testItem, message, result);
        if (expectation == null) {
            System.err.println("Could not locate result in pre-defined testcase, item tag '" + testItem.getTag() + "'. Message id: '" + message.getTaskId() + "'. ");
            return false;
        }

        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new GuavaModule());

        final Object validatedObject = getValidatedObject(testItem, message, result);

        final PropertyMap expectationPropertyMap = mapper.readValue(mapper.writeValueAsString(expectation), PropertyMap.class);

        final PropertyMap propertyMap = mapper.readValue(mapper.writeValueAsString(validatedObject), PropertyMap.class);

        final PropertyValidator validator = validatorFactory.createRootValidator();

        validator.validate("Root", propertyMap, expectationPropertyMap);

        testItem.setCompleted(isCompleted(testItem, message, result));
        return true;
    }

    /**
     * Is completed boolean.
     *
     * @param testItem the test item
     * @param message the message
     * @param result the result
     * @return the boolean
     */
    protected abstract boolean isCompleted(TestItem<TInput, TExpected> testItem, TaskMessage message, TResult result);

    /**
     * Gets expectation map.
     *
     * @param testItem the test item
     * @param message the message
     * @param result the result
     * @return the expectation map
     */
    protected abstract Map<String, Object> getExpectationMap(TestItem<TInput, TExpected> testItem, TaskMessage message, TResult result);

    /**
     * Gets validated object.
     *
     * @param testItem the test item
     * @param message the message
     * @param result the result
     * @return the validated object
     */
    protected abstract Object getValidatedObject(TestItem<TInput, TExpected> testItem, TaskMessage message, TResult result);
}
