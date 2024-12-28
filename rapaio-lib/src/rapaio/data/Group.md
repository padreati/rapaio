# Group

This document describes how work the group by facility and how can be used.

Group is a data structure which allows aggregating data from a Frame. The Group construct can be 
applied on a specific Frame instance, and it is not transferable to other data frames.

## Index variables and aggregating functions

There are two important constructs when working with a group: index variables and aggregating functions.

Index variables are the variables who's unique values identifies a group of rows. If there is more than 
one index variable, the index is composed and consists of multiple nested levels. Thus, the order in which 
the index variables are specified is important, since each index variable is nested in the previously 
declared index variable.

The unique combination of values from the index variables identifies a group of rows from the original 
data frame. On each group of rows one or multiple aggregating functions can be applied. Each aggregating 
function is applied on the corresponding group, and one or multiple values are generated from that. 
It is important to understand that, while the group of rows which corresponds to a unique combination of values, 
a single row is obtained for each group of rows. There is however the possibility to generate a single row 
with multiple variable values, but not multiple rows.

TODO:
- normalization level
- to Frame and unstack level

It would be interesting to refactor everything to allow grouping with custom code.