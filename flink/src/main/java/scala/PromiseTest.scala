package scala

import java.util.concurrent.Executors

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success, Try}
import scala.util.{Failure, Success}
case class TaxCut(reduction: Int)
case class LameExcuse(msg: String) extends Exception(msg)
object PromiseTest {
  def main(args: Array[String]): Unit = {
//    promiseOne
    promiseTwo
  }
  def promiseOne:Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global
    import scala.concurrent.{Future,Promise}
    //  import scala.concurrent.duration._
    val p=Promise[Int]
    val f=p.future
    val producer=Future{
      p complete Try{
        100
      }
    }
    val consumer=Future{
      f onComplete{
        case Success(i)=>println(i)
        case Failure(e)=>e.printStackTrace()
      }
    }
    Thread.sleep(1000)
  }

  def redeemCampaignPledge(): Future[TaxCut] = {
    val p = Promise[TaxCut]()
    //future会在上下文找线程池来执行,到底是否异步,还要看线程池
    import scala.concurrent.ExecutionContext.Implicits.global

//    import play.api.libs.concurrent.Execution.Implicits.defaultContext

    Future {
      println("Starting the new legislative period.")
      Thread.sleep(2000)
//      p.failure(LameExcuse("global economy crisis"))
//      println("We didn't fulfill our promises, but surely they'll understand.")

      p.success(TaxCut(20))
      println("We reduced the taxes! You must reelect us!!!!1111")
    }
    p.future
  }

  def promiseTwo:Unit={
    val taxCutF: Future[TaxCut] = redeemCampaignPledge()
    println("Now that they're elected, let's see if they remember their promises...")
    import scala.concurrent.ExecutionContext.Implicits.global

    taxCutF.onComplete {
      case Success(TaxCut(reduction)) =>
        println(s"A miracle! They really cut our taxes by $reduction percentage points!")
      case Failure(ex) =>
        println(s"They broke their promises! Again! Because of a ${ex.getMessage}")
    }
    Thread.sleep(4000)

  }
}
