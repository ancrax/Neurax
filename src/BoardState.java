package neurax;

import java.util.*;

/**
 * Abstract of gobain. Contains move history, score (captured stones) and stones
 * postitions
 *
 * @author ancrax
 */
public class BoardState implements Cloneable {

    protected Set<Move> moveHistory = new HashSet<Move>();
    protected int[] stonesCaptured = {0, 0, 0};
    protected int[][] boardState;
    protected int boardSize = 9;
    protected int turnsCount;

    /**
     * @param boardState array of bin values boardState if is free 0 otherwise 1
     */
    public BoardState(int[][] boardState) {
        this.boardSize = boardState.length;
        this.boardState = boardState;
    }

    /**
     * clear board and reset turns count
     */
    public void actionClearBoard() {
        for (int[] row : boardState) {
            Arrays.fill(row, 0);
        }
        Arrays.fill(stonesCaptured, 0);
        turnsCount = 0;
    }

    /**
     * removes all dead stones - from other player in input
     *
     * @param colorToRemove by GameInterface.PLAYER_(WHITE/BLACK)_ID
     */
    public void actionRemoveDeadStones(int colorToRemove) {
        Set<Coord> checked = new HashSet<Coord>();
        Group tmpGroup;
        //for all board
        for (int x = 0; x < boardSize; x++) {
            for (int y = 0; y < boardSize; y++) {
                //pass other color and check if is checked
                if (boardState[x][y] == colorToRemove && !checked.contains(
                        new Coord(x, y, boardSize))) {
                    tmpGroup = this.getStoneGroup(new Coord(x, y, boardSize));

                    //add to checked
                    checked.addAll(tmpGroup.getMembers());
                    if (!tmpGroup.isAlive()) {
                        this.removeStonesGroup(tmpGroup);
                    }
                }
            }
        }
    }

    /**
     * Set stone in goman on desired positon, add this move to move history.
     * Incerase turnsCount
     *
     * @param move
     * @throws Exception
     */
    public void actionSetStone(Move move) throws Exception {
        if (move.isPassed()) {
            //not exception
            //not remove dead stones
        }
        else if (boardState[move.getPositionX()][move.getPositionY()]
                == GameInterface.FREE_NODE_ID) {
            //checked if position at boardstate is free
            
            boardState[move.getPositionX()][move.getPositionY()] = move.getColor();
            //after move, remove dead stones
            this.actionRemoveDeadStones(Helpers.getInvertedColor(move.getColor()));
        }
        else {
            //@todo define own exception(s)
            throw new Exception("coord is allready filled");
        }

        turnsCount++;
        //add move to moves history
        moveHistory.add(move);
    }

    /**
     * check if move is valid
     *
     * @param move to validate
     * @return boolean if is move valid (except kó - @fixme)
     */
    public boolean isMoveValid(Move move) {
        //@todo kó
        if (boardState[move.getPositionX()][move.getPositionY()]
                == GameInterface.FREE_NODE_ID) {

            Group position = this.getStoneGroup(
                    new Coord(move.getPositionX(), move.getPositionY(), this.
                    getBoardSize()));

            if (position.isAlive()) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @return int
     */
    public int getBoardSize() {
        return boardSize;
    }

    /**
     *
     * @return int [][] of board state
     */
    public int[][] getBoardState() {
        return boardState;
    }

    /**
     * remove stone on desired position
     *
     * @param x position
     * @param y position
     */
    public void removeStone(int x, int y) {
        stonesCaptured[Helpers.getInvertedColor(boardState[x][y])]++;
        boardState[x][y] = GameInterface.FREE_NODE_ID;
    }

    /**
     * for each stone in group run this.removeStone
     *
     * @param group
     */
    public void removeStonesGroup(Group group) {
        for (Coord coord : group.getMembers()) {
            removeStone(coord.getX(), coord.getY());
        }
    }

    /**
     * set new goban size
     *
     * @param size
     */
    public void setBoardSize(int size) {
        boardSize = size;
        boardState = new int[boardSize][boardSize];
    }

    /**
     * Return all stones same color and with neighbourhood with START stone
     *
     * @param start from where start creating group
     * @return Group, contains all stones in same group as stone on pos. start
     */
    protected Group getStoneGroup(Coord start) {
        int color = boardState[start.getX()][start.getY()];

        Queue<Coord> edge = new LinkedList<Coord>();
        Set<Coord> group = new HashSet<Coord>();

        edge.add(start);
        group.add(start);

        //boolean isSurrounded = false;
        boolean live = false;

        Coord current;

        current = edge.poll();
        //while is current (some unexplored stone)
        while (current != null) {
            for (Coord neighbour : current.getNeighbours()) {
                if (!edge.contains(neighbour) && !group.contains(neighbour)
                        && boardState[neighbour.getX()][neighbour.getY()]
                        == color) {
                    //add to fringe
                    edge.add(neighbour);
                    group.add(neighbour);
                }

                if (boardState[neighbour.getX()][neighbour.getY()]
                        == GameInterface.FREE_NODE_ID) {
                    live = true;
                }
            }
            current = edge.poll();
        }

        return new Group(group, live, color);
    }

    /**
     *
     * @return int
     */
    public int getTurnsCount() {
        return turnsCount;
    }

    /**
     *
     * @return Object
     */
    @Override
    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
