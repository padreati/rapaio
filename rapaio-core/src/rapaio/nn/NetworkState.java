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

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the network state. The network state is a container which holds
 * all the tensors which are used for inference and learning in a network.
 * <p>
 * For serialization purposes the network code is not saved. This is in order
 * to give enough freedom to the used to customize the network behavior
 * with custom code at training and inference time. Instead of that,
 * if a network has to be serialized, the following scenario can be followed:
 *
 * <li>
 * <item>create a network instance and do whatever is needed to be used later (including initialization, training, other customizations)</item>
 * <item>save the network state into a persistent storage using one of the methods {@code Network#saveState}</item>
 * <item>For later usage, create again a new instance of the network</item>
 * <item>Loads the network state from a persistent storage using one of the methods {@code Network#loadState}</item>
 * <item>The new network is ready to be used like the old network instance for inference of for other scenarios, like further training</item>
 * </li>
 */
public final class NetworkState {

    private final ArrayList<Tensor> tensors;

    public NetworkState() {
        this.tensors = new ArrayList<>();
    }

    public NetworkState add(Tensor tensor) {
        tensors.add(tensor);
        return this;
    }

    public NetworkState addTensors(List<Tensor> tensors) {
        this.tensors.addAll(tensors);
        return this;
    }

    public NetworkState merge(NetworkState networkState) {
        tensors.addAll(networkState.tensors);
        return this;
    }

    public List<Tensor> tensors() {
        return tensors;
    }
}
