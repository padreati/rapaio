package rapaio.cluster.algorithm;

import rapaio.cluster.distance.Distance;
import rapaio.cluster.util.Pair;
import rapaio.data.Frame;

import java.util.List;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: tincu
 * Date: 2/4/14
 * Time: 12:53 PM
 */
public class KMeans {
    private int clusterNumber;
    private Distance distance;
    private double[] centroids;
    private double[] meanDistances;
    private int maxIterations;
    public static final int MAX_ITERATIONS = 100;

    public KMeans(int clusterNumber, Distance distance, int maxIterations){
        Random rand = new Random();
        this.maxIterations = maxIterations;
        this.clusterNumber = clusterNumber;
        this.distance = distance;
        this.centroids = new double[clusterNumber];
        this.meanDistances = new double[clusterNumber];
        for(int i=0 ; i<clusterNumber ; i++){
            centroids[i] = rand.nextDouble();
            meanDistances[i] = Double.NaN;
        }
    }

    public KMeans(int clusterNumber, Distance distance){
        this(clusterNumber,distance,MAX_ITERATIONS);
    }

    public List<Pair<Integer,Integer>> learn(Frame frame){
        boolean isStable = false;
        int iterations = 0;
        while (!isStable || iterations < maxIterations){

        }
        return null;
    }

    private  void distributeValues(Frame frame){

    }

    public int classify(Frame frame, int rowNumber){
        return 0;
    }
}
