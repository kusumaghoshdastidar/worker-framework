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
#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import com.hpe.caf.worker.testing.ContentFileTestExpectation;

/**
 * ${workerName}TestExpectation forms a component of the test item, and contains the expected ${workerName}Result, used to compare
 * with the actual worker result.
 */
public class ${workerName}TestExpectation  extends ContentFileTestExpectation {

    /**
     * ${workerName}Result read in from the yaml test case, used to validate the result of the worker is as expected.
     */
    private ${workerName}Result result;

    public ${workerName}TestExpectation() {
    }

    public ${workerName}Result getResult() {
        return result;
    }

    public void setResult(${workerName}Result result) {
        this.result = result;
    }
}
