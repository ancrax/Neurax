package neurax;

import java.util.HashSet;
import java.util.Set;

/**
 * Abstraction of coords on board
 *
 * @author ancrax
 */
public class Coord {

    private final int x;
    private final int y;
    private final int boardSize;

    /**
     *
     * @param x position
     * @param y position
     * @param boardSize of whole board (one axis)
     */
    Coord(int x, int y, int boardSize) {
        this.x = x;
        this.y = y;
        this.boardSize = boardSize;
    }

    /**
     *
     * @return int x position
     */
    public int getX() {
        return x;
    }

    /**
     *
     * @return int y position
     */
    public int getY() {
        return y;
    }

    /**
     *
     * @return Coord [] neighbourts (not diagonialy)
     */
    public Coord[] getNeighbours() {
        Set<Coord> coords = new HashSet<Coord>();
        if (x + 1 < boardSize) {
            coords.add(new Coord(x + 1, y, boardSize));
        }
        if (x - 1 >= 0) {
            coords.add(new Coord(x - 1, y, boardSize));
        }
        if (y + 1 < boardSize) {
            coords.add(new Coord(x, y + 1, boardSize));
        }
        if (y - 1 >= 0) {
            coords.add(new Coord(x, y - 1, boardSize));
        }
        return coords.toArray(new Coord[coords.size()]);
    }

    /**
     *
     * @return int formated: "yyxx" where yy = y pos. and xx = x post
     */
    @Override
    public int hashCode() {
        return x + y * 100;
    }

    /**
     *
     * @param crd object type Coord
     * @return boolean if point to same location return true otherwise false
     */
    @Override
    public boolean equals(Object crd) {
        Coord testnigCoord = (Coord) crd;
        if (testnigCoord.getX() == this.getX()
                && testnigCoord.getY() == this.getY()) {
            return true;
        }
        return false;
    }
}
