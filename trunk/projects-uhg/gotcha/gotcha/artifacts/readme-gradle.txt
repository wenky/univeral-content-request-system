I'm using gradle for local builds, but gradle basically uses the maven structure so the maven despots can have their way.

gateway-core is the prerequisite for basically all the other gateway-* projects. 

the gradle build scripts in each project rely on a local "repository" for the jars, which are in artifacts/lib

gradle used is gradle 0.8

type [gradle uploadArchives] in each folder with a build.gradle. The project will be placed in artifacts/built/<project>