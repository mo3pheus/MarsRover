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
    private double prevLogProb               = -100000.0d;

    private double[]   initialStateDist = null;
    private double[][] observationProb  = null;
    private double[][] stateTransitions = null;

    private double[][] alpha       = null;
    private double[][] beta        = null;
    private double[][] gammaT      = null;
    private double[]   gammat      = null;
    private double[]   scaleFactor = null;

    /**
     * @param numObservations
     * @param numStates
     * @param lengthObservationSequence
     * @param maxIterations
     */
    public Oracle(int numObservations, int numStates, int lengthObservationSequence, int maxIterations) {
        this.lengthObservationSequence = lengthObservationSequence;
        this.numObservations = numObservations;
        this.numStates = numStates;
        this.maxIterations = maxIterations;

        initialStateDist = new double[numStates];
        observationProb = new double[numStates][numObservations];
        stateTransitions = new double[numStates][numStates];

        alpha = new double[lengthObservationSequence][numStates];
        beta = new double[lengthObservationSequence][numStates];
        gammaT = new double[numStates][numStates];
        gammat = new double[numStates];
        scaleFactor = new double[lengthObservationSequence];

        // initialize matrices
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

    private void trainHMM() {
        /* 1. alpha pass */
        /* 2. beta pass */
        /* 3. gammaT[i][j] and gammaT[i] */
        /* 4. reEstimate A, B and PI */
        /* 5. compute log(P[O|lambda]) */
        /* 6. evaluate termination criteria */
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
        double   c0     = 0.0d;
        double[] alpha0 = new double[numStates];

        /* compute alpha0(i) */
        for (int i = 0; i < numStates; i++) {
            alpha0[i] = initialStateDist[i] * observationProb[i][0];
            c0 += alpha0[i];
        }

        /* scale the alpha0[i] */
        c0 = 1.0d / c0;
        for (int i = 0; i < numStates; i++) {
            alpha0[i] *= c0;
        }
    }
}
