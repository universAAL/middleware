Since 18.12.2009, sodapop instances exchange messages in encrypted form using symmetric
cryptography algorithms.

The first implication is for your run configurations: you must add the URL

    wrap:mvn:org.bouncycastle/jce.jdk13/144
    
to the Pax-Cursor configuration at level 2. 

The implementation also needs to have read+write access to a folder ${confdir},
where ${confdir} is taken from the system properties using the following statement:

    System.getProperty(
            "bundles.configuration.location",
            System.getProperty("user.dir"))

That is, it first checks if "-Dbundles.configuration.location=<absolute-path>" was
defined as a JVM argument; if yes, the absolute path given there is used, otherwise
the current working directory is taken.

When the sodapop bundle starts, it checks if a file called sodapop.key exists in
${confdir}/sodapop.osgi; if yes, the key stored in that file is used for doing
the crypto-specific tasks, otherwise it generates a new key and stores it in the
above mentioned file. In the latter case, the following message will appear in
sodapop logs:

    New Key generated. Please copy ${confdir}/sodapop.osgi/sodapop.key to the
    confadmin folder of all the other instances of sodapop in your ensemble!
    
This (making the same key available to all instances) is a necessary step because
of the usage of symmetric cryptography algorithms. The manual installation of the
key instead of exchanging it online is the guarantee for the fact that some
administrator has decided that certain instances of sodapop belong to the same
ensemble.

Experience has shown that the above automatic generation of shared keys may still
lead to generating exceptions during exchange of messages. So, the recommended
way for creating the key is to execute the main method of

    org.universAAL.middleware.sodapop.impl.CryptUtil
    
by right-clicking the corresponding java class in sodapop.osgi and choosing "Run
As -> Java Application". The program creates the file sodapop.key in the working
directory and tests it 10 times. The test must print the following lines in the
console:

    Encrypted: This is the simple test #1
    Decrypted: This is the simple test #1
    Encrypted: This is the simple test #2
    Decrypted: This is the simple test #2
    Encrypted: This is the simple test #3
    Decrypted: This is the simple test #3
    Encrypted: This is the simple test #4
    Decrypted: This is the simple test #4
    Encrypted: This is the simple test #5
    Decrypted: This is the simple test #5
    Encrypted: This is the simple test #6
    Decrypted: This is the simple test #6
    Encrypted: This is the simple test #7
    Decrypted: This is the simple test #7
    Encrypted: This is the simple test #8
    Decrypted: This is the simple test #8
    Encrypted: This is the simple test #9
    Decrypted: This is the simple test #9
    Encrypted: This is the simple test #10
    Decrypted: This is the simple test #10
    
The order of the messages is not significant because the printing of encrypted
(actually the original string before encryption) and decrypted (actually the
resulted string after decryption) messages are done in separate threads.

Before running the program, make sure that from the following code lines:

    line 44: SecretKey mainkey = generateKey(keyFile);
	line 45: SecretKey mainkey = readKey(keyFile);

the first line is uncommented but the second line is commented out. Now you can run
the program. If you see any exceptions in the console, then try the same so long
until there is no exception. Then comment out the code line 44 and uncomment the
line #45 and run the program again for several times. If no exceptions appear after
several times of checking, then you can take that file and install it in the
${confdir}/sodapop.osgi/ of all instances of sodapop that you plan to deploy. If
you see exceptions, then you must repeat all the steps from the beginning.

For the second part of the check (with line 44 commented and line 45 umcommented)
it would be better to make sure that you are using JRE 1.3. The following shows
a set of commands in a console with which I have tried to make sure about this by
changing the PATH and JAVA_HOME environment variables and using an absolute path
for running java:  

D:\> cd \projects\Persona\dev\svn\mw\sodapop.osgi\target\classes

D:target\classes> copy ..\..\sodapop.key .

D:target\classes> set PATH=C:\WINDOWS\system32

D:target\classes> set JAVA_HOME="C:\Program Files (x86)\Java\jre1.3.1_20"

D:target\classes> set CLASSPATH=.;D:\Saied\.m2\repository\org\bouncycastle\jce.jdk13\144\jce.jdk13-144.jar

D:target\classes> %JAVA_HOME%\bin\java org.universAAL.middleware.sodapop.impl.CryptUtil
Encrypted: This is the simple test #1
Decrypted: This is the simple test #1
Encrypted: This is the simple test #2
Decrypted: This is the simple test #2
Encrypted: This is the simple test #3
Decrypted: This is the simple test #3
Encrypted: This is the simple test #4
Decrypted: This is the simple test #4
Encrypted: This is the simple test #5
Decrypted: This is the simple test #5
Encrypted: This is the simple test #6
Decrypted: This is the simple test #6
Encrypted: This is the simple test #7
Decrypted: This is the simple test #7
Encrypted: This is the simple test #8
Decrypted: This is the simple test #8
Encrypted: This is the simple test #9
Decrypted: This is the simple test #9
Encrypted: This is the simple test #10
Decrypted: This is the simple test #10
