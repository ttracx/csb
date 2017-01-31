package edu.msstate.dasi.csb

import org.apache.spark.SparkContext
import org.apache.spark.graphx.Graph

import scala.reflect.ClassTag

/**
 * Created by scordio on 1/4/17.
 */
class Neo4jPersistence(sc: SparkContext) extends GraphPersistence {
  /**
   * Save the graph
   *
   * @param graph
   * @param overwrite
   */
  def saveGraph[VD: ClassTag, ED: ClassTag](graph: Graph[VD, ED], overwrite :Boolean = false): Unit = {
    if (overwrite) {
      // delete existing graph in the DB
    }

    // save graph to the DB
  }
}
