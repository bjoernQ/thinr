/*
 *    Copyright (C) 2016 Björn Quentin
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package de.mobilej.thinr;

import org.junit.BeforeClass;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

/**
 * Tests "checkValidFunction"
 * <p>
 * Created by bjoern on 01.07.2016.
 */

public class RuntimeCheckDisabledTest {

    @BeforeClass
    public static void setup() throws Exception {
        Thinr.setRuntimeChecksEnabled(false);
    }

    @Test
    public void testAValidFunction() throws Exception {
        Object function = new ValidFunction();
        Whitebox.invokeMethod(Thinr.class, "checkValidFunction", function);
    }

    @Test
    public void testAValidFunctionWithJacocoField() throws Exception {
        Object function = new ValidJacocoFunction();
        Whitebox.invokeMethod(Thinr.class, "checkValidFunction", function);
    }

    @Test
    public void testAValidFunctionWithRetroLambdaField() throws Exception {
        Object function = new ValidRetroLambdaFunction();
        Whitebox.invokeMethod(Thinr.class, "checkValidFunction", function);
    }

    @Test
    public void testInavlidValidFunction() throws Exception {
        Object function = new InvalidFunction();
        Whitebox.invokeMethod(Thinr.class, "checkValidFunction", function);
    }


    public static class ValidJacocoFunction {
        public Object $jacocoData;
    }

    public static class ValidRetroLambdaFunction {
        public ValidRetroLambdaFunction instance;
    }

    public static class ValidFunction {
    }

    public class InvalidFunction {
    }

}
