package rapaio.data

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */

object VALUE extends VectorType(true, false)

object INDEX extends VectorType(true, false)

object NOMINAL extends VectorType(false, true)

class VectorType(isNumeric: Boolean, isNominal: Boolean)
