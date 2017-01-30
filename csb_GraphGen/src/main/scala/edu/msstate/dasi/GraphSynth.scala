package edu.msstate.dasi

import org.apache.spark.SparkContext
import org.apache.spark.graphx.{Edge, Graph, VertexId}
import org.apache.spark.rdd.RDD

/***
  * The GraphSynth trait contains the basic operations available on all synthesis algorithms.
  */
trait GraphSynth {

  /***
   * Generates a graph with empty properties from a seed graph.
   */
  protected def genGraph(seed: Graph[nodeData, edgeData], seedDists : DataDistributions): Graph[nodeData, Int]

  /***
   * Fills the properties of a synthesized graph using the property distributions of the seed.
   */
  private def genProperties(sc: SparkContext, synthNoProp: Graph[nodeData, Int], seedDists : DataDistributions): Graph[nodeData, edgeData] = {
    val dataDistBroadcast = sc.broadcast(seedDists)

    val eRDD: RDD[Edge[edgeData]] = synthNoProp.edges.map(record => Edge(record.srcId, record.dstId, {
      val ORIGBYTES = dataDistBroadcast.value.getOrigBytesSample
      val ORIGIPBYTE = dataDistBroadcast.value.getOrigIPBytesSample(ORIGBYTES)
      val CONNECTSTATE = dataDistBroadcast.value.getConnectionStateSample(ORIGBYTES)
      val PROTOCOL = dataDistBroadcast.value.getProtoSample(ORIGBYTES)
      val DURATION = dataDistBroadcast.value.getDurationSample(ORIGBYTES)
      val ORIGPACKCNT = dataDistBroadcast.value.getOrigPktsSample(ORIGBYTES)
      val RESPBYTECNT = dataDistBroadcast.value.getRespBytesSample(ORIGBYTES)
      val RESPIPBYTECNT = dataDistBroadcast.value.getRespIPBytesSample(ORIGBYTES)
      val RESPPACKCNT = dataDistBroadcast.value.getRespPktsSample(ORIGBYTES)
      val DESC = dataDistBroadcast.value.getDescSample(ORIGBYTES)
      edgeData("", PROTOCOL, DURATION, ORIGBYTES, RESPBYTECNT, CONNECTSTATE, ORIGPACKCNT, ORIGIPBYTE, RESPPACKCNT, RESPIPBYTECNT, DESC)
    }))
    val vRDD: RDD[(VertexId, nodeData)] = synthNoProp.vertices.map(record => (record._1, {
      val DATA = dataDistBroadcast.value.getIpSample
      nodeData(DATA)
    }))
    val synth = Graph(vRDD, eRDD, nodeData())

    synth
  }

  /***
   * Fills the properties of a synthesized graph using empty data.
   */
  private def genProperties(synthNoProp: Graph[nodeData, Int]): Graph[nodeData, edgeData] = {
    val eRDD: RDD[Edge[edgeData]] = synthNoProp.edges.map(record => Edge(record.srcId, record.dstId, edgeData()))
    val vRDD: RDD[(VertexId, nodeData)] = synthNoProp.vertices.map(record => (record._1, nodeData()))
    val synth = Graph(vRDD, eRDD, nodeData())

    synth
  }

  /***
   * Synthesizes a graph from a seed graph and its properties distributions.
   */
  def synthesize(sc: SparkContext, seed: Graph[nodeData, edgeData], seedDists : DataDistributions, withProperties: Boolean): Graph[nodeData, edgeData] = {
    var startTime = System.nanoTime()

    val synthNoProp = genGraph(seed, seedDists)
    println("Vertices #: " + synthNoProp.numVertices + ", Edges #: " + synthNoProp.numEdges)

    var timeSpan = (System.nanoTime() - startTime) / 1e9
    println()
    println("Finished generating graph.")
    println("\tTotal time elapsed: " + timeSpan.toString)
    println()


    startTime = System.nanoTime()
    println()
    println("Generating Edge and Node properties")

    if (withProperties) {
      val synth = genProperties(sc, synthNoProp, seedDists)

      // TODO: a RDD action should precede the following in order to have a significant timeSpan
      timeSpan = (System.nanoTime() - startTime) / 1e9
      println("Finished generating Edge and Node Properties. Total time elapsed: " + timeSpan.toString)

      synth
    } else {
      val synth = genProperties(synthNoProp)

      // TODO: a RDD action should precede the following in order to have a significant timeSpan
      timeSpan = (System.nanoTime() - startTime) / 1e9
      println("Finished generating Edge and Node Properties. Total time elapsed: " + timeSpan.toString)

      synth
    }
  }
}
