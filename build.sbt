import Dependencies._
import net.moznion.sbt.spotless.config.{ KotlinConfig, KtlintConfig }

lazy val root = (project in file("."))
  .settings(
    Settings.baseSettings,
    name := "adceet-root"
  ).aggregate(`write-api-base`, `write-api-server-scala`, `write-api-server-kotlin`)

lazy val `write-api-base` = (project in file("write-api-base"))
  .settings(
    Settings.baseSettings
  ).settings(
    name := "adceet-write-api-base",
    libraryDependencies ++= Seq(
      typesafeAkka.akkaPersistenceTyped,
      j5ik2o.akkaPersistenceDynamoDBJournal,
      j5ik2o.akkaPersistenceDynamoDBSnapshot,
      kamon.core,
      airframe.ulid,
      logback.logbackClassic,
      jakarta.rsApi,
      swaggerAkkaHttp.swaggerAkkaHttp,
      megard.akkaHttpCors,
      typesafeAkka.akkaHttp,
      typesafeAkka.akkaHttpSprayJson,
      typesafeAkka.akkaHttpJackson,
      typesafeAkka.akkaSlf4j,
      typesafeAkka.akkaActorTyped,
      typesafeAkka.akkaStreamTyped,
      typesafeAkka.akkaClusterTyped,
      typesafeAkka.akkaClusterShardingTyped,
      typesafeAkka.akkaSerializationJackson,
      typesafeAkka.akkaDiscovery,
      lightbend.akkaManagement,
      lightbend.akkaManagementClusterHttp,
      lightbend.akkaManagementClusterBootstrap,
      lightbend.akkaDiscoveryAwsApiAsync,
      fasterXmlJackson.scala,
      kamon.statusPage,
      kamon.akka,
      kamon.akkaHttp,
      kamon.systemMetrics,
      kamon.logback,
      kamon.datadog,
      aichler.jupiterInterface(JupiterKeys.jupiterVersion.value) % Test,
      scalatest.scalatest                                        % Test,
      jupiter.jupiterApi                                         % Test,
      jupiter.jupiter                                            % Test,
      jupiter.jupiterMigrationSupport                            % Test,
      typesafeAkka.actorTestkitTyped                             % Test,
      typesafeAkka.streamTestkit                                 % Test,
      typesafeAkka.httpTestkit                                   % Test,
      typesafeAkka.multiNodeTestkit                              % Test,
      awaitility.awaitility                                      % Test,
      commonsIO.commonsIO                                        % Test,
      // テストでは使っていないので削除してもよい
      fusesource.leveldbjniAll % Test,
      iq80LevelDb.leveldb      % Test
    ),
  )

lazy val `write-api-server-scala` = (project in file("write-api-server-scala"))
  .enablePlugins(JavaAgent, JavaAppPackaging, EcrPlugin, MultiJvmPlugin)
  .configs(MultiJvm)
  .settings(
    Settings.baseSettings,
    Settings.multiJvmSettings,
    Settings.dockerCommonSettings,
    Settings.ecrSettings
  )
  .settings(
    name := "adceet-write-api-server-scala",
    Compile / run / mainClass := Some("com.github.j5ik2o.api.write.Main"),
    dockerEntrypoint := Seq(s"/opt/docker/bin/${name.value}"),
    dockerExposedPorts := Seq(8081, 8558, 25520),
    javaAgents += "io.kamon" % "kanela-agent" % "1.0.14",
    run / javaOptions ++= Seq(
      s"-Dcom.sun.management.jmxremote.port=${sys.env.getOrElse("JMX_PORT", "8999")}",
      "-Dcom.sun.management.jmxremote.authenticate=false",
      "-Dcom.sun.management.jmxremote.ssl=false",
      "-Dcom.sun.management.jmxremote.local.only=false",
      "-Dcom.sun.management.jmxremote",
      "-Xms1024m",
      "-Xmx1024m",
      "-Djava.library.path=./target/native"
    ),
    Universal / javaOptions ++= Seq(
      "-Dcom.sun.management.jmxremote",
      "-Dcom.sun.management.jmxremote.local.only=true",
      "-Dcom.sun.management.jmxremote.authenticate=false",
      "-Dorg.aspectj.tracing.factory=default"
    ),
    Test / publishArtifact := false,
    run / fork := false,
    Test / parallelExecution := false,
    Global / cancelable := false
  ).dependsOn(`write-api-base` % "compile->compile;test->test")

lazy val `write-api-server-kotlin` = (project in file("write-api-server-kotlin"))
  .enablePlugins(JavaAgent, JavaAppPackaging, EcrPlugin, MultiJvmPlugin)
  .configs(MultiJvm)
  .settings(
    Settings.baseSettings,
    Settings.kotlinSettings,
    Settings.javaSettings,
    Settings.multiJvmSettings,
    Settings.dockerCommonSettings,
    Settings.ecrSettings
  )
  .settings(
    name := "adceet-write-api-server-kotlin",
    Compile / run / mainClass := Some("com.github.j5ik2o.api.write.Main"),
    dockerEntrypoint := Seq(s"/opt/docker/bin/${name.value}"),
    dockerExposedPorts := Seq(8081, 8558, 25520),
    javaAgents += "io.kamon" % "kanela-agent" % "1.0.14",
    run / javaOptions ++= Seq(
      s"-Dcom.sun.management.jmxremote.port=${sys.env.getOrElse("JMX_PORT", "8999")}",
      "-Dcom.sun.management.jmxremote.authenticate=false",
      "-Dcom.sun.management.jmxremote.ssl=false",
      "-Dcom.sun.management.jmxremote.local.only=false",
      "-Dcom.sun.management.jmxremote",
      "-Xms1024m",
      "-Xmx1024m",
      "-Djava.library.path=./target/native"
    ),
    Universal / javaOptions ++= Seq(
      "-Dcom.sun.management.jmxremote",
      "-Dcom.sun.management.jmxremote.local.only=true",
      "-Dcom.sun.management.jmxremote.authenticate=false",
      "-Dorg.aspectj.tracing.factory=default"
    ),
    // for Kotlin
    libraryDependencies ++= Seq(
      fasterXmlJackson.kotlin,
      kodeinDI.kodeinDIJvm,
      kotlinx.coroutinesCoreJvm,
      xenomachina.kotlinArgParser,
      arrowKt.arrowCore,
      vavr.varKotlin,
      mockk.mockk            % Test,
      kotlinx.coroutinesTest % Test
    ),
    spotlessKotlin := KotlinConfig(
      target = Seq("src/**/*.kt", "test/**/*.kt"),
      ktlint = KtlintConfig(version = "0.40.0", userData = Map("indent_size" -> "2", "continuation_indent_size" -> "2"))
    ),
    Test / publishArtifact := false,
    run / fork := false,
    Test / parallelExecution := false,
    Global / cancelable := false
  ).dependsOn(`write-api-base` % "compile->compile;test->test")

// --- Custom commands
addCommandAlias("lint", ";spotlessCheck;scalafmtCheck;test:scalafmtCheck;scalafmtSbtCheck;scalafixAll --check")
addCommandAlias("fmt", ";spotlessApply;scalafmtAll;scalafmtSbt;scalafix RemoveUnused")
