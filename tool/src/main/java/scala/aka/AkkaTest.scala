package scala.aka

import akka.actor.{AbstractActor, Actor, ActorSystem, Props}
import akka.event.Logging
import akka.japi.pf.FI.UnitApply
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration.DurationInt

/**
  * actor: 开发高效的并发程序,对并发模型有一个更好的抽象,异步非阻塞
  * actor: 由mailbox和自身状态组成
  * actor之间通过邮件通信,每个actor串行处理每条消息,actor单线程
  *
  *    AKKA中使用dispatcher对actor进行执行，当一个actor启动之后会将自身绑定到一个dispatcher上，
  * 我们可以在系统配置中定义自己的dispathcer。Dispatcher本身其实是一个线程池，默认的dispatcher是
  * 一个fork-join-executor，读者可以参考下表来了解不通的dispatcher。

    默认Dispatcher	适合大多数场景，默认实现
    PinnedDispatcher	适合高优先级actor，为actor使用独立的线程
    BalancingDispatcher	使用该dispatcher的actor将共享一个邮箱，只适合相同类型actor使用，2.3版本之后被BalancingPool替代

     //首先在配置文件中定义一个新的dispatcher
      crawler-dispatcher{
       type=Dispatcher
       executor = "thread-pool-executor"
       thread-pool-executor{
         core-pool-size-min = 4
         core-pool-size-max = 64
       }
       throughput=5 //设置actor让出线程前处理消息的数目，可以进行设置降低cpu上下文切换次数
     }

     //调用以下withDispatcher方法绑定actor到指定dispatcher
      val actor = context.actorOf(Props[MyActor].withDispatcher("crawler-dispatcher"))


  *    Actor的一条重要准则就是尽量不要去阻塞一个Actor，因为Actor本身为单线程处理消息，
  * 一旦被阻塞会导致消息积压、dispatcher资源被大量占据等问题，一般使用future去对IO阻塞类的操作进行处理。
  * 另外还有一条思路就是为存在阻塞的操作创建多个独立的actor，并将这些actor绑定到一个独立的dispatcher，
  * 将阻塞actor与常规actor进行隔离，避免影响到其他actor的执行
  *
  *
  *
  *
  *
  *
  */
class AliceActor extends AbstractActor{
  override def createReceive(): AbstractActor.Receive = {
    receiveBuilder().`match`(classOf[String],new UnitApply[String](){
      override def apply(i: String):Unit = {
        println("alice got string message:" + i)
        getSender().tell("i'm alice",getSelf())
      }
    }).matchAny(new UnitApply[Object] {
      override def apply(i: Object): Unit = {
        println("alice got object message:" + i)
      }
    }).build()
  }
}
class BobActor extends Actor{
  implicit val askTimeout = Timeout(5 seconds)
  import context.dispatcher

  //  override def createReceive(): AbstractActor.Receive = {
//    receiveBuilder().`match`(classOf[String],new UnitApply[String](){
//      override def apply(i: String):Unit = {
//        println("bob got string message:" + i)
//        getSender().tell("i'm bob",getSelf())
//      }
//    }).matchAny(new UnitApply[Object] {
//      override def apply(i: Object): Unit = {
//        println("bob got object message:" + i)
//        getSender() ? ""
//      }
//    }).build()
//  }
  override def receive: Receive = {
    case str:String => {
      println("bob got string message:" + str)
//      sender().tell("i'm bob",self)
    }
    case _ => {
      sender() ? ""
    }

  }
}
trait Message {
  val content: String
}
case class Meeting(content: String) extends Message
object AkkaTest {
  def main(args: Array[String]): Unit = {
    implicit val askTimeout = Timeout(5 seconds)
    val actorSystem = ActorSystem.create()
    val alice = actorSystem.actorOf(Props.create(classOf[AliceActor]),"aliceActor")
    alice ?""
    val bob = actorSystem.actorOf(Props.create(classOf[BobActor]),"bobActor")

    //tell	fire and forget,发送后立刻返回	直接发送
    //ask	发送后会等待一段时间，并返回一个future	创建一个中间代理再发送

    //bob给Alice发消息
    alice.tell("你好",bob)
    println("------------------")
    //给Alice发消息
    alice.!("this is .!")
    alice ! ("this is !")
//    alice.forward("this is forward")()

    alice ?  "message by ?"


//    actorSystem.stop(alice)
    actorSystem.terminate()
  }


}
