# Welcome

Rapaio is a java library which provides a comprehensive set of tools for 
data manipulation, linear algebra, statistics and machine learning. 
It contains a comprehensive and growing list of tools and models
for various tasks. 

The design of usage API followed two main goals: useful ans succint.

An example:

```java
String url = "http://faculty.marshall.usc.edu/gareth-james/ISL/Advertising.csv";
Frame df = Csv.instance().withSkipCols(0).readUrl(url);
CForest cf = CForest.newModel().runs.set(100);
ClassifierResult prediction = cf.fit(df, "class"),predict(df);

Confusion.of
```


