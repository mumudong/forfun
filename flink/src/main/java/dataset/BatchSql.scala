package dataset

import java.time.{Instant, LocalDateTime, ZoneOffset}
import java.util.NoSuchElementException

import org.apache.flink.api.common.io.InputFormat
import org.apache.flink.api.java.io.IteratorInputFormat
import org.apache.flink.table.api.{DataTypes, EnvironmentSettings, TableEnvironment, TableSchema, Types}
import org.apache.flink.table.sinks.CsvTableSink
import org.apache.flink.table.sources.InputFormatTableSource
import org.apache.flink.table.types.DataType
import org.apache.flink.types.Row

object BatchSql {
  def main(args: Array[String]): Unit = {
    val outpath = "batchSql"
    val sqlStatement = " insert into sinkTable select key,rowtime from table2 "

    val tEnv = TableEnvironment.create(EnvironmentSettings.newInstance.useBlinkPlanner.inBatchMode.build)
    tEnv.registerTableSource("table1",new GeneratorTableSource(10,100,60,0))
    tEnv.registerTableSource("table2",new GeneratorTableSource(5,0.2f,60,5))

    tEnv.registerTableSink("sinkTable",new CsvTableSink(outpath).configure(Array("fo","f1"),Array(Types.INT(),Types.SQL_TIMESTAMP())))
    tEnv.sqlUpdate(sqlStatement)
    tEnv.execute("sql")
  }
}

class GeneratorTableSource(numKeys:Int,recordsPerKeyAndSecond:Float,durationSeconds:Int,offsetSeconds:Int) extends InputFormatTableSource[Row] {

  override def getInputFormat: InputFormat[Row, _] = {
    new IteratorInputFormat(createDataGenerator(numKeys, recordsPerKeyAndSecond, durationSeconds, offsetSeconds))
  }

  override def getTableSchema: TableSchema = {
    TableSchema.builder().field("key", DataTypes.INT())
      .field("rowtime", DataTypes.TIMESTAMP(3))
      .field("payload", DataTypes.STRING())
      .build()
  }

  override def getProducedDataType: DataType = getTableSchema.toRowDataType

  def createDataGenerator(numKeys: Int, rowsPerKeyAndSecond: Float, durationSeconds: Int, offsetSeconds: Int):java.util.Iterator[Row] = {
    val sleepMs = (1000 / rowsPerKeyAndSecond).intValue()
    new DataGenerator(numKeys,durationSeconds * 1000,sleepMs,offsetSeconds * 2000L)
  }
}


class DataGenerator(numKeys:Int,durationMs:Long,stepMs:Long,offsetMs:Long) extends java.util.Iterator[Row] with Serializable{
  var ms:Long = 0
  var keyIndex:Int = 0

  override def hasNext: Boolean = ms < durationMs

  override def next() : Row = {
    if(!hasNext) new NoSuchElementException()
    val row = Row.of(Int.box(keyIndex),LocalDateTime.ofInstant(Instant.ofEpochMilli(ms + offsetMs),ZoneOffset.UTC),"some payload ...")
    keyIndex += 1
    if(keyIndex >= numKeys){
      keyIndex = 0
      ms += stepMs
    }
    row
  }
}






