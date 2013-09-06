package rapaio.data;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public abstract class AbstractVector implements Vector {

    private final String name;

    public AbstractVector(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
