C4.5 classifier
================

C4.5 classifier was created by Quinlan, and is the next generation of decision tree
after ID3.

The main improvements over ID3 are:
- handles numeric values
- handles missing values
- has a pruning procedure

There are also some features described in his paper which could be implemented:

- **windowing** which is an iterative way to build trees from a limited amount of the
training instances; the built tree is tested against the remaining instances from
the train set and the performance on the remaining instances ais used as a
criterion for selecting the best tree.

- **grouping option** allows the tree to consider nodes with multiple test labels;
for example if at a given node the test attribute is a nominal attribute A, with
a_1, .. a_h labels, the tree would normally build h nodes, however grouping option
allows to group together children nodes (and their data sets) if the predicted
label is the same. this option makes a step in the direction given by CART trees

- **max nodes option** allows one to early stop the growth of a given tree in order
 to not overfit; this is somehow a weaker way to try not to overfit (other than pruning)
 which has the main advantage that is computationally faster

 These are the features which are **NOT** implemented:
 - pruning procedure
 - windowing option
 - grouping option
 - max nodes option
- column selector option (which can enable C45 to be used in RandomForests)
