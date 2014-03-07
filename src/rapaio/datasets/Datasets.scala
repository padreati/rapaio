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

package rapaio.datasets

import rapaio.data._
import rapaio.io.CsvPersistence

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
object Datasets {

  def loadIrisDataset: Frame = {
    new CsvPersistence(
      defaultTypeHint = "val",
      typeHints = Map("class" -> "nom")
    ).read(Datasets.getClass, "iris.csv")
  }

  def loadPearsonHeightDataset: Frame = {
    new CsvPersistence(
      defaultTypeHint = "val"
    ).read(Datasets.getClass, "pearsonheight.csv")
  }

  def loadChestDataset: Frame = {
    new CsvPersistence(
      hasQuotas = false,
      defaultTypeHint = "val"
    ).read(Datasets.getClass, "chest.csv")
  }

  def loadCarMpgDataset: Frame = {
    new CsvPersistence(
      defaultTypeHint = "val",
      typeHints = Map("MAKE" -> "nom")
    ).read(Datasets.getClass, "carmpg.csv")
  }

  def loadSpamBase: Frame = {
    new CsvPersistence(
      defaultTypeHint = "val",
      typeHints = Map("spam" -> "nom")
    ).read(this.getClass, "spam-base.csv")
  }

  def loadMushrooms: Frame = {
    new CsvPersistence().read(Datasets.getClass, "mushrooms.csv")
  }

  def loadPlay: Frame = {
    new CsvPersistence(
      defaultTypeHint = "nom",
      typeHints = Map("temp" -> "val", "humidity" -> "val"),
      hasQuotas = false
    ).read(Datasets.getClass, "play.csv")
  }

  def loadOlympic: Frame = {
    new CsvPersistence(
      hasQuotas = false,
      typeHints = Map("Edition" -> "val")
    ).read(Datasets.getClass, "olympic.csv")
  }

  def loadProstateCancer: Frame = {
    new CsvPersistence(
      separator = '\t',
      defaultTypeHint = "val"
    ).read(Datasets.getClass, "prostate.csv")
  }
}