package mumudong.core

import java.io._

import org.apache.hadoop.io.Text
import org.apache.spark.SerializableWritable

object TransientTest {
    def main(args: Array[String]): Unit = {
        println("start transient serialization test...")
        val testString = new Text("test")
        val testObject = new SerializableWritable[Text](testString)
        val fout = new FileOutputStream("test.dat")
        val out = new ObjectOutputStream(fout)
        out.writeObject(testObject)
        
        val fin = new FileInputStream("test.dat")
        val in = new ObjectInputStream(fin)
        val n = in.readObject()
        println(n.asInstanceOf[SerializableWritable[Text]].value.toString)
        println("test transient serialization end...")
    }
}
