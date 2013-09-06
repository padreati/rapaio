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