package models.example

import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.Actor
import akka.routing.RoundRobinRouter
import akka.pattern.ask
import play.Logger
import akka.util.Timeout
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._

object Akka {
  
  val workerRouter1 = ActorSystem("Example").actorOf(
      Props[Worker].withRouter(RoundRobinRouter(10)), name = "workerRouter")
  val workerRouter2 = ActorSystem("Example2").actorOf(
      Props[Worker].withRouter(RoundRobinRouter(5)), name = "workerRouter2")

  def doHeavyWork = {
    implicit val timeout = Timeout(3 seconds)
    val future1 = workerRouter2 ? Wait5
    val future2 = workerRouter1 ? Wait8
    val result = for {
      result1 <- future1.mapTo[String]
      result2 <- future2.mapTo[String]
    } yield{
      //Logger.info("Workers.done");
      result1 :: result2 :: List()
    }
    result
  }
}

class Worker extends Actor{
  
  def receive = {
    case Wait5 => /*Logger.info("Worker.startSleep5");*/Thread.sleep(100);/*Logger.info("Worker.FinishSleep5");*/sender ! "Wait5"
    case Wait8 => /*Logger.info("Worker.startSleep8");*/Thread.sleep(200);/*Logger.info("Worker.FinishSleep8");*/ sender ! "Wait8"
  }
}

class ResultHandler extends Actor{
  def receive = {
    case message: String => sender ! message 
  }
}

/*MESSAGES*/

case object Wait5
case object Wait8