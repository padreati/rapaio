package rapaio.data;

import rapaio.data.stream.VSpot;
import rapaio.data.stream.VSpots;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public abstract class AbstractVar implements Var {

    private String name;
    private List<VSpot> spots;

    public String name() {
        return name;
    }

    public AbstractVar withName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public Var bindRows(Var var) {
        return BoundVar.newFrom(this, var);
    }

    @Override
    public Var mapRows(Mapping mapping) {
        return MappedVar.newByRows(this, mapping);
    }

    @Override
    public VSpots stream() {
        if (spots == null || spots.size() != rowCount()) {
            spots = new LinkedList<>();
            for (int i = 0; i < this.rowCount(); i++) {
                spots.add(new VSpot(i, this));
            }
        }
        return new VSpots(spots.stream());
    }

    @Override
    public Var solidCopy() {
        switch (type()) {
            case NOMINAL:
                Nominal nom = Nominal.newEmpty(rowCount(), dictionary());
                for (int i = 0; i < rowCount(); i++) {
                    nom.setLabel(i, label(i));
                }
                return nom;
            case ORDINAL:
                Ordinal ord = Ordinal.newEmpty(rowCount(), dictionary());
                for (int i = 0; i < rowCount(); i++) {
                    ord.setLabel(i, label(i));
                }
                return ord;
            case INDEX:
                Index idx = Index.newEmpty(rowCount());
                for (int i = 0; i < rowCount(); i++) {
                    idx.setIndex(i, index(i));
                }
                return idx;
            case STAMP:
                Stamp stamp = Stamp.newEmpty(rowCount());
                for (int i = 0; i < rowCount(); i++) {
                    stamp.setStamp(i, stamp(i));
                }
                return stamp;
            case NUMERIC:
                Numeric num = Numeric.newEmpty(rowCount());
                for (int i = 0; i < rowCount(); i++) {
                    num.setValue(i, value(i));
                }
                return num;
            case BINARY:
                Binary bin = Binary.newEmpty(rowCount());
                for (int i = 0; i < rowCount(); i++) {
                    bin.setIndex(i, index(i));
                }
                return bin;
            default:
                throw new NotImplementedException();
        }
    }
}
