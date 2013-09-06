package rapaio.core;

/**
 * @author Aurelian Tutuianu
 */
public interface RandomSource {

    void setSeed(long seed);

    double nextDouble();

    int nextInt(int n);
}
