# Complete list of features

The code is classified in two categories of maturity:

* *Definitive* - This category includes all the code which has a stable API and it was covered carefully with tests, 
usually with a percentage over 90%
* *Experimental* - All the code found under the package `rapaio.experimental`. This includes drafts and untested code, 
which can be used, but production tools cannot rely on this code. Usually, when code migrates outside the experimental 
package, API can change and sometimes even the philosophy behind the implementation.


## Definitive features

**Data handling**

Any data library needs a way to structure and handle data. Rapaio library defines a high level API for data handling 
in form of *Var* and *Frame*. Variables and frames are an in-memory data model.   

* *Variables and frames* - the in memory data model. There are 3 flavors of variables and frames: solid, mapped or bind.
Solid variable and frames defined dense array data type implementations. Mapped or bind variables or arrays are 
implementations which relays on other data structures for storing data. Those looks like concept of views from 
relational data bases.  
* Data access using streams is available on variables and frames.
* DensityVector - vector of frequencies
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
* *Distance Matrix*


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
etc.    

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
* SVM: BinarySMO (Platt, Keerthi & all)

**Regression**

* Simple: ConstantRegression
* Simple: L1Regression
* Simple: L2Regression
* Simple: RandomValueRegression
* LinearRegression
* RidgeRegression
* WeightedLinearRegression
* Decision Trees: CART (no pruning), C45 (no pruning), DecisionStumps
* Ensemble: RForest
* Boost: Gradient Boosting Trees
* RVM (Relevance Vector Machine)

**Analysis**

* Principal Components Analysis

**Clustering**

* KMeans
* Minkowski Weighted KMeans
* KMedians
* Cluster Silhouette


**Graphics**

* QQ Plot - quantile to quantile plots
* Box Plot - boc plots
* Bar Plot - bar plots
* Histogram - histograms
* 2D Histogram - 2 dimensional histograms
* Function line - function lines
* Vertical/horizontal/ab line - simple lines
* Plot lines - lines from points
* Plot points - scatter plot points
* Density line KDE 
* ROC Curve
* Segment2D - line segment
* Plot legend - legends
* PolyLine, PolyFill - polygons from plots 
* CorrGram - diagram of correlations
* Silhouette - cluster silhouette
* Text - simple texts
* IsoCurves - iso bands and iso curves
* Matrix - plot of a matrix
* Image - images



## Experimental Features

Most of the features contained under this section does not meet the production ready bar. 
This does not mean that most of them are not usable, and sometimes what is missing
is only a tiny piece like no complete printing facilities. 

However, due to the high likelihood of future changes, they will be kept under this umbrella until enough time 
and code is spend on improvements and testing to raise those tools to a production 
ready state. Until that happens, these are the experimental features:  


**Core**

* Special Math functions

**Evaluation: metrics**

* Receiver Operator Characteristic - ROC curves and ROC Area
* Root Mean Square Error
* Mean Absolute Error
* Gini / Normalized Gini

**Analysis**

* Fast Fourier Transform
* Fischer Linear Discriminant Analysis

**Classification**

* Ensemble: SplitClassifier

**Regression**

* NNet: MultiLayer Perceptron Regressor

**Time Series**

* Acf (correlation, covariance)
* Pacf 

**Matrices and vectors**

* Numeric vector operations
* Basic matrix operations and matrix decompositions


