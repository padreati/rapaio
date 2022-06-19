# Welcome to Rapaio

Rapaio is a java library which provides a comprehensive set of tools for 
data manipulation, linear algebra, statistics and machine learning.  

### Pure Java, No Dependencies

The library is self-contained *100% pure Java*. No machine dependence, 
no tricky configuration, no headaches while trying to maintain a 
hell of dependencies. Thus, there are *no dependencies*. 

With low probability this might change in the future. However, one goal 
of the library is to provide a complete set of well integrated tools 
with uniform usage interface. It is not in our intention to provide a 
diverse overcrowded umbrella of wired external libraries and algorithms. 
We appreciate many of them enough to give you the opportunity to use them 
as they are. 


### Expressive API

It is a fact that Java is sometimes too verbose. Idiomatic Java can be thought as 
not the best way for interactive notebook investigations. Thus, we worked on 
API to improve it's friendly-ness for ad-hoc interactive code. Sometimes, the 
employed syntax looks a bit far away from usual Java. This is to make it more 
succint and expressive. Let's take an example:

```java
// create a variable holding a sequence of values from 0 to 10
Var x = VarDouble.seq(0, 10).withName("x");

double sum = x.dv().nansum(); // compute sum
x.dv().apply(value -> value/sum); // make unit vector
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

### Comprehensive set of ML algorithms and statistical tools

There is an extensive set of algorithms for machine learning provided by the library
for regresion, classification, clustering and feature engineering, among which there are
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

### Linear Algebra

For linear algebra, rapaio library provides currently only dense implementations for 
double float precision vectors and arrays. Most of the time, the features provided is enough 
for many tasks. The upcoming versions will provide also specialized versions of various objects 
and also other types for the underlying values. We provide also a set of matrix decompositions 
like Cholesky, LU, Eigen Decomposition, QR and SVD. The API syntax was designed for clean 
interactive usage, for which we provide an example:

```

// load first 4 numeric features of iris dataset into a matrix
DMatrix m = DMStripe.copy(Datasets.loadIrisDataset().removeVars("class"));
// compute mean on row axis
DVector mean = m.mean(0);
// compute sd on row axis
DVector sd = m.sd(0);
// create a matrix which is the standardized version of m
DMatrix c = m.copy().sub(mean, 0).div(sd, 0);
``` 

### Data Visualization

For data visualization rapaio offers a set of graphical tools like histograms, 
2d histogram, scatter points, lines, qq plots, corrgram, segments, lines, and more. 
Most of them can be combined in the same graphic and also multiple graphics can be 
combine in the same output grid layout. 