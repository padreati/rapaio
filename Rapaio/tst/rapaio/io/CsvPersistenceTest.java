/*
 * Copyright 2013 Aurelian Tutuianu <padreati@yahoo.com>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package rapaio.io;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import rapaio.data.Frame;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class CsvPersistenceTest {

    private CsvPersistence persistence;
    private String[] strings = new String[]{
            "Year,Make,Model,Description,Price\n"
                    + "1997,Ford,E350,\"ac, abs, moon\",3000.00\n"
                    + "1999,Chevy,\"Venture \"\"Extended Edition\"\"\",\"\",4900.00\n"
                    + "1999,Chevy,\"Venture \"\"Extended Edition, Very Large\"\"\",,5000.00\n"
                    + "1996,Jeep,Grand Cherokee,\"MUST SELL! air, moon roof, loaded\",4799.00",
            //
            "\"Year \", \"Make, Model\" , \"Description\"\"\", Price",};

    @Before
    public void setUp() {
        persistence = new CsvPersistence();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testHeader() {
        try {
            Frame f = persistence.read("header", new ByteArrayInputStream(strings[0].getBytes()));
            assertNotNull(f);
            assertEquals(5, f.getColCount());
            assertArrayEquals(new String[]{"Year", "Make", "Model", "Description", "Price"}, f.getColNames());
//            Summary.summary(f);

            f = persistence.read("header", new ByteArrayInputStream(strings[1].getBytes()));
            assertNotNull(f);
            assertEquals(4, f.getColCount());
            assertArrayEquals(new String[]{"Year", "Make, Model", "Description\"\"", "Price"}, f.getColNames());

        } catch (IOException ex) {
            assertTrue("this shoud not happen.", false);
        }

    }
}