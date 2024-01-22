# Tensors

## Brief introduction

Tensors in rapaio library are N-dimensional arrays. A tensor contains indexed numerical data of the same type and is a generalization 
of many linear algebra objects like scalars (tensors with no dimensionality), vectors (1 dimension), matrices (2 dimensions) and so on.

The implementation uses Java generics for some numerical types. The implemented data types are:

* `DType.BYTE` - `byte`, represented as `Tensor<Byte>`
* `DType.INTEGER` - `int`, represented as `Tensor<Integer>`
* `DType.FLOAT` - `float`, represented as `Tensor<Float>`
* `DType.DOUBLE` - `double`, represented as `Tensor<Double>`

Tensor implementation uses some high level concepts which needs presentation. 

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
    x.dt().abs_();
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