package com.hpe.caf.worker.testing.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hpe.caf.worker.testing.validation.PropertyMap;
import org.junit.Test;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Created by ploch on 05/12/2015.
 */
public class PropertyMapTest {

    public class TestClassSimple1 {

        private String stringProp1;
        private int intProp1;

        public TestClassSimple1() {
        }

        public TestClassSimple1(String stringProp1, int intProp1) {
            this.stringProp1 = stringProp1;
            this.intProp1 = intProp1;
        }

        /**
         * Getter for property 'stringProp1'.
         *
         * @return Value for property 'stringProp1'.
         */
        public String getStringProp1() {
            return stringProp1;
        }

        /**
         * Setter for property 'stringProp1'.
         *
         * @param stringProp1 Value to set for property 'stringProp1'.
         */
        public void setStringProp1(String stringProp1) {
            this.stringProp1 = stringProp1;
        }

        /**
         * Getter for property 'intProp1'.
         *
         * @return Value for property 'intProp1'.
         */
        public int getIntProp1() {
            return intProp1;
        }

        /**
         * Setter for property 'intProp1'.
         *
         * @param intProp1 Value to set for property 'intProp1'.
         */
        public void setIntProp1(int intProp1) {
            this.intProp1 = intProp1;
        }
    }

    public class TestClassSimple2 {
        private boolean boolProp1;
        private Date dateProp1;

        public TestClassSimple2() {
        }

        public TestClassSimple2(boolean boolProp1, Date dateProp1) {
            this.boolProp1 = boolProp1;
            this.dateProp1 = dateProp1;
        }

        /**
         * Getter for property 'boolProp1'.
         *
         * @return Value for property 'boolProp1'.
         */
        public boolean isBoolProp1() {
            return boolProp1;
        }

        /**
         * Setter for property 'boolProp1'.
         *
         * @param boolProp1 Value to set for property 'boolProp1'.
         */
        public void setBoolProp1(boolean boolProp1) {
            this.boolProp1 = boolProp1;
        }

        /**
         * Getter for property 'dateProp1'.
         *
         * @return Value for property 'dateProp1'.
         */
        public Date getDateProp1() {
            return dateProp1;
        }

        /**
         * Setter for property 'dateProp1'.
         *
         * @param dateProp1 Value to set for property 'dateProp1'.
         */
        public void setDateProp1(Date dateProp1) {
            this.dateProp1 = dateProp1;
        }
    }

    public class TestClassComplex1 {

        private TestClassSimple1 complexProp1;
        private TestClassSimple2 complexProp2;

        private String simpleProp1;

        public TestClassComplex1() {
        }

        public TestClassComplex1(TestClassSimple1 complexProp1, TestClassSimple2 complexProp2, String simpleProp1) {
            this.complexProp1 = complexProp1;
            this.complexProp2 = complexProp2;
            this.simpleProp1 = simpleProp1;
        }

        /**
         * Getter for property 'complexProp1'.
         *
         * @return Value for property 'complexProp1'.
         */
        public TestClassSimple1 getComplexProp1() {
            return complexProp1;
        }

        /**
         * Setter for property 'complexProp1'.
         *
         * @param complexProp1 Value to set for property 'complexProp1'.
         */
        public void setComplexProp1(TestClassSimple1 complexProp1) {
            this.complexProp1 = complexProp1;
        }

        /**
         * Getter for property 'complexProp2'.
         *
         * @return Value for property 'complexProp2'.
         */
        public TestClassSimple2 getComplexProp2() {
            return complexProp2;
        }

        /**
         * Setter for property 'complexProp2'.
         *
         * @param complexProp2 Value to set for property 'complexProp2'.
         */
        public void setComplexProp2(TestClassSimple2 complexProp2) {
            this.complexProp2 = complexProp2;
        }

        /**
         * Getter for property 'simpleProp1'.
         *
         * @return Value for property 'simpleProp1'.
         */
        public String getSimpleProp1() {
            return simpleProp1;
        }

        /**
         * Setter for property 'simpleProp1'.
         *
         * @param simpleProp1 Value to set for property 'simpleProp1'.
         */
        public void setSimpleProp1(String simpleProp1) {
            this.simpleProp1 = simpleProp1;
        }
    }


    @Test
    public void testIsComplexProperty() throws Exception {



      /*  TestClassComplex1 testClass = new TestClassComplex1(
                new TestClassSimple1(stringProp1, intProp1), new TestClassSimple2(boolProp1, dateProp1), stringProp2
        );*/

        TestClassComplex1 testClass = createComplex();

        PropertyMap propertyMap = convertToMap(testClass);

        assertThat(propertyMap.isComplexProperty("complexProp1"), equalTo(true));
        assertThat(propertyMap.isComplexProperty("complexProp2"), equalTo(true));
        assertThat(propertyMap.isComplexProperty("simpleProp1"), equalTo(false));
    }

    @Test
    public void testGetComplexProperty() throws Exception {
        TestClassComplex1 testClass = createComplex();
        PropertyMap propertyMap = convertToMap(testClass);

        PropertyMap complexProp1 = propertyMap.getComplex("complexProp1");
        assertThat(complexProp1.get("stringProp1"), equalTo(testClass.getComplexProp1().getStringProp1()));
        assertThat(complexProp1.get("intProp1"), equalTo(testClass.getComplexProp1().getIntProp1()));

        PropertyMap complexProp2 = propertyMap.getComplex("complexProp2");
        assertThat(complexProp2.get("dateProp1"), equalTo(testClass.getComplexProp2().getDateProp1().getTime()));
        assertThat(complexProp2.get("boolProp1"), equalTo(testClass.getComplexProp2().isBoolProp1()));

    }

    private PropertyMap convertToMap(Object source) {
        ObjectMapper mapper = new ObjectMapper();
        PropertyMap propertyMap = mapper.convertValue(source, PropertyMap.class);
        return propertyMap;
    }

    private TestClassComplex1 createComplex() {

        TestClassComplex1 testClass = new TestClassComplex1(
                new TestClassSimple1(stringProp1, intProp1), new TestClassSimple2(boolProp1, dateProp1), stringProp2
        );
        return testClass;
    }

    String stringProp1 = UUID.randomUUID().toString();
    int intProp1 = 123;
    boolean boolProp1 = true;
    Date dateProp1 = Date.from(Instant.now());
    String stringProp2 = UUID.randomUUID().toString();
}
