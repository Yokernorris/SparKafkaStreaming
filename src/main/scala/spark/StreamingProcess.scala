package spark

import org.apache.hadoop.mapreduce.lib.map.WrappedMapper
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka010._

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

    //leemos tabla y generamos un dataFrame
    val dictFly = session.sql("select * from dicFly")
    val dataFly = session.sql("select * from dataFly")


    lines.foreachRDD(rdd =>{
      val rows = rdd.collect()
      for (r <- rows){
        //enriquecemos el dataset y creamos nueva tabla

        //obtenemos ref
        val ref = dictFly.filter(dictFly.col("cod").===(
          r.asInstanceOf[(String,String,Int)]._3
        )).select("referencia")
        println(ref)

        //obtenemos dato de avion



      }

    })





    //a partir de aqui podemos combinar datos y realizar combinaciones



    // mostramos los datos de lines
    lines.print()



    ssc.start()
    ssc.awaitTermination()

  }

  def main(args: Array[String]): Unit = {
    process()

  }

}
