
M2.zip reflects the state of a user's maven home after running the examples "heating" & "lighting".

Before running the examples, the maven home originally contained only an empty folder "repository"
and a file "settings.xml" that allowed access to the universAAL nexus server.

Having a local repository equivalent to this zip file, the examples should run even in offline mode.
The offline mode can be simulated by changing the file "settings.xml" the following way:

	<settings ...>                   <!-- "..." indicates omitted stuff for brevity -->

		<mirrors>
			<mirror>
				<id>artifactory</id>
				<mirrorOf>*</mirrorOf>
				<url>file:///D:/Saied/.m2/repository</url>    <!-- change this path -->
				<name>Artifactory</name>
			</mirror>
		</mirrors>


		<!-- profiles>
			...
		</profiles -->


		<!-- proxies>
			...
		</proxies -->


		<!-- servers>
			...
		</servers -->

	</settings>

	That is, all profiles, proxies, and servers are commented out. For testing the offline mode,
        you must change the content of the "url" tag to point to your local repository. But all other
        values in the specified mirror should be kept the same.
