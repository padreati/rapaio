
<table style="border: none">
<tr><td>

![rapaio logo](./docs/logo/logo-medium.png)
</td>
<td>

## Disambiguation

1.  (Italian dictionary) *Field of turnips. It is also a place 
where there is confusion, where tricks and sims are plotted.*

2.  (Computer science) *Statistics, data mining and machine learning 
library written in Java.*
</td>
</tr>
</table>
<table style="border: none">
<tr><td>

### Try it online
 
[![Launch rapaio with IJava binder jupyter](images/launch-binder.svg)](https://mybinder.org/v2/gh/padreati/rapaio-notebooks/master) 
[![Launch rapaio with IJava binder jupyter lab](images/launch-binder-lab.svg)](https://mybinder.org/v2/gh/padreati/rapaio-notebooks/master?urlpath=lab)
</td>
<td>

### Build status

[![build status](https://travis-ci.org/padreati/rapaio.svg?branch=master)](https://travis-ci.org/padreati/rapaio)
[![codecov.io](https://codecov.io/github/padreati/rapaio/coverage.svg?branch=master)](https://codecov.io/github/padreati/rapaio?branch=master)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/c478ddddea484a39a7225a3fd98b3d80)](https://www.codacy.com/manual/padreati/rapaio?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=padreati/rapaio&amp;utm_campaign=Badge_Grade)
</td>
</tr>
</table>

## Installation

Library can be used through maven. Last published release on maven central is 2.3.0

       <dependency>
         <groupId>io.github.padreati</groupId>
         <artifactId>rapaio</artifactId>
         <version>2.3.0</version>
       </dependency>

Another way to use the library is in jupyter / jupyter-lab notebooks. This is excellent for experimenting with short interactive 
notebooks or to document the idea you are working on. You have to install jupyter / jupyter-lab and IJava kernel. For more information
on that you can follow the instruction from [here](https://github.com/SpencerPark/IJava#installing). The following notation is
 specific to IJava kernel jupyter notation. 

    %maven io.github.padreati:rapaio:2.3.0  

## Documentation

**Rapaio Tutorials** is now published in the form of IPython notebooks format. 
Please inspect the tutorials folder of 
[rapaio-notebooks repository](https://github.com/padreati/rapaio-notebooks). 

There is also a [Tutorial on Kaggle's Titanic Competition
](https://aureliantutuianu.gitbooks.io/rapaio-manual/content/kaggle-titanic.html)!

## What gap *rapaio* tries to fill?

There are a lot of software stacks out there which provides plenty of 
nicely crafted tools for statistics, machine learning, data mining or 
pattern recognition. Many of them are available as open source, 
quality is high and they are full of reach features.

It appears like a legitimate question to ask *"Why another library for statistics 
and machine learning, when there are many available already?"*. 
My answer is because none of them covers the taste and needs of everybody.

I would really love to have an environment, a box full with 
plenty of tools, which can be extended, which allows me to experiment, 
study and learn. And I want to do all those things in an interactive way, 
where I would program my ideas. Java community deserves such kind of tool 
and this library aims to fill the gap.

## Features provided

Currently the code can be classified in two categories of code maturity:

* *Definitive* - This category includes all the code which has a stable API and it was covered carefully with tests, 
usually with a percentage over 90%
* *Experimental* - All the code found under the package `rapaio.experimental`. This includes drafts and untested code, 
which can be used, but production tools cannot rely on this code. Usually, when code migrates outside the experimental 
package, API can change and sometimes even the philosophy behind the implementation.


### Definitive features

**Data handling**

Any data library needs a way to structure and handle data. Rapaio library defines a high level API for data handling 
in form of *Var* and *Frame*. Variables and frames are an in-memory data model.   

* *Variables and frames* - the in memory data model. There are 3 flavors of variables and frames: solid, mapped or bind.
Solid variable and frames defined dense array data type implementations. Mapped or bind variables or arrays are 
implementations which relays on other data structures for storing data. Those looks like concept of views from 
relational data bases.  
* Data access using streams is available on variables and frames.
* DVector - vector of frequencies
* Unique - data structure to collect and manipulate unique values of a variable
* Group - data structure to build and manipulate group by aggregations
* Index - data structure for transforming value domains into dense indexes

**Core**

Core package contains various basic tools like computing various statistics, various types of random sampling or 
other sampling strategies for sampling rows.   

* *Statistics*: Maximum, Minimum, Sum, Mean, Variance, Quantiles, GeometricMean, Skewness, Kurtosis
* *Online Statistics*: minimum, maximum, count, mean, variance, standard deviation, skewness, kurtosis
* *Op interface*: provides simple way to apply mathematical operations or functions on variables or between them
* *WeightedMean*, *WeightedOnlineStat*: weighted variant of statistics
* *Pearson product-moment coefficient*: linear correlation
* *Spearman's rank correlation coefficient*: linear correlation on rankings
* *SamplingTools*
    * generates discrete integer samples with/without replacement, weighted/non-weighted
    * offers utility methods for bootstraps, simple random, stratified sampling
* *RowSampler* implementations used in machine learning algorithms: bootstrap, identity, subsampling
* *DensityVector* onw way discrete density vector tool
* *DensityTable* two way discrete density table tool


**Distributions**

This package provides access to some very common statistical distributions in an uniform way 

* Bernoulli
* Binomial
* ChiSquare
* Discrete Uniform
* Fisher
* Gamma
* Hypergeometric
* Normal/Gaussian
* Poisson
* Student t
* Continuous Uniform
* Empirical KDE (gaussian, epanechnikov, cosine, tricube, biweight, triweight, triangular, uniform)

**Hypothesis Testing**

Hypothesis testing framework provides implementations for some common hypothesis tests  

* *z test*
    * one sample test for testing the sample mean
    * two unpaired samples test for testing difference of the sample means
    * two paired samples test for testing sample mean of the differences
* *t test*
    * one sample test for testing the sample mean
    * two unpaired samples t test with same variance
    * two unpaired samples Welch t test with different variances
    * two paired samples test for testing sample mean of differences
* *Kolmogorov Smirnoff* KS test
    * one sample test for testing if a sample belongs to a distribution
    * two samples test for testing if both samples comes from the same distribution
* *Pearson Chi-Square tests*
    * goodness of fit 
    * independence test
    * conditional independence test
* *Anderson-Darling goodness of fit*
    * normality test
    
**Filters**

Data can be manipulated ad hoc or by using various types of APIs they expose (direct methods, streams, ops interface).
All those methods are useful in different contexts. Some of data transformation operations, however, needs to be 
applied multiple times or ar too complex to be implemented all over again. Those operations can be collected 
into a chain of transformations and applied to multiple sets of data many times - this is the concept of filter.
A filter have two fundamental operations: *fit* to the data and *apply* the filter to data. 
There are filters which can be applied to a single variable or filters which can be applied to multiple variables 
from a data frame.   

**Frame filters**

* *FApplyDouble* - apply a function on the double values of variables
* *FFillNaDouble* - apply a given fill value over missing values
* *FIntercept* - add an intercept variable to a given data frame
* *FJitter* - add jitter to data according with a noise distribution
* *FMapVars* - select some variables according with a VRange pattern
* *FOneHotEncoding* - encodes nominal variables into multiple 0/1 variables
* *FQuantileDiscrete* - splits numeric variables into nominal categories based on quantile intervals
* *FRandomProjection* - project a data frame onto random projections
* *FRefSort* - sort a data frame based on reference comparators
* *FRemoveVars* - removes some variables according with a VRange pattern
* *FRetainTypes* - retain only variables of given types
* *FShuffle* - shuffle rows from a data frame
* *FStandardize* - standardize variables from a given data frame
* *FToDouble* - convert variables to double
* *FTransformBoxCox* - apply box cox transformation
    
**Var filters**

* *VApply* - apply a function over the stream spots
* *VApplyDouble* - apply a function over the double values
* *VApplyInt* - updates a variable using a lambda on int value
* *VApplyLabel* - updates a variable using a lambda on label value
* *VCumSum* - builds a numeric vector with a cumulative sum
* *VJitter* - adds noise to a given numeric vector according with a noise distribution
* *VQUantileDiscrete* - converts a numerical variable into a nominal based on quantile intervals
* *VRefSort* - sorts a variable according with a given set of row comparators
* *VShuffle* - shuffles values from a variable
* *VSort* - sorts a variable according with default comparator
* *VStandardize* - standardize values from a given numeric variable
* *VToDouble* - transforms a variable into double using a lambda
* *VToInt* - transforms a variable into an int type using a lambda
* *VTransformBoxCox* - transform a variable with BoxCox transform
* *VTransformPower* - transform a variable with power transform

**Machine learning**
There are a lot of various problems which can be solved using methods from 
the field of machine learning: classification, regression, clustering, time series forecasting, 
etc. This library provides some of them and the final aim is to contain a consistent collection 
of production ready tools from this field.   

**Model selection and evaluation**

* Confusion Matrix
* CrossValidation for Classifiers (metrics: Accuracy, LogLoss)

**Classification**

* ZeroRule
* OneRule
* Bayesian: NaiveBayes (Gaussian, KernelDensity, Bernoulli, Multinoulli, Multinomial, Poisson)
* Linear: BinaryLogistic (optionally L2 penalization)
* Decision Trees - CTree: DecisionStump, ID3, C45, CART
  * purity: entropy, infogain, gain ration, gini index
  * weight on instances
  * split: numeric binary, nominal binary, nominal full
  * missing value handling: ignore, random, majority, weighted
  * reduced-error pruning
  * variable importance: frequency, gain and permutation based
* Ensemble: CForest - Bagging, Random Forests
* Boosting: AdaBoost
* Boosting: GBT Classifier

**Regression**

* Simple: ConstantRegression
* Simple: L1Regression
* Simple: L2Regression
* Simple: RandomValueRegression
* LinearRegression
* RidgeRegression
* Tree: CART (no pruning)
* Tree: C45 (no pruning)
* Tree: DecisionStump
* Ensemble: RForest
* Boost: Gradient Boosting Trees


### Experminental Features

Most of the features contained under this section does not meet the production ready bar. 
This does not mean that most of them are not usable, and sometimes what is missing
is only a tiny piece like no complete printing facilities. 

However, due to the high
likelihood of future changes, they will be kept under this umbrella until enough time 
and code is spend on improvements and testing to raise those tools to a production 
ready state. Until that happens, these are the experimental features:  


**Core**

* Special Math functions

**Core tools**

* Distance Matrix

**Evaluation: metrics**

* Receiver Operator Characteristic - ROC curves and ROC Area
* Root Mean Square Error
* Mean Absolute Error
* Gini / Normalized Gini

**Analysis**

* Fast Fourier Transform
* Principal Components Analysis
* Fischer Linear Discriminant Analysis

**Classification**

* SVM: BinarySMO (Platt)
* Ensemble: SplitClassifier

**Regression**

* NNet: MultiLayer Perceptron Regressor

**Clustering**

* Cluster Silhouette
* KMeans clustering
* Minkowski Weighted KMeans

**Time Series**

* Acf (correlation, covariance)
* Pacf 

**Graphics**

* QQ Plot
* Box Plot
* Histogram
* 2d Histogram
* Plot function line
* Plot vertical/horizontal/ab line
* Plot lines
* Plot points
* Density line KDE
* ROC Curve
* Discrete Vertical Lines
* Segment2D
* Plot legend
* BarChart

**Matrices and vectors**

* Numeric vector operations
* Basic matrix operations and matrix decompositions


## Acknowledgements

Many thanks to 
[![JetBrains](images/jetbrains-variant-3_logos/jetbrains-variant-3.svg)](https://www.jetbrains.com/?from=rapaio) 
who provided open source licenses for their brilliant IDE 
[![a](images/intellij-idea_logos/logo.svg)](https://www.jetbrains.com/?from=rapaio)
 
Many thanks to [SpencerPark](https://github.com/SpencerPark) for the java kernel he realized 
[IJava jupyter kernel](https://github.com/SpencerPark/IJava).

