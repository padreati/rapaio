/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package rapaio.data.mapping

import rapaio.data._

/**
 * A vector which is learn on the base of another vector and the row selection
 * and order is specified by a getMapping give at construction time.
 * <p/>
 * This vector does not hold actual values, it delegate the behavior to the
 * wrapped vector, thus the wrapping affects only the getRowCount selected anf the
 * order of these getRowCount.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
class MappedFeature(private val _source: Feature,
                    private val _mapping: Mapping) extends AbstractFeature {

  require(!_source.isMappedVector, "Now allowed mapped vector as source")

  def isNominal: Boolean = _source.isNominal

  def isNumeric: Boolean = _source.isNumeric

  def rowCount: Int = _mapping.size

  def isMappedVector: Boolean = true

  def source: Feature = _source

  def mapping: Mapping = _mapping

  def rowId(row: Int): Int = _source.rowId(_mapping(row))

  def remove(row: Int) {
    throw new IllegalArgumentException("operation not available on mapped vectors")
  }

  def removeRange(from: Int, to: Int) {
    throw new IllegalArgumentException("operation not available on mapped vectors")
  }

  def clear {
    throw new IllegalArgumentException("operation not available on mapped vectors")
  }

  def trimToSize {
    throw new IllegalArgumentException("operation not available on mapped vectors")
  }

  def ensureCapacity(minCapacity: Int) {
    throw new IllegalArgumentException("operation not available on mapped vectors")
  }

  val missing = new Missing {
    override def apply(row: Int): Boolean = _source.missing(mapping(row))

    override def update(row: Int, value: Boolean): Unit = _source.missing(mapping(row))

    override def ++(): Unit = {
      throw new IllegalArgumentException("operation not available on mapped vectors")
    }
  }

  val values = new Values {
    override def apply(row: Int): Double = _source.values.apply(mapping(row))

    override def update(row: Int, value: Double): Unit = {
      _source.values.update(mapping(row), value)
    }

    override def ++(value: Double): Unit = _source.values.++(value)
  }

  val indexes = new Indexes {
    override def apply(row: Int): Int = _source.indexes.apply(mapping(row))

    override def update(row: Int, value: Int): Unit = {
      _source.indexes.update(mapping(row), value)
    }

    override def ++(value: Int): Unit = _source.indexes.++(value)
  }

  val labels = new Labels {
    override def apply(row: Int): String = _source.labels.apply(mapping(row))

    override def update(row: Int, value: String): Unit = {
      _source.labels.update(mapping(row), value)
    }

    override def ++(value: String): Unit = _source.labels.++(value)

    override def dictionary: Array[String] = _source.labels.dictionary

    override def dictionary_=(dict: Array[String]): Unit = _source.labels.dictionary = dict
  }
}

object MappedFeature {
  def apply(instances: Array[VInst]): MappedFeature = {
    val src = instances(0).vector.source
    if (instances.forall((inst: VInst) => src eq inst.vector.source)) {
      val mapping = new Mapping
      instances.foreach((inst: VInst) => mapping.add(inst.rowId))
      new MappedFeature(src, mapping)
    } else {
      throw new IllegalArgumentException("Cannot build mapped vector from multiple source vectors")
    }
  }
}