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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;

import rapaio.io.atom.AtomInputStream;
import rapaio.io.atom.AtomOutputStream;
import rapaio.datasets.Batch;
import rapaio.util.NotImplementedException;

/**
 * Interface which defines a neural network. A neural network can consist
 * of one or multiple layers, each of them being neural networks, in a recursive manner.
 * <p>
 * A network layer is network, and you can compose layers and networks to obtain
 * other networks.
 */
public interface Network extends Serializable {

    /**
     * Tensor manager required to provide factories for creating tensors and default values
     * when working with tensors (like what numerical data type to use for computations).
     *
     * @return instance of tensor manager
     */
    TensorManager tm();

    /**
     * List of tensors which will be optimized based on computed gradients in the {@link Optimizer#step()}.
     * The tensors which are exposed as parameters will require gradient computation with all their ancestors
     * along their computation chain.
     * <p>
     * This method has also a default implementation which is useful for standard cases when one builds a
     * network only with layers and all the layers are defined also as fields in the network class.
     * If the network implementation goes beyond that with various customization, it needs to provide its own
     * implementation of this method.
     *
     * @return list of tensors used by optimizer
     */
    List<Tensor> parameters();

    /**
     * A network state is the collection of variables which needs to be serialized so that at deserialization
     * the status of a network is restored.
     * <p>
     * When a network is saved to external storages for later use, only the network state is saved. Since
     * the network is a class, when saving for later use is needed it has to have the following steps:
     * <li>
     * <item>Save network state from an instance of the network to a file</item>
     * <item>Create a new instance of the same network</item>
     * <item>Load serialized state into the new instance of the network</item>
     * </li>
     * <p>
     * In this way the new network will behave as the original network.
     * <p>
     * A default implementation is provided with a limited usage scenario.
     * If one creates a network which uses only layers which are declared also as fields, then the default
     * implementation cover that scenario. If one uses some custom code, or does not declare all the layers
     * as fields, a custom implementation of this method is needed in order to save the whole network state.
     *
     * @return instance of the network state which captures whole information needed to serialize and deserialize
     * a network for offline usage
     */
    NetworkState state();

    /**
     * Flags the network to be in <i>train mode</i>. When a network is in train mode its parameters
     * are traced for optimization using computed gradients.
     *
     * @see #eval()
     */
    void train();

    /**
     * Flags network to be in <i>eval mode</i>. When a network is in eval mode its parameters are not
     * traced for optimization. This is useful to speed up the inference process.
     *
     * @see #train()
     */
    void eval();

    /**
     * Performs network inference by taking an array of input tensors and computing an array of output tensors.
     * Usually a network takes a single input and produces a single output. For this purpose the default
     * implementation delegates to {@link #forward11(Tensor)}.
     * <p>
     * If the default scenario is not in the purpose of the network, this method needs to be implemented.
     * <p>
     * The forward method contains operations on tensors. All operations on tensors are tracked by the
     * computational graph since each tensor operation leaves a trace which consists of backpropagation
     * functions. When {@link Autograd#backward(Tensor)} method is called on some tensor which has
     * a scalar gradient, the computational graph starts to back propagate gradients.
     *
     * @param xs input tensors
     * @return computed output tensors
     */
    default Tensor[] forward(Tensor... xs) {
        if (xs.length == 1) {
            return new Tensor[] {forward11(xs[0])};
        }
        throw new NotImplementedException();
    }

    /**
     * The default case of {@link #forward(Tensor...)} method which receives a single input tensor
     * and outputs a single tensor.
     *
     * @param x input tensor
     * @return computed output tensor
     */
    default Tensor forward11(Tensor x) {
        throw new NotImplementedException();
    }

    /**
     * Improved forward method which trades memory for parallel batched execution of the forward pass.
     * <p>
     * The execution consists of splitting the input tensors in batches and parallel execution
     * of those batches in the forward step. The tradeoff consists in the fact that all the computational
     * graph will reside in memory, thus one can use this method if the dataset is small enough,
     * depending on the available memory.
     * <p>
     * The result consists of a list of batches. Each batch contains input data and also contains the
     * network output tensors computed for the given specific batch.
     * <p>
     * The batch size is given as parameter. Before splitting in batches the data from the dataset is shuffled
     * and all the batches are used for execution.
     *
     * @param batchSize the number of instances for each batch, the last batch might contain few instances
     * @param inputs    input tensors
     * @return list of computed batches
     */
    default List<Batch> batchForward(int batchSize, Tensor... inputs) {
        return batchForward(batchSize, true, false, inputs);
    }

    /**
     * Fully customizable version of {@link #batchForward(int, Tensor...)}.
     * <p>
     * Improved forward method which trades memory for parallel batched execution of the forward pass.
     * <p>
     * The execution consists of splitting the input tensors in batches and parallel execution
     * of those batches in the forward step. The tradeoff consists in the fact that all the computational
     * graph will reside in memory, thus one can use this method if the dataset is small enough,
     * depending on the available memory.
     * <p>
     * The result consists of a list of batches. Each batch contains input data and also contains the
     * network output tensors computed for the given specific batch.
     * <p>
     * The batch size is given as parameter. Before splitting in batches the data from the instances are shuffled if
     * {@code shuffle} parameter is true. In some cases the last batch might contain fewer instances. If this
     * is not desirable, one can set {@code skipLast} to {@code true} to skip the last batch.
     *
     * @param batchSize the batch size
     * @param shuffle   if data is shuffled before splitting in batches
     * @param skipLast  if the last batch, which might be smaller, is skipped for execution
     * @param inputs    input tensors
     * @return list of computed batches
     */
    List<Batch> batchForward(int batchSize, boolean shuffle, boolean skipLast, Tensor... inputs);

    /**
     * Saves the state of the network to an atom output stream.
     *
     * @param out atom output stream
     * @throws IOException thrown if something goes wrong
     */
    void saveState(AtomOutputStream out) throws IOException;

    /**
     * Loads the state of the network from an atom input stream
     *
     * @param in atom input stream
     * @throws IOException thrown if something goes wrong
     */
    void loadState(AtomInputStream in) throws IOException;

    /**
     * Save the network state using atom binary serialization protocol to a file.
     *
     * @param file file which will store the network state
     * @throws IOException thrown if something goes wrong
     */
    void saveState(File file) throws IOException;

    /**
     * Saves the network state using atom binary serialization protocol to a generic output stream.
     *
     * @param out output stream
     * @throws IOException thrown if something goes wrong
     */
    void saveState(OutputStream out) throws IOException;

    /**
     * Loads the network state using atom binary serialization protocol from a file.
     *
     * @param file file which contains the serialized network state
     * @throws IOException thrown if something goes wrong
     */
    void loadState(File file) throws IOException;

    /**
     * Loads the network state using atom binary serialization protocol from a generic input stream
     *
     * @param in input stream
     * @throws IOException if something goes wrong
     */
    void loadState(InputStream in) throws IOException;
}
