This java implementation is a refactory of libsvm for java forked at version 325.
The copyright is retained in libsvm-COPYRIGHT in the same source.


    Chih-Chung Chang and Chih-Jen Lin, LIBSVM : a library for support vector machines. ACM Transactions on Intelligent Systems and Technology, 2:27:1--27:27, 2011. Software available at http://www.csie.ntu.edu.tw/~cjlin/libsvm

The bibtex format

    @article{CC01a,
    author = {Chang, Chih-Chung and Lin, Chih-Jen},
    title = {{LIBSVM}: A library for support vector machines},
    journal = {ACM Transactions on Intelligent Systems and Technology},
    volume = {2},
    issue = {3},
    year = {2011},
    pages = {27:1--27:27},
    note =	 {Software available at \url{http://www.csie.ntu.edu.tw/~cjlin/libsvm}}
    }

The purpose of refactory is to have a proper integration in rapaio and also a complete rewrite using 
OOP idiomatic Java and also to extend the possibilities.

In this very moment the only extension is by allowing other kernel functions to be used and to use double 
floating point values for computing. We hope to make this libsvm version valuable as a sign of recognition
and respect to the authors and to the whole open source community. 