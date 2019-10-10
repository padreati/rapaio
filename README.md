
<table style="border: none">
<tr><td>

![rapaio logo](./docs/logo/logo-medium.png)
</td>
<td>

### Disambiguation

1.  (Italian dictionary) *Field of turnips. It is also a place 
where there is confusion, where tricks and sims are plotted.*

2.  (Computer science) *Statistics, data mining and machine learning 
library written in Java.*
</td>
</tr>
</table>
<table style="border: none">
<tr><td>

##### Try it online
 
[![Launch rapaio with IJava binder jupyter](images/launch-binder.svg)](https://mybinder.org/v2/gh/padreati/rapaio-notebooks/master) 
[![Launch rapaio with IJava binder jupyter lab](images/launch-binder-lab.svg)](https://mybinder.org/v2/gh/padreati/rapaio-notebooks/master?urlpath=lab)
</td>
<td>

##### Build status

[![build status](https://travis-ci.org/padreati/rapaio.svg?branch=master)](https://travis-ci.org/padreati/rapaio)
[![codecov.io](https://codecov.io/github/padreati/rapaio/coverage.svg?branch=master)](https://codecov.io/github/padreati/rapaio?branch=master)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/c478ddddea484a39a7225a3fd98b3d80)](https://www.codacy.com/manual/padreati/rapaio?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=padreati/rapaio&amp;utm_campaign=Badge_Grade)
</td>
</tr>
</table>

##### Installation

Library can be used through maven. Last published release on maven central is 2.0.0.

       <dependency>
         <groupId>io.github.padreati</groupId>
         <artifactId>rapaio</artifactId>
         <version>2.0.0</version>
       </dependency>

Latest snapshot is published 2.0.1-SNAPSHOP and is published on oss-sonatype. 

Another way to use the library is in jupyter / jupyter-lab notebooks. This is excellent for experimenting with short interactive 
notebooks or to document the idea you are working on. You have to install jupyter / jupyter-lab and IJava kernel. For more information
on that you can follow the instruction from [here](https://github.com/SpencerPark/IJava#installing). The following notation is
 specific to IJava kernel jupyter notation. 

    %mavenRepo oss-sonatype-snapshots https://oss.sonatype.org/content/repositories/snapshots/
    %maven io.github.padreati:rapaio:2.0.1-SNAPSHOT    

The last option to use the library is do download the release files from this repository. If you use IntelliJ Idea IDE, you can use 
also the rapaio-studio plugin, which was developed as a companion for folks who like to use the IDE. The plugin has also a github 
[repository](https://github.com/padreati/rapaio-studio).  

##### Documentation

**Rapaio Tutorials** is now published in the form of IPython notebooks format. 
Please inspect the tutorials folder of 
[rapaio-notebooks repository](https://github.com/padreati/rapaio-notebooks). 

There is also a [Tutorial on Kaggle's Titanic Competition
](https://aureliantutuianu.gitbooks.io/rapaio-manual/content/kaggle-titanic.html)!

##### What gap *rapaio* tries to fill?

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

##### Acknowledgements

Many thanks to JetBrains for providing an open source license for their [Java IDE](https://www.jetbrains.com/idea/).
Many thanks to SpencerPark for the java kernel he realized [IJava jupyter kernel](https://github.com/SpencerPark/IJava).