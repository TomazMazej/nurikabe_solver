package nurikabe;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

public class Nurikabe extends Application {
    // Grid
    public static final int grid_size = 10;
    public static Container container;
    public static int land = 0;

    // Polja
    public static final int island = 9999;
    public static final int sea = -1;
    public static final int unknown = 0;

    public static void main(String[] args) throws FileNotFoundException {
        readFile(); // Preberemo primer v grid
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader();
            BorderPane root = (BorderPane)loader.load(getClass().getResource("Grid.fxml").openStream());
            Button btn = new Button("Next");

            root.setPadding(new Insets(10, 0, 0, 0));
            root.setTop(btn);

            EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {
                public void handle(ActionEvent e) {
                    loop();
                }
            };
            btn.setOnAction(event);

            primaryStage.setTitle("Nurikabe Solver");
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void loop() {
        // OSNOVNE HEVRISTIKE
        unreachableBlocks(); // Polnjenje nedosegljivih blokov
        islandOfOne(); // Dodajanje morja enojnim otokom
        separatedByOneSquare(); // Dodajanje morja med otoka, ki imata skupnega soseda
        diagonallyAdjacent(); // Dodajanje morja med otoka, ki imata skupno diagonalo
        finishedIsland(); // Dodajanje morja že dokončanim otokom

        // Dodamo morje na diagonalo, ko je otoku preostalo še samo 1 mesto za širjenje, mesti za širjenje pa sta v smeri iste diagonale
        // Diagonalno sekanje otokov z 2 možnima razvojema
        // Dodamo otok otokom s samo eno možnostjo razvoja

        // NAPREDNE HEVRISTIKE
        //dfs(); // Dodajanje virtualnih otokov za iskanje morja (dfs)
        // naivno iskanje nedosegljivih celic

        /*if(isError()){
            FAIL
        }
        if(c se je spremenil)
            break;
        }
        if(c ostane enak skozi vse hevristike){
            naključna poteza
        }*/
    }

    public static void dfs(){
        int total_sea = grid_size*grid_size - land;
        int [][] grid_copy = container.getGrid();

        for(int i = 0; i < grid_size; i++) {
            for (int j = 0; j < grid_size; j++) {
                if(grid_copy[i][j] == unknown){
                    grid_copy[i][j] = island;
                    // vse 4 strani

                }
            }
        }
    }

    public static void finishedIsland(){
        for(int i = 0; i < container.getIslands().size(); i++){
            Island island = container.getIslands().get(i);
            if(island.getCoordinates().size() == island.getSize()){
                for(int j = 0; j < island.getCoordinates().size(); j++){
                    int x = island.getCoordinates().get(j).getX();
                    int y = island.getCoordinates().get(j).getY();
                    if(x+1 < grid_size && container.getGrid()[x + 1][y] == unknown) {
                        container.getGrid()[x + 1][y] = sea;
                    }
                    if(x-1 >= 0 && container.getGrid()[x-1][y] == unknown) {
                        container.getGrid()[x-1][y] = sea;
                    }
                    if(y+1 < grid_size && container.getGrid()[x][y + 1] == unknown) {
                        container.getGrid()[x][y + 1] = sea;
                    }
                    if(y-1 >= 0 && container.getGrid()[x][y - 1] == unknown) {
                        container.getGrid()[x][y - 1] = sea;
                    }
                }
            }
        }
    }

    public static void unreachableBlocks(){
        for(int i = 0; i < grid_size; i++){
            for(int j = 0; j < grid_size; j++){
                if(container.getGrid()[i][j] == unknown){
                    boolean reachable = false;
                    for(int k = 0; k < container.getIslands().size(); k++){
                        Island island = container.getIslands().get(k);
                        if(Point2D.distance(i, j, island.getX(), island.getY()) < island.getSize()){
                            reachable = true;
                        }
                    }
                    if(!reachable){
                        container.getGrid()[i][j] = sea;
                    }
                }
            }
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
                }
            }
        }
    }

    public static boolean canIslandExpand(Island island){
        int x = island.getX();
        int y = island.getY();
        int counter = 0;

        if(x+1 < grid_size) {
            if(container.getGrid()[x + 1][y] == unknown){
                counter += 1;
            }
        }
        if(x-1 >= 0) {
            if(container.getGrid()[x - 1][y] == unknown){
                counter += 1;
            }
        }
        if(y+1 < grid_size) {
            if(container.getGrid()[x][y + 1] == unknown){
                counter += 1;
            }
        }
        if(y-1 >= 0) {
            if(container.getGrid()[x][y - 1] == unknown){
                counter += 1;
            }
        }
        return counter > 0;
    }

    public static boolean isError(){
        // Pride do morja velikost 2x2
        for(int i = 0; i < grid_size; i++){
            for(int j = 0; j < grid_size; j++) {
                if(container.getGrid()[i][j] == sea && container.getGrid()[i+1][j] == sea && container.getGrid()[i][j+1] == sea && container.getGrid()[i+1][j+1] == sea){
                    return true;
                }
            }
        }

        // Otok se ne more več širiti
        for(int k = 0; k < container.getIslands().size(); k++){
            Island island = container.getIslands().get(k);
            if(!canIslandExpand(island) && island.getCoordinates().size() != island.getSize()){
                return true;
            }
        }

        // Voda se deli na 2 dela

        return false;
    }

    public static void readFile() throws FileNotFoundException {
        // Preberemo grid iz datoteke
        int [][] grid = new int[grid_size][grid_size];
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
                    land += container.getGrid()[i][j];
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
