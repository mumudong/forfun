//package mumudong.akka
//
//import akka.actor.{Actor, Props}
//
///**
//  * 异常处理：一个actor失败将由其父actor处理，默认是重启
//  */
//class SupervisingActor extends Actor{
//    val child = context.actorOf(Props[SuperVisedActor],"supervisor")
//    override def receive: Receive = {
//        case "failChild" => child ! "fail"
//    }
//}
//class SuperVisedActor extends Actor{
//
//    override def preStart(): Unit = println("supervised actor started!");super.preStart()
//
//    override def postStop(): Unit = println("supervised actor stopped!")
//
//    override def receive: Receive = {
//        case "fail" => {
//            println("supervised actor fails now")
//            throw new Exception("i failed!")
//        }
//    }
//}
//object Main{
//    def main(args: Array[String]): Unit = {
////        val supervisingActor = system.actorOf(Props[SupervisingActor],"supervising-actor")
////        supervisingActor ! "failChild"
//    }
//}