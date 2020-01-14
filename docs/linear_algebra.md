# Linear Algebra: Design and Implementation

## Introduction

Linear algebra tools and associated data structures are basic requirements for any 
decent library which aims to manipulate data and solve scientific problems. 
Building a library which needs linear algebra tools poses many problems and choices.

A first immediate choice is to use an existent library for this purposes.
Of course, the invested time saved by this option is huge and manifests also in other 
directions, like correctness, performance and variety of tools. Most options points
out to JVM-external options (written is C / C++ / Fortran) and there are also some 
JVM hosted library available. There are no doubts that there are plenty very good 
options out there and aiming to write a new implementation which is better then 
any other library at least in one aspect it is a very ambitions and eccentric statement.
I will not make that statement. 

Still, we want rapaio to have it's own implementation. The sole reason is that I still 
believe that Java should be enough for scientific computing and should go on par
with C/C++ libraries (thus all external libraries are not considered an option) and 
and if we find a good design of the linear algebra library we can build incrementally 
on performance an optimizations. As a starting point we will use Jama reference 
implementation.

## Design choices

There are some ideas which the design must met:

* The API must be exposed through a small set of interfaces, independent of their implementations
* The linear algebra library is an in-memory data structures library
* Initially the library must provide a set of implementations which must be correct but not necessary 
optimal in terms of memory and computation
* The base set of data structures will be implemented using only dimension management methods and
getter and setter methods 
* Any specialized implementation derived should be immediately usable once dimension management and
getter and setter methods are implemented (this makes possible incremental development of new 
implementations)
* We want to explore multiple types of implementations and make possible performance comparisons, 
for example a matrix can be implemented as an array of arrays (natural way in Java) or strided 
column or row oriented arrays

## Base classes and interfaces

Let's take double matrices. The following classes are involved:

* *DMatrix interface*: contains the documented API any implementation must obey
* *AbstractDMatrix class*: contains implementations based solely on getter, setter and rowCount and colCount, 
this is an abstract class
* *BaseDMatrix class*: contains base implementation of methods on which AbstractDMatrix relies and 
serves as a reference implementation purpose
* *SolidDMatrix class*: implements an array of arrays implementation, with methods which avoids 
getter and setter if possible and optimized code
* *StrideColDMatrix class*: implements a stride column oriented matrix with optimized methods 

and perhaps other implementation classes perhaps for specializations.