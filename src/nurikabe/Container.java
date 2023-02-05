package nurikabe;

import java.util.ArrayList;

public class Container {

    public int [][] grid;
    ArrayList<Island> islands;

    public Container(int[][] grid) {
        this.grid = grid;
        this.islands = new ArrayList<Island>();
    }

    public void addIsland(Island island){
        islands.add(island);
    }

    public int[][] getGrid() {
        return grid;
    }

    public void setGrid(int[][] grid) {
        this.grid = grid;
    }

    public ArrayList<Island> getIslands() {
        return islands;
    }

    public void setIslands(ArrayList<Island> islands) {
        this.islands = islands;
    }
}
