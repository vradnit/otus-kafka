package homework

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ClosedShape}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, RunnableGraph, Sink, Source, ZipWith}


object homework {
  implicit val system: ActorSystem = ActorSystem("fusion")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  val graph = GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
    import GraphDSL.Implicits._
    val input = builder.add(Source(1 to 20))
    val broadcast = builder.add(Broadcast[Int](3))
    val mult10 = builder.add(Flow[Int].map(x=>x*10))
    val mult2 = builder.add(Flow[Int].map(x=>x*2))
    val mult3 = builder.add(Flow[Int].map(x=>x*3))
    val zip = builder.add(ZipWith[Int,Int,Int,(Int,Int,Int)]((x,y,z)=>(x,y,z)))
    val sum = builder.add(Flow[(Int,Int,Int)].map[Int](i => i._1 + i._2 + i._3))
    val out = builder.add(Sink.foreach(println))

    input ~> broadcast
    broadcast.out(0) ~> mult10 ~> zip.in0
    broadcast.out(1) ~> mult2 ~> zip.in1
    broadcast.out(2) ~> mult3 ~> zip.in2
    zip.out ~> sum ~> out

    ClosedShape
  }

  def main(args: Array[String]) : Unit ={
    RunnableGraph.fromGraph(graph).run()
  }
}