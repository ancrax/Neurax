package neurax;

import java.util.Arrays;

/**
 * Abstract of one tain sample. Contain before-state, current-state and
 * future-state of board
 *
 * @author ancrax
 */
public class Sample {

    private final int[][] currentState;
    private final int[][] futureState;
    private final Move chosenAction;

    /**
     *
     * @param currentStateIn
     * @param futureStateIn
     * @param chosenActionIn 
     */
    public Sample(
            int[][] currentStateIn, int[][] futureStateIn, Move chosenActionIn) {

        this.currentState = new int[currentStateIn.length][currentStateIn[0].length];
        this.futureState = new int[futureStateIn.length][futureStateIn[0].length];

        this.chosenAction = chosenActionIn;

        //need to do copy not reference assign
        for (int i = 0; i < currentStateIn.length; i++) {
            this.currentState[i] = Arrays.copyOf(currentStateIn[i], currentStateIn[i].length);
        }

        for (int i = 0; i < futureStateIn.length; i++) {
            this.futureState[i] = Arrays.copyOf(futureStateIn[i], futureStateIn[i].length);
        }
    }

    /**
     * 
     * @return int [][] array of boardstate
     */
    public int[][] getCurrentState() {
        return currentState;
    }

    /**
     * 
     * @return int [][] array of boardstate
     */
    public int[][] getFutureState() {
        return futureState;
    }

    /**
     * 
     * @return Move chosen move
     */
    public Move getChosenMove() {
        return chosenAction;
    }

    /**
     * experiment method
     * @return String
     */
    @Override
    public String toString() {
        StringBuilder toPrint = new StringBuilder("current\n");
        for (int[] row : currentState) {
            for (int cl : row) {
                toPrint.append(cl);
            }
            toPrint.append('\n');
        }

        toPrint.append("t + 1\n");
        for (int[] row : futureState) {
            for (int cl : row) {
                toPrint.append(cl);
            }
            toPrint.append('\n');
        }

        return toPrint.toString();
    }
}
