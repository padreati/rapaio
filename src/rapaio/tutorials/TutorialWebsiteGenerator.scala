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

package rapaio.tutorials

import rapaio.tutorials.pages.{LawOfLargeNumbers, TutorialPage}
import java.io.File
import rapaio.workspace.Workspace._
import rapaio.printer.html.HTMLPrinter

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
object TutorialWebsiteGenerator {
  private final val TUTORIAL_WEB_ROOT: String = "/home/ati/work/rapaio-tutorial/"

  def main(args: Array[String]) {

    val webRoot: File = new File(TUTORIAL_WEB_ROOT)
    val pageRoot: File = new File(webRoot, "pages")
    deleteRoot(pageRoot)
    deleteRoot(new File(webRoot, "index.html"))
    webRoot.mkdir
    pageRoot.mkdir


    //        category = "Graphics";
    //        pages.put(category, new ArrayList<TutorialPage>());
    //        pages.get(category).add(new HistogramDensityTutorial());
    //        category = "StatisticalProcedures";
    //        pages.put(category, new ArrayList<TutorialPage>());
    //        pages.get(category).add(new DiscreteSampling());
    //        pages.get(category).add(new CorrelationsPage());
    //        pages.get(category).add(new ROCCurvesPage());
    //
    //        category = "SampleAnalysis";
    //        pages.put(category, new ArrayList<TutorialPage>());
    //        pages.get(category).add(new PearsonHeight());
    //        pages.get(category).add(new LawOfLargeNumbers());
    //
    //        category = "SupervisedClassification";
    //        pages.put(category, new ArrayList<TutorialPage>());
    //        pages.get(category).add(new ClassificationWithRF());
    //
    //    category = "Regression"
    //    pages.put(category, new ArrayList[TutorialPage])
    //    pages.get(category).add(new LinearRegression1Page)
    //		pages.get(category).add(new LinearRegression2Page());
    //		pages.get(category).add(new LinearRegression3Page());
    //        category = "WorkInProgress";
    //        pages.put(category, new ArrayList<TutorialPage>());
    //        pages.get(category).add(new IrisExplore());
    //        pages.get(category).add(new StudentTDistribution());
    val pages = Map(
      "SampleAnalysis" -> List(new LawOfLargeNumbers())
    )

    makeIndexPage(webRoot, pages)
    for (categ <- pages.keySet) {
      val categoryRoot = new File(pageRoot, categ)
      if (!categoryRoot.exists) {
        categoryRoot.mkdir
      }
      for (page <- pages(categ)) {
        val pageFile: File = new File(categoryRoot, page.pageName + ".html")
        printer = new HTMLPrinter(pageFile.getAbsolutePath, page.pageTitle, "<a href=\"../../getIndex.html\">Back</a>")
        printer.preparePrinter()
        page.render()
        printer.closePrinter()
      }
    }
  }

  private def makeIndexPage(webRoot: File, pages: Map[String, List[TutorialPage]]) {
    val indexPage: File = new File(webRoot, "index.html")
    printer = new HTMLPrinter(indexPage.getAbsolutePath, "Rapaio Tutorials")
    printer.preparePrinter()
    heading(1, "Rapaio Tutorial page")
    p(
      """
        |This is the home page for Rapaio tutorials.
      """.stripMargin)
    p( """
         |A Rapaio tutorial page is a document generated with the Rapaio library documenting
         |facilities in order to exemplify how an analysis could be accomplished using Rapaio
         |statistical, data mining and machine learning toolbox.
       """.stripMargin)
    p( """
         |Most of the tutorials will be strictly oriented on a small facility Rapaio
         |offers. As a sample how one ca read and write data with CVSPersistence.
         |Other tutorials will be oriented on the pieces of output facilities useful
         |in data visualization, either as text or as graphical images.
       """.stripMargin)

    p(
      """
        |There are also some tutorials which tries to do an exploration analysis,
        |trying to put together the small pieces into something fluent and understandable
        |as a whole. However, these pages does not contain a full exploration analysis
        |or other getType of analysis.
        |
        |Its purpose is to illustrate how Rapaio toolbox could be used. They are not a
        |golden-standard for how an exploration must be conduct.
      """.stripMargin)
    p(
      """
        |From time to time the whole tutorials will be regenerated and republished.
      """.stripMargin)
    p(
      """
        |That happens because they will grow together with the library and its
        |facilities. The tutorials are generated from source code, using Rapaio
        |library documenting facilities. The advantage of writing tutorials in this
        |manner is that they will remain compatible with the last revision of the library,
        |thus they will be up-to-date and ready for immediate usage.
      """.stripMargin)
    p(
      """
        |Have fun on learning and using Rapaio!
      """.stripMargin)
    heading(2, "Rapaio Tutorial Gallery")

    for (category <- pages.keySet) {
      heading(3, category)
      for (page <- pages(category)) {
        print( f"""<a href="pages/${category}/${page.pageName}.html">${page.pageTitle}</a></br>""")
      }
    }
    printer.closePrinter()
  }

  private def deleteRoot(root: File) {
    if (root.isDirectory) {
      for (child <- root.listFiles) {
        deleteRoot(child)
      }
    }
    root.delete
  }

}