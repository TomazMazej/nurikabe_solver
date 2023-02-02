package nurikabe;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Scanner;

public class Nurikabe extends Application {
    public static final int size = 10;
    public static int [][] grid = new int[size][size];

    public static void main(String[] args) throws FileNotFoundException {
        // Preberemo vhodno datoteko
        readFile();
        // Starting techniques
        islandOfOne();
        separatedByOneSquare();
        diagonallyAdjacent();
        // Basic techniques
        surroundedSquare();

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader();
            BorderPane root =
                    (BorderPane)loader.load(getClass().getResource("Grid.fxml").openStream());
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void islandOfOne(){
        for(int i = 0; i < grid.length; i++){
            for(int j = 0; j < grid.length; j++){
                if(grid[i][j] == 1){
                    if(i+1 < size){
                        grid[i+1][j] =-1;
                    }
                    if(i-1 >= 0){
                        grid[i-1][j] =-1;
                    }
                    if(j+1 < size){
                        grid[i][j+1] =-1;
                    }
                    if(j-1 >= 0){
                        grid[i][j-1] =-1;
                    }
                }
            }
        }
    }

    public static void separatedByOneSquare(){
        for(int i = 0; i < grid.length; i++){
            for(int j = 0; j < grid.length; j++){
                if(grid[i][j] > 0 && i + 2 < size && grid[i+2][j] > 0){
                    grid[i+1][j] = -1;
                }
                if(grid[i][j] > 0 && j + 2 < size && grid[i][j+2] > 0){
                    grid[i][j+1] = -1;
                }
            }
        }
    }

    public static void diagonallyAdjacent(){
        for(int i = 0; i < grid.length; i++){
            for(int j = 0; j < grid.length; j++){
                if(grid[i][j] > 0 && i + 1 < size &&  j + 1 < size && grid[i+1][j+1] > 0){
                    grid[i+1][j] = -1;
                    grid[i][j+1] = -1;
                }
                if(grid[i][j] > 0 && i - 1 > 0 &&  j + 1 < size && grid[i-1][j+1] > 0){
                    grid[i-1][j] = -1;
                    grid[i][j+1] = -1;
                    System.out.println(grid[i][j] + " " + grid[i-1][j-1]);
                }
            }
        }
    }

    public static void surroundedSquare(){
        for(int i = 0; i < grid.length; i++){
            for(int j = 0; j < grid.length; j++){
                if(i + 1 == size && grid[i][j] == 0){
                    if(j + 1 == size){
                        if(grid[i-1][j] == -1 && grid[i][j-1] == -1){
                            grid[i][j] = -1;
                        }
                    } else if(j - 1 < 0){
                        if(grid[i-1][j] == -1 && grid[i][j+1] == -1){
                            grid[i][j] = -1;
                        }
                    } else if(grid[i][j+1] == -1 && grid[i][j-1] == -1 && grid[i-1][j] == -1){
                        grid[i][j] = -1;
                    }
                }
            }
        }
    }

    public static void readFile() throws FileNotFoundException {
        Scanner sc = new Scanner(new BufferedReader(new FileReader("primer1.txt")));
        while(sc.hasNextLine()) {
            for (int i=0; i<grid.length; i++) {
                String[] line = sc.nextLine().trim().split(" ");
                for (int j=0; j<line.length; j++) {
                    grid[i][j] = Integer.parseInt(line[j]);
                }
            }
        }

        // Izpis matrike
        printGrid();
    }

    public static void printGrid(){
        for(int i = 0; i < grid.length; i++){
            for(int j = 0; j < grid.length; j++){
                System.out.print(grid[i][j] + " ");
            }
            System.out.println();
        }
    }

    public static int[][] getGrid() {
        return grid;
    }

    public static void setGrid(int[][] grid) {
        Nurikabe.grid = grid;
    }

}
