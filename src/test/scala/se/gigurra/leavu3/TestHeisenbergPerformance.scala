package se.gigurra.leavu3

import se.gigurra.leavu3.datamodel.GameDataWire
import se.gigurra.leavu3.util.{CurTime, Resource2String}
import com.github.gigurra.serviceutils.json.JSON

/**
  * Created by kjolh on 4/2/2016.
  */
object TestHeisenbergPerformance {

  def main(args: Array[String]): Unit = {

    val json = Resource2String("TestGameData.json")
    val duration = 30.0
    val iterations = benchMark("GameDataBenchmark", duration)(JSON.read[GameDataWire](json))

    val nBytesTotal = json.length.toLong * iterations.toLong
    val bytesPerSecond = nBytesTotal.toDouble / duration

    println("Benchmark finished!")
    println(s"  k iterations: ${iterations.toDouble / 1024.0}")
    println(s"  M bytesTotal: ${nBytesTotal / 1024.0 / 1024.0}")
    println(s"  M bytesPerSecond: ${bytesPerSecond / 1024.0 / 1024.0}")
    println(s"  M bitsPerSecond: ${bytesPerSecond * 8.0 / 1024.0 / 1024.0}")
  }

  def benchMark(name: String, duration: Double)(algorithm: => Unit): Long = {
    def time = CurTime.seconds
    val t0 = time
    def elapsed = time - t0
    var i = 0L
    while(elapsed < duration) {
      algorithm
      i += 1L
    }
    i
  }

}
