//
///*
// * Apache License
// * Version 2.0, January 2004
// * http://www.apache.org/licenses/
// *
// *    Copyright 2013 Aurelian Tutuianu
// *    Copyright 2014 Aurelian Tutuianu
// *    Copyright 2015 Aurelian Tutuianu
// *
// *    Licensed under the Apache License, Version 2.0 (the "License");
// *    you may not use this file except in compliance with the License.
// *    You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// *    Unless required by applicable law or agreed to in writing, software
// *    distributed under the License is distributed on an "AS IS" BASIS,
// *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *    See the License for the specific language governing permissions and
// *    limitations under the License.
// *
// */
//
//package rapaio.ml.classifier.tree.ctree;
//
//import org.junit.Assert;
//import org.junit.Test;
//import rapaio.data.Frame;
//import rapaio.data.VarType;
//import rapaio.data.filter.FFAbstractRetainTypes;
//import rapaio.datasets.Datasets;
//import rapaio.core.tools.DTable;
//import rapaio.ml.classifier.tree.*;
//import rapaio.ws.Summary;
//
//import java.io.IOException;
//
///**
// * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
// */
//@Deprecated
//public class ID3ClassifierTest {
//
//    @Test
//    public void testBasicID3Entropy() throws IOException {
//        Frame df = Datasets.loadPlay();
//        Summary.printSummary(df);
//        df = new FFAbstractRetainTypes(VarType.NOMINAL).filter(df);
//        final String className = "class";
//
//        CTree id3 = CTree.newID3();
//        id3.learn(df, className);
//        id3.fit(df);
//
//        DTable dtWindy = new DTable(df.var("windy"), df.var("class"));
//        DTable dtOutlook = new DTable(df.var("outlook"), df.var("class"));
//        String splitCol = (dtWindy.getSplitEntropy() < dtOutlook.getSplitEntropy()) ? "windy" : "outlook";
//        id3.printSummary();
//        Assert.assertTrue(id3.getRoot().getChildren().get(0).getGroupName().startsWith(splitCol));
//
//        Summary.printSummary(id3);
//    }
//
//    @Test
//    public void testBasicID3InfoGain() throws IOException {
//        Frame df = Datasets.loadPlay();
//        df = new FFAbstractRetainTypes(VarType.NOMINAL).filter(df);
//        final String className = "class";
//
//        CTree id3 = new CTree()
//                .withNominalMethod(new CTreeNominalMethod.Full())
//                .withNumericMethod(new CTreeNumericMethod.Ignore())
//                .withSplitter(new CTreeSplitter.RemainsIgnored())
//                .withFunction(new CTreeTestFunction.InfoGain());
//        id3.learn(df, className);
//        id3.fit(df);
//        id3.printSummary();
//
//        DTable dtWindy = new DTable(df.var("windy"), df.var("class"));
//        DTable dtOutlook = new DTable(df.var("outlook"), df.var("class"));
//        String splitCol = (dtWindy.getInfoGain() > dtOutlook.getInfoGain()) ? "windy" : "outlook";
//        Assert.assertTrue(id3.getRoot().getChildren().get(0).getGroupName().startsWith(splitCol));
//
//        Summary.printSummary(id3);
//    }
//}
