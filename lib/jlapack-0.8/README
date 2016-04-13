
JLAPACK 0.8
May 31, 2007
----------------

This directory should contain the following files:

  README               - this file
  INSTALL              - installation details
  CHANGES              - what has changed in this version
  examples             - directory containing a few examples of calling JLAPACK

The following jar files should exist:

  blas.jar             - the BLAS library
  blas_simple.jar      - the simplified interfaces to BLAS
  lapack.jar           - the LAPACK library
  lapack_simple.jar    - the simplified interfaces to LAPACK
  xerbla.jar           - LAPACK error reporting routine
  f2jutil.jar          - utilities required for running f2j translated code

If you downloaded the 'strict' distribution, there will be
four subdirectories:

  strict_math_lib      - calls java.lang.StrictMath instead of java.lang.Math,
                         but the methods are not declared as strictfp
  strict_fp            - methods are declared strictfp, but does not call
                         java.lang.StrictMath
  strict_both          - methods are declared strictfp and calls 
                         java.lang.StrictMath
  plain                - not strict

Each of the subdirectories will contain all of the jar files mentioned
above.

In addition to raw translations of the numerical routines, the blas_simple
and lapack_simple jar files contain classes that provide a more Java-like
interface to the underlying numerical functions.  There is one such class
for each numerical routine.  The name of the class is simply the function
name in all caps.  For example, the fortran routine 'ddot' is translated
into two classes: Ddot.java and DDOT.java.  Ddot.java contains the actual
translation of the fortran code while DDOT.java contains only a call to
the real ddot (Ddot), but provides a more simple interface.  Since the
interface may have to do matrix transposition and copying for some routines,
it is faster to use the 'raw' numerical routines.

API documentation for the BLAS and LAPACK can be found online at the 
following URL:

  http://www.cs.utk.edu/f2j/docs/html/packages.html

NOTES:

1.  This release has not been tuned for performance - it is a simple,
    automatic translation.

2.  Some scalars must be wrapped in objects.  The wrapper classes are
    located in the org.netlib.util package.  Therefore, your code 
    should contain "import org.netlib.util.*;" to have access to the
    wrappers.

    In addition, your code should import org.netlib.lapack.Blah or
    org.netlib.blas.Blah, where Blah represents the routine your
    code calls.  See the files DdotTest.java and DlaruvTest.java
    for examples.

3.  See the warnings on recompilation in the INSTALL file.

4.  If you are using a JVM with a JIT complier and encounter a
    fault in calling JLAPACK, try turning off the JIT and report
    the problem to f2j@cs.utk.edu.

5.  The appropriate jar files should be in your CLASSPATH.
      f2jutil.jar - should always be included
      blas.jar - include if calling BLAS routines
      lapack.jar - include if calling LAPACK routines
      xerbla.jar - include for LAPACK error handling

    So, if calling LAPACK, you'll want to include all four 
    jar files in your CLASSPATH.
 
    You may customize your error handling by replacing xerbla.jar
    with your own error reporting package.

The following two notes only apply to interfacing with the 'raw'
numerical routines, not the Java style front-ends.

6.  All array arguments are followed by an extra "offset" argument.
    This allows passing array subsections.

7.  It is important to keep this in mind when interfacing Java code
    to the JLAPACK routines:  all multidimensional arrays are mapped 
    to one-dimensional arrays in the translated code and the original 
    column-major layout is maintained.

The following note only applies to using the Java style front-ends.

8.  When you pass Java 2D arrays to one of the interface routines,
    it will make a copy of it and convert it into a linearized 1D
    array to be passed to the underlying numerical routine.  If some
    routine takes two matrices and you pass the same matrix for both
    arguments, the interface will generate two copies of the same
    array rather than the single copy that would normally be provided
    to the underlying routine.  Therefore, some inconsistency in the
    results could occur. 

Contact f2j@cs.utk.edu with any questions, comments, or suggestions.
