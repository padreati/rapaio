# Welcome to Rapaio Book

Rapaio is a Java library which provides a comprehensive set of tools for 
data manipulation, linear algebra, statistics and machine learning. 

## Pure Java, No Dependencies

The library is self-contained *100% pure Java*. No machine dependence, 
no tricky configuration, no headaches while trying to maintain a 
hell of dependencies. Thus, there are *no dependencies*. 

With low probability this might change in the future. However, one goal 
of the library is to provide a complete set of well integrated tools 
with uniform usage interface. It is not in our intention to provide a 
diverse overcrowded umbrella of wired external libraries and algorithms. 
We appreciate many of them enough to give you the opportunity to use them 
as they are. 


## Expressive API

It is a fact that Java is sometimes too verbose. Idiomatic Java can be thought as 
not the best way for interactive notebook investigations. Thus, we worked on 
API to improve it's friendly-ness for ad-hoc interactive code. Sometimes, the 
employed syntax looks a bit far away from usual Java. This is to make it more 
succinct and expressive. Let's take an example:

```java
// create a variable holding a sequence of values from 0 to 10
Var x = VarDouble.seq(0, 10).withName("x");

double sum = x.dv().nansum(); // compute sum
x.darray_().apply(value -> value/sum); // make unit vector
x.printContent(); // print variable content
```
```
VarDouble [name:"x", rowCount:11]
row    value   row    value   row    value   
 [0] 0          [4] 0.0727273  [8] 0.1454545 
 [1] 0.0181818  [5] 0.0909091  [9] 0.1636364 
 [2] 0.0363636  [6] 0.1090909 [10] 0.1818182 
 [3] 0.0545455  [7] 0.1272727 
```

## Comprehensive set of ML algorithms and statistical tools

There is an extensive set of algorithms for machine learning provided by the library
for regression, classification, clustering and feature engineering, among which there are
decision trees, random forests, gradient boosting trees and support vector machines.

Model assessment and selection is realized in a flexible way, through an unified interface,
which allow one to configure splitting strategy, metrics for evaluation and comparison.

Most common distributions are available. Those can be used to generate samples, compute 
various statistics, data visualisation and so on. There are also useful tools for 
computing correlations, various statistics and hypothesis tests.

A vast set of data handling and transformation tools are available in form of 
filters for operations like type transformation, missing value imputation, unique values, 
grouping and group aggregate functions.

A complete list of features can be explored here [Complete Library Features](complete-library-features.md).

## Data Visualization

For data visualization rapaio offers a set of graphical tools like histograms,
2d histogram, scatter points, lines, qq plots, corrgram, segments, lines, and more.
Most of them can be combined in the same graphic and also multiple graphics can be
combined in the same output grid layout.

## Linear Algebra

For linear algebra, rapaio library provides currently only dense implementations for 
tensors with `byte`, `int`, `float` and `double` data types. We provide also a set of matrix decompositions 
like Cholesky, LU, Eigen Decomposition, QR and SVD. The API syntax was designed for clean 
interactive usage, for which we provide an example:

```java

// load first 4 numeric features of iris dataset into a matrix
var x = Datasets.loadIrisDataset().mapVars(VarRange.of("0~3")).darray();

// compute mean on row axis
var m = x.mean1d(0);

// compute sd on row axis
var std = x.std1d(0, 0);

// create a matrix which is the standardized version of m
var s = x.sub(m).div(std);

// print first 10 rows of standardized values
s.sel(0, IntArrays.newSeq(10));
``` 

## Tensors and Neural Networks

Starting with version 7.0.0 this library contains the concept of differentiable multidimensional array.
The name of this object is a tensor, in order to maintain the tradition of well established libraries.
The same libraries are the inspiration for this implementation, especially Pytorch. As such, we 
tried to maintain a similar syntax, although there are some conceptual differences. 

In rapaio library the tensor has a value and a eventually computed gradient. The value and the gradient 
have the same shape and its type id DArray, the multidimensional array implementation. 

Tensor values are created most of the time when tensors are created. This is so because most tensors are 
a result of a tensor computation (an operation which takes as input one or more tensors, performs a 
computation and returns a result in the form of a tensor). Tensors are keeps a track of dependent 
objects when are created. Each operation available on tensors provides also a way to back propagate
derivatives and gradients. Those traces are used by an auto differentiation engine to perform 
backpropagation. Currently only auto differentiation in reverse mode is available. 

This allows us to create neural networks which can be trained and used for inference.

An example of a simple neural network:

```java
TensorManager tm = TensorManager.ofFloat();
Network nn = new Sequential(
    tm,
    new LayerNorm(tm, Shape.of(4)),
    new Linear(tm, 4, 10, true),
    new ELU(tm),
    new LayerNorm(tm, Shape.of(10)),
    new Linear(tm, 10, 3, true),
    new ELU(tm),
    new LogSoftmax(tm, 1)
);
```



```{tableofcontents}
```
