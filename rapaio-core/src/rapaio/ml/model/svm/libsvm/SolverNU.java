/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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
 *
 */

package rapaio.ml.model.svm.libsvm;

import rapaio.narray.NArray;

/**
 * Solver for nu-svm classification and regression.
 * <p>
 * additional constraint: e^T \alpha = constant
 */
public final class SolverNU extends SolverC {
    private Svm.SolutionInfo si;

    public void solve(int len, AbstractKernelMatrix q, double[] p, byte[] y, double[] alpha, double cp, double cn, double eps,
            Svm.SolutionInfo si, int shrinking) {
        this.si = si;
        super.solve(len, q, p, y, alpha, cp, cn, eps, si, shrinking);
    }

    // return 1 if already optimal, return 0 otherwise
    @Override
    WorkingSet selectWorkingSet() {
        // return i,j such that y_i = y_j and
        // i: maximizes -y_i * grad(f)_i, i in I_up(\alpha)
        // j: minimizes the decrease of obj value
        //    (if quadratic coefficeint <= 0, replace it with tau)
        //    -y_j*grad(f)_j < -y_i*grad(f)_i, j in I_low(\alpha)

        double gmaxp = Double.NEGATIVE_INFINITY;
        double gmaxp2 = Double.NEGATIVE_INFINITY;
        int gmaxpIdx = -1;

        double gmaxn = Double.NEGATIVE_INFINITY;
        double gmaxn2 = Double.NEGATIVE_INFINITY;
        int gmaxnIdx = -1;

        int Gmin_idx = -1;
        double obj_diff_min = Double.POSITIVE_INFINITY;

        for (int t = 0; t < activeSize; t++) {
            if (y[t] == 1) {
                if (!isUpperBound(t)) {
                    if (-grad[t] >= gmaxp) {
                        gmaxp = -grad[t];
                        gmaxpIdx = t;
                    }
                }
            } else {
                if (!isLowerBound(t)) {
                    if (grad[t] >= gmaxn) {
                        gmaxn = grad[t];
                        gmaxnIdx = t;
                    }
                }
            }
        }

        int ip = gmaxpIdx;
        int in = gmaxnIdx;
        NArray<Double> qip = null;
        NArray<Double> qin = null;

        // null Q_ip not accessed: Gmaxp=-INF if ip=-1
        if (ip != -1) {
            qip = q.getQ(ip, activeSize);
        }
        if (in != -1) {
            qin = q.getQ(in, activeSize);
        }

        for (int j = 0; j < activeSize; j++) {
            if (y[j] == 1) {
                if (!isLowerBound(j)) {
                    double gradDiff = gmaxp + grad[j];
                    if (grad[j] >= gmaxp2) {
                        gmaxp2 = grad[j];
                    }
                    if (gradDiff > 0) {
                        double objDiff;
                        double quadCoef = qd[ip] + qd[j] - 2 * qip.get(j);
                        if (quadCoef > 0) {
                            objDiff = -(gradDiff * gradDiff) / quadCoef;
                        } else {
                            objDiff = -(gradDiff * gradDiff) / 1e-12;
                        }

                        if (objDiff <= obj_diff_min) {
                            Gmin_idx = j;
                            obj_diff_min = objDiff;
                        }
                    }
                }
            } else {
                if (!isUpperBound(j)) {
                    double gradDiff = gmaxn - grad[j];
                    if (-grad[j] >= gmaxn2) {
                        gmaxn2 = -grad[j];
                    }
                    if (gradDiff > 0) {
                        double objDiff;
                        double quadCoef = qd[in] + qd[j] - 2 * qin.get(j);
                        if (quadCoef > 0) {
                            objDiff = -(gradDiff * gradDiff) / quadCoef;
                        } else {
                            objDiff = -(gradDiff * gradDiff) / 1e-12;
                        }

                        if (objDiff <= obj_diff_min) {
                            Gmin_idx = j;
                            obj_diff_min = objDiff;
                        }
                    }
                }
            }
        }

        if (Math.max(gmaxp + gmaxp2, gmaxn + gmaxn2) < eps || Gmin_idx == -1) {
            return null;
        }
        return new WorkingSet(y[Gmin_idx] == 1 ? gmaxpIdx : gmaxnIdx, Gmin_idx);
    }

    private boolean beShrunk(int i, double gmax1, double gmax2, double gmax3, double gmax4) {
        if (isUpperBound(i)) {
            return y[i] == 1 ? (-grad[i] > gmax1) : (-grad[i] > gmax4);
        } else if (isLowerBound(i)) {
            return (y[i] == 1) ? (grad[i] > gmax2) : (grad[i] > gmax3);
        } else {
            return false;
        }
    }

    @Override
    void doShrinking() {
        double Gmax1 = Double.NEGATIVE_INFINITY;    // max { -y_i * grad(f)_i | y_i = +1, i in I_up(\alpha) }
        double Gmax2 = Double.NEGATIVE_INFINITY;    // max { y_i * grad(f)_i | y_i = +1, i in I_low(\alpha) }
        double Gmax3 = Double.NEGATIVE_INFINITY;    // max { -y_i * grad(f)_i | y_i = -1, i in I_up(\alpha) }
        double Gmax4 = Double.NEGATIVE_INFINITY;    // max { y_i * grad(f)_i | y_i = -1, i in I_low(\alpha) }

        // find maximal violating pair first
        int i;
        for (i = 0; i < activeSize; i++) {
            if (!isUpperBound(i)) {
                if (y[i] == 1) {
                    if (-grad[i] > Gmax1) {
                        Gmax1 = -grad[i];
                    }
                } else if (-grad[i] > Gmax4) {
                    Gmax4 = -grad[i];
                }
            }
            if (!isLowerBound(i)) {
                if (y[i] == 1) {
                    if (grad[i] > Gmax2) {
                        Gmax2 = grad[i];
                    }
                } else if (grad[i] > Gmax3) {
                    Gmax3 = grad[i];
                }
            }
        }

        if (!unshrink && StrictMath.max(Gmax1 + Gmax2, Gmax3 + Gmax4) <= eps * 10) {
            unshrink = true;
            reconstruct_gradient();
            activeSize = len;
        }

        for (i = 0; i < activeSize; i++) {
            if (beShrunk(i, Gmax1, Gmax2, Gmax3, Gmax4)) {
                activeSize--;
                while (activeSize > i) {
                    if (!beShrunk(activeSize, Gmax1, Gmax2, Gmax3, Gmax4)) {
                        swapIndex(i, activeSize);
                        break;
                    }
                    activeSize--;
                }
            }
        }
    }

    @Override
    double calculateRho() {
        int nrFree1 = 0;
        int nrFree2 = 0;
        double ub1 = Double.POSITIVE_INFINITY;
        double ub2 = Double.POSITIVE_INFINITY;
        double lb1 = Double.NEGATIVE_INFINITY;
        double lb2 = Double.NEGATIVE_INFINITY;
        double sumFree1 = 0;
        double sumFree2 = 0;

        for (int i = 0; i < activeSize; i++) {
            if (y[i] == 1) {
                if (isUpperBound(i)) {
                    lb1 = Math.max(lb1, grad[i]);
                } else if (isLowerBound(i)) {
                    ub1 = Math.min(ub1, grad[i]);
                } else {
                    nrFree1++;
                    sumFree1 += grad[i];
                }
            } else {
                if (isUpperBound(i)) {
                    lb2 = Math.max(lb2, grad[i]);
                } else if (isLowerBound(i)) {
                    ub2 = Math.min(ub2, grad[i]);
                } else {
                    nrFree2++;
                    sumFree2 += grad[i];
                }
            }
        }

        double r1 = (nrFree1 > 0) ? sumFree1 / nrFree1 : (ub1 + lb1) / 2;
        double r2 = (nrFree2 > 0) ? sumFree2 / nrFree2 : (ub2 + lb2) / 2;

        si.r = (r1 + r2) / 2;
        return (r1 - r2) / 2;
    }
}
