# Tensors

## Brief introduction

Tensors in rapaio library are N-dimensional arrays. A tensor contains indexed numerical data of the same type and is a generalization 
of many linear algebra objects like scalars (tensors with no dimensionality), vectors (1 dimension), matrices (2 dimensions) and so on.

Tensor implementation uses some high level concepts which needs presentation.

* *Data type* - presented by DType, describes data types used by tensors and offers low level operations for working with values 
like casting, floating point flag, not a number check. 
* *Storage* - presented by `Storage` and `StorageFactory` interfaces; abstracts how data is physically stored and offers primitive methods
to access and update data values, creating storages from different sources
* *Layout* - presented by `Layout` describes how tensors logically organizes data. The only implementation is 
`StrideLayout` which uses the concept of stride array to organize data
* *Tensor* - multi dimensional array which offers methods to compute various operations with them. A tensors uses a data storage as
data layer and encodes a way to perform computations (single vs multi threaded, scalar or vectorized instructions). There are 
available only a single implementation of stride tensor over data storages which uses Java arrays.
* *TensorManager* - presented by the interface with the same name handles creation of tensors with a specific computation strategy. 
It has associated a specific implementation of tensors, handles global parameters which customize tensor computations and it may 
also offer operations for tensor collections.
* *Tensors* - offers a set of operations similar with a tensor manager, but simplifies access to a default tensor manager. This 
can be used when the default tensor manager is enough, having double as default data type. For parametric usage one can use directly 
tensor managers where they can parametrize the used implementation. In other words this tool trade off the benefit of using a simpler 
friendlier API to less customizable behavior. 


### Data types

The implementation uses Java generics for some numerical types. The implemented data types are:

* `DType.BYTE` - `byte`, represented as `Tensor<Byte>`
* `DType.INTEGER` - `int`, represented as `Tensor<Integer>`
* `DType.FLOAT` - `float`, represented as `Tensor<Float>`
* `DType.DOUBLE` - `double`, represented as `Tensor<Double>`

### Storage

A storage is a container for data which offers simple low-level API for data manipulation. 
This is an abstraction over the real data buffers which allows implementations of different data storages like Java arrays, 
memory segments on heap or off heap or any other type of storage. Data storage laso abstracts idea of dense or sparse arrays.

Currently there are present only two implementations. The base implementation uses natural language arrays for dense data. 
The second available implementation abstracts storage for data frames and data variables. This implementation was built in order 
to allow using data manipulation operations implemented for tensors directly over data frames and variables, avoiding code 
duplication and API. For example a `VarDouble` has a method called `dt()` which creates a `Tensor<Double>` over the same data
as the variable and inplace operations will have operate directly on the data stored in the `VarDouble` variable. 

Example:

    Random random = new Random(42);
    VarDouble x = VarDouble.from(100, i -> random.nextGaussian());
    // apply absolute value function inplace on x
    x.tensor_().abs_();
    // now all the values in x are positive

### Tensor Engine

A tensor engine encapsulates a strategy for creation of data tensors. Each tensor engine uses a specific storage factory and offers 
methods for creation of data tensors. Creation methods have two variants, one variant uses data type as parameter and the second one
uses an interface for a specific type. For example a double tensor with a sequence can be created in two ways, the result being the same:

    TensorEngine engine = TensorEngine.base();
    Tensor<Double> x = engine.seq(DType.DOUBLE, Shape.of(10, 10));
    Tensor<Double> y = engine.ofDouble().seq(Shape.of(10, 10));
    assert(x.deepEquals(y));

Which method is used depends entirely on the user preference or context. 

**TODO: plenty of work** 