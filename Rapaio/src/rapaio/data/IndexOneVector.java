package rapaio.data;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class IndexOneVector extends IndexVector {

    public IndexOneVector(int index) {
        this("", index);
    }

    public IndexOneVector(String name, int index) {
        super(name, 1);
        setIndex(0, index);
    }
}
