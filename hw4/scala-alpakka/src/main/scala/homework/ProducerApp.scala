package homework

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.Source
import akka.stream.{ActorMaterializer, Attributes, Materializer}
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{IntegerSerializer, StringSerializer}
import org.slf4j.LoggerFactory
import ch.qos.logback.classic.{Level, Logger}

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

object ProducerApp extends App {
  implicit val system: ActorSystem = ActorSystem("producer-sys")
  implicit val mat: Materializer  = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  LoggerFactory
    .getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
    .asInstanceOf[Logger]
    .setLevel(Level.WARN)

  val config = ConfigFactory.load()
  val producerConfig = config.getConfig("akka.kafka.producer")
  val producerSettings = ProducerSettings(producerConfig, new StringSerializer, new IntegerSerializer)

  val produce: Future[Done] =
    Source(1 to 20)
      .log(name = "LOG", _.intValue).addAttributes(Attributes.logLevels(onElement = Attributes.LogLevels.Warning))
      .map(value => new ProducerRecord[String, Integer]("test", value: Int))
      .runWith(Producer.plainSink(producerSettings))

  produce onComplete {
    case Success(_) => println("Done"); system.terminate()
    case Failure(err) => println(err.toString); system.terminate()
  }
}