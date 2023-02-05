package nurikabe;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

public class Nurikabe extends Application {
    // Grid
    static Container container;
    public static final int grid_size = 10;
    public static int [][] grid;
    // Polja
    public static final int island = 9999;
    public static final int sea = -1;
    public static final int unknown = 0;

    public static void main(String[] args) throws FileNotFoundException {

        // Preberemo vhodno datoteko
        grid = new int[grid_size][grid_size];
        readFile(); // Preberemo primer v grid
        printGrid(); // Izpis matrike

        // Zanka skozi hevristike
        /*while(true) {
            // vse hevristike

            if(isSolved()){
                FAIL
            }
            if(c se je spremenil)
                break;
            }

            if(c ostane enak skozi vse hevristike) naključna poteza
        }*/

        // Osnovne hevristike
        islandOfOne();
        separatedByOneSquare();
        diagonallyAdjacent();
        // Polnjenje nedosegljivih blokov
        // Dodamo morje na diagonalo, ko je otoku preostalo še samo 1 mesto za širjenje, mesti za širjenje pa sta v smeri iste diagonale
        // Diagonalno sekanje otokov z 2 možnima razvojema
        // Dodamo otok otokom s samo eno možnostjo razvoja

        // Napredne hevristike
        // dodajanje virtualnih otokov za iskanje morja (dfs)
        // naivno iskanje nedosegljivih celic
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
        for(int i = 0; i < container.getIslands().size(); i++){
            if(container.getIslands().get(i).size == 1){
                int x = container.getIslands().get(i).x;
                int y = container.getIslands().get(i).y;

                if(x+1 < grid_size) {
                    container.getGrid()[x + 1][y] = sea;
                }
                if(x-1 >= 0) {
                    container.getGrid()[x-1][y] = sea;
                }
                if(y+1 < grid_size) {
                    container.getGrid()[x][y + 1] = sea;
                }
                if(y-1 >= 0) {
                    container.getGrid()[x][y - 1] = sea;
                }
            }
        }
    }

    public static void separatedByOneSquare(){
        for(int i = 0; i < grid_size; i++){
            for(int j = 0; j < grid_size; j++){
                if(container.getGrid()[i][j] > 0 && i + 2 < grid_size && container.getGrid()[i+2][j] > 0){
                    container.getGrid()[i+1][j] = sea;
                }

                if(container.getGrid()[i][j] > 0 && j + 2 < grid_size && container.getGrid()[i][j+2] > 0){
                    container.getGrid()[i][j+1] = sea;
                }
            }
        }
    }

    public static void diagonallyAdjacent(){
        for(int i = 0; i < grid_size; i++){
            for(int j = 0; j < grid_size; j++){
                if(container.getGrid()[i][j] > 0 && i + 1 < grid_size &&  j + 1 < grid_size && container.getGrid()[i+1][j+1] > 0){
                    container.getGrid()[i+1][j] = sea;
                    container.getGrid()[i][j+1] = sea;
                }
                if(container.getGrid()[i][j] > 0 && i - 1 > 0 &&  j + 1 < grid_size && container.getGrid()[i-1][j+1] > 0){
                    container.getGrid()[i-1][j] = sea;
                    container.getGrid()[i][j+1] = sea;
                    System.out.println(grid[i][j] + " " + grid[i-1][j-1]);
                }
            }
        }
    }

    public static boolean isSolved(){
        // Pride do morja velikost 2x2
        // Voda se deli na 2 dela
        // Otok se ne more več širiti
        return false;
    }

    public static void readFile() throws FileNotFoundException {
        // Preberemo grid iz datoteke
        Scanner sc = new Scanner(new BufferedReader(new FileReader("primer1.txt")));
        while(sc.hasNextLine()) {
            for (int i=0; i<grid_size; i++) {
                String[] line = sc.nextLine().trim().split(" ");
                for (int j=0; j<line.length; j++) {
                    grid[i][j] = Integer.parseInt(line[j]);
                }
            }
        }
        container = new Container(grid); // Dodamo grid v container
        for(int i = 0; i < grid_size; i++){ // Dodamo otoke v container
            for(int j = 0; j < grid_size; j++){
                if(container.getGrid()[i][j] > 0){
                    Island island = new Island(i, j, container.getGrid()[i][j]);
                    container.addIsland(island);
                }
            }
        }
    }

    public static void printGrid(){
        for(int i = 0; i < grid_size; i++){
            for(int j = 0; j < grid_size; j++){
                System.out.print(container.grid[i][j] + " ");
            }
            System.out.println();
        }
    }

    public static Container getContainer() {
        return container;
    }

    public static void setContainer(Container container) {
        Nurikabe.container = container;
    }
}
