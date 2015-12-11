## Introduction into data visualization using rapaio library

The purpose of this document is not to exhaust the graphics features of the library
and graphics components which are already built in. There are good reasons why such a
goal would be inappropriate:

* the library it's still in working phase, even there are many components ready for
production use; any careful enumeration would be incomplete as soon as another
graphical features would be added
* stressing the whole features even for a single component would be time consuming since
working with all the combinations of graphical aspect options would take too much space
to time for everybody

The goal of this document is to illustrate the core ideas behind the graphic system
and to provide enough examples to have a fast and productive feedback.

The design of the graphical components of this library is influenced by many ideas
from existing popular graphical systems. Among the main inspirations there are
 some which deserves appropriate consideration: `R` standard graphical library,
 `ggplot2` package  and `matplotlib` from Python stack.

## Data sets

For the purpose of this tutorial some built-in data sets are used. Most of the built
in data sets are available via `rapaio.datasets.Datasets` class. This is an utility class
which provides some standard data sets used in many statistical and machine learning text
books. There are a few which we will use for the purpose of this tutorial.


### Iris data set

The **Iris flower data set** or **Fisher's Iris data set** is a multivariate data set
introduced by Ronald Fisher in his 1936 paper *The use of multiple measurements in
taxonomic problems as an example of linear discriminant analysis*. It is sometimes
called **Anderson's Iris data** set because Edgar Anderson collected the data to
quantify the morphologic variation of Iris flowers of three related species.
Two of the three species were collected in the Gaspé Peninsula *all from the same
pasture, and picked on the same day and measured at the same time
by the same person with the same apparatus*.

The data set consists of 50 samples from each of three species of Iris
(Iris setosa, Iris virginica and Iris versicolor). Four features were measured from
each sample: the length and the width of the sepals and petals, in centimetres.
Based on the combination of these four features,
Fisher developed a linear discriminant model to distinguish the species
from each other.


    Frame iris = Datasets.loadIrisDataset();
    iris.printSummary();

This is the summary of this data set:

     > printSummary(frame, [sepal-length, sepal-width, petal-length, petal-width, class])
    rowCount: 150
    complete: 150/150
    varCount: 5
    varNames:

     0. sepal-length : NUMERIC |  2. petal-length : NUMERIC |  4. class : NOMINAL |
     1.  sepal-width : NUMERIC |  3.  petal-width : NUMERIC |

       sepal-length      sepal-width     petal-length      petal-width            class
       Min. : 4.300     Min. : 2.000     Min. : 1.000     Min. : 0.100      setosa : 50
    1st Qu. : 5.100  1st Qu. : 2.800  1st Qu. : 1.600  1st Qu. : 0.300  versicolor : 50
     Median : 5.800   Median : 3.000   Median : 4.350   Median : 1.300   virginica : 50
       Mean : 5.843     Mean : 3.057     Mean : 3.758     Mean : 1.199
    2nd Qu. : 6.400  2nd Qu. : 3.300  2nd Qu. : 5.100  2nd Qu. : 1.800
       Max. : 7.900     Max. : 4.400     Max. : 6.900     Max. : 2.500


## Box plots

![test text](../images/graphics-boxplot-iris.png)
