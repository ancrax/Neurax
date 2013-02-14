package neurax;

import java.util.HashSet;
import java.util.Set;

/**
 * This class make operations over playing board, store Engine instance
 *
 * @author ancrax
 */
public class GameController extends BoardState implements Cloneable {

    /**
     * instance of engine witcha make desigions and learning
     */
    private Engine engine;
    /**
     * store komi, komi is include in final result of white player
     */
    private double komi;

    /**
     * create implicit board state
     */
    public GameController() {
        super(new int[9][9]);   //create tmp board

        //create engine instance
        engine = new Engine(boardSize);
        engine.initNetwors();
        boardState = new int[boardSize][boardSize];
    }

    /**
     * create new instance with desired Engine instance
     *
     * @param savedEngine Engine instance (can be from saved config file)
     */
    public GameController(Engine savedEngine) {
        super(new int[9][9]);   //create tmp board

        //get loaded engine
        engine = savedEngine;
        boardSize = engine.getBoardSize();
        boardState = new int[boardSize][boardSize];
    }

    /**
     * set komi
     *
     * @param komi small number - mostly under 5
     */
    public void setKomi(double komi) {
        this.komi = komi;
    }

    /**
     * this calls engine to make desigion if engine cant this return pass as
     * move
     *
     * @param color to generate move
     * @return String parsed Move String
     */
    public String actionGenerateMove(int color) {
        Move generatedMove = engine.generateMove(color, this);
        try {
            this.actionSetStone(generatedMove);
        }
        catch (Exception ex) { //if coors are already filled
            if (GameInterface.DEBUG) {
                System.err.println(ex);
            }
            return "pass";
        }

        //parse from Move object
        if (generatedMove.isPassed()) {
            return "pass";
        }
        else {
            return Helpers.getVertexFromCoord(generatedMove.getPositionX(),
                    generatedMove.getPositionY());
        }
    }

    /**
     * return final score array int [0] = empty pos.[1] = player 1 score, [2] =
     * player 2 score. Figure only explicit score
     *
     * @return int [] array
     */
    public int[] getScores() {
        int[] scores = new int[3];

        //store allready checked stones
        Set<Coord> checked = new HashSet<Coord>();
        Group tmpGroup;

        //for all board
        for (int x = 0; x < boardSize; x++) {
            for (int y = 0; y < boardSize; y++) {
                if (boardState[x][y] == GameInterface.FREE_NODE_ID && !checked.
                        contains(new Coord(x, y, boardSize))) {
                    //create group
                    tmpGroup = this.getStoneGroup(new Coord(x, y, boardSize));

                    for (Coord crd : tmpGroup.getMembers()) {
                        checked.add(crd);
                    }
                    scores[tmpGroup.getColor()] += tmpGroup.getMembers().
                            size();
                }
            }
        }

        scores[GameInterface.PLAYER_BLACK_ID] += stonesCaptured[GameInterface.PLAYER_BLACK_ID];
        scores[GameInterface.PLAYER_WHITE_ID] += stonesCaptured[GameInterface.PLAYER_WHITE_ID] + komi;

        return scores;
    }

    /**
     * accepts parsed sample - training set - and delegate it to engine
     *
     * @param samples
     */
    public void learnSamples(Sample[] samples) {
        int counter = 0;
        for (Sample s : samples) {
            if (s != null) {
                engine.learnSample(s);

                if (counter == 5000) {
                    //agter each n intration update learning rate
                    counter = 0;
                    NetworkOperations.updateLearningRate();
                }
                counter++;
            }
        }
    }

    /**
     * experimental function
     *
     * @param color
     * @return String with stones of desired color positions
     */
    public String listStones(int color) {
        StringBuilder stones = new StringBuilder();

        //for all game board
        for (int x = 0; x < boardSize; x++) {
            for (int y = 0; y < boardSize; y++) {
                if (boardState[x][y] == color) {
                    //append stone
                    stones.append(Helpers.getColumnByIndex(y) + "" + x + " ");
                }
            }
        }
        return stones.toString();
    }

    /**
     *
     * @return Engine instance
     */
    public Engine getEngine() {
        return engine;
    }

    /**
     * set new board size and create new Engine instance (with new neural
     * netowrks)
     *
     * @param size
     */
    @Override
    public void setBoardSize(int size) {
        if (size != boardSize) {
            boardSize = size;
            boardState = new int[boardSize][boardSize];
            engine = new Engine(boardSize);
            engine.initNetwors();
        }
    }

    /**
     * clonning - used in storin to file
     * @return Object
     */
    @Override
    public Object clone() {
        return super.clone();
    }
}
