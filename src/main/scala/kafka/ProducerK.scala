package kafka

import java.util.Properties
import org.apache.kafka.clients.producer.{Producer,KafkaProducer,ProducerRecord}
import java.util.concurrent.TimeUnit


object ProducerK {
  private val topic = "prueba"
  private val host = "localhost:9092"



  def main(args: Array[String]): Unit = {

    val props = new Properties()

    //Assign localhost id
    props.put("bootstrap.servers", host)

    //Set acknowledgements for producer requests.
    props.put("acks", "all")

    //If the request fails, the producer can automatically retry,
    props.put("retries", "0")

    //Specify buffer size in config
    props.put("batch.size", "16384")

    //Reduce the no of requests less than 0
    props.put("linger.ms", "1")

    //The buffer.memory controls the total amount of memory available to the producer for buffering.
    props.put("buffer.memory", "33554432");

    props.put("key.serializer",
      "org.apache.kafka.common.serialization.StringSerializer")

    props.put("value.serializer",
      "org.apache.kafka.common.serialization.StringSerializer")


    val producer = new KafkaProducer[String, String](props)

    for(a <- 1 to 60){
      //se manda algo asi como id, placa y otro dato el caracter espacio es el separador
      producer.send(new ProducerRecord[String, String](topic,
        Integer.toString(a), Integer.toString(a)+" placa"+ " varius"));
      println("“Message sent successfully”")
      TimeUnit.MINUTES.sleep(1)
      producer.close();
    }

  }

}
