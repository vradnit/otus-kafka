package homework

import akka.actor.ActorSystem
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.stream.{ActorMaterializer, FlowShape, Materializer}
import com.typesafe.config.ConfigFactory
import org.apache.kafka.common.serialization.{IntegerDeserializer, StringDeserializer}

import scala.language.postfixOps
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Sink, ZipWith}

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}
import ch.qos.logback.classic.{Level, Logger}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory


object  ConsumerApp extends App{
  implicit val system: ActorSystem = ActorSystem("consumer-sys")
  implicit val mat: Materializer  = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  LoggerFactory
    .getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
    .asInstanceOf[Logger]
    .setLevel(Level.WARN)

  val config = ConfigFactory.load()
  val consumerConfig = config.getConfig("akka.kafka.consumer")
  val consumerSettings = ConsumerSettings(consumerConfig, new StringDeserializer, new IntegerDeserializer)

  val busflow = GraphDSL.create(){ implicit builder =>
    import GraphDSL.Implicits._
    val broadcast = builder.add(Broadcast[ConsumerRecord[String,Integer]](3))
    val mult10 = builder.add(Flow[Integer].map(x=>x*10))
    val mult2 = builder.add(Flow[Integer].map(x=>x*2))
    val mult3 = builder.add(Flow[Integer].map(x=>x*3))
    val zip = builder.add(ZipWith[Int,Int,Int,(Int,Int,Int)]((x,y,z)=>(x,y,z)))
    val sum = builder.add(Flow[(Int,Int,Int)].map[Int](i => i._1 + i._2 + i._3))

    broadcast.out(0).map(_.value()) ~> mult10 ~> zip.in0
    broadcast.out(1).map(_.value()) ~> mult2 ~> zip.in1
    broadcast.out(2).map(_.value()) ~> mult3 ~> zip.in2
    zip.out ~> sum

    FlowShape(broadcast.in, sum.out)
  }

  val consumer = Consumer
    .plainSource(consumerSettings, Subscriptions.topics("test"))
    .via(busflow)
    .runWith(Sink.foreach(println))

  consumer onComplete {
    case Success(_) => println("Done"); system.terminate()
    case Failure(err) => println(err.toString); system.terminate()
  }
}