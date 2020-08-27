val sparkVersion = "2.4.6"
lazy val sparkCore = "org.apache.spark" %% "spark-core" % sparkVersion
lazy val sparkStreamming = "org.apache.spark" %% "spark-streaming" % sparkVersion
lazy val sparkStreamingKafka = "org.apache.spark" %% "spark-streaming-kafka-0-10" % sparkVersion
lazy val sparkSql = "org.apache.spark" %% "spark-sql" % sparkVersion
lazy val sparkHive = "org.apache.spark" %% "spark-hive" % sparkVersion
lazy val kafkaClient = "org.apache.kafka" % "kafka-clients" % "2.3.0"


ThisBuild / scalaVersion := "2.11.12"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"
ThisBuild / organizationName := "example"
lazy val root = (project in file("."))
  .settings(
    name := "SparKafkaStreaming",
    libraryDependencies += sparkCore,
    libraryDependencies += sparkStreamming,
    libraryDependencies += sparkStreamingKafka,
    libraryDependencies += kafkaClient,
    libraryDependencies += sparkSql,
    libraryDependencies += sparkHive)

