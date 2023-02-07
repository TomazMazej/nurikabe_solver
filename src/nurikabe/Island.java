package nurikabe;

import java.util.ArrayList;

public class Island {

    int x;
    int y;
    int size;
    ArrayList<IslandCoordinate> coordinates;

    public Island(int x, int y, int size) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.coordinates = new ArrayList<IslandCoordinate>();
        coordinates.add(new IslandCoordinate(x, y));
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public ArrayList<IslandCoordinate> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(ArrayList<IslandCoordinate> coordinates) {
        this.coordinates = coordinates;
    }
}
