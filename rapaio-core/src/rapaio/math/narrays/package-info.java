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
///
/// NArrays are multi-dimensional arrays. A NArray contains indexed numerical data of the same type and is a generalization
/// of many linear algebra objects like scalars (arrays with no dimensionality), vectors (1 dimension), matrices (2 dimensions) and so on.
///
/// NArray implementation uses some high level concepts which needs presentation.
///
/// * *Data type* - defined by [DType][rapaio.math.narrays.DType], describes data types used by arrays and offers low level operations
/// for working with values like casting, floating point flag, not a number check.
/// * *Storage* - presented by [Storage][rapaio.math.narrays.Storage] and [StorageManager][rapaio.math.narrays.StorageManager] interfaces;
/// abstracts how data is physically stored and offers primitive methods to access and update data values,
/// creating storages from different sources
/// * *Layout* - presented by [Layout][rapaio.math.narrays.Layout] describes how arrays logically organizes data. The only implementation is
/// [StrideLayout][rapaio.math.narrays.layout.StrideLayout] which uses the concept of stride array to organize data
/// * *NArray* - multi dimensional array which offers methods to compute various operations with them. A arrays uses a data storage as
/// data layer and encodes a way to perform computations (single vs multi threaded, scalar or vectorized instructions). There are
/// available only a single implementation of stride array over data storages which uses Java arrays.
/// * *NArrayManager* - presented by the interface [NArrayManager][rapaio.math.narrays.NArrayManager] handles creation of arrays with a specific computation strategy.
/// It has associated a specific implementation of arrays, handles global parameters which customize array computations and it may
/// also offer operations for array collections.
/// * *NArrays* - offers a set of operations similar with a array manager, but simplifies access to a default array manager. This
/// can be used when the default array manager is enough, having double as default data type. For parametric usage one can use directly
/// array managers where they can parametrize the used implementation. In other words this tool trade off the benefit of using a simpler
/// friendlier API to less customizable behavior.
///
///
/// ## Data types
///
/// The implementation uses Java generics for some numerical types. The implemented data types are:
///
/// * `DType.BYTE` - `byte`, represented as `NArray<Byte>`
/// * `DType.INTEGER` - `int`, represented as `NArray<Integer>`
/// * `DType.FLOAT` - `float`, represented as `NArray<Float>`
/// * `DType.DOUBLE` - `double`, represented as `NArray<Double>`
///
/// ## Storage
///
/// A storage is a container for data which offers simple low-level API for data manipulation.
/// This is an abstraction over the real data buffers which allows implementations of different data storages like Java arrays,
/// memory segments on heap or off heap or any other type of storage.
///
/// There are present only two implementations. The base implementation uses natural language arrays for dense data.
/// The second available implementation abstracts storage for data frames and data variables. This implementation was built in order
/// to allow using data manipulation operations implemented for arrays directly over data frames and variables, avoiding code
/// duplication and API. For example a `VarDouble` has a method called `narray()` which creates a `NArray<Double>` over the same data
/// as the variable and in-place operations will operate directly on the data stored in the `VarDouble` variable.
///
/// Example:
///
///     Random random = new Random(42);
///     VarDouble x = VarDouble.from(100, i -> random.nextGaussian());
///     // apply absolute value function inplace on x
///     x.narray_().abs_();
///     // now all the values in x are positive
///
/// ## NArray Manager
///
/// A array manager encapsulates a strategy for creation of data arrays. Each array manager uses a specific storage factory and offers
/// methods for creation of NArrays. Creation methods have two variants, one variant uses data type as parameter and the second one
/// uses an interface for a specific type. For example a double array with a sequence can be created in two ways, the result being the same:
///
///     NArrayManager manager = NArrayManager.base();
///     Tensor<Double> x = manager.seq(DType.DOUBLE, Shape.of(10, 10));
///     Tensor<Double> y = manager.ofDouble().seq(Shape.of(10, 10));
///     assert(x.deepEquals(y));
///
/// Which method is used depends entirely on the user preference or context.
///
/// **TODO: plenty of work**
///
/// Methods from pytorch which are good candidates for implementation:
///
/// * `torch.diff(input, n=1, dim=-1, prepend=None, append=None)` Computes the n-th forward difference along the given dimension.
/// * `range`
/// * `arange`
/// * `kron`
/// * `torch.addbmm(input, batch1, batch2, *, beta=1, alpha=1, out=None)` Performs a batch matrix-matrix product of matrices stored in batch1
/// and batch2, with a reduced add step (all matrix multiplications get accumulated along the first dimension). input is added to the final result.
/// * `torch.addmm(input, mat1, mat2, *, beta=1, alpha=1, out=None)` Performs a matrix multiplication of the matrices mat1 and mat2.
/// The matrix input is added to the final result.
/// * `torch.addmv(input, mat, vec, *, beta=1, alpha=1, out=None)` Performs a matrix-vector product of the matrix mat and the vector vec.
/// The vector input is added to the final result.
/// * `torch.addr(input, vec1, vec2, *, beta=1, alpha=1, out=None)` Performs the outer-product of vectors vec1 and vec2 and adds it to the matrix input.
/// * `torch.baddbmm(input, batch1, batch2, *, beta=1, alpha=1, out=None)` Performs a batch matrix-matrix product of matrices in batch1 and batch2. input is added to the final result.
/// * `torch.chain_matmul(*matrices, out=None)`
package rapaio.math.narrays;