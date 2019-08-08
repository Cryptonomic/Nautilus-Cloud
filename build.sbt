name := "nautilus-cloud"

version := "0.1"

scalaVersion := "2.12.8"

val akkaHttpVersion = "10.1.0"
val circeVersion = "0.11.0"
val endpointsVersion = "0.9.0"
val catsVersion = "1.6.0"
val doobieVersion = "0.7.0-M4"
val macwireVersion = "2.3.2"

parallelExecution in Test := false

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

libraryDependencies ++= Seq(
  "org.tpolecat"                       % "doobie-core_2.12"                % doobieVersion,
  "org.tpolecat"                       %% "doobie-postgres"                % doobieVersion,
  "org.tpolecat"                       %% "doobie-scalatest"               % doobieVersion % Test,
  "ch.qos.logback"                     % "logback-classic"                 % "1.2.3",
  "com.typesafe"                       % "config"                          % "1.3.2",
  "com.typesafe.scala-logging"         %% "scala-logging"                  % "3.7.2",
  "com.typesafe.akka"                  %% "akka-http"                      % akkaHttpVersion exclude ("com.typesafe", "config"),
  "com.typesafe.akka"                  %% "akka-http-spray-json"           % akkaHttpVersion exclude ("com.typesafe", "config"),
  "com.softwaremill.akka-http-session" %% "core"                           % "0.5.6",
  "org.scalaj"                         %% "scalaj-http"                    % "2.4.1",
  "com.softwaremill.sttp"              %% "core"                           % "1.5.16",
  "com.github.pureconfig"              %% "pureconfig"                     % "0.10.2",
  "org.typelevel"                      %% "cats-core"                      % catsVersion,
  "io.circe"                           %% "circe-core"                     % circeVersion,
  "io.circe"                           %% "circe-parser"                   % circeVersion,
  "io.circe"                           %% "circe-generic"                  % circeVersion,
  "io.circe"                           %% "circe-generic-extras"           % circeVersion,
  "com.softwaremill.macwire"           %% "macros"                         % macwireVersion % "provided",
  "com.softwaremill.macwire"           %% "util"                           % macwireVersion,
  "com.softwaremill.macwire"           %% "proxy"                          % macwireVersion,
  "com.softwaremill.sttp"              % "core_2.12"                       % "1.5.16",
  "com.softwaremill.sttp"              %% "async-http-client-backend-cats" % "1.5.16",
  "de.heikoseeberger"                  %% "akka-http-circe"                % "1.23.0" exclude ("com.typesafe.akka", "akka-http"),
  "org.julienrf"                       %% "endpoints-algebra"              % endpointsVersion,
  "org.julienrf"                       %% "endpoints-openapi"              % endpointsVersion,
  "org.julienrf"                       %% "endpoints-json-schema-generic"  % endpointsVersion,
  "org.julienrf"                       %% "endpoints-akka-http-server"     % endpointsVersion,
  "io.scalaland"                       %% "chimney"                        % "0.3.0",
  "org.scalatest"                      %% "scalatest"                      % "3.0.4" % Test,
  "com.stephenn"                       %% "scalatest-json-jsonassert"      % "0.0.3" % Test,
  "org.scalamock"                      %% "scalamock"                      % "4.1.0" % Test,
  "ru.yandex.qatools.embed"            % "postgresql-embedded"             % "2.10" % Test,
  "com.typesafe.akka"                  %% "akka-http-testkit"              % akkaHttpVersion % Test exclude ("com.typesafe", "config"),
  "com.github.tomakehurst"             % "wiremock-jre8"                   % "2.23.2" % Test
)

assemblyMergeStrategy in assembly := {
  case "META-INF/io.netty.versions.properties" => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

assemblyJarName in assembly := s"nautilus-cloud-${version.value}.jar"
