package utils

import org.apache.spark.sql.SparkSession

object TableGenerator {
//
//Generamos unas falsas tablas cargadas por un csv en el entorno de spark

  def main(args: Array[String]): Unit = {

    val session = SparkSession.builder()
      .appName("generador_tablas")
      .enableHiveSupport()
      .master("local[*]")
      .getOrCreate()

    val reader = session.read
    val responsesDic = reader
      .option("header", "true")
      .option("inferSchema", value = true)
      .csv("in/diccionario.csv")

    val responsesFly = reader
      .option("header", "true")
      .option("inferSchema", value = true)
      .csv("in/dataFly.csv")


    import session.implicits._
    val dataSetDict = responsesDic.as[DictAverias]

    dataSetDict.createOrReplaceTempView("dic")
    responsesFly.createOrReplaceTempView("data")

    session.sql("create table if not exists dicFly as select * from dic")
    session.sql("create table if not exists dataFly as select * from data")

  }

}
