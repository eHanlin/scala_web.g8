name := "$name;format=\"camel\"$"

version := "0.0.1"

scalaVersion := "2.11.6"

val springVersion = "4.1.6.RELEASE"
val jettyVersion = "9.2.10.v20150310"
val json4sVersion = "3.2.11"

libraryDependencies ++= Seq(
  "javax.servlet" % "javax.servlet-api" % "3.1.0" % Provided,
  "org.springframework" % "spring-webmvc" % springVersion,
  "org.springframework" % "spring-aspects" % springVersion,
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.5.2",
  "org.json4s" %% "json4s-native" % json4sVersion,
  "org.json4s" %% "json4s-jackson" % json4sVersion,
  "org.mongodb" %% "casbah" % "2.8.0",
  "org.thymeleaf" % "thymeleaf-spring4" % "2.1.4.RELEASE",
  "nz.net.ultraq.thymeleaf" % "thymeleaf-layout-dialect" % "1.2.7"
)

libraryDependencies ++= Seq(
  "org.eclipse.jetty" % "jetty-webapp" % jettyVersion % "container",
  "org.eclipse.jetty" % "jetty-plus" % jettyVersion % "container"
)

initialCommands := """
  import com.mongodb.casbah.Imports._
  import $name;format="camel"$.repository._
  import $name;format="camel"$.service._
  import org.springframework.context.annotation.AnnotationConfigApplicationContext
  import config.AppConfig
  val ctx: AnnotationConfigApplicationContext = new AnnotationConfigApplicationContext
  ctx.register(classOf[AppConfig])
  ctx.refresh
"""

seq(webSettings :_*)