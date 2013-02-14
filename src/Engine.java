package neurax;

import java.io.Serializable;

/**
 * Engine, the heart of everything. Contain neural networs, support learning and
 * storing
 *
 * @author ancrax
 */
public class Engine implements Serializable {

    /**
     * netork layers
     */
    private Neuron[] hiddenRecurrentLayer, hiddenLayer2, outputLayer;
    /**
     * for one Engine instance is board size fixed parameter, because of neurons
     * structure - it is fixed
     */
    private final int FIXED_BOARD_SIZE;
    private final int FIXED_BOARD_SIZE_SQUARE;
    private final double OUT_OF_BOUNDARY_RATIO = NetworkOperations.epsilon;
    private double[] recurrentLayerResults;
    private double[] recurrentLayerTransmissions;
    private int sharedSynapticId;
    private double[][][] inputParams;
    private int recurrentLayerResultsIndex;
    private int recurrentLayerTransmissionsIndex;
    private int learnedSamplesCount;

    /**
     *
     * @param boardSize final boardSize cant change during one instance!
     */
    public Engine(int boardSize) {
        FIXED_BOARD_SIZE = boardSize;
        FIXED_BOARD_SIZE_SQUARE = boardSize * boardSize;
    }

    /**
     * init weights of every neuron in all layers by small values
     */
    public void initNetwors() {
        //init learning counter
        learnedSamplesCount = 0;

        //first layer is not represented by array
        //it is represented ony as input values array


        hiddenRecurrentLayer = new Neuron[FIXED_BOARD_SIZE_SQUARE];
        hiddenLayer2 = new Neuron[FIXED_BOARD_SIZE_SQUARE];
        outputLayer = new Neuron[FIXED_BOARD_SIZE_SQUARE];

        //init recurrent layer
        for (int i = 0; i < hiddenRecurrentLayer.length; i++) {
            hiddenRecurrentLayer[i] = new Neuron(4, false);
        }

        //init hidden layer
        for (int i = 0; i < hiddenLayer2.length; i++) {
            hiddenLayer2[i] = new Neuron(4 * FIXED_BOARD_SIZE_SQUARE, false);
        }

        //init output layer
        for (int i = 0; i < outputLayer.length; i++) {
            outputLayer[i] = new Neuron(FIXED_BOARD_SIZE_SQUARE, false);
        }

        //arrays for save pars in recurrent layer
        //because of sharing neuron for all dimensions
        recurrentLayerResults = new double[FIXED_BOARD_SIZE_SQUARE * 4];
        recurrentLayerTransmissions =
                new double[hiddenRecurrentLayer[0].getWeightsCount()
                * recurrentLayerResults.length * 4];
    }

    /**
     * excitate neurons and call NetworkOperations members (they set error and
     * update weights)
     *
     * @param sample samples to larn
     */
    public void learnSample(Sample sample) {
        learnedSamplesCount++;

        //make decision - excitate all neurons
        Move networkDecision = this.generateMove(
                sample.getChosenMove().getColor(),
                new BoardState(sample.getCurrentState()));

        //@todo pass

        //get one dimensional index of desired output
        int expectedOutputIndex =
                sample.getChosenMove().getPositionX() * FIXED_BOARD_SIZE
                + sample.getChosenMove().getPositionY();



        //update output layer
        NetworkOperations.updateOutputLayer(outputLayer, expectedOutputIndex);
        //update second hidden layer
        NetworkOperations.updateLastHiddenLayer(
                hiddenLayer2, outputLayer, expectedOutputIndex);

        //update first hidden - recurrent - layer
        NetworkOperations.updateRecurrentLayer(
                hiddenRecurrentLayer, hiddenLayer2, expectedOutputIndex,
                recurrentLayerResults, recurrentLayerTransmissions);
    }

    /**
     * generate move by excitate all neurons in all layers and return position
     * of valid winner (only valid moves are allowed)
     *
     * @param color to generate
     * @param controller instance of BoardState
     * @return Move, maybe optimal move :)
     */
    public Move generateMove(int color, BoardState controller) {

        int[][] boardState = controller.getBoardState();
        inputParams = new double[boardState.length][boardState.length][2];

        //for all board
        for (int i = 0; i < boardState.length; i++) {
            for (int j = 0; j < boardState[0].length; j++) {
                /*
                 * fill input 1 if stone is own color 0 otherwise
                 *
                 * 1 if stone is opponents color 0 otherwise
                 */
                if (boardState[i][j] == color) {
                    inputParams[i][j][0] = 1 - NetworkOperations.epsilon;
                }
                else {
                    inputParams[i][j][0] = NetworkOperations.epsilon;
                }
                if (boardState[i][j] == Helpers.getInvertedColor(color)) {
                    inputParams[i][j][1] = 1 - NetworkOperations.epsilon;
                }
                else {
                    inputParams[i][j][1] = NetworkOperations.epsilon;
                }
            }
        }

        recurrentLayerResultsIndex = 0;
        recurrentLayerTransmissionsIndex = 0;

        //go over all diagonal directions and fill reurrent neurons inputs
        //from top left
        for (int n = 0; n < hiddenRecurrentLayer.length; n++) {
            decideInput(n, -1, -1, 0, 0);
            assignStdBoardInput(n,
                    inputParams[n / FIXED_BOARD_SIZE][n % FIXED_BOARD_SIZE]);
        }
        //from lower right
        for (int n = 0; n < hiddenRecurrentLayer.length; n++) {
            decideInput(n, +1, +FIXED_BOARD_SIZE, FIXED_BOARD_SIZE_SQUARE - 1,
                    FIXED_BOARD_SIZE_SQUARE - 1);

            //sorry of not warping but netbeans cant warp between array dimensions
            assignStdBoardInput(n,
                    inputParams[FIXED_BOARD_SIZE - 1 - (n / FIXED_BOARD_SIZE)][FIXED_BOARD_SIZE - 1 - (n % FIXED_BOARD_SIZE)]);
        }

        //from top right
        for (int n = 0; n < hiddenRecurrentLayer.length; n++) {
            decideInput(n, +1, -FIXED_BOARD_SIZE, FIXED_BOARD_SIZE_SQUARE - 1, 0);
            assignStdBoardInput(n,
                    inputParams[FIXED_BOARD_SIZE - 1 - (n / FIXED_BOARD_SIZE)][n % FIXED_BOARD_SIZE]);
        }

        //from lower left
        for (int n = 0; n < hiddenRecurrentLayer.length; n++) {
            decideInput(n, -1, +FIXED_BOARD_SIZE, 0, FIXED_BOARD_SIZE_SQUARE - 1);
            assignStdBoardInput(n, inputParams[n / FIXED_BOARD_SIZE][FIXED_BOARD_SIZE - 1 - (n % FIXED_BOARD_SIZE)]);
        }


        //fill hidden layers neurons inputs (by the recurrent neurons output)
        for (int i = 0; i < hiddenLayer2.length; i++) {
            for (int k = 0; k < recurrentLayerResults.length; k++) {
                hiddenLayer2[i].setSynapticTransmision(k, recurrentLayerResults[k]);
            }
        }

        //feed last - outup - layer
        for (int i = 0; i < outputLayer.length; i++) {
            for (int k = 0; k < outputLayer[0].getWeightsCount(); k++) {
                outputLayer[i].setSynapticTransmision(
                        k, hiddenLayer2[k].getPotential());
            }
        }

        //get winner (neuron with max potential = max val of output function)
        int maxIndex = -1;
        double tmpMax = -100000000;
        Move choosenMove = null;
        for (int i = 0; i < outputLayer.length; i++) {

            if (GameInterface.DEBUG) {
                //if DEBUG is on, print potentials of neurons
                System.out.print(outputLayer[i].getPotential() + " ");
                if ((i + 1) % FIXED_BOARD_SIZE == 0) {
                    System.out.println(" ");
                }
            }

            //choose winner
            if (outputLayer[i].getPotential() >= tmpMax
                    && boardState[i / FIXED_BOARD_SIZE][i % FIXED_BOARD_SIZE]
                    == GameInterface.FREE_NODE_ID) {

                Move tmpMove = new Move(
                        i / FIXED_BOARD_SIZE, i % FIXED_BOARD_SIZE, color);
                
                if (controller.isMoveValid(tmpMove)) {
                    maxIndex = i;
                    tmpMax = outputLayer[i].getPotential();
                    choosenMove = tmpMove;
                }
            }
        }

        if (maxIndex != -1) {
            //was some choosen
            return choosenMove;
        }
        else {
            return new Move(true, color);
        }
    }

    /**
     *
     * @return int
     */
    public int getBoardSize() {
        return FIXED_BOARD_SIZE;
    }

    /**
     * assing input to recurrent neurons (first hidden layer) and save this
     * transmission to clas variable
     *
     * @param neuronIndex
     * @param inputs
     */
    private void assignStdBoardInput(int neuronIndex, double[] inputs) {
        //for each recurrent neuron is two inputs directly from board
        hiddenRecurrentLayer[neuronIndex].setSynapticTransmision(
                sharedSynapticId++, inputs[0]);
        hiddenRecurrentLayer[neuronIndex].setSynapticTransmision(
                sharedSynapticId++, inputs[1]);

        //save to classes variable
        recurrentLayerTransmissions[recurrentLayerTransmissionsIndex++] = inputs[0];
        recurrentLayerTransmissions[recurrentLayerTransmissionsIndex++] = inputs[1];

    }

    /**
     * assign output of first neuron to input of second neuron
     *
     * @param to withc neuron
     * @param from whitch neuron
     */
    public void assignRecurrentInput(int to, int from) {
        //insert recurrent inputs
        hiddenRecurrentLayer[to].setSynapticTransmision(
                sharedSynapticId++, hiddenRecurrentLayer[from].getPotential());

        //save input
        recurrentLayerTransmissions[recurrentLayerTransmissionsIndex++] =
                hiddenRecurrentLayer[from].getPotential();
    }

    /**
     *
     * @return int
     */
    public int getLearnedSamplesCount() {
        return learnedSamplesCount;
    }

    /**
     * decide what input of recurrent neuron are out of board and assign
     * incident values
     *
     * @param neuronIndex neuron to assign
     * @param diff1 first differece
     * @param diff2 second difference they decide witch diagonal is it
     * @param limit1 limit for first difference
     * @param limit2 limit for second difference
     */
    private void decideInput(
            int neuronIndex, int diff1, int diff2, int limit1, int limit2) {

        //init array index
        sharedSynapticId = 0;

        //try all variants for this parir diff and limit
        if (limit1 == 0) {
            if (neuronIndex + diff1 >= limit1) {
                //std asing
                assignRecurrentInput(neuronIndex, neuronIndex + diff1);
            }
            else {
                //out out index
                hiddenRecurrentLayer[neuronIndex].setSynapticTransmision(
                        sharedSynapticId++, OUT_OF_BOUNDARY_RATIO);
            }
        }
        else {
            if (neuronIndex + diff1 < limit1) {
                //std asing
                assignRecurrentInput(neuronIndex, neuronIndex + diff1);
            }
            else {
                //out out index
                hiddenRecurrentLayer[neuronIndex].setSynapticTransmision(
                        sharedSynapticId++, OUT_OF_BOUNDARY_RATIO);
            }
        }

        //try all variants for this parir diff and limit
        if (limit2 == 0) {
            if (neuronIndex + diff2 >= limit2) {
                //std asing
                assignRecurrentInput(neuronIndex, neuronIndex + diff2);
            }
            else {
                //out out index
                hiddenRecurrentLayer[neuronIndex].setSynapticTransmision(
                        sharedSynapticId++, OUT_OF_BOUNDARY_RATIO);
            }
        }
        else {
            if (neuronIndex + diff2 < limit2) {
                //std asing
                assignRecurrentInput(neuronIndex, neuronIndex + diff2);
            }
            else {
                //out out index
                hiddenRecurrentLayer[neuronIndex].setSynapticTransmision(
                        sharedSynapticId++, OUT_OF_BOUNDARY_RATIO);
            }
        }

        //save result
        recurrentLayerResults[recurrentLayerResultsIndex++] =
                hiddenRecurrentLayer[neuronIndex].getPotential();
    }
}
