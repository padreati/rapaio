package rapaio.io;

import rapaio.explore.DataContainer;

import java.io.*;

/**
 * @author Aurelian Tutuianu
 */
public class DataContainerPersistence {

    public DataContainer restoreFromFile(String file) throws IOException, ClassNotFoundException {
        FileInputStream fileIn = new FileInputStream(file);
        ObjectInputStream in = new ObjectInputStream(fileIn);
        DataContainer s = (DataContainer) in.readObject();
        return s;
    }

    public void storeToFile(DataContainer session, String file) throws FileNotFoundException, IOException {
        FileOutputStream fileOut = new FileOutputStream(file);
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(session);
    }
}
