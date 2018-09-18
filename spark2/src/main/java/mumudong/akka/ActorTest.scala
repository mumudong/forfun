package mumudong.akka

/**
  * scala2.11 Actor已废弃，需要导Akka包，其是基于时间模型的并发机制
  *     ！ 发送异步消息，无返回值
  *     !? 发送同步消息
  *     !! 发送异步消息，返回Future
  *  新版本：
  *     !! 替换为 ?
  */
//case class Tom(name:String,actor:Actor)
class ActorTest {
//    def act(): Unit ={
//        while (true){
//            receive {
//                case Tom(name,actor) => println(name);actor.sender()!1
//            }
//        }
//    }
//
//    override def receive: Receive = ???
}
