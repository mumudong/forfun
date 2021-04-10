package scala.aka

import akka.actor.{Actor, ActorPath, ActorSystem, Props}
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration.DurationInt
trait Message {
  val content: String
}
case class Business(content: String) extends Message
case class Meeting(content: String) extends Message
case class Confirm(content: String, actorPath: ActorPath) extends Message
case class DoAction(content: String) extends Message
case class Done(content: String) extends Message

class BossActor extends Actor {
  val log = Logging(context.system, this)
  implicit val askTimeout = Timeout(5 seconds)
  import context.dispatcher
  var taskCount = 0
  def receive: Receive = {
    case b: Business =>
      log.info("I must to do some thing,go,go,go!")
      println("bossActor self.path.address : " + self.path.address)
      //建立Actor获得ActorRef的另外一种方式，利用ActorContext.actorOf
      val managerActors = (1 to 3).map(i =>
        context.actorOf(Props[ManagerActor], s"manager${i}")) //这里咱们召唤3个主管
      //告诉他们开会商量大计划
      managerActors foreach {
        _ ? Meeting("Meeting to discuss big plans") map {
          case c: Confirm =>
            //为何这里能够知道父级Actor的信息？
            //熟悉树结构的同窗应该知道每一个节点有且只有一个父节点（根节点除外）
            log.info(c.actorPath.parent.toString)
            //根据Actor路径查找已经存在的Actor得到ActorRef
            //这里c.actorPath是绝对路径,你也能够根据相对路径获得相应的ActorRef
            val manager = context.actorSelection(c.actorPath)
            manager ! DoAction("Do thing")
        }
      }
    case d: Done => {
      taskCount += 1
      if (taskCount == 3) {
        log.info("the project is done, we will earn much money")
        context.system.terminate()
      }
    }
  }
}
class ManagerActor extends Actor {
  val log = Logging(context.system, this)
  def receive: Receive = {
    case m: Meeting =>
      sender() ! Confirm("I have receive command", self.path)
    case d: DoAction =>
      val workerActor = context.actorOf(Props[WorkerActor], "worker")
      workerActor forward d
  }
}

class WorkerActor extends Actor {
  val log = Logging(context.system, this)
  def receive: Receive = {
    case d: DoAction =>
      log.info("I have receive task")
      sender() ! Done("I hava done work")
  }
}

/**
  * 每一个ActorSystem都有一个根守护者，用/表示,在根守护者下有一个名user的Actor，
  * 它是全部system.actorOf()建立的父Actor
  * /user/boss
  * akka://company-system/user/boss
  * 其中akka表明纯本地的，Akka中默认远程Actor的位置通常用akka.tcp或者akka.udp开头，固然你也可使用第三方插件
  */
object AkkaTest2 {
  def main(args: Array[String]): Unit = {
    val actorSystem = ActorSystem("company-system") //首先咱们建立一家公司
    //建立Actor获得ActorRef的一种方式，利用ActorSystem.actorOf
    val bossActor = actorSystem.actorOf(Props[BossActor], "boss") //公司有一个Boss
    bossActor ! Business("Fitness industry has great prospects") //从市场上观察到健身行业将会有很大的前景


  }
}











