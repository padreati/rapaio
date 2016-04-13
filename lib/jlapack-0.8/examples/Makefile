JAVAC=javac

BLAS_CLASSPATH=.:../f2jutil.jar:../blas.jar
SIMPLE_BLAS_CLASSPATH=$(BLAS_CLASSPATH):../blas_simple.jar
LAPACK_CLASSPATH=$(BLAS_CLASSPATH):../lapack.jar:../xerbla.jar
SIMPLE_LAPACK_CLASSPATH=$(LAPACK_CLASSPATH):../lapack_simple.jar

all: DdotTest.class SimpleDdotTest.class DgesvdTest.class SimpleDgesvdTest.class \
     DlaruvTest.class DsygvTest.class DstevrTest.class SimpleDsygvTest.class

DdotTest.class: DdotTest.java
	$(JAVAC) -classpath $(BLAS_CLASSPATH) DdotTest.java
SimpleDdotTest.class: SimpleDdotTest.java
	$(JAVAC) -classpath $(SIMPLE_BLAS_CLASSPATH) SimpleDdotTest.java
DlaruvTest.class: DlaruvTest.java
	$(JAVAC) -classpath $(LAPACK_CLASSPATH) DlaruvTest.java
DsygvTest.class: DsygvTest.java
	$(JAVAC) -classpath $(LAPACK_CLASSPATH) DsygvTest.java
DstevrTest.class: DstevrTest.java
	$(JAVAC) -classpath $(LAPACK_CLASSPATH) DstevrTest.java
SimpleDsygvTest.class: SimpleDsygvTest.java
	$(JAVAC) -classpath $(SIMPLE_LAPACK_CLASSPATH) SimpleDsygvTest.java
DgesvdTest.class: DgesvdTest.java
	$(JAVAC) -classpath $(LAPACK_CLASSPATH) DgesvdTest.java
SimpleDgesvdTest.class: SimpleDgesvdTest.java
	$(JAVAC) -classpath $(SIMPLE_LAPACK_CLASSPATH) SimpleDgesvdTest.java

clean:
	/bin/rm -f *.class
