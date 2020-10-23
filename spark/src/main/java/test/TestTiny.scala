package test

import java.io.{File, PrintWriter}

import org.apache.calcite.sql.parser.SqlParser

import scala.io.Source

/**
  * Created by Administrator on 2018/6/6.
  */
object TestTiny {
    def main(args: Array[String]): Unit = {
         val a = Seq("1","2","3","4")
        a.filter(x => x.equals("1")).foreach(println(_))
    }
    def wan():Unit = ???


}
