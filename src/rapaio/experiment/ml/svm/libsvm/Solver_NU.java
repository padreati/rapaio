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

package rapaio.experiment.ml.svm.libsvm;

//
// Solver for nu-svm classification and regression
//
// additional constraint: e^T \alpha = constant
//
public final class Solver_NU extends Solver {
    private SolutionInfo si;

    public void Solve(int trainingSize, AbstractKernelMatrix Q, double[] p, byte[] y,
            double[] alpha, double Cp, double Cn, double eps,
            SolutionInfo si, int shrinking) {
        this.si = si;
        super.Solve(trainingSize, Q, p, y, alpha, Cp, Cn, eps, si, shrinking);
    }

    // return 1 if already optimal, return 0 otherwise
    int select_working_set(int[] working_set) {
        // return i,j such that y_i = y_j and
        // i: maximizes -y_i * grad(f)_i, i in I_up(\alpha)
        // j: minimizes the decrease of obj value
        //    (if quadratic coefficeint <= 0, replace it with tau)
        //    -y_j*grad(f)_j < -y_i*grad(f)_i, j in I_low(\alpha)

        double Gmaxp = -INF;
        double Gmaxp2 = -INF;
        int Gmaxp_idx = -1;

        double Gmaxn = -INF;
        double Gmaxn2 = -INF;
        int Gmaxn_idx = -1;

        int Gmin_idx = -1;
        double obj_diff_min = INF;

        for (int t = 0; t < active_size; t++) {
            if (y[t] == +1) {
                if (!is_upper_bound(t)) {
                    if (-G[t] >= Gmaxp) {
                        Gmaxp = -G[t];
                        Gmaxp_idx = t;
                    }
                }
            } else {
                if (!is_lower_bound(t)) {
                    if (G[t] >= Gmaxn) {
                        Gmaxn = G[t];
                        Gmaxn_idx = t;
                    }
                }
            }
        }

        int ip = Gmaxp_idx;
        int in = Gmaxn_idx;
        float[] Q_ip = null;
        float[] Q_in = null;
        if (ip != -1) // null Q_ip not accessed: Gmaxp=-INF if ip=-1
        {
            Q_ip = Q.get_Q(ip, active_size);
        }
        if (in != -1) {
            Q_in = Q.get_Q(in, active_size);
        }

        for (int j = 0; j < active_size; j++) {
            if (y[j] == +1) {
                if (!is_lower_bound(j)) {
                    double grad_diff = Gmaxp + G[j];
                    if (G[j] >= Gmaxp2) {
                        Gmaxp2 = G[j];
                    }
                    if (grad_diff > 0) {
                        double obj_diff;
                        double quad_coef = QD[ip] + QD[j] - 2 * Q_ip[j];
                        if (quad_coef > 0) {
                            obj_diff = -(grad_diff * grad_diff) / quad_coef;
                        } else {
                            obj_diff = -(grad_diff * grad_diff) / 1e-12;
                        }

                        if (obj_diff <= obj_diff_min) {
                            Gmin_idx = j;
                            obj_diff_min = obj_diff;
                        }
                    }
                }
            } else {
                if (!is_upper_bound(j)) {
                    double grad_diff = Gmaxn - G[j];
                    if (-G[j] >= Gmaxn2) {
                        Gmaxn2 = -G[j];
                    }
                    if (grad_diff > 0) {
                        double obj_diff;
                        double quad_coef = QD[in] + QD[j] - 2 * Q_in[j];
                        if (quad_coef > 0) {
                            obj_diff = -(grad_diff * grad_diff) / quad_coef;
                        } else {
                            obj_diff = -(grad_diff * grad_diff) / 1e-12;
                        }

                        if (obj_diff <= obj_diff_min) {
                            Gmin_idx = j;
                            obj_diff_min = obj_diff;
                        }
                    }
                }
            }
        }

        if (Math.max(Gmaxp + Gmaxp2, Gmaxn + Gmaxn2) < eps || Gmin_idx == -1) {
            return 1;
        }

        if (y[Gmin_idx] == +1) {
            working_set[0] = Gmaxp_idx;
        } else {
            working_set[0] = Gmaxn_idx;
        }
        working_set[1] = Gmin_idx;

        return 0;
    }

    private boolean be_shrunk(int i, double Gmax1, double Gmax2, double Gmax3, double Gmax4) {
        if (is_upper_bound(i)) {
            if (y[i] == +1) {
                return (-G[i] > Gmax1);
            } else {
                return (-G[i] > Gmax4);
            }
        } else if (is_lower_bound(i)) {
            if (y[i] == +1) {
                return (G[i] > Gmax2);
            } else {
                return (G[i] > Gmax3);
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

        if (unshrink == false && Math.max(Gmax1 + Gmax2, Gmax3 + Gmax4) <= eps * 10) {
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
        int nr_free1 = 0, nr_free2 = 0;
        double ub1 = INF, ub2 = INF;
        double lb1 = -INF, lb2 = -INF;
        double sum_free1 = 0, sum_free2 = 0;

        for (int i = 0; i < active_size; i++) {
            if (y[i] == +1) {
                if (is_upper_bound(i)) {
                    lb1 = Math.max(lb1, G[i]);
                } else if (is_lower_bound(i)) {
                    ub1 = Math.min(ub1, G[i]);
                } else {
                    ++nr_free1;
                    sum_free1 += G[i];
                }
            } else {
                if (is_upper_bound(i)) {
                    lb2 = Math.max(lb2, G[i]);
                } else if (is_lower_bound(i)) {
                    ub2 = Math.min(ub2, G[i]);
                } else {
                    ++nr_free2;
                    sum_free2 += G[i];
                }
            }
        }

        double r1, r2;
        if (nr_free1 > 0) {
            r1 = sum_free1 / nr_free1;
        } else {
            r1 = (ub1 + lb1) / 2;
        }

        if (nr_free2 > 0) {
            r2 = sum_free2 / nr_free2;
        } else {
            r2 = (ub2 + lb2) / 2;
        }

        si.r = (r1 + r2) / 2;
        return (r1 - r2) / 2;
    }
}
