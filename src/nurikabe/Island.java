package nurikabe;

import java.util.ArrayList;

public class Island {

    int x;
    int y;
    int size;
    ArrayList<IslandCoordinate> coordinates;
    ArrayList<IslandCoordinate> possibilities;

    public Island(int x, int y, int size) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.coordinates = new ArrayList<IslandCoordinate>();
        coordinates.add(new IslandCoordinate(x, y));
        this.possibilities = new ArrayList<IslandCoordinate>();
    }

    public void addCoordinate(IslandCoordinate coordinate){
        coordinates.add(coordinate);
    }

    public void addPossibility(IslandCoordinate possibility){
        possibilities.add(possibility);
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

    public ArrayList<IslandCoordinate> getPossibilities() {
        return possibilities;
    }

    public void setPossibilities(ArrayList<IslandCoordinate> possibilities) {
        this.possibilities = possibilities;
    }
}
