package spark

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
    val ssc = new StreamingContext(sc, Seconds(30))
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

    println(messages)



    ssc.start()
    ssc.awaitTermination()

  }

  def main(args: Array[String]): Unit = {
    process()

  }

}
