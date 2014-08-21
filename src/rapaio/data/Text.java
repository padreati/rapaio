/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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

package rapaio.data;

import rapaio.data.stream.VSpots;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
@Deprecated
public class Text extends AbstractVar {

    private List<String> values = new ArrayList<>();
    private int rows;

    @Override
    public Text withName(String name) {
        return (Text) super.withName(name);
    }

    @Override
    public VarType type() {
        return VarType.TEXT;
    }

    @Override
    public int rowCount() {
        return rows;
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
    public double value(int row) {
        return 0;
    }

    @Override
    public void setValue(int row, double value) {

    }

    @Override
    public void addValue(double value) {

    }

    @Override
    public int index(int row) {
        return 0;
    }

    @Override
    public void setIndex(int row, int value) {

    }

    @Override
    public void addIndex(int value) {

    }

    @Override
    public String label(int row) {
        return null;
    }

    @Override
    public void setLabel(int row, String value) {

    }

    @Override
    public void addLabel(String value) {

    }

    @Override
    public String[] dictionary() {
        return new String[0];
    }

    @Override
    public void setDictionary(String[] dict) {

    }

    @Override
    public boolean binary(int row) {
        return false;
    }

    @Override
    public void setBinary(int row, boolean value) {

    }

    @Override
    public void addBinary(boolean value) {

    }

    @Override
    public long stamp(int row) {
        return 0;
    }

    @Override
    public void setStamp(int row, long value) {

    }

    @Override
    public void addStamp(long value) {

    }

    @Override
    public boolean missing(int row) {
        return false;
    }

    @Override
    public void setMissing(int row) {

    }

    @Override
    public void addMissing() {

    }

    @Override
    public void remove(int row) {

    }

    @Override
    public void clear() {

    }

    @Override
    public Var solidCopy() {
        return null;
    }

    @Override
    public VSpots stream() {
        return null;
    }
}
