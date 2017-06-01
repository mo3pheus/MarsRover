package space.exploration.mars.rover.learning.hmm;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by sanketkorgaonkar on 5/31/17.
 */
public class Oracle {
    private int    numObservations           = 0;
    private int    numStates                 = 0;
    private int    lengthObservationSequence = 0;
    private int    maxIterations             = 0;
    private double numerator                 = 0.0d;
    private double denominator               = 0.0d;
    private double prevLogProb               = -100000.0d;

    private double[]   initialStateDist = null;
    private double[][] observationProb  = null;
    private double[][] stateTransitions = null;

    private double[][]   alpha        = null;
    private double[][]   beta         = null;
    private double[][][] gammaT       = null;
    private double[][]   gammat       = null;
    private double[]     scaleFactor  = null;
    private int[]        observations = null;

    /**
     * @param numObservations
     * @param numStates
     * @param maxIterations
     * @param observations
     */
    public Oracle(int numObservations, int numStates, int maxIterations, int[] observations) {
        this.lengthObservationSequence = observations.length;
        this.numObservations = numObservations;
        this.numStates = numStates;
        this.maxIterations = maxIterations;

        initialStateDist = new double[numStates];
        observationProb = new double[numStates][numObservations];
        this.observations = observations;
        stateTransitions = new double[numStates][numStates];

        alpha = new double[lengthObservationSequence][numStates];
        beta = new double[lengthObservationSequence][numStates];
        gammaT = new double[lengthObservationSequence][numStates][numStates];
        gammat = new double[lengthObservationSequence][numStates];
        scaleFactor = new double[lengthObservationSequence];

        // initialize matrices
        fillArrays();
        seedInitialStateDist();
        initializeObservationMatrix();
        initializeStateTransitions();

        System.out.println("Printing the stateTransition matrix:: A :: ");
        printMatrix(stateTransitions, numStates, numStates);

        System.out.println("Printing the observationProb matrix:: B :: ");
        printMatrix(observationProb, numStates, numObservations);

        System.out.println("Printing the initialState matrix PI :: ");
        System.out.println(Arrays.toString(initialStateDist));

        trainHMM();
    }

    public double[] getInitialStateDist() {
        return initialStateDist;
    }

    public double[][] getObservationProb() {
        return observationProb;
    }

    public double[][] getStateTransitions() {
        return stateTransitions;
    }

    public void setObservations(int[] observations) {
        if (observations.length == lengthObservationSequence) {
            this.observations = observations;
        }
    }

    private void fillArrays() {
        Arrays.fill(this.scaleFactor, 0.0d);
        Arrays.fill(this.gammat, 0.0d);
        Arrays.fill(this.initialStateDist, 0.0d);
        Arrays.fill(this.alpha, 0.0d);
        Arrays.fill(this.gammaT, 0.0d);
        Arrays.fill(this.beta, 0.0d);
    }

    private void trainHMM() {
        double currentProb = 0.0d;
        int    i           = 0;
        while ((currentProb > prevLogProb) && (i < maxIterations)) {
            prevLogProb = currentProb;
            alphaPass();
            betaPass();
            updateGammas();
            reEstimateABPI();
            currentProb = computeLogProb();
            i++;
        }
    }

    private void betaPass() {
        for (int i = 0; i < numStates; i++) {
            this.beta[lengthObservationSequence - 1][i] = scaleFactor[lengthObservationSequence - 1];
        }

        for (int t = lengthObservationSequence - 2; t < 0; t--) {
            for (int i = 0; i < numStates; i++) {
                for (int j = 0; j < numStates; j++) {
                    beta[t][j] += stateTransitions[i][j] * observationProb[j][t + 1] * beta[t + 1][j];
                }
                beta[t][i] *= scaleFactor[t];
            }
        }
    }

    private void updateGammas() {
        for (int t = 0; t < (lengthObservationSequence - 1); t++) {
            for (int i = 0; i < numStates; i++) {
                for (int j = 0; j < numStates; j++) {
                    denominator += (alpha[t][i] * stateTransitions[i][j] * observationProb[j][t + 1] * beta[t + 1][j]);
                }
            }
            for (int i = 0; i < numStates; i++) {
                for (int j = 0; j < numStates; j++) {
                    gammaT[t][i][j] = ((alpha[t][i] * stateTransitions[i][j] * observationProb[j][t + 1] * beta[t +
                            1][j]) / denominator);
                    gammat[t][i] += gammaT[t][i][j];
                }
            }
        }

        denominator = 0.0d;
        for (int i = 0; i < numStates; i++) {
            denominator += alpha[lengthObservationSequence - 1][i];
        }

        for (int i = 0; i < numStates; i++) {
            gammat[lengthObservationSequence - 1][i] = alpha[lengthObservationSequence - 1][i] / denominator;
        }
    }

    private void reEstimateABPI() {
        for (int i = 0; i < numStates; i++) {
            initialStateDist[i] = gammat[0][i];
        }

        for (int i = 0; i < numStates; i++) {
            for (int j = 0; j < numStates; j++) {
                for (int t = 0; t < (lengthObservationSequence - 1); t++) {
                    numerator += gammaT[t][i][j];
                    denominator += gammat[t][i];
                }
                stateTransitions[i][j] = numerator / denominator;
            }
        }

        for (int i = 0; i < numStates; i++) {
            for (int j = 0; j < lengthObservationSequence; j++) {
                numerator = 0.0d;
                denominator = 0.0d;
                for (int t = 0; t < lengthObservationSequence; t++) {
                    if (observations[t] == j) {
                        numerator += gammat[t][i];
                        denominator += gammat[t][i];
                    }
                }
                observationProb[i][j] = numerator / denominator;
            }
        }
    }

    private double computeLogProb() {
        double logProb = 0.0d;
        for (int i = 0; i < lengthObservationSequence; i++) {
            logProb += Math.log(scaleFactor[i]);
        }
        logProb *= -1.0d;
        return logProb;
    }

    private void seedInitialStateDist() {
        double maxPi = 1.0d / (double) numStates;
        double sum   = 0.0d;
        for (int i = 0; i < initialStateDist.length; i++) {
            double temp = ThreadLocalRandom.current().nextDouble(maxPi);

            if (i == (numStates - 1)) {
                initialStateDist[i] = 1.0d - sum;
                continue;
            }
            sum += temp;
            initialStateDist[i] = temp;
        }
    }

    private void initializeStateTransitions() {
        double maxIS = 1.0d / (double) numStates;
        for (int i = 0; i < stateTransitions[0].length; i++) {
            double sum = 0.0d;
            for (int j = 0; j < stateTransitions[0].length; j++) {
                double temp = ThreadLocalRandom.current().nextDouble(maxIS);

                if (j == (numStates - 1)) {
                    stateTransitions[i][j] = 1.0d - sum;
                    continue;
                }
                sum += temp;
                stateTransitions[i][j] = temp;
            }
        }
    }

    private void initializeObservationMatrix() {
        double maxOM = 1.0d / (double) numObservations;
        for (int i = 0; i < numStates; i++) {
            double sum = 0.0d;
            for (int j = 0; j < numObservations; j++) {
                double temp = ThreadLocalRandom.current().nextDouble(maxOM);

                if (j == (numObservations - 1)) {
                    observationProb[i][j] = 1.0d - sum;
                    continue;
                }
                sum += temp;
                observationProb[i][j] = temp;
            }
        }
    }

    private void printMatrix(double[][] matrix, int row, int col) {
        for (int i = 0; i < row; i++) {
            System.out.println(Arrays.toString(matrix[i]));
        }
    }

    private void alphaPass() {
        for (int i = 0; i < numStates; i++) {
            alpha[0][i] = this.initialStateDist[i] * this.observationProb[i][0];
            scaleFactor[0] += alpha[0][i];
        }
        scaleFactor[0] = 1.0d / scaleFactor[0];

        for (int i = 0; i < numStates; i++) {
            alpha[0][i] *= scaleFactor[0];
        }

        for (int t = 1; t < lengthObservationSequence; t++) {
            for (int i = 0; i < numStates; i++) {
                for (int j = 0; j < numStates; j++) {
                    alpha[t][i] += (alpha[t - 1][j] * stateTransitions[j][i]);
                }
                alpha[t][i] *= this.observationProb[i][t];
                scaleFactor[t] += alpha[t][i];
            }
            scaleFactor[t] = 1.0d / scaleFactor[t];
            for (int i = 0; i < numStates; i++) {
                alpha[t][i] *= scaleFactor[t];
            }
        }
    }
}
