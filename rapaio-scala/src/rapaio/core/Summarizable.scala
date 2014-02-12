package rapaio.core

/**
 * Interface implemented by all objects which outputs summaries about themselves
 * for exploratory purposes or for other reasons.
 * <p/>
 * Implementations of this interface works directly with default printer and
 * does not returns a string format description due to various ways the output
 * is rendered by different printer implementations.
 * <p/>
 * See also {@link rapaio.printer.Printer}
 * <p/>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
abstract trait Summarizable {
  /**
   * Prints a summary of the object to the system printer configured
   * with {@link rapaio.workspace.Workspace#setPrinter(rapaio.printer.Printer)}.
   */
  def summary
}

