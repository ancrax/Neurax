package neurax;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Abstract of mathematicaly specified neuron
 *
 * @author ancrax
 */
public class Neuron implements Serializable {

    private double[] weights;
    private double[] inputValues;
    private double[] deltas;
    private double output;
    private final double dynamic = 0.2;

    /**
     *
     * @param countOfInputs
     * @param isFirstLayer flag if is in first layer
     */
    public Neuron(int countOfInputs, boolean isFirstLayer) {
        inputValues = new double[countOfInputs];
        deltas = new double[countOfInputs];

        if (isFirstLayer) {
            weights = new double[countOfInputs];
            Arrays.fill(weights, 1);
        }
        else {
            weights = new double[countOfInputs + 1]; //inputs plus bias
            this.initRandomWeights();
        }
    }

    /**
     * get neuron output (excitacion). It is counted by sum of potencials (input
     * * weight) and by get val of activation function from the sum
     *
     * @return number, dependence of activation funcion in this particular case
     * it is between 0 and 1
     */
    public double getPotential() {
        double sumOfPotential = 0;

        for (int i = 0; i < inputValues.length; i++) {
            sumOfPotential += inputValues[i] * weights[i];
        }

        //if is not neuron form first layer
        if (inputValues.length != weights.length) {
            //add bias
            sumOfPotential += weights[weights.length - 1];
        }

        //get value of activation function
        output = this.getValueOfActivationFuncion(sumOfPotential);
        return output;
    }

    /**
     * assign synaptic transmission
     *
     * @param synaptionId synaption index
     * @param signal strength of signal
     */
    public void setSynapticTransmision(int synaptionId, double signal) {
        inputValues[synaptionId] = signal;
    }

    /**
     * get before transmissed signal
     *
     * @param synaptionId
     * @return double
     */
    public double getSynapticTransmision(int synaptionId) {
        return inputValues[synaptionId];
    }

    /**
     * neuron activation function
     *
     * @param value
     * @return double between 0 and 1
     */
    private double getValueOfActivationFuncion(double value) {
        //in this particular case it is:
        //hyperbolic tangens
        return (1 - Math.exp(-value)) / (1 + Math.exp(-value));
    }

    /**
     * start init of small random weights
     */
    private void initRandomWeights() {
        for (int i = 0; i < weights.length; i++) {
            weights[i] = Math.random() / 64;
        }
    }

    /**
     * get number of neuron inputs
     *
     * @return int
     */
    public int getWeightsCount() {
        return inputValues.length;
    }

    /**
     * update neuron weight, need input from NetworkOperations OR
     * backpropagation forumla
     *
     * @param synaptionId synaption index
     * @param delta param by BP formula
     */
    public void updateWeight(int synaptionId, double delta) {
        weights[synaptionId] =
                weights[synaptionId] + delta + dynamic * deltas[synaptionId];
    }

    /**
     * update only bias by desired correction
     *
     * @param correction
     */
    public void updateBias(double correction) {
        weights[weights.length - 1] += correction; //last weight is bias
    }

    /**
     * set delta for next update weights iteration
     *
     * @param synaptionId
     * @param delta by BP fromula
     */
    public void setDelta(int synaptionId, double delta) {
        deltas[synaptionId] = delta;
    }

    /**
     * return particular weight
     *
     * @param synaptionId synaption index
     * @return double desired weight
     */
    double getWegiht(int synaptionId) {
        return weights[synaptionId];
    }
}
