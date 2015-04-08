///*
// * Apache License
// * Version 2.0, January 2004
// * http://www.apache.org/licenses/
// *
// *    Copyright 2013 Aurelian Tutuianu
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
// */
//
//package rapaio.ml.classifier.svm;
//
//import weka.classifiers.Classifier;
//import weka.classifiers.AbstractClassifier;
//import weka.classifiers.functions.supportVector.Kernel;
//import weka.classifiers.functions.supportVector.PolyKernel;
//import weka.classifiers.functions.supportVector.SMOset;
//import weka.core.Attribute;
//import weka.core.Capabilities;
//import weka.core.FastVector;
//import weka.core.Instance;
//import weka.core.DenseInstance;
//import weka.core.Instances;
//import weka.core.Option;
//import weka.core.OptionHandler;
//import weka.core.RevisionUtils;
//import weka.core.SelectedTag;
//import weka.core.SerializedObject;
//import weka.core.Tag;
//import weka.core.TechnicalInformation;
//import weka.core.TechnicalInformationHandler;
//import weka.core.Utils;
//import weka.core.WeightedInstancesHandler;
//import weka.core.Capabilities.Capability;
//import weka.core.TechnicalInformation.Field;
//import weka.core.TechnicalInformation.Type;
//import weka.filters.Filter;
//import weka.filters.unsupervised.attribute.NominalToBinary;
//import weka.filters.unsupervised.attribute.Normalize;
//import weka.filters.unsupervised.attribute.ReplaceMissingValues;
//import weka.filters.unsupervised.attribute.Standardize;
//
//import java.io.Serializable;
//import java.util.Enumeration;
//import java.util.Random;
//import java.util.Vector;
//
///**
// * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/16/15.
// */
//public class T {
//}
//
//
///**
// * <!-- globalinfo-start -->
// * Implements John Platt's sequential minimal optimization algorithm for training a support vector classifier.<br/>
// * <br/>
// * This implementation globally replaces all missing values and transforms nominal attributes into binary ones. It also normalizes all attributes by default. (In that case the coefficients in the output are based on the normalized data, not the original data --- this is important for interpreting the classifier.)<br/>
// * <br/>
// * Multi-class problems are solved using pairwise classification (1-vs-1 and if logistic models are built pairwise coupling according to Hastie and Tibshirani, 1998).<br/>
// * <br/>
// * To obtain proper probability estimates, use the option that fits logistic regression models to the outputs of the support vector machine. In the multi-class case the predicted probabilities are coupled using Hastie and Tibshirani's pairwise coupling method.<br/>
// * <br/>
// * Note: for improved speed normalization should be turned off when operating on SparseInstances.<br/>
// * <br/>
// * For more information on the SMO algorithm, see<br/>
// * <br/>
// * J. Platt: Fast Training of Support Vector Machines using Sequential Minimal Optimization. In B. Schoelkopf and C. Burges and A. Smola, editors, Advances in Kernel Methods - Support Vector Learning, 1998.<br/>
// * <br/>
// * S.S. Keerthi, S.K. Shevade, C. Bhattacharyya, K.R.K. Murthy (2001). Improvements to Platt's SMO Algorithm for SVM Classifier Design. Neural Computation. 13(3):637-649.<br/>
// * <br/>
// * Trevor Hastie, Robert Tibshirani: Classification by Pairwise Coupling. In: Advances in Neural Information Processing Systems, 1998.
// * <p>
// * <!-- globalinfo-end -->
// * <p>
// * <!-- technical-bibtex-start -->
// * BibTeX:
// * <pre>
// * &#64;incollection{Platt1998,
// *    author = {J. Platt},
// *    booktitle = {Advances in Kernel Methods - Support Vector Learning},
// *    editor = {B. Schoelkopf and C. Burges and A. Smola},
// *    publisher = {MIT Press},
// *    title = {Fast Training of Support Vector Machines using Sequential Minimal Optimization},
// *    year = {1998},
// *    URL = {http://research.microsoft.com/\~jplatt/smo.html},
// *    PS = {http://research.microsoft.com/\~jplatt/smo-book.ps.gz},
// *    PDF = {http://research.microsoft.com/\~jplatt/smo-book.pdf}
// * }
// *
// * &#64;article{Keerthi2001,
// *    author = {S.S. Keerthi and S.K. Shevade and C. Bhattacharyya and K.R.K. Murthy},
// *    journal = {Neural Computation},
// *    number = {3},
// *    pages = {637-649},
// *    title = {Improvements to Platt's SMO Algorithm for SVM Classifier Design},
// *    volume = {13},
// *    year = {2001},
// *    PS = {http://guppy.mpe.nus.edu.sg/\~mpessk/svm/smo_mod_nc.ps.gz}
// * }
// *
// * &#64;inproceedings{Hastie1998,
// *    author = {Trevor Hastie and Robert Tibshirani},
// *    booktitle = {Advances in Neural Information Processing Systems},
// *    editor = {Michael I. Jordan and Michael J. Kearns and Sara A. Solla},
// *    publisher = {MIT Press},
// *    title = {Classification by Pairwise Coupling},
// *    volume = {10},
// *    year = {1998},
// *    PS = {http://www-stat.stanford.edu/\~hastie/Papers/2class.ps}
// * }
// * </pre>
// * <p>
// * <!-- technical-bibtex-end -->
// * <p>
// * <!-- options-start -->
// * Valid options are: <p/>
// * <p>
// * <pre> -D
// *  If set, classifier is run in debug mode and
// *  may output additional info to the console</pre>
// * <p>
// * <pre> -no-checks
// *  Turns off all checks - use with caution!
// *  Turning them off assumes that data is purely numeric, doesn't
// *  contain any missing values, and has a nominal class. Turning them
// *  off also means that no header information will be stored if the
// *  machine is linear. Finally, it also assumes that no instance has
// *  a weight equal to 0.
// *  (default: checks on)</pre>
// * <p>
// * <pre> -C &lt;double&gt;
// *  The complexity constant C. (default 1)</pre>
// * <p>
// * <pre> -N
// *  Whether to 0=normalize/1=standardize/2=neither. (default 0=normalize)</pre>
// * <p>
// * <pre> -L &lt;double&gt;
// *  The tolerance parameter. (default 1.0e-3)</pre>
// * <p>
// * <pre> -P &lt;double&gt;
// *  The epsilon for round-off error. (default 1.0e-12)</pre>
// * <p>
// * <pre> -M
// *  Fit logistic models to SVM outputs. </pre>
// * <p>
// * <pre> -RV &lt;double&gt;
// *  The number of folds for the internal
// *  cross-validation. (default -1, use training data)</pre>
// * <p>
// * <pre> -W &lt;double&gt;
// *  The random number seed. (default 1)</pre>
// * <p>
// * <pre> -K &lt;classname and parameters&gt;
// *  The Kernel to use.
// *  (default: weka.classifiers.functions.supportVector.PolyKernel)</pre>
// * <p>
// * <pre>
// * Options specific to kernel weka.classifiers.functions.supportVector.PolyKernel:
// * </pre>
// * <p>
// * <pre> -D
// *  Enables debugging output (if available) to be printed.
// *  (default: off)</pre>
// * <p>
// * <pre> -no-checks
// *  Turns off all checks - use with caution!
// *  (default: checks on)</pre>
// * <p>
// * <pre> -C &lt;num&gt;
// *  The size of the cache (a prime number), 0 for full cache and
// *  -1 to turn it off.
// *  (default: 250007)</pre>
// * <p>
// * <pre> -E &lt;num&gt;
// *  The Exponent to use.
// *  (default: 1.0)</pre>
// * <p>
// * <pre> -L
// *  Use lower-order terms.
// *  (default: no)</pre>
// * <p>
// * <!-- options-end -->
// *
// * @author Eibe Frank (eibe@cs.waikato.ac.nz)
// * @author Shane Legg (shane@intelligenesis.net) (sparse vector code)
// * @author Stuart Inglis (stuart@reeltwo.com) (sparse vector code)
// * @version $Revision: 6024 $
// */
//public class SMO
//        extends AbstractClassifier
//        implements WeightedInstancesHandler, TechnicalInformationHandler {
//
//    /**
//     * for serialization
//     */
//    static final long serialVersionUID = -6585883636378691736L;
//
//    /**
//     * Returns a string describing classifier
//     *
//     * @return a description suitable for
//     * displaying in the explorer/experimenter gui
//     */
//    public String globalInfo() {
//
//        return "Implements John Platt's sequential minimal optimization "
//                + "algorithm for training a support vector classifier.\n\n"
//                + "This implementation globally replaces all missing values and "
//                + "transforms nominal attributes into binary ones. It also "
//                + "normalizes all attributes by default. (In that case the coefficients "
//                + "in the output are based on the normalized data, not the "
//                + "original data --- this is important for interpreting the classifier.)\n\n"
//                + "Multi-class problems are solved using pairwise classification "
//                + "(1-vs-1 and if logistic models are built pairwise coupling "
//                + "according to Hastie and Tibshirani, 1998).\n\n"
//                + "To obtain proper probability estimates, use the option that fits "
//                + "logistic regression models to the outputs of the support vector "
//                + "machine. In the multi-class case the predicted probabilities "
//                + "are coupled using Hastie and Tibshirani's pairwise coupling "
//                + "method.\n\n"
//                + "Note: for improved speed normalization should be turned off when "
//                + "operating on SparseInstances.\n\n"
//                + "For more information on the SMO algorithm, see\n\n"
//                + getTechnicalInformation().toString();
//    }
//
//    /**
//     * Returns an instance of a TechnicalInformation object, containing
//     * detailed information about the technical background of this class,
//     * e.g., paper reference or book this class is based on.
//     *
//     * @return the technical information about this class
//     */
//    public TechnicalInformation getTechnicalInformation() {
//        TechnicalInformation result;
//        TechnicalInformation additional;
//
//        result = new TechnicalInformation(Type.INCOLLECTION);
//        result.setValue(Field.AUTHOR, "J. Platt");
//        result.setValue(Field.YEAR, "1998");
//        result.setValue(Field.TITLE, "Fast Training of Support Vector Machines using Sequential Minimal Optimization");
//        result.setValue(Field.BOOKTITLE, "Advances in Kernel Methods - Support Vector Learning");
//        result.setValue(Field.EDITOR, "B. Schoelkopf and C. Burges and A. Smola");
//        result.setValue(Field.PUBLISHER, "MIT Press");
//        result.setValue(Field.URL, "http://research.microsoft.com/~jplatt/smo.html");
//        result.setValue(Field.PDF, "http://research.microsoft.com/~jplatt/smo-book.pdf");
//        result.setValue(Field.PS, "http://research.microsoft.com/~jplatt/smo-book.ps.gz");
//
//        additional = result.add(Type.ARTICLE);
//        additional.setValue(Field.AUTHOR, "S.S. Keerthi and S.K. Shevade and C. Bhattacharyya and K.R.K. Murthy");
//        additional.setValue(Field.YEAR, "2001");
//        additional.setValue(Field.TITLE, "Improvements to Platt's SMO Algorithm for SVM Classifier Design");
//        additional.setValue(Field.JOURNAL, "Neural Computation");
//        additional.setValue(Field.VOLUME, "13");
//        additional.setValue(Field.NUMBER, "3");
//        additional.setValue(Field.PAGES, "637-649");
//        additional.setValue(Field.PS, "http://guppy.mpe.nus.edu.sg/~mpessk/svm/smo_mod_nc.ps.gz");
//
//        additional = result.add(Type.INPROCEEDINGS);
//        additional.setValue(Field.AUTHOR, "Trevor Hastie and Robert Tibshirani");
//        additional.setValue(Field.YEAR, "1998");
//        additional.setValue(Field.TITLE, "Classification by Pairwise Coupling");
//        additional.setValue(Field.BOOKTITLE, "Advances in Neural Information Processing Systems");
//        additional.setValue(Field.VOLUME, "10");
//        additional.setValue(Field.PUBLISHER, "MIT Press");
//        additional.setValue(Field.EDITOR, "Michael I. Jordan and Michael J. Kearns and Sara A. Solla");
//        additional.setValue(Field.PS, "http://www-stat.stanford.edu/~hastie/Papers/2class.ps");
//
//        return result;
//    }
//
//
//
//    /**
//     * filter: Normalize training data
//     */
//    public static final int FILTER_NORMALIZE = 0;
//    /**
//     * filter: Standardize training data
//     */
//    public static final int FILTER_STANDARDIZE = 1;
//    /**
//     * filter: No normalization/standardization
//     */
//    public static final int FILTER_NONE = 2;
//    /**
//     * The filter to apply to the training data
//     */
//    public static final Tag[] TAGS_FILTER = {
//            new Tag(FILTER_NORMALIZE, "Normalize training data"),
//            new Tag(FILTER_STANDARDIZE, "Standardize training data"),
//            new Tag(FILTER_NONE, "No normalization/standardization"),
//    };
//
//    /**
//     * The binary classifier(s)
//     */
//    protected BinarySMO[][] m_classifiers = null;
//
//    /**
//     * The complexity parameter.
//     */
//    protected double C = 1.0;
//
//    /**
//     * Epsilon for rounding.
//     */
//    protected double eps = 1.0e-12;
//
//    /**
//     * Tolerance for accuracy of result.
//     */
//    protected double m_tol = 1.0e-3;
//
//    /**
//     * Whether to normalize/standardize/neither
//     */
//    protected int m_filterType = FILTER_NORMALIZE;
//
//    /**
//     * The filter used to make attributes numeric.
//     */
//    protected NominalToBinary m_NominalToBinary;
//
//    /**
//     * The filter used to standardize/normalize all values.
//     */
//    protected Filter m_Filter = null;
//
//    /**
//     * The filter used to get rid of missing values.
//     */
//    protected ReplaceMissingValues m_Missing;
//
//    /**
//     * The class index from the training data
//     */
//    protected int targetIndex = -1;
//
//    /**
//     * The class attribute
//     */
//    protected Attribute m_classAttribute;
//
//    /**
//     * whether the kernel is a linear one
//     */
//    protected boolean m_KernelIsLinear = false;
//
//    /**
//     * Turn off all checks and conversions? Turning them off assumes
//     * that data is purely numeric, doesn't contain any missing values,
//     * and has a nominal class. Turning them off also means that
//     * no header information will be stored if the machine is linear.
//     * Finally, it also assumes that no instance has a weight equal to 0.
//     */
//    protected boolean m_checksTurnedOff;
//
//    /**
//     * Precision constant for updating sets
//     */
//    protected static double m_Del = 1000 * Double.MIN_VALUE;
//
//    /**
//     * Whether logistic models are to be fit
//     */
//    protected boolean m_fitLogisticModels = false;
//
//    /**
//     * The number of folds for the internal cross-validation
//     */
//    protected int m_numFolds = -1;
//
//    /**
//     * The random number seed
//     */
//    protected int m_randomSeed = 1;
//
//    /**
//     * the kernel to use
//     */
//    protected Kernel kernel = new PolyKernel();
//
//    /**
//     * Turns off checks for missing values, etc. Use with caution.
//     */
//    public void turnChecksOff() {
//
//        m_checksTurnedOff = true;
//    }
//
//    /**
//     * Turns on checks for missing values, etc.
//     */
//    public void turnChecksOn() {
//
//        m_checksTurnedOff = false;
//    }
//
//    /**
//     * Returns default capabilities of the classifier.
//     *
//     * @return the capabilities of this classifier
//     */
//    public Capabilities getCapabilities() {
//        Capabilities result = getKernel().getCapabilities();
//        result.setOwner(this);
//
//        // attribute
//        result.enableAllAttributeDependencies();
//        // with NominalToBinary we can also handle nominal attributes, but only
//        // if the kernel can handle numeric attributes
//        if (result.handles(Capability.NUMERIC_ATTRIBUTES))
//            result.enable(Capability.NOMINAL_ATTRIBUTES);
//        result.enable(Capability.MISSING_VALUES);
//
//        // class
//        result.disableAllClasses();
//        result.disableAllClassDependencies();
//        result.enable(Capability.NOMINAL_CLASS);
//        result.enable(Capability.MISSING_CLASS_VALUES);
//
//        return result;
//    }
//
//    /**
//     * Method for building the classifier. Implements a one-against-one
//     * wrapper for multi-class problems.
//     *
//     * @param insts the set of training instances
//     * @throws Exception if the classifier can't be built successfully
//     */
//    public void buildClassifier(Instances insts) throws Exception {
//
//        if (!m_checksTurnedOff) {
//            // can classifier handle the data?
//            getCapabilities().testWithFail(insts);
//
//            // remove instances with missing class
//            insts = new Instances(insts);
//            insts.deleteWithMissingClass();
//
//      /* Removes all the instances with weight equal to 0.
//       MUST be done since condition (8) of Keerthi's paper
//       is made with the assertion Ci > 0 (See equation (3a). */
//            Instances data = new Instances(insts, insts.numInstances());
//            for (int i = 0; i < insts.numInstances(); i++) {
//                if (insts.instance(i).weight() > 0)
//                    data.add(insts.instance(i));
//            }
//            if (data.numInstances() == 0) {
//                throw new Exception("No training instances left after removing " +
//                        "instances with weight 0!");
//            }
//            insts = data;
//        }
//
//        if (!m_checksTurnedOff) {
//            m_Missing = new ReplaceMissingValues();
//            m_Missing.setInputFormat(insts);
//            insts = Filter.useFilter(insts, m_Missing);
//        } else {
//            m_Missing = null;
//        }
//
//        if (getCapabilities().handles(Capability.NUMERIC_ATTRIBUTES)) {
//            boolean onlyNumeric = true;
//            if (!m_checksTurnedOff) {
//                for (int i = 0; i < insts.numAttributes(); i++) {
//                    if (i != insts.targetIndex()) {
//                        if (!insts.attribute(i).isNumeric()) {
//                            onlyNumeric = false;
//                            break;
//                        }
//                    }
//                }
//            }
//
//            if (!onlyNumeric) {
//                m_NominalToBinary = new NominalToBinary();
//                m_NominalToBinary.setInputFormat(insts);
//                insts = Filter.useFilter(insts, m_NominalToBinary);
//            } else {
//                m_NominalToBinary = null;
//            }
//        } else {
//            m_NominalToBinary = null;
//        }
//
//        if (m_filterType == FILTER_STANDARDIZE) {
//            m_Filter = new Standardize();
//            m_Filter.setInputFormat(insts);
//            insts = Filter.useFilter(insts, m_Filter);
//        } else if (m_filterType == FILTER_NORMALIZE) {
//            m_Filter = new Normalize();
//            m_Filter.setInputFormat(insts);
//            insts = Filter.useFilter(insts, m_Filter);
//        } else {
//            m_Filter = null;
//        }
//
//        targetIndex = insts.targetIndex();
//        m_classAttribute = insts.classAttribute();
//        m_KernelIsLinear = (kernel instanceof PolyKernel) && (((PolyKernel) kernel).getExponent() == 1.0);
//
//        // Generate subsets representing each class
//        Instances[] subsets = new Instances[insts.numClasses()];
//        for (int i = 0; i < insts.numClasses(); i++) {
//            subsets[i] = new Instances(insts, insts.numInstances());
//        }
//        for (int j = 0; j < insts.numInstances(); j++) {
//            Instance inst = insts.instance(j);
//            subsets[(int) inst.classValue()].add(inst);
//        }
//        for (int i = 0; i < insts.numClasses(); i++) {
//            subsets[i].compactify();
//        }
//
//        // Build the binary classifiers
//        Random rand = new Random(m_randomSeed);
//        m_classifiers = new BinarySMO[insts.numClasses()][insts.numClasses()];
//        for (int i = 0; i < insts.numClasses(); i++) {
//            for (int j = i + 1; j < insts.numClasses(); j++) {
//                m_classifiers[i][j] = new BinarySMO();
//                m_classifiers[i][j].setKernel(Kernel.makeCopy(getKernel()));
//                Instances data = new Instances(insts, insts.numInstances());
//                for (int k = 0; k < subsets[i].numInstances(); k++) {
//                    data.add(subsets[i].instance(k));
//                }
//                for (int k = 0; k < subsets[j].numInstances(); k++) {
//                    data.add(subsets[j].instance(k));
//                }
//                data.compactify();
//                data.randomize(rand);
//                m_classifiers[i][j].buildClassifier(data, i, j,
//                        m_fitLogisticModels,
//                        m_numFolds, m_randomSeed);
//            }
//        }
//    }
//
//    /**
//     * Estimates class probabilities for given instance.
//     *
//     * @param inst the instance to compute the probabilities for
//     * @throws Exception in case of an error
//     */
//    public double[] distributionForInstance(Instance inst) throws Exception {
//
//        // Filter instance
//        if (!m_checksTurnedOff) {
//            m_Missing.input(inst);
//            m_Missing.batchFinished();
//            inst = m_Missing.output();
//        }
//
//        if (m_NominalToBinary != null) {
//            m_NominalToBinary.input(inst);
//            m_NominalToBinary.batchFinished();
//            inst = m_NominalToBinary.output();
//        }
//
//        if (m_Filter != null) {
//            m_Filter.input(inst);
//            m_Filter.batchFinished();
//            inst = m_Filter.output();
//        }
//
//        if (!m_fitLogisticModels) {
//            double[] result = new double[inst.numClasses()];
//            for (int i = 0; i < inst.numClasses(); i++) {
//                for (int j = i + 1; j < inst.numClasses(); j++) {
//                    if ((m_classifiers[i][j].alpha != null) ||
//                            (m_classifiers[i][j].sparseWeights != null)) {
//                        double output = m_classifiers[i][j].SVMOutput(-1, inst);
//                        if (output > 0) {
//                            result[j] += 1;
//                        } else {
//                            result[i] += 1;
//                        }
//                    }
//                }
//            }
//            Utils.normalize(result);
//            return result;
//        } else {
//
//            // We only need to do pairwise coupling if there are more
//            // then two classes.
//            if (inst.numClasses() == 2) {
//                double[] newInst = new double[2];
//                newInst[0] = m_classifiers[0][1].SVMOutput(-1, inst);
//                newInst[1] = Utils.missingValue();
//                return m_classifiers[0][1].m_logistic.
//                        distributionForInstance(new DenseInstance(1, newInst));
//            }
//            double[][] r = new double[inst.numClasses()][inst.numClasses()];
//            double[][] n = new double[inst.numClasses()][inst.numClasses()];
//            for (int i = 0; i < inst.numClasses(); i++) {
//                for (int j = i + 1; j < inst.numClasses(); j++) {
//                    if ((m_classifiers[i][j].alpha != null) ||
//                            (m_classifiers[i][j].sparseWeights != null)) {
//                        double[] newInst = new double[2];
//                        newInst[0] = m_classifiers[i][j].SVMOutput(-1, inst);
//                        newInst[1] = Utils.missingValue();
//                        r[i][j] = m_classifiers[i][j].m_logistic.
//                                distributionForInstance(new DenseInstance(1, newInst))[0];
//                        n[i][j] = m_classifiers[i][j].sumOfWeights;
//                    }
//                }
//            }
//            return weka.classifiers.meta.MultiClassClassifier.pairwiseCoupling(n, r);
//        }
//    }
//
//    /**
//     * Returns an array of votes for the given instance.
//     *
//     * @param inst the instance
//     * @return array of votex
//     * @throws Exception if something goes wrong
//     */
//    public int[] obtainVotes(Instance inst) throws Exception {
//
//        // Filter instance
//        if (!m_checksTurnedOff) {
//            m_Missing.input(inst);
//            m_Missing.batchFinished();
//            inst = m_Missing.output();
//        }
//
//        if (m_NominalToBinary != null) {
//            m_NominalToBinary.input(inst);
//            m_NominalToBinary.batchFinished();
//            inst = m_NominalToBinary.output();
//        }
//
//        if (m_Filter != null) {
//            m_Filter.input(inst);
//            m_Filter.batchFinished();
//            inst = m_Filter.output();
//        }
//
//        int[] votes = new int[inst.numClasses()];
//        for (int i = 0; i < inst.numClasses(); i++) {
//            for (int j = i + 1; j < inst.numClasses(); j++) {
//                double output = m_classifiers[i][j].SVMOutput(-1, inst);
//                if (output > 0) {
//                    votes[j] += 1;
//                } else {
//                    votes[i] += 1;
//                }
//            }
//        }
//        return votes;
//    }
//
//    /**
//     * Returns the weights in sparse format.
//     */
//    public double[][][] sparseWeights() {
//
//        int numValues = m_classAttribute.numValues();
//        double[][][] sparseWeights = new double[numValues][numValues][];
//
//        for (int i = 0; i < numValues; i++) {
//            for (int j = i + 1; j < numValues; j++) {
//                sparseWeights[i][j] = m_classifiers[i][j].sparseWeights;
//            }
//        }
//
//        return sparseWeights;
//    }
//
//    /**
//     * Returns the indices in sparse format.
//     */
//    public int[][][] sparseIndices() {
//
//        int numValues = m_classAttribute.numValues();
//        int[][][] sparseIndices = new int[numValues][numValues][];
//
//        for (int i = 0; i < numValues; i++) {
//            for (int j = i + 1; j < numValues; j++) {
//                sparseIndices[i][j] = m_classifiers[i][j].sparseIndices;
//            }
//        }
//
//        return sparseIndices;
//    }
//
//    /**
//     * Returns the bias of each binary SMO.
//     */
//    public double[][] bias() {
//
//        int numValues = m_classAttribute.numValues();
//        double[][] bias = new double[numValues][numValues];
//
//        for (int i = 0; i < numValues; i++) {
//            for (int j = i + 1; j < numValues; j++) {
//                bias[i][j] = m_classifiers[i][j].b;
//            }
//        }
//
//        return bias;
//    }
//
//    /*
//     * Returns the number of values of the class attribute.
//     */
//    public int numClassAttributeValues() {
//
//        return m_classAttribute.numValues();
//    }
//
//    /*
//     * Returns the names of the class attributes.
//     */
//    public String[] classAttributeNames() {
//
//        int numValues = m_classAttribute.numValues();
//
//        String[] classAttributeNames = new String[numValues];
//
//        for (int i = 0; i < numValues; i++) {
//            classAttributeNames[i] = m_classAttribute.value(i);
//        }
//
//        return classAttributeNames;
//    }
//
//    /**
//     * Returns the attribute names.
//     */
//    public String[][][] attributeNames() {
//
//        int numValues = m_classAttribute.numValues();
//        String[][][] attributeNames = new String[numValues][numValues][];
//
//        for (int i = 0; i < numValues; i++) {
//            for (int j = i + 1; j < numValues; j++) {
//                //	int numAttributes = m_classifiers[i][j].m_data.numAttributes();
//                int numAttributes = m_classifiers[i][j].sparseIndices.length;
//                String[] attrNames = new String[numAttributes];
//                for (int k = 0; k < numAttributes; k++) {
//                    attrNames[k] = m_classifiers[i][j].
//                            m_data.attribute(m_classifiers[i][j].sparseIndices[k]).name();
//                }
//                attributeNames[i][j] = attrNames;
//            }
//        }
//        return attributeNames;
//    }
//
//    /**
//     * Returns an enumeration describing the available options.
//     *
//     * @return an enumeration of all the available options.
//     */
//    public Enumeration listOptions() {
//
//        Vector result = new Vector();
//
//        Enumeration enm = super.listOptions();
//        while (enm.hasMoreElements())
//            result.addElement(enm.nextElement());
//
//        result.addElement(new Option(
//                "\tTurns off all checks - use with caution!\n"
//                        + "\tTurning them off assumes that data is purely numeric, doesn't\n"
//                        + "\tcontain any missing values, and has a nominal class. Turning them\n"
//                        + "\toff also means that no header information will be stored if the\n"
//                        + "\tmachine is linear. Finally, it also assumes that no instance has\n"
//                        + "\ta weight equal to 0.\n"
//                        + "\t(default: checks on)",
//                "no-checks", 0, "-no-checks"));
//
//        result.addElement(new Option(
//                "\tThe complexity constant C. (default 1)",
//                "C", 1, "-C <double>"));
//
//        result.addElement(new Option(
//                "\tWhether to 0=normalize/1=standardize/2=neither. " +
//                        "(default 0=normalize)",
//                "N", 1, "-N"));
//
//        result.addElement(new Option(
//                "\tThe tolerance parameter. " +
//                        "(default 1.0e-3)",
//                "L", 1, "-L <double>"));
//
//        result.addElement(new Option(
//                "\tThe epsilon for round-off error. " +
//                        "(default 1.0e-12)",
//                "P", 1, "-P <double>"));
//
//        result.addElement(new Option(
//                "\tFit logistic models to SVM outputs. ",
//                "M", 0, "-M"));
//
//        result.addElement(new Option(
//                "\tThe number of folds for the internal\n" +
//                        "\tcross-validation. " +
//                        "(default -1, use training data)",
//                "RV", 1, "-RV <double>"));
//
//        result.addElement(new Option(
//                "\tThe random number seed. " +
//                        "(default 1)",
//                "W", 1, "-W <double>"));
//
//        result.addElement(new Option(
//                "\tThe Kernel to use.\n"
//                        + "\t(default: weka.classifiers.functions.supportVector.PolyKernel)",
//                "K", 1, "-K <classname and parameters>"));
//
//        result.addElement(new Option(
//                "",
//                "", 0, "\nOptions specific to kernel "
//                + getKernel().getClass().getName() + ":"));
//
//        enm = ((OptionHandler) getKernel()).listOptions();
//        while (enm.hasMoreElements())
//            result.addElement(enm.nextElement());
//
//        return result.elements();
//    }
//
//    /**
//     * Parses a given list of options. <p/>
//     * <p>
//     * <!-- options-start -->
//     * Valid options are: <p/>
//     * <p>
//     * <pre> -D
//     *  If set, classifier is run in debug mode and
//     *  may output additional info to the console</pre>
//     * <p>
//     * <pre> -no-checks
//     *  Turns off all checks - use with caution!
//     *  Turning them off assumes that data is purely numeric, doesn't
//     *  contain any missing values, and has a nominal class. Turning them
//     *  off also means that no header information will be stored if the
//     *  machine is linear. Finally, it also assumes that no instance has
//     *  a weight equal to 0.
//     *  (default: checks on)</pre>
//     * <p>
//     * <pre> -C &lt;double&gt;
//     *  The complexity constant C. (default 1)</pre>
//     * <p>
//     * <pre> -N
//     *  Whether to 0=normalize/1=standardize/2=neither. (default 0=normalize)</pre>
//     * <p>
//     * <pre> -L &lt;double&gt;
//     *  The tolerance parameter. (default 1.0e-3)</pre>
//     * <p>
//     * <pre> -P &lt;double&gt;
//     *  The epsilon for round-off error. (default 1.0e-12)</pre>
//     * <p>
//     * <pre> -M
//     *  Fit logistic models to SVM outputs. </pre>
//     * <p>
//     * <pre> -RV &lt;double&gt;
//     *  The number of folds for the internal
//     *  cross-validation. (default -1, use training data)</pre>
//     * <p>
//     * <pre> -W &lt;double&gt;
//     *  The random number seed. (default 1)</pre>
//     * <p>
//     * <pre> -K &lt;classname and parameters&gt;
//     *  The Kernel to use.
//     *  (default: weka.classifiers.functions.supportVector.PolyKernel)</pre>
//     * <p>
//     * <pre>
//     * Options specific to kernel weka.classifiers.functions.supportVector.PolyKernel:
//     * </pre>
//     * <p>
//     * <pre> -D
//     *  Enables debugging output (if available) to be printed.
//     *  (default: off)</pre>
//     * <p>
//     * <pre> -no-checks
//     *  Turns off all checks - use with caution!
//     *  (default: checks on)</pre>
//     * <p>
//     * <pre> -C &lt;num&gt;
//     *  The size of the cache (a prime number), 0 for full cache and
//     *  -1 to turn it off.
//     *  (default: 250007)</pre>
//     * <p>
//     * <pre> -E &lt;num&gt;
//     *  The Exponent to use.
//     *  (default: 1.0)</pre>
//     * <p>
//     * <pre> -L
//     *  Use lower-order terms.
//     *  (default: no)</pre>
//     * <p>
//     * <!-- options-end -->
//     *
//     * @param options the list of options as an array of strings
//     * @throws Exception if an option is not supported
//     */
//    public void setOptions(String[] options) throws Exception {
//        String tmpStr;
//        String[] tmpOptions;
//
//        setChecksTurnedOff(Utils.getFlag("no-checks", options));
//
//        tmpStr = Utils.getOption('C', options);
//        if (tmpStr.length() != 0)
//            setC(Double.parseDouble(tmpStr));
//        else
//            setC(1.0);
//
//        tmpStr = Utils.getOption('L', options);
//        if (tmpStr.length() != 0)
//            setToleranceParameter(Double.parseDouble(tmpStr));
//        else
//            setToleranceParameter(1.0e-3);
//
//        tmpStr = Utils.getOption('P', options);
//        if (tmpStr.length() != 0)
//            setEpsilon(Double.parseDouble(tmpStr));
//        else
//            setEpsilon(1.0e-12);
//
//        tmpStr = Utils.getOption('N', options);
//        if (tmpStr.length() != 0)
//            setFilterType(new SelectedTag(Integer.parseInt(tmpStr), TAGS_FILTER));
//        else
//            setFilterType(new SelectedTag(FILTER_NORMALIZE, TAGS_FILTER));
//
//        setBuildLogisticModels(Utils.getFlag('M', options));
//
//        tmpStr = Utils.getOption('RV', options);
//        if (tmpStr.length() != 0)
//            setNumFolds(Integer.parseInt(tmpStr));
//        else
//            setNumFolds(-1);
//
//        tmpStr = Utils.getOption('W', options);
//        if (tmpStr.length() != 0)
//            setRandomSeed(Integer.parseInt(tmpStr));
//        else
//            setRandomSeed(1);
//
//        tmpStr = Utils.getOption('K', options);
//        tmpOptions = Utils.splitOptions(tmpStr);
//        if (tmpOptions.length != 0) {
//            tmpStr = tmpOptions[0];
//            tmpOptions[0] = "";
//            setKernel(Kernel.forName(tmpStr, tmpOptions));
//        }
//
//        super.setOptions(options);
//    }
//
//    /**
//     * Gets the current settings of the classifier.
//     *
//     * @return an array of strings suitable for passing to setOptions
//     */
//    public String[] getOptions() {
//        int i;
//        Vector result;
//        String[] options;
//
//        result = new Vector();
//        options = super.getOptions();
//        for (i = 0; i < options.length; i++)
//            result.add(options[i]);
//
//        if (getChecksTurnedOff())
//            result.add("-no-checks");
//
//        result.add("-C");
//        result.add("" + getC());
//
//        result.add("-L");
//        result.add("" + getToleranceParameter());
//
//        result.add("-P");
//        result.add("" + getEpsilon());
//
//        result.add("-N");
//        result.add("" + m_filterType);
//
//        if (getBuildLogisticModels())
//            result.add("-M");
//
//        result.add("-RV");
//        result.add("" + getNumFolds());
//
//        result.add("-W");
//        result.add("" + getRandomSeed());
//
//        result.add("-K");
//        result.add("" + getKernel().getClass().getName() + " " + Utils.joinOptions(getKernel().getOptions()));
//
//        return (String[]) result.toArray(new String[result.size()]);
//    }
//
//    /**
//     * Disables or enables the checks (which could be time-consuming). Use with
//     * caution!
//     *
//     * @param value if true turns off all checks
//     */
//    public void setChecksTurnedOff(boolean value) {
//        if (value)
//            turnChecksOff();
//        else
//            turnChecksOn();
//    }
//
//    /**
//     * Returns whether the checks are turned off or not.
//     *
//     * @return true if the checks are turned off
//     */
//    public boolean getChecksTurnedOff() {
//        return m_checksTurnedOff;
//    }
//
//    /**
//     * Returns the tip text for this property
//     *
//     * @return tip text for this property suitable for
//     * displaying in the explorer/experimenter gui
//     */
//    public String checksTurnedOffTipText() {
//        return "Turns time-consuming checks off - use with caution.";
//    }
//
//    /**
//     * Returns the tip text for this property
//     *
//     * @return tip text for this property suitable for
//     * displaying in the explorer/experimenter gui
//     */
//    public String kernelTipText() {
//        return "The kernel to use.";
//    }
//
//    /**
//     * sets the kernel to use
//     *
//     * @param value the kernel to use
//     */
//    public void setKernel(Kernel value) {
//        kernel = value;
//    }
//
//    /**
//     * Returns the kernel to use
//     *
//     * @return the current kernel
//     */
//    public Kernel getKernel() {
//        return kernel;
//    }
//
//    /**
//     * Returns the tip text for this property
//     *
//     * @return tip text for this property suitable for
//     * displaying in the explorer/experimenter gui
//     */
//    public String cTipText() {
//        return "The complexity parameter C.";
//    }
//
//    /**
//     * Get the value of C.
//     *
//     * @return Value of C.
//     */
//    public double getC() {
//
//        return C;
//    }
//
//    /**
//     * Set the value of C.
//     *
//     * @param v Value to assign to C.
//     */
//    public void setC(double v) {
//
//        C = v;
//    }
//
//    /**
//     * Returns the tip text for this property
//     *
//     * @return tip text for this property suitable for
//     * displaying in the explorer/experimenter gui
//     */
//    public String toleranceParameterTipText() {
//        return "The tolerance parameter (shouldn't be changed).";
//    }
//
//    /**
//     * Get the value of tolerance parameter.
//     *
//     * @return Value of tolerance parameter.
//     */
//    public double getToleranceParameter() {
//
//        return m_tol;
//    }
//
//    /**
//     * Set the value of tolerance parameter.
//     *
//     * @param v Value to assign to tolerance parameter.
//     */
//    public void setToleranceParameter(double v) {
//
//        m_tol = v;
//    }
//
//    /**
//     * Returns the tip text for this property
//     *
//     * @return tip text for this property suitable for
//     * displaying in the explorer/experimenter gui
//     */
//    public String epsilonTipText() {
//        return "The epsilon for round-off error (shouldn't be changed).";
//    }
//
//    /**
//     * Get the value of epsilon.
//     *
//     * @return Value of epsilon.
//     */
//    public double getEpsilon() {
//
//        return eps;
//    }
//
//    /**
//     * Set the value of epsilon.
//     *
//     * @param v Value to assign to epsilon.
//     */
//    public void setEpsilon(double v) {
//
//        eps = v;
//    }
//
//    /**
//     * Returns the tip text for this property
//     *
//     * @return tip text for this property suitable for
//     * displaying in the explorer/experimenter gui
//     */
//    public String filterTypeTipText() {
//        return "Determines how/if the data will be transformed.";
//    }
//
//    /**
//     * Gets how the training data will be transformed. Will be one of
//     * FILTER_NORMALIZE, FILTER_STANDARDIZE, FILTER_NONE.
//     *
//     * @return the filtering mode
//     */
//    public SelectedTag getFilterType() {
//
//        return new SelectedTag(m_filterType, TAGS_FILTER);
//    }
//
//    /**
//     * Sets how the training data will be transformed. Should be one of
//     * FILTER_NORMALIZE, FILTER_STANDARDIZE, FILTER_NONE.
//     *
//     * @param newType the new filtering mode
//     */
//    public void setFilterType(SelectedTag newType) {
//
//        if (newType.getTags() == TAGS_FILTER) {
//            m_filterType = newType.getSelectedTag().getID();
//        }
//    }
//
//    /**
//     * Returns the tip text for this property
//     *
//     * @return tip text for this property suitable for
//     * displaying in the explorer/experimenter gui
//     */
//    public String buildLogisticModelsTipText() {
//        return "Whether to fit logistic models to the outputs (for proper "
//                + "probability estimates).";
//    }
//
//    /**
//     * Get the value of buildLogisticModels.
//     *
//     * @return Value of buildLogisticModels.
//     */
//    public boolean getBuildLogisticModels() {
//
//        return m_fitLogisticModels;
//    }
//
//    /**
//     * Set the value of buildLogisticModels.
//     *
//     * @param newbuildLogisticModels Value to assign to buildLogisticModels.
//     */
//    public void setBuildLogisticModels(boolean newbuildLogisticModels) {
//
//        m_fitLogisticModels = newbuildLogisticModels;
//    }
//
//    /**
//     * Returns the tip text for this property
//     *
//     * @return tip text for this property suitable for
//     * displaying in the explorer/experimenter gui
//     */
//    public String numFoldsTipText() {
//        return "The number of folds for cross-validation used to generate "
//                + "training data for logistic models (-1 means use training data).";
//    }
//
//    /**
//     * Get the value of numFolds.
//     *
//     * @return Value of numFolds.
//     */
//    public int getNumFolds() {
//
//        return m_numFolds;
//    }
//
//    /**
//     * Set the value of numFolds.
//     *
//     * @param newnumFolds Value to assign to numFolds.
//     */
//    public void setNumFolds(int newnumFolds) {
//
//        m_numFolds = newnumFolds;
//    }
//
//    /**
//     * Returns the tip text for this property
//     *
//     * @return tip text for this property suitable for
//     * displaying in the explorer/experimenter gui
//     */
//    public String randomSeedTipText() {
//        return "Random number seed for the cross-validation.";
//    }
//
//    /**
//     * Get the value of randomSeed.
//     *
//     * @return Value of randomSeed.
//     */
//    public int getRandomSeed() {
//
//        return m_randomSeed;
//    }
//
//    /**
//     * Set the value of randomSeed.
//     *
//     * @param newrandomSeed Value to assign to randomSeed.
//     */
//    public void setRandomSeed(int newrandomSeed) {
//
//        m_randomSeed = newrandomSeed;
//    }
//
//    /**
//     * Prints out the classifier.
//     *
//     * @return a description of the classifier as a string
//     */
//    public String toString() {
//
//        StringBuffer text = new StringBuffer();
//
//        if ((m_classAttribute == null)) {
//            return "SMO: No model built yet.";
//        }
//        try {
//            text.append("SMO\n\n");
//            text.append("Kernel used:\n  " + kernel.toString() + "\n\n");
//
//            for (int i = 0; i < m_classAttribute.numValues(); i++) {
//                for (int j = i + 1; j < m_classAttribute.numValues(); j++) {
//                    text.append("Classifier for classes: " +
//                            m_classAttribute.value(i) + ", " +
//                            m_classAttribute.value(j) + "\n\n");
//                    text.append(m_classifiers[i][j]);
//                    if (m_fitLogisticModels) {
//                        text.append("\n\n");
//                        if (m_classifiers[i][j].m_logistic == null) {
//                            text.append("No logistic model has been fit.\n");
//                        } else {
//                            text.append(m_classifiers[i][j].m_logistic);
//                        }
//                    }
//                    text.append("\n\n");
//                }
//            }
//        } catch (Exception e) {
//            return "Can't print SMO classifier.";
//        }
//
//        return text.toString();
//    }
//
//    /**
//     * Returns the revision string.
//     *
//     * @return the revision
//     */
//    public String getRevision() {
//        return RevisionUtils.extract("$Revision: 6024 $");
//    }
//
//    /**
//     * Main method for testing this class.
//     */
//    public static void main(String[] argv) {
//        runClassifier(new SMO(), argv);
//    }
//}
//
