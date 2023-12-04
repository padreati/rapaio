/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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
 *
 */

package rapaio.math.tensor;

public interface FTensor extends Tensor<Float, FTensor> {

    @Override
    default Float get(int... indexes) {
        return getFloat(indexes);
    }

    float getFloat(int... indexes);

    @Override
    default void set(Float value, int... indexes) {
        setFloat(value, indexes);
    }

    void setFloat(float value, int... indexes);

    @Override
    default Float ptrGet(int ptr) {
        return ptrGetFloat(ptr);
    }

    float ptrGetFloat(int ptr);

    @Override
    default void ptrSet(int ptr, Float value) {
        ptrSetFloat(ptr, value);
    }

    void ptrSetFloat(int ptr, float value);

    @Override
    default FTensor add_(Float value) {
        return add_(value.floatValue());
    }

    FTensor add_(float value);

    @Override
    default FTensor sub_(Float value) {
        return sub_(value.floatValue());
    }

    FTensor sub_(float value);

    @Override
    default FTensor mul_(Float value) {
        return mul_(value.floatValue());
    }

    FTensor mul_(float value);

    @Override
    default FTensor div_(Float value) {
        return div_(value.floatValue());
    }

    FTensor div_(float value);

    default Float vdot(FTensor tensor) {
        return vdotFloat(tensor);
    }

    float vdotFloat(FTensor tensor);

    default Float vdot(FTensor tensor, int start, int end) {
        return vdotFloat(tensor, start, end);
    }

    float vdotFloat(FTensor tensor, int start, int end);

    default Float mean() {
        return meanFloat();
    }

    float meanFloat();

    default Float nanMean() {
        return nanMeanFloat();
    }

    float nanMeanFloat();

    default Float std() {
        return stdFloat();
    }

    float stdFloat();

    default Float nanStd() {
        return nanStdFloat();
    }

    float nanStdFloat();

    default Float variance() {
        return varianceFloat();
    }

    float varianceFloat();

    default Float nanVariance() {
        return nanVarianceFloat();
    }

    float nanVarianceFloat();

    default Float sum() {
        return sumFloat();
    }

    float sumFloat();

    default Float nanSum() {
        return nanSumFloat();
    }

    float nanSumFloat();

    default Float prod() {
        return prodFloat();
    }

    float prodFloat();

    default Float nanProd() {
        return nanProdFloat();
    }

    float nanProdFloat();

    default Float max() {
        return maxFloat();
    }

    float maxFloat();

    default Float nanMax() {
        return nanMaxFloat();
    }

    float nanMaxFloat();

    default Float min() {
        return minFloat();
    }

    float minFloat();

    default Float nanMin() {
        return nanMinFloat();
    }

    float nanMinFloat();

    int nanCount();

    int zeroCount();

}
