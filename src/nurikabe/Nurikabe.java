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
import java.util.*;

import static java.lang.Math.abs;
import static java.lang.Math.max;

public class Nurikabe extends Application {
    // Grid
    public static final int grid_size = 6;
    public static Container container;

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
        unreachableBlocks2(); // Polnjenje nedosegljivih blokov iz posamezne točke
        islandOfOne(); // Dodajanje morja enojnim otokom
        separatedByOneSquare(); // Dodajanje morja med otoka, ki imata skupnega soseda
        diagonallyAdjacent(); // Dodajanje morja med otoka, ki imata skupno diagonalo
        finishedIsland(); // Dodajanje morja že dokončanim otokom
        calculatePossibilities();
        onlyOnePossibility(); // Dodamo otok otokom s samo eno možnostjo razvoja
        oneRemainingInDiagonal(); // Dodamo morje na diagonalo, ko je otoku preostalo še samo 1 mesto za širjenje, mesti za širjenje pa sta v smeri iste diagonale
        twoPossibilityDiagonal(); // Diagonalno sekanje otokov z 2 možnima razvojema

        // NAPREDNE HEVRISTIKE
        seaConstraint(); // bfs
        // naivno iskanje nedosegljivih celic

        /*if(isError()){
            return;
        }
        if(c se je spremenil)
            break;
        }
        if(c ostane enak skozi vse hevristike){
            naključna poteza
        }*/
    }

    public static int[][] copyArray(int[][] grid){
        int[][] copy = new int[grid_size][grid_size];
        for (int i = 0; i < grid_size; i++) {
            copy[i] = grid[i].clone();
        }
        return copy;
    }

    public static void seaConstraint(){
        int [][] grid_copy = copyArray(container.getGrid());

        for(int x = 0; x < grid_size; x++) {
            for (int y = 0; y < grid_size; y++) {
                if (grid_copy[x][y] == unknown) {
                    grid_copy[x][y] = island;
                    if(x+1 < grid_size) {
                        if(grid_copy[x + 1][y] == sea){
                            if(!bfs(grid_copy, x + 1, y)){
                                container.getGrid()[x][y] = sea;
                                return;
                            } else{
                                grid_copy[x][y] = unknown;
                                continue;
                            }
                        }
                    }
                    if(x-1 >= 0) {
                        if(grid_copy[x - 1][y] == sea){
                            if(!bfs(grid_copy, x - 1, y)){
                                container.getGrid()[x][y] = sea;
                                return;
                            } else{
                                grid_copy[x][y] = unknown;
                                continue;
                            }
                        }
                    }
                    if(y+1 < grid_size) {
                        if(grid_copy[x][y + 1] == sea){
                            if(!bfs(grid_copy, x, y + 1)){
                                container.getGrid()[x][y] = sea;
                                return;
                            } else{
                                grid_copy[x][y] = unknown;
                                continue;
                            }
                        }
                    }
                    if(y-1 >= 0) {
                        if(grid_copy[x][y - 1] == sea){
                            if(!bfs(grid_copy, x, y - 1)){
                                container.getGrid()[x][y] = sea;
                                return;
                            } else{
                                grid_copy[x][y] = unknown;
                                continue;
                            }
                        }
                    }
                    grid_copy[x][y] = unknown;
                }
            }
        }
    }

    public static boolean bfs(int [][] grid_copy1, int x, int y){
        int [][] grid_copy = copyArray(grid_copy1);
        ArrayList<IslandCoordinate> queue = new ArrayList<>();
        queue.add(new IslandCoordinate(x, y));
        grid_copy[x][y] = island;

        while (queue.size() > 0) {
            IslandCoordinate p = queue.get(0);
            x = p.getX();
            y = p.getY();

            if(x+1 < grid_size) {
                if(grid_copy[x + 1][y] <= unknown){
                    grid_copy[x + 1][y] = island;
                    queue.add(new IslandCoordinate(x + 1, y));
                }
            }
            if(x-1 >= 0) {
                if(grid_copy[x - 1][y] <= unknown){
                    grid_copy[x - 1][y] = island;
                    queue.add(new IslandCoordinate(x - 1, y));
                }
            }
            if(y+1 < grid_size) {
                if(grid_copy[x][y + 1] <= unknown){
                    grid_copy[x][y + 1] = island;
                    queue.add(new IslandCoordinate(x, y + 1));
                }
            }
            if(y-1 >= 0) {
                if(grid_copy[x][y - 1] <= unknown){
                    grid_copy[x][y - 1] = island;
                    queue.add(new IslandCoordinate(x, y - 1));
                }
            }
            queue.remove(0);
        }

        for(int i = 0; i < grid_size; i++) {
            for (int j = 0; j < grid_size; j++) {
                if (grid_copy[i][j] == sea) {
                    return false;
                }
            }
        }
        return true;
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

    public static void twoPossibilityDiagonal(){
        for(int i = 0; i < container.getIslands().size(); i++){
            Island island = container.getIslands().get(i);
            if(island.getPossibilities().size() == 2){
                IslandCoordinate islandCoordinate1 = island.getPossibilities().get(0);
                IslandCoordinate islandCoordinate2 = island.getPossibilities().get(1);
                if(abs(islandCoordinate1.getX() - islandCoordinate2.getX()) == 1 && abs(islandCoordinate1.getY() - islandCoordinate2.getY()) == 1){
                    if(container.getGrid()[islandCoordinate1.getX()][islandCoordinate2.getY()] > 0){
                        container.getGrid()[islandCoordinate1.getX()][islandCoordinate2.getY()] = max(container.getGrid()[islandCoordinate1.getX()][islandCoordinate2.getY()], sea);
                    }
                    if(container.getGrid()[islandCoordinate2.getX()][islandCoordinate1.getY()] > 0){
                        container.getGrid()[islandCoordinate2.getX()][islandCoordinate1.getY()] = max(container.getGrid()[islandCoordinate2.getX()][islandCoordinate1.getY()], sea);
                    }
                }
            }
        }
    }

    public static void oneRemainingInDiagonal(){
        for(int i = 0; i < container.getIslands().size(); i++){
            Island island = container.getIslands().get(i);
            if(island.getPossibilities().size() == 2 && island.getSize() - island.getCoordinates().size() == 1){
                IslandCoordinate islandCoordinate1 = island.getPossibilities().get(0);
                IslandCoordinate islandCoordinate2 = island.getPossibilities().get(1);
                if(abs(islandCoordinate1.getX() - islandCoordinate2.getX()) == 1 && abs(islandCoordinate1.getY() - islandCoordinate2.getY()) == 1){
                    container.getGrid()[islandCoordinate1.getX()][islandCoordinate1.getY()] = max(container.getGrid()[islandCoordinate1.getX()][islandCoordinate1.getY()], sea);
                    container.getGrid()[islandCoordinate2.getX()][islandCoordinate2.getY()] = max(container.getGrid()[islandCoordinate2.getX()][islandCoordinate2.getY()], sea);
                }
            }
        }
    }

    public static void onlyOnePossibility(){
        for(int i = 0; i < container.getIslands().size(); i++){
            Island island = container.getIslands().get(i);
            if(island.getPossibilities().size() == 1){
                IslandCoordinate islandCoordinate = island.getPossibilities().get(0);
                island.addCoordinate(islandCoordinate);
                container.getGrid()[islandCoordinate.getX()][islandCoordinate.getY()] = 9999;
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
                        int dist = abs(i - island.getX()) + abs(j - island.getY());
                        if(dist < island.getSize()){
                            reachable = true;
                            break;
                        }
                    }
                    if(!reachable){
                        container.getGrid()[i][j] = sea;
                    }
                }
            }
        }
    }

    public static void unreachableBlocks2(){
        for(int i = 0; i < grid_size; i++){
            for(int j = 0; j < grid_size; j++){
                if(container.getGrid()[i][j] == unknown){
                    boolean reachable = false;
                    for(int k = 0; k < container.getIslands().size(); k++){
                        Island island = container.getIslands().get(k);
                        int d = island.getSize() - island.getCoordinates().size();
                        for(int l = 0; l < island.getCoordinates().size(); l++){
                            int dist = abs(i - island.getCoordinates().get(l).getX()) + abs(j - island.getCoordinates().get(l).getY());
                            if(dist <= d){
                                reachable = true;
                                break;
                            }
                        }
                        if(reachable){
                            break;
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
                    if(container.getGrid()[i+1][j] <= 0){
                        container.getGrid()[i+1][j] = sea;
                    }
                }

                if(container.getGrid()[i][j] > 0 && j + 2 < grid_size && container.getGrid()[i][j+2] > 0){
                    if(container.getGrid()[i][j+1] <= 0) {
                        container.getGrid()[i][j+1] = sea;
                    }
                }
            }
        }
    }

    public static void diagonallyAdjacent(){
        for(int i = 0; i < grid_size; i++){
            for(int j = 0; j < grid_size; j++){
                if(container.getGrid()[i][j] > 0 && i + 1 < grid_size &&  j + 1 < grid_size && container.getGrid()[i+1][j+1] > 0){
                    if(container.getGrid()[i+1][j] <= 0 && container.getGrid()[i][j+1] <= 0) {
                        container.getGrid()[i + 1][j] = sea;
                        container.getGrid()[i][j + 1] = sea;
                    }
                }
                if(container.getGrid()[i][j] > 0 && i - 1 > 0 &&  j + 1 < grid_size && container.getGrid()[i-1][j+1] > 0){
                    if(container.getGrid()[i-1][j] <= 0 && container.getGrid()[i][j+1] <= 0){
                        container.getGrid()[i-1][j] = sea;
                        container.getGrid()[i][j+1] = sea;
                    }
                }
            }
        }
    }

    public static void calculatePossibilities(){

        for(int i = 0; i < container.getIslands().size(); i++){
            Island island = container.getIslands().get(i);
            island.getPossibilities().clear();
            for(int j = 0; j < island.getCoordinates().size(); j++){
                int x = island.getCoordinates().get(j).getX();
                int y = island.getCoordinates().get(j).getY();

                if(x+1 < grid_size) {
                    if(container.getGrid()[x + 1][y] == unknown){
                        island.addPossibility(new IslandCoordinate(x + 1, y));
                    }
                }
                if(x-1 >= 0) {
                    if(container.getGrid()[x - 1][y] == unknown){
                        island.addPossibility(new IslandCoordinate(x - 1, y));
                    }
                }
                if(y+1 < grid_size) {
                    if(container.getGrid()[x][y + 1] == unknown){
                        island.addPossibility(new IslandCoordinate(x, y + 1));
                    }
                }
                if(y-1 >= 0) {
                    if(container.getGrid()[x][y - 1] == unknown){
                        island.addPossibility(new IslandCoordinate(x, y - 1));
                    }
                }
            }

        }
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
            if(island.getPossibilities().size() == 0 && island.getCoordinates().size() != island.getSize()){
                return true;
            }
        }

        // Voda se deli na 2 dela
        for(int x = 0; x < grid_size; x++) {
            for (int y = 0; y < grid_size; y++) {
                if (container.getGrid()[x][y] == sea) {
                    if (!bfs(container.getGrid(), x, y)) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        }
        return false;
    }

    public static void readFile() throws FileNotFoundException {
        // Preberemo grid iz datoteke
        int [][] grid = new int[grid_size][grid_size];
        Scanner sc = new Scanner(new BufferedReader(new FileReader("primer2.txt")));
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
