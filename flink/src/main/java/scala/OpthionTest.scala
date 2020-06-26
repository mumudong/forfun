package scala

object OpthionTest {
  def main(args: Array[String]): Unit = {
    val x = Option(null)
    val y = Option("yyyy")
    val z = Option("zzzz")
    for(i <- x){
      println(i)
    }
    for(i <- y;j<-z;if i.length == j.length){
      println(s"i = $i,j = $j")
    }
  }
}
