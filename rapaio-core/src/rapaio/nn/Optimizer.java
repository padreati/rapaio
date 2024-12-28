/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

package rapaio.nn;

import java.util.Collection;

import rapaio.nn.optimizer.Adam;
import rapaio.nn.optimizer.SGD;

/**
 * Defines the contract for optimization algorithms.
 * <p>
 * An optimization algorithm is an algorithm which uses computed gradients and updates the values of tracked tensors according
 * to its own strategy. The tracked tensors are received at creation time.
 * <p>
 * Even if a tensor is tracked for optimization, it can be skipped for optimization if it has {@code requiresGrad} set to false.
 * This is useful for scenarios when somebody wants to freeze some parts of the networks and update only other parts.
 */
public interface Optimizer {

    /**
     * Creates a new instance of Stochastic Gradient Descent optimizer. Further customization can be done on the returned instance.
     *
     * @param tm     tensor manager used for computation
     * @param params list of tracked tensors for optimization
     * @return new optimizer instance
     */
    static SGD SGD(TensorManager tm, Collection<Tensor> params) {
        return new SGD(tm, params);
    }

    /**
     * Creates a new instance of Adam optimizer. Further customization can be done on the returned instance.
     *
     * @param tm     tensor manager used for computation
     * @param params list of tracked tensors for optimization
     * @return new optimizer instance
     */
    static Adam Adam(TensorManager tm, Collection<Tensor> params) {
        return new Adam(tm, params);
    }

    /**
     * Deletes all the computed gradients for the tracked tensors
     */
    void zeroGrad();

    /**
     * Performs the optimization which consists of updating tensor values according to the computed gradients and algorithm strategy.
     */
    void step();
}
