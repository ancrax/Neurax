package neurax;

/**
 * Class to separate learing of layers from engine
 *
 * @author ancrax
 */
public class NetworkOperations {

    /**
     * constant to make inputs between ]0;1[
     */
    protected static double epsilon = 0.0000001;
    /**
     * set learnig rate (smaller make good results, known by testing)
     */
    protected static double learningRate = 0.2;

    /**
     * update weights of all neurons in output layer
     *
     * @param outputLayer array of neurons
     * @param expectedOutputIndex index of choosen action in sample
     */
    public static void updateOutputLayer(Neuron[] outputLayer, int expectedOutputIndex) {
        int j = 0;

        //for each neuron in output layer
        for (Neuron outputNeuron : outputLayer) {
            //for each neuron connection
            for (int i = 0; i < outputNeuron.getWeightsCount(); i++) {

                //count derivation by the backpropagation formula
                double derivation = -outputNeuron.getPotential()
                        * (1 - outputNeuron.getPotential())
                        * outputNeuron.getSynapticTransmision(i);

                //if this neuron is on position of desired output
                if (j == expectedOutputIndex) {
                    // 1 as desired output
                    derivation *= (1 - epsilon - outputNeuron.getPotential());
                }
                else {
                    // 0 as inhibit neuron
                    derivation *= (0 + epsilon - outputNeuron.getPotential());
                }

                //updating weights
                double delta = -learningRate * derivation;
                outputNeuron.updateWeight(i, delta);
                outputNeuron.setDelta(i, delta);
                outputNeuron.updateBias(delta);
            }
            j++;
        }
    }

    /**
     * update weights of all neurons in last hidden layer will work with any
     * other hidden layer - but not recurrent
     *
     * @param hiddenLayerToUpdate
     * @param upperLayer
     * @param expectedOutputIndex
     */
    public static void updateLastHiddenLayer(
            Neuron[] hiddenLayerToUpdate,
            Neuron[] upperLayer,
            int expectedOutputIndex) {

        int upperNeuronId = 0;
        //for each neuron in hidden layer
        for (Neuron hiddenNeuron : hiddenLayerToUpdate) {
            //for each neuron connection
            for (int i = 0; i < hiddenNeuron.getWeightsCount(); i++) {
                double sumOutputLayer = 0;

                //cout sum of outputs on upper layer (in this case output layer)
                for (Neuron outputNeuron : upperLayer) {
                    double expectedOutput;

                    //if this neuron is on position of desired output
                    if (upperNeuronId == expectedOutputIndex) {
                        // 1 as desired output
                        expectedOutput = 1 - epsilon;
                    }
                    else {
                        // 0 as inhibit neuron
                        expectedOutput = 0 + epsilon;
                    }

                    //count sum by the backpropagation formula
                    sumOutputLayer +=
                            -(expectedOutput - outputNeuron.getPotential())
                            * outputNeuron.getPotential()
                            * (1 - outputNeuron.getPotential())
                            * outputNeuron.getWegiht(upperNeuronId);


                }

                //count derivation by the backpropagation formula
                double derivation = hiddenNeuron.getPotential()
                        * (1 - hiddenNeuron.getPotential())
                        * hiddenNeuron.getSynapticTransmision(i)
                        * sumOutputLayer;
                
                //updating weights
                double delta = -learningRate * derivation;
                hiddenNeuron.updateWeight(i, delta);
                hiddenNeuron.setDelta(i, delta);
                hiddenNeuron.updateBias(delta);
            }

            upperNeuronId++;
        }
    }

    /**
     * update weights of all neurons in recurrent layer in this particular case
     * it is first hidden layer
     *
     * @param hiddenRecurrentLayer array of Neuron instancies
     * @param upperLayer array of Neuron instances of upeer layer (in this case
     * it is second hidden layer)
     * @param expectedOutputIndex
     * @param recurrentLayerResults ressults from all directions during
     * excitacions
     * @param recurrentLayerTransmissions
     */
    public static void updateRecurrentLayer(
            Neuron[] hiddenRecurrentLayer,
            Neuron[] upperLayer,
            int expectedOutputIndex,
            double[] recurrentLayerResults,
            double[] recurrentLayerTransmissions) {

        //fun start here
        //save partial deltas to do average on bottom
        double partialDeltas[] = new double[recurrentLayerResults.length];

        int i = 0;
        //for each neuron in recurrent layer
        for (double neuronOutput : recurrentLayerResults) {
            double sumOutputLayer = 0;

            //for each upper neuron (neuron whitch takes outupt of this neuron)
            for (Neuron upperNeuron : upperLayer) {
                double expectedOutput;

                //if this neuron is on position of desired output
                if (i == expectedOutputIndex) {
                    // 1 as desired output
                    expectedOutput = 1 - epsilon;
                }
                else {
                    // 0 as inhibit neuron
                    expectedOutput = 0 + epsilon;
                }
                sumOutputLayer += -(expectedOutput - upperNeuron.getPotential()) * upperNeuron.
                        getPotential() * (1 - epsilon - upperNeuron.getPotential()) * upperNeuron.
                        getWegiht(i);

            }

            //count derivation by the backpropagation formula
            double derivation = neuronOutput * (1 - neuronOutput) 
                    * recurrentLayerTransmissions[i] * sumOutputLayer;
            double delta = -learningRate * derivation;
            
            //assign current delta
            partialDeltas[i] = delta;

            i++;
        }

        //updating weights
        for (int j = 0; j < hiddenRecurrentLayer.length; j++) {
            double deltaAverage =
                    partialDeltas[j]
                    + partialDeltas[j + hiddenRecurrentLayer.length]
                    + partialDeltas[j + 2 * hiddenRecurrentLayer.length]
                    + partialDeltas[j + 3 * hiddenRecurrentLayer.length];

            deltaAverage /= 4; //make averages from deltas
            for (int k = 0; k < hiddenRecurrentLayer[j].getWeightsCount(); k++) {
                hiddenRecurrentLayer[j].updateWeight(k, deltaAverage);
                hiddenRecurrentLayer[j].setDelta(k, deltaAverage);
            }
            hiddenRecurrentLayer[j].updateBias(deltaAverage);
        }

    }

    /**
     * decrease learning rate (can be called after one training set)
     */
    public static void updateLearningRate() {
        learningRate *= 0.95;
    }
}
