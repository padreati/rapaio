####

![codecov.io](https://codecov.io/gh/padreati/rapaio/branch/master/graphs/tree.svg)

#### Info about gpg key

    /home/local/ANT/tutuianu/.gnupg/pubring.gpg
    -------------------------------------------
    pub   rsa2048/3366B7F3 2019-08-27 [SC] [expires: 2024-08-25]
    uid         [ultimate] Aurelian Tutuianu (Rapaio) <padreati@yahoo.com>
    sub   rsa2048/AA958C71 2019-08-27 [E] [expires: 2024-08-25]



#### Creating a Detached Signature File
Problem: 
* You want to sign a file digitally, but have the signature reside in 
a separate file.

Solution:
* To create a binary-format detached signature, myfile.sig:

      $ gpg --detach-sign myfile
      
* To create an ASCII-format detached signature, myfile.asc:

      $ gpg --detach-sign -a myfile
      
* deploy 
      
      $ mvn -f pom-maven-release.xml clean verify gpg:sign install:install deploy:deploy