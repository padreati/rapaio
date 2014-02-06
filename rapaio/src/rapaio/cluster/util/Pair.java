package rapaio.cluster.util;

/**
 * Created with IntelliJ IDEA.
 * User: tincu
 * Date: 2/4/14
 * Time: 1:40 PM
 */
public class Pair<T1,T2> {

    public final T1 first;
    public final T2 second;

    public Pair(T1 first, T2 second){
        this.first = first;
        this.second = second;
    }
}
