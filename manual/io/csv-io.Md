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
a `rapaio.io.Csv` object and calls it's read method.

    Frame iris = new Csv().read(Datasets.class, "iris-r.csv");
    iris.mapRows(0, 1, 50, 51, 100, 101).printLines();
