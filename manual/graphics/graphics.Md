
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

## Brief overview

Drawing of graphical figures or text output is handled by classes which implements
interface `rapaio.printer.Printer`. There are some built-in implementations of this interface.
The default one is `StandardPrinter`, which allows printing text at console and
drawing graphics in a Swing modal window. Another used implementation is `IdeaPrinter`,
which is similar with standard printer for text output, but for graphics drawing implements
a TCP/IP protocol with serialization which allows plug-ins like `rapaio-studio` to handle
graphics drawing.

A printer can be instantiated by itself by a handy way of using it is by the means of
the utility class called `rapaio.sys.WS` (which comes from **W**ork **W**pace). This is
an utility class which offers static shortcut methods yo allow one to simulate a working
session. This class has an instance of a printer implementation (which can be changes by
`WS.setPrinter` method) and shortcuts for text output and drawing.

We are interested here in methods for drawing, so the workspace contains methods for this
purpose. All of those methods works with a construct which contains all the indications
regarding what kind of graphic should be built. This construct implements interface
`rapaio.graphics.base.Figure`. Thus a figure is a construct which allows the printer system
to obtain images which can be later drawn over graphical surfaces. Also, there are methods
which allows one to obtain an image directly from a figure, or store an image directly on
a disk file.

Here are the shortcut methods implemented in `WS` utility:

* *void draw(Figure figure)* - draws a figure in a printing system; the size of drawing is
either adaptive of has the default size of a graphical image contained in the printer
implementation.

* *void draw(Figure figure, int width, int height)* - draws a figure with dimensions specified
by width and height

* *BufferedImage buildImage(Figure figure)* - builds an image from a figure with dimensions
specified as default width and default height from printer system

* *BufferedImage buildImage(Figure figure, int width, int height)* - builds an image from
a figure with specified dimensions

* *void saveImage(Figure figure, int width, int height, String fileName)* - builds an image
with specified dimensions by width and height from a given figure, and save the image into
a *png* file with the specified file name

* *void saveImage(Figure figure, int width, int height, OutputStream os)* - builds an image
with specified dimensions by width and height, and send the image in *png* format to the
specified output stream (could be a file or could be sent over network)


Now in order to build figures one can instantiate and compose figures directly in a
pure Java way. Here is an example:

    Plot plot = new Plot();
    plot.add(new Histogram(iris.var("sepal-length"), 0, 10, bins(40), color(10), prob(true)));
    plot.add(new DensityLine(iris.var("sepal-length"), lwd(2), color(2)));
    WS.draw(plot);


It is simple enough and structured. However graphical tools offers also a class named
`rapaio.graphics.Plotter` which has a lot of shortcut methods to help one to
simplify the above code. Here is an example:

    WS.draw(hist(iris.var("sepal-length"), 0, 10, bins(40), color(10), prob(true))
            .densityLine(iris.var("sepal-length"), lwd(2), color(2)));


## Box plot

A box plot is a standard way of displaying information about the distribution of
a continuous data variable based on five data summary. The five data summary consists
of: *minimum*, *first quartile*, *median* (*second quartile*), *third quartile* and
*maximum* number summaries.

Box plots might be constructed in different manners by different authors. The common
characteristics for all types of box-plots is *the box*.

Which means that in all
cases the bottom margin of the box lies on first quartile, the top margin of the box
lies on third quartile and the line inside the box lies on median values.

The extensions which comes from the box might differ and rapaio system implements the
version which it's usually named: **Tuckey's box plot**. What is specific to this
box plot is that the whiskers lies at the datum still within 1.5 IQR
(*Interquartile range*).

Any other points above or below whiskers are outliers. Outliers are of two different
types:

* *extreme outliers* - outliers which are at a distance greater or equal than 3*IQR
* *outliers* - outliers which are at a distance greater or equal than 1.5*IQR

### Example 1

**Scope**: *Draw one box plot for each numerical variable from iris data set. We
want each box plot to have a different color and we want some de-saturated colors.*

**Solution**:

    WS.draw(boxPlot(iris.mapVars("0~3"), color(1, 2, 3, 4), alpha(0.5f)))

![One box plot for each numerical variable](../images/graphics-boxplot-iris-frame.png)

`iris.mapVars("0~3")` obtain a data set from `iris` data set, by keeping only the
first 4 variables. We do that using the range notation (index of the start variable,
concatenation symbol `~`, index of the last variable inclusive). Pay attention that
variable indexes are 0 based.

`color(1, 2, 3, 4)` we use colors from the current color palette, indexed with the
specified integer values.

`alpha(0.3f)` we de-saturate the drawing keeping only 0.3 of the actual color.

### Example 2

**Scope**: *In order to identify the overlap between values of `sepal-length` variable
from iris data set, we draw one box plot for each segment of the nominal `class`
variable, and add a title*

**Solution**:

    WS.draw(boxPlot(iris.var("sepal-length"), iris.var("class"))
        .title("sepal-length separation"));

`iris.var("sepal-length")` is the variable named `sepal-length` from `iris` data set,
 which is the numerical variable to be segmented and later box-plotted

 `iris.var("class")` specifies the segment discriminator, depending on the levels of
 this variable, the same number of levels will be created

 `.title("..")` adds a title to the box plot

![Box plots for the "sepal-length" variable, but discriminated by class](../images/graphics-boxplot-iris-sepal-length.png)

## Points (XY Scatter plot)

We can study the relation between two numerical variables by drawing one point for
each instance.

### Example 1

**Scope**: *Study which is the relation between `petal-length` and `sepal-length`
from `iris` data set.*

**Solution**:

        WS.draw(points(iris.var("petal-length"), iris.var("petal-width")));

`iris.var("petal-length")` - variable used to define horizontal axis

`iris.var("petal-width")` - variable used to define vertical axis


![Scatter XY points for `petal-length` and `petal-width` variables](../images/graphics-points-iris-1.png)

### Example 2

**Scope**: *Study which is the relation between `petal-length` and `sepal-length`
from `iris` data set. Color each point with a different color corresponding with
value from variable `class` and add a legend for colors.*

**Solution**:

        WS.draw(points(iris.var("petal-length"), iris.var("petal-width"),
            color(iris.var("class")), pch(2))
                .legend(1.5, 2.2, labels("setosa", "versicolor", "virginica"));

![Scatter XY points for `petal-length` and `petal-width` variables with colors and legend](../images/graphics-points-iris-2.png)


`iris.var("petal-length")` - variable used to define horizontal axis

`iris.var("petal-width")` - variable used to define vertical axis

`color(iris.var("class"))` - norminal variable which provides indexes to select colors from
current palette

`pch(2)` - select the type of figure used to draw points (in this case is a circle
filled with solid color and a black border)

`legend(1.5, 2.2, labels("setosa", "versicolor", "virginica"))` - adds a legend at the specific
position specified in the data range; labels are specified by parameter, colors are taken as
default from current palette starting with 1

## Histogram

A histogram is a graphical representation of the distribution of a continuous variable.
The histogram is only an estimation of the distribution. To construct a histogram you have
to *bin* the range of values from the variable in a sequence of equal length intervals,
and later on counting the values from each bin. Histograms can display counts, or can display
proportions which are counts divided by the total number of values.

Because the histogram uses bins that the main parameter of a histogram is the bin width. The bin
width is specified indirectly by specifying the number of bins to be computed. To compute
the width of the bins we need the number of bins and the minimum and maximum value of the
range of values. The range of values can be computed automatically from data or they can be
specified specifically when the histogram is built.

Also, the number of bins can be omited, in which case this number is estimated also from data,
using Freedman-Diaconis rule (see [Freedman-Diaconis wikipedia page](https://en.wikipedia.org/wiki/Freedman%E2%80%93Diaconis_rule))
for more details.

### Example 1

**Scope**: *Build a histogram with default values to estimate the pdf of `sepal-length`
variable from `iris` data set.*

**Solution**:

    WS.draw(hist(iris.var("sepal-length")));

![Histogram of `sepal-length` variable from iris data set](../images/graphics-hist-iris-1.png)

### Example 2

**Scope**: *Build two overlapped histograms with default values to estimate the
pdf of `sepal-length` and `petal-length` variables from `iris` data set.
We want to get bins in range (0-10) of width 0.25, colored with red, and blue, with a big
transparency for visibility*

**Solution**:

    WS.draw(plot(alpha(0.3f))
            .hist(iris.var("sepal-length"), 0, 10, bins(40), color(1))
            .hist(iris.var("petal-length"), 0, 10, bins(40), color(2))
            .legend(7, 20, labels("sepal-length", "petal-length"), color(1, 2))
            .xLab("variable"));

![Histogram of `sepal-length`, `petal-length` variable from iris data set](../images/graphics-hist-iris-2.png)

* `plot(alpha(0.3f))` - builds an empty plot; this is used only to pass default values for alpha
for all plot components, otherwise the plot construct would not be needed
* `hist` - adds a histogram to the current plot
* `iris.var("sepal-length")` - variable used to build histogram
* `0, 10` - specifies the range used to compute bins
* `bins(40)` - specifies the number of bins for histogram
* `color(1)` - specifies the color to draw the histogram, which is the color indexed with 1 in
color palette (in this case is red)
* `legend(7, 20, ...)` - draws a legend at the specified coordinates, values are in the units
specified by data
* `labels(..)` - specifies labels for legend
* `color(1, 2)` - specifies color for legend
* `xLab` = specifies label text for horizontal axis

## Density line

A density line is a graphical representation of the distribution of a
continuous variable. The density line is similar with a histogram in purpose but
it has a different strategy to build the estimate. The density line plot component implements
a *kernel density estimator* which is basically a non-parametric smoothing method, named
also Parzen-Rosenblatt window method.

There are two main parameters for a density line: bandwidth and base density kernel function.
The default bandwidth is computed according with Silverman's rule of thumb (more details on
[Wikipedia kernel density page](https://en.wikipedia.org/wiki/Kernel_density_estimation)).
The default kernel function is the Gaussian pdf.

Kernel function estimators can be constructed using various kernel functions like Gaussian,
uniform, triangular, Epanechnikov, cosine, tricube, triweight, biweight. All of them are
available in rapaio library and also some custom can be built.

### Example 1

**Scope**: *Illustrate the process of building the KDE estimation*
**Solution**:

        // this is our sample
        Numeric x = Numeric.wrap(-2.1, -1.3, -0.4, 1.9, 5.1, 6.2);

        // declare a bandwidth for smoothing
        double bw = 1.25;

        // build a density line
        Plot p = densityLine(x, bw);

        // for each point draw a normal distribution
        x.stream().forEach(s -> p.funLine(xi -> new Normal(s.value(), bw).pdf(xi) / x.rowCount(), color(1)));
        WS.draw(p);

![KDE construction from a sample of points]("../images/graphics-kde-1.png")

With red are depicted the kernel functions used to spread probability around sample points.
With black is depicted the kernel density estimation which is the sum of all individual
kernel functions.

### Example 2

**Scope**: *Estimate density of iris `sepal-length` variable by histogram and density function*
**Solution**:

    WS.draw(hist(iris.var("sepal-length"), prob(true)).densityLine(iris.var("sepal-length")));

![Density function and histogram estimation](../images/graphics-kde-2.png)

### Example 3

**Scope**: *Build multiple kernel density estimates for various
bandwidth values*
**Solution**:

    Plot p2 = plot();
    DoubleStream.iterate(0.05, xi -> xi + 0.02).limit(20)
            .forEach(v -> p2.densityLine(iris.var("sepal-length"), v));
    WS.draw(p2);

![Multiple kernel density estimates with various bandwith parameter values](../graphics/graphics-kde-3.png)