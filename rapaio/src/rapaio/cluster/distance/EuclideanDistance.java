package rapaio.cluster.distance;

import rapaio.core.ColRange;
import rapaio.core.MathBase;
import rapaio.data.Frame;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: tincu
 * Date: 2/4/14
 * Time: 12:58 PM
 */
public class EuclideanDistance implements Distance {

    private ColRange range;

    public EuclideanDistance(ColRange range){
        this.range = range;
    }

    @Override
    public double getDistance(Frame from, int fromRow, Frame targetFrame, int targetRow) {
        List<Integer> sourceFields = range.parseColumnIndexes(from);
        List<Integer> targetFields = range.parseColumnIndexes(targetFrame);
        if(sourceFields.size() != targetFields.size()){
            throw new IllegalArgumentException("Source frame and target frame have a different number of columns !");
        }
        double distance = 0;
        for(int i=0 ; i<sourceFields.size() ; i++){
            distance += (MathBase.pow(from.getCol(sourceFields.get(i)).getValue(fromRow),2) -
                    MathBase.pow(targetFrame.getCol(targetFields.get(i)).getValue(targetRow),2));
        }
        return MathBase.sqrt(distance);
    }
}
