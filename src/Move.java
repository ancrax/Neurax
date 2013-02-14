package neurax;

/**
 * Class for move. Move contain x and y position of move and color of move OR
 * pass
 *
 * @author ancrax
 */
public class Move {

    private int positionX;
    private int positionY;
    private int color;
    private boolean isPassed = false;

    /**
     * Constructor for non passed move
     *
     * @param postionX
     * @param positionY
     * @param color
     */
    public Move(int postionX, int positionY, int color) {
        this.initParams(postionX, positionY, color);
    }

    /**
     * Constructor for passed move
     *
     * @param isPassed
     * @param player
     */
    public Move(boolean isPassed, int player) {
        this.isPassed = isPassed;
        this.color = player;
    }

    /**
     *
     * @return boolean
     */
    public boolean isPassed() {
        return isPassed;
    }

    /**
     *
     * @return int
     */
    public int getPositionX() {
        return positionX;
    }

    /**
     *
     * @return int
     */
    public int getPositionY() {
        return positionY;
    }

    /**
     *
     * @return int
     */
    public int getColor() {
        return color;
    }

    /**
     * because of two different constructors
     *
     * @param positionX
     * @param positionY
     * @param color
     */
    private void initParams(int positionX, int positionY, int color) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.color = color;
    }
}
