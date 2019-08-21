rapaio
===========
Statistics, data mining and machine learning toolbox in Java.

[![build status](https://travis-ci.org/padreati/rapaio.svg?branch=master)](https://travis-ci.org/padreati/rapaio)
[![codecov.io](https://codecov.io/github/padreati/rapaio/coverage.svg?branch=master)](https://codecov.io/github/padreati/rapaio?branch=master)
![codecov.io](https://codecov.io/github/padreati/rapaio/branch.svg?branch=master)

![codecov.io](https://codecov.io/gh/padreati/rapaio/branch/master/graphs/tree.svg)

**Rapaio Manual** is now published in this repository in the form of IPython notebooks format. Please inspect the notebooks folder 
of this repository.

The manual contains a [Tutorial on Kaggle's Titanic Competition](https://aureliantutuianu.gitbooks.io/rapaio-manual/content/kaggle-titanic.html)!

Acknowledgements
================

Many thanks to JetBrains for providing an open source license for their [Java IDE](https://www.jetbrains.com/idea/).


CURRENT STATE OF THE CODE
=========================

Currently the code can be classified into three categories of code maturity:

* __Definitive__ - This category includes all the code which has a stable API and it was covered carefully with tests, 
usually with a percentage over 90%
* __Experimental__ - All the code found under the package `rapaio.experimental`. This includes drafts and untested code, 
which can be used, but production tools cannot rely on this code. Usually, when code migrates outside the experimental 
package, API can change and sometimes even the philosophy behind the implementation.


Definitive Features
====================

**Data structures**

* Variables and frames. There are 3 flavors of variables and frames: solid, mapped or bind. Access
using streams is also available on variables and also on data frames.
* Unique - data structure to collect and manipulate unique values of a variable
* Group - data structure to build and manipulate group by aggregations
* DVector - vector of frequencies

**Frame Filters**

* FApplyDouble - apply a function on the double values of variables
* FFillNaDouble - apply a given fill value over all missing values
* FIntercept - add an intercept variable to a given data frame
* FJitter - add jitter to data according with a noise distribution
* FMapVars - select some variables according with a VRange pattern
* FOneHotEncoding - encodes nominal variables into multiple 0/1 variables
* FQuantileDiscrete - splits numeric variables i**Core tools**

nto nominal categories based on quantile intervals
* FRandomProjection - project a data frame onto random projections
* FRefSort - sort a data frame based on reference comparators
* FRemoveVars - removes some variables according with a VRange pattern
* FRetainTypes - retain only variables of given types
* FShuffle - shuffle rows from a data frame
* FStandardize - standardize variables from a given data frame
* FToDouble - convert variables to double
* FTransformBoxCox - apply box cox transformation
    
**Var filters**

* VApply - apply a function over the stream spots
* VApplyDouble - apply a function over the double values
* VApplyInt - updates a variable using a lambda on int value
* VApplyLabel - updates a variable using a lambda on label value
* VCumSum - builds a numeric vector with a cumulative sum
* VJitter - adds noise to a given numeric vector according with a noise distribution
* VQUantileDiscrete - converts a numerical variable into a nominal based on quantile intervals
* VRefSort - sorts a variable according with a given set of row comparators
* VShuffle - shuffles values from a variable
* VSort - sorts a variable according with default comparator
* VStandardize - standardize values from a given numeric variable
* VToDouble - transforms a variable into double using a lambda
* VToInt - transforms a variable into an int type using a lambda
* VTransformBoxCox - transform a variable with BoxCox transform
* VTransformPower - transform a variable with power transform

**Core**

* Maximum, Minimum, Sum, Mean, Variance, Quantiles, GeometricMean, Skewness, Kurtosis
* Online Statistics: minimum, maximum, count, mean, variance, standard deviation, skewness, kurtosis
* WeightedMean, WeightedOnlineStat
* Pearson product-moment coefficient
* Spearman's rank correlation coefficient
* SamplingTool
    * generates discrete integer samples with/without replacement, weighted/non-weighted
    * offers utility methods for bootstraps, simple random, stratified sampling
* RowSampler implementations used in machine learning algorithms: bootstrap, identity, subsampling


**Distributions**

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

* z test
    * one sample test for testing the sample mean
    * two unpaired samples test for testing difference of the sample means
    * two paired samples test for testing sample mean of the differences
* t test
    * one sample test for testing the sample mean
    * two unpaired samples t test with same variance
    * two unpaired samples Welch t test with different variances
    * two paired samples test for testing sample mean of differences
* Kolmogorov Smirnoff KS test
    * one sample test for testing if a sample belongs to a distribution
    * two samples test for testing if both samples comes from the same distribution
* Pearson Chi-Square tests
    * goodness of fit 
    * independence test
    * conditional independence test
* Anderson-Darling goodness of fit
    * normality test
    
**Classification**

* Rule: OneRule
* Bayesian: NaiveBayes (GaussianPdf, EmpiricalPdf, MultinomialPmf)

**Regression**

* Simple: ConstantRegression
* Simple: L1Regression
* Simple: L2Regression
* Simple: RandomValueRegressor
* LinearRegression
* RidgeRegression
* Tree: CART (no pruning)
* Tree: C45 (no pruning)
* Tree: DecisionStump

**Evaluation: metrics**

* Confusion Matrix



Experminental Stage Features
============================

For each feature there are some notes regarding the development stage. 
If there are no notes it means the feature is considered to be fully implemented and well tested.

**Core**

* Special Math functions

**Core tools**

* DTable
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

* Linear: BinaryLogistic
* Decision Trees - CTree: DecisionStump, ID3, C45, CART
  * purity: entropy, infogain, gain ration, gini index
  * weight on instances
  * split: numeric binary, nominal binary, nominal full
  * missing value handling: ignore, random, majority, weighted
  * reduced-error pruning
  * variable importance: frequency, gain and permutation based
* Ensemble: CForest - Bagging, Random Forests
* Boosting: AdaBoost.SAMME
* SVM: BinarySMO (Platt)
* Boosting: GBT (Gradient Boosting Trees) Classifier
* Ensemble: SplitClassifier

**Regression**

* Boost: GBT (Gradient Boosting Tree) Regressor
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
