name := """commonlib"""

scalaVersion := "2.11.8"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava,PlayEbean)

crossPaths := false
isSnapshot := true

libraryDependencies ++= Seq(
  javaWs,
  "com.github.fge" % "json-patch" % "1.9",
  "org.mongodb" % "mongo-java-driver" % "3.2.2",
  "commons-io" % "commons-io" % "2.4",
  "net.logstash.logback" % "logstash-logback-encoder" % "4.7",
  "org.apache.commons" % "commons-pool2" % "2.4.2",
  "com.github.fge" % "json-patch" % "1.9",
  "commons-httpclient" % "commons-httpclient" % "3.1",

   
  //logger
  "org.codehaus.janino" % "janino" % "3.0.6",
  "org.codehaus.groovy" % "groovy-all" % "2.4.8"
   
)


// Compile the project before generating Eclipse files, so that generated .scala or .class files for views and routes are present
EclipseKeys.preTasks := Seq(compile in Compile)
EclipseKeys.projectFlavor := EclipseProjectFlavor.Java           // Java project. Don't expect Scala IDE
EclipseKeys.createSrc := EclipseCreateSrc.ValueSet(EclipseCreateSrc.ManagedClasses, EclipseCreateSrc.ManagedResources)  // Use .class files instead of generated .scala files for views and routes
