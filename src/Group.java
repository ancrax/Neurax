package neurax;

import java.util.HashSet;
import java.util.Set;

/**
 * Class to abstract several positions on game board. Contains stones of same
 * color, can say if is alive or surrounded
 *
 * @author ancrax
 */
public class Group {

    private Set<Coord> members = new HashSet<Coord>();
    private boolean live;
    private int color;

    /**
     * 
     * @param coords
     * @param isAlive
     * @param color 
     */
    public Group(Set<Coord> coords, boolean isAlive, int color) {
        members = coords;
        live = isAlive;
        this.color = color;
    }

    /**
     * 
     * @return Set of Coord instances
     */
    public Set<Coord> getMembers() {
        return members;
    }

    /**
     * 
     * @return boolean
     */
    public boolean isAlive() {
        return live;
    }

    /**
     * 
     * @return boolean
     */
    public boolean isSurrounded() {
        return !live;
    }

    /**
     * 
     * @return int
     */
    public int getColor() {
        return color;
    }
}
