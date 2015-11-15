## CSV - reading and writing

Reading and writing CSV files is an important feature for any
system which works with data. The reason for its importance is the
simplicity of the file format and its popularity.

Rapaio library offers support for both reading and writing operations.
It has many features and allows flexibility. However we read a file
only into a data frame, and we write a csv file only from a data frame.
This might look like a constraint in the beginning, but it comes natural
since both are tabular data having the only difference the fact that
one operates in the memory of a program and the other one is persisted
on disk.

#### Simple read/write data frames from/into Csv files

We can read a file with the default options simply by instantiating
a `rapaio.io.Csv` object and calls one of `read` methods.

    Frame iris = new Csv().read(Datasets.class, "iris-r.csv");

We select only a few rows and inspect what it is inside:

    // use only few rows
    iris = iris.mapRows(0, 1, 50, 51, 100, 101);
    iris.printLines();

with the following output:

     sepal-length  sepal-width  petal-length  petal-width      class
         5.100000     3.500000      1.400000     0.200000     setosa
         4.900000     3.000000      1.400000     0.200000     setosa
         7.000000     3.200000      4.700000     1.400000 versicolor
         6.400000     3.200000      4.500000     1.500000 versicolor
         6.300000     3.300000      6.000000     2.500000  virginica
         5.800000     2.700000      5.100000     1.900000  virginica

Consequently, persisting a data frame into csv file format is also
simple. We simply instantiate a `rapaio.io.Csv` object and call one
of implementation of `write` methods:

    new Csv().write(iris, "/tmp/iris.csv");

If we open the `/tmp/iris.csv` file with an editor, we can discover
that it will have the following content:

    sepal-length,sepal-width,petal-length,petal-width,class
    5.1,3.5,1.4,0.2,setosa
    4.9,3,1.4,0.2,setosa
    7,3.2,4.7,1.4,versicolor
    6.4,3.2,4.5,1.5,versicolor
    6.3,3.3,6,2.5,virginica
    5.8,2.7,5.1,1.9,virginica

#### Various read and write methods for Csv

