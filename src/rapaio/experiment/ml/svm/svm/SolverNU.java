/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.experiment.ml.svm.svm;

/**
 * Solver for nu-svm classification and regression.
 * <p>
 * additional constraint: e^T \alpha = constant
 */
public final class SolverNU extends Solver {
    private SolutionInfo si;

    public void solve(int trainingSize, AbstractKernelMatrix q, double[] p, byte[] y,
            double[] alpha, double cp, double cn, double eps,
            SolutionInfo si, int shrinking) {
        this.si = si;
        super.solve(trainingSize, q, p, y, alpha, cp, cn, eps, si, shrinking);
    }

    // return 1 if already optimal, return 0 otherwise
    int select_working_set(int[] workingSet) {
        // return i,j such that y_i = y_j and
        // i: maximizes -y_i * grad(f)_i, i in I_up(\alpha)
        // j: minimizes the decrease of obj value
        //    (if quadratic coefficeint <= 0, replace it with tau)
        //    -y_j*grad(f)_j < -y_i*grad(f)_i, j in I_low(\alpha)

        double gmaxp = -INF;
        double gmaxp2 = -INF;
        int gmaxpIdx = -1;

        double gmaxn = -INF;
        double gmaxn2 = -INF;
        int gmaxnIdx = -1;

        int Gmin_idx = -1;
        double obj_diff_min = INF;

        for (int t = 0; t < active_size; t++) {
            if (y[t] == +1) {
                if (!is_upper_bound(t)) {
                    if (-G[t] >= gmaxp) {
                        gmaxp = -G[t];
                        gmaxpIdx = t;
                    }
                }
            } else {
                if (!is_lower_bound(t)) {
                    if (G[t] >= gmaxn) {
                        gmaxn = G[t];
                        gmaxnIdx = t;
                    }
                }
            }
        }

        int ip = gmaxpIdx;
        int in = gmaxnIdx;
        float[] qip = null;
        float[] qin = null;
        // null Q_ip not accessed: Gmaxp=-INF if ip=-1
        if (ip != -1) {
            qip = Q.getQ(ip, active_size);
        }
        if (in != -1) {
            qin = Q.getQ(in, active_size);
        }

        for (int j = 0; j < active_size; j++) {
            if (y[j] == +1) {
                if (!is_lower_bound(j)) {
                    double gradDiff = gmaxp + G[j];
                    if (G[j] >= gmaxp2) {
                        gmaxp2 = G[j];
                    }
                    if (gradDiff > 0) {
                        double objDiff;
                        double quadCoef = QD[ip] + QD[j] - 2 * qip[j];
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
                if (!is_upper_bound(j)) {
                    double gradDiff = gmaxn - G[j];
                    if (-G[j] >= gmaxn2) {
                        gmaxn2 = -G[j];
                    }
                    if (gradDiff > 0) {
                        double objDiff;
                        double quadCoef = QD[in] + QD[j] - 2 * qin[j];
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
            return 1;
        }

        if (y[Gmin_idx] == +1) {
            workingSet[0] = gmaxpIdx;
        } else {
            workingSet[0] = gmaxnIdx;
        }
        workingSet[1] = Gmin_idx;

        return 0;
    }

    private boolean be_shrunk(int i, double gmax1, double gmax2, double gmax3, double gmax4) {
        if (is_upper_bound(i)) {
            if (y[i] == +1) {
                return (-G[i] > gmax1);
            } else {
                return (-G[i] > gmax4);
            }
        } else if (is_lower_bound(i)) {
            if (y[i] == +1) {
                return (G[i] > gmax2);
            } else {
                return (G[i] > gmax3);
            }
        } else {
            return (false);
        }
    }

    void do_shrinking() {
        double Gmax1 = -INF;    // max { -y_i * grad(f)_i | y_i = +1, i in I_up(\alpha) }
        double Gmax2 = -INF;    // max { y_i * grad(f)_i | y_i = +1, i in I_low(\alpha) }
        double Gmax3 = -INF;    // max { -y_i * grad(f)_i | y_i = -1, i in I_up(\alpha) }
        double Gmax4 = -INF;    // max { y_i * grad(f)_i | y_i = -1, i in I_low(\alpha) }

        // find maximal violating pair first
        int i;
        for (i = 0; i < active_size; i++) {
            if (!is_upper_bound(i)) {
                if (y[i] == +1) {
                    if (-G[i] > Gmax1) {
                        Gmax1 = -G[i];
                    }
                } else if (-G[i] > Gmax4) {
                    Gmax4 = -G[i];
                }
            }
            if (!is_lower_bound(i)) {
                if (y[i] == +1) {
                    if (G[i] > Gmax2) {
                        Gmax2 = G[i];
                    }
                } else if (G[i] > Gmax3) {
                    Gmax3 = G[i];
                }
            }
        }

        if (!unshrink && Math.max(Gmax1 + Gmax2, Gmax3 + Gmax4) <= eps * 10) {
            unshrink = true;
            reconstruct_gradient();
            active_size = l;
        }

        for (i = 0; i < active_size; i++) {
            if (be_shrunk(i, Gmax1, Gmax2, Gmax3, Gmax4)) {
                active_size--;
                while (active_size > i) {
                    if (!be_shrunk(active_size, Gmax1, Gmax2, Gmax3, Gmax4)) {
                        swap_index(i, active_size);
                        break;
                    }
                    active_size--;
                }
            }
        }
    }

    double calculate_rho() {
        int nrFree1 = 0;
        int nrFree2 = 0;
        double ub1 = INF;
        double ub2 = INF;
        double lb1 = -INF;
        double lb2 = -INF;
        double sumFree1 = 0;
        double sumFree2 = 0;

        for (int i = 0; i < active_size; i++) {
            if (y[i] == +1) {
                if (is_upper_bound(i)) {
                    lb1 = Math.max(lb1, G[i]);
                } else if (is_lower_bound(i)) {
                    ub1 = Math.min(ub1, G[i]);
                } else {
                    ++nrFree1;
                    sumFree1 += G[i];
                }
            } else {
                if (is_upper_bound(i)) {
                    lb2 = Math.max(lb2, G[i]);
                } else if (is_lower_bound(i)) {
                    ub2 = Math.min(ub2, G[i]);
                } else {
                    ++nrFree2;
                    sumFree2 += G[i];
                }
            }
        }

        double r1;
        double r2;
        if (nrFree1 > 0) {
            r1 = sumFree1 / nrFree1;
        } else {
            r1 = (ub1 + lb1) / 2;
        }

        if (nrFree2 > 0) {
            r2 = sumFree2 / nrFree2;
        } else {
            r2 = (ub2 + lb2) / 2;
        }

        si.r = (r1 + r2) / 2;
        return (r1 - r2) / 2;
    }
}
