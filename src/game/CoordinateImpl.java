package game;

/**
 * @author s0568823 - Leon Enzenberger
 */
public class CoordinateImpl implements Coordinate {
    private final int xCoordinate;
    private final int yCoordinate;

    CoordinateImpl(int x, int y){
        this.xCoordinate=x;
        this.yCoordinate=y;
    }

    @Override
    public int getXCoordinate() {
        return this.xCoordinate;
    }

    @Override
    public int getYCoordinate() {
        return this.yCoordinate;
    }

    @Override
    public void setCoordinateStatus(int x, char y) {

    }
}
