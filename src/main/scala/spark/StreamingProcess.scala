package spark

import org.apache.hadoop.mapreduce.lib.map.WrappedMapper
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka010._

import scala.collection.mutable

object StreamingProcess {
  private val host = "localhost:9092"
  private val topics = "prueba"
  private val groupId = "0"


  def process():Unit ={

    val session = SparkSession.builder()
      .appName("exampleKafkaStreaming")
      .master("local[*]")
      .enableHiveSupport()
      .getOrCreate()

    val sc = session.sparkContext
    val ssc = new StreamingContext(sc, Seconds(60))
    val topicsSet = topics.split(",").toSet

    val kafkaParams = Map[String, Object](
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> host,
      ConsumerConfig.GROUP_ID_CONFIG -> groupId,
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer])

    val dictFly = session.sql("select * from dicFly")
    val dataFly = session.sql("select * from dataFly")

    def generateTable(cod:String, placa:String, date:String): Unit ={
      //la estructura final debe ser hora_incidencia,placa,compañia,origen,destino,cod,incidencia,tiempollegada
      //obtenemos data de incidencia

      val incidencia = dictFly.filter(dictFly.col("cod").===(cod)).select("*").collect()
        .map(line => line.toString.replace("[","").replace("]","").split(","))
      val fly = dataFly.filter(dataFly.col("codigo").===(placa)).select("*").collect()
        .map(line => line.toString().replace("[","").replace("]","").split(","))

      //TODO falta cambiar formato fecha, añadir tiempo faltante y decidir como escribir la info
      if(incidencia != null && fly != null ) {
        val result = (date, fly(0)(0), fly(0)(1), fly(0)(2), incidencia(0)(0), incidencia(0)(1), "FALTA")
        println(result)
      }
    }



    val messages = KafkaUtils.createDirectStream[String, String](
      ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String,String](topicsSet, kafkaParams))

    //leemos informacion de la placa y creamos una tupla<string,string,int>
    val lines = messages.map(_.value).map(line =>{
      val fields = line.split("~")
      if(fields.length>=3) {
        //session.sql("select referencia from dicFly").show()
        Tuple3(fields(0),fields(1),fields(2))
      }
    })



    lines.foreachRDD(rdd =>{
      val rows = rdd.collect()

      for (r <- rows){
        //enriquecemos el dataset y creamos nueva tabla
        generateTable(
          r.asInstanceOf[(String,String,String)]._3,
          r.asInstanceOf[(String,String,String)]._2,
          r.asInstanceOf[(String,String,String)]._1)
      }
    })


    ssc.start()
    ssc.awaitTermination()
  }

  def main(args: Array[String]): Unit = {
    process()

  }

}
