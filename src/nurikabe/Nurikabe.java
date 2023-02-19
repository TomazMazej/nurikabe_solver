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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

import static java.lang.Math.abs;
import static java.lang.Math.max;

public class Nurikabe extends Application {
    // Nastavitve mreže
    public static final int grid_size = 10;
    public static final String filename = "10x10.txt";

    // Container, ki hrani mrežo in otoke
    public static Container container;

    // Možna polja
    public static final int island = 9999;
    public static final int sea = -1;
    public static final int unknown = 0;

    public static boolean isRunning = true;

    public static void main(String[] args) throws FileNotFoundException {
        readFile(); // Preberemo primer v grid
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader();
            BorderPane root = (BorderPane)loader.load(getClass().getResource("Grid.fxml").openStream());
            Button btn = new Button("Start");
            Button btn2 = new Button("Debug");

            root.setPadding(new Insets(10, 0, 10, 0));
            root.setTop(btn);
            root.setBottom(btn2);

            EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {
                public void handle(ActionEvent e) {
                    long start = System.currentTimeMillis();
                    nurikabe(container);
                    long end = System.currentTimeMillis();;
                    System.out.println("Time: " + (end - start) / 1000F + "s");
                }
            };
            EventHandler<ActionEvent> event2 = new EventHandler<ActionEvent>() {
                public void handle(ActionEvent e) {
                    debug(container);
                }
            };
            btn.setOnAction(event);
            btn2.setOnAction(event2);

            primaryStage.setTitle("Nurikabe Solver");
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void nurikabe(Container c) {
        while (isRunning) {
            int[][] grid_copy = copyArray(c.getGrid()); // Naredimo kopijo mreže za vsako, ko naredimo poskus
            executeHeuristics(c); // Izvedemo vse hevristike
            if (isError(c)) { // Če, kateri od pogojev ne ustreza, končamo poskus
                return;
            }
            if(getNumOfMoves(grid_copy) == 0){ // Če so vsi pogoji ustrezali in ni več možnih potez smo končali
                container = copyContainer(c);
                isRunning = false;
                printGrid(c);
                System.out.println("KONEC");
                return;
            }
            if (compareGrids(grid_copy, c.getGrid())) { // Če ni prišlo do sprememb naredimo naključno potezo
                for(int i = 0; i < c.getIslands().size(); i++) {
                    Island c_island = c.getIslands().get(i);
                    for(int j = 0; j < c_island.getPossibilities().size(); j++) {
                        Container c2 = copyContainer(c);
                        IslandCoordinate ic = c_island.getPossibilities().get(j);
                        c2.getIslands().get(i).addCoordinate(ic);
                        c2.grid[ic.getX()][ic.getY()] = island;
                        nurikabe(c2);
                        if(!isRunning){
                            return;
                        }
                    }
                }
            }
        }
    }

    public static void debug(Container cx) {
        Container c = copyContainer(cx);
        int[][] grid_copy = copyArray(c.getGrid()); // Naredimo kopijo mreže za vsako, ko naredimo poskus
        executeHeuristics(c); // Izvedemo vse hevristike
        printGrid(c);
        if (isError(c)) { // Če, kateri od pogojev ne ustreza, končamo poskus
            return;
        }
        if(getNumOfMoves(grid_copy) == 0){ // Če so vsi pogoji ustrezali in ni več možnih potez smo končali
            container = copyContainer(c);
            isRunning = false;
            printGrid(c);
            System.out.println("KONEC");
            return;
        }
        if (compareGrids(grid_copy, c.getGrid())) { // Če ni prišlo do sprememb naredimo naključno potezo
            for(int i = 0; i < c.getIslands().size(); i++) {
                Island c_island = c.getIslands().get(i);
                for(int j = 0; j < c_island.getPossibilities().size(); j++) {
                    Container c2 = copyContainer(c);
                    IslandCoordinate ic = c_island.getPossibilities().get(j);
                    c2.getIslands().get(i).addCoordinate(ic);
                    c2.grid[ic.getX()][ic.getY()] = island;
                    nurikabe(c2);
                    if(!isRunning){
                        return;
                    }
                }
            }
        }
        setContainer(c);
    }

    public static void executeHeuristics(Container c) {
        unreachableBlocks(c); // Polnjenje nedosegljivih blokov
        unreachableBlocks2(c); // Polnjenje nedosegljivih blokov iz posamezne točke
        islandOfOne(c); // Dodajanje morja enojnim otokom
        separatedByOneSquare(c); // Dodajanje morja med otoka, ki imata skupnega soseda
        diagonallyAdjacent(c); // Dodajanje morja med otoka, ki imata skupno diagonalo
        finishedIsland(c); // Dodajanje morja že dokončanim otokom
        calculatePossibilities(c); // Izračunamo možnosti vsakega otoka
        onlyOnePossibility(c); // Dodamo otok otokom s samo eno možnostjo razvoja
        oneRemainingInDiagonal(c); // Dodamo morje na diagonalo, ko je otoku preostalo še samo 1 mesto za širjenje, mesti za širjenje pa sta v smeri iste diagonale
        twoPossibilityDiagonal(c); // Diagonalno sekanje otokov z 2 možnima razvojema
        seaConstraint(c); // bfs
    }

    public static void seaConstraint(Container c){
        int [][] grid_copy = copyArray(c.getGrid());

        for(int x = 0; x < grid_size; x++) {
            for (int y = 0; y < grid_size; y++) {
                if (grid_copy[x][y] == unknown) {
                    grid_copy[x][y] = island;
                    if(x+1 < grid_size) {
                        if(grid_copy[x + 1][y] == sea){
                            if(!bfs(grid_copy, x + 1, y)){
                                c.getGrid()[x][y] = sea;
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
                                c.getGrid()[x][y] = sea;
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
                                c.getGrid()[x][y] = sea;
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
                                c.getGrid()[x][y] = sea;
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

    public static boolean bfs(int [][] grid, int x, int y){
        int [][] grid_copy = copyArray(grid);
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

    public static void finishedIsland(Container c){
        for(int i = 0; i < c.getIslands().size(); i++){
            Island island = c.getIslands().get(i);
            if(island.getCoordinates().size() == island.getSize()){
                for(int j = 0; j < island.getCoordinates().size(); j++){
                    int x = island.getCoordinates().get(j).getX();
                    int y = island.getCoordinates().get(j).getY();
                    if(x+1 < grid_size && c.getGrid()[x + 1][y] == unknown) {
                        c.getGrid()[x + 1][y] = sea;
                    }
                    if(x-1 >= 0 && c.getGrid()[x-1][y] == unknown) {
                        c.getGrid()[x-1][y] = sea;
                    }
                    if(y+1 < grid_size && c.getGrid()[x][y + 1] == unknown) {
                        c.getGrid()[x][y + 1] = sea;
                    }
                    if(y-1 >= 0 && c.getGrid()[x][y - 1] == unknown) {
                        c.getGrid()[x][y - 1] = sea;
                    }
                }
            }
        }
    }

    public static void twoPossibilityDiagonal(Container c){
        for(int i = 0; i < c.getIslands().size(); i++){
            Island island = c.getIslands().get(i);
            if(island.getPossibilities().size() == 2){
                IslandCoordinate islandCoordinate1 = island.getPossibilities().get(0);
                IslandCoordinate islandCoordinate2 = island.getPossibilities().get(1);
                if(abs(islandCoordinate1.getX() - islandCoordinate2.getX()) == 1 && abs(islandCoordinate1.getY() - islandCoordinate2.getY()) == 1){
                    if(c.getGrid()[islandCoordinate1.getX()][islandCoordinate2.getY()] > 0){
                        c.getGrid()[islandCoordinate1.getX()][islandCoordinate2.getY()] = max(c.getGrid()[islandCoordinate1.getX()][islandCoordinate2.getY()], sea);
                    }
                    if(c.getGrid()[islandCoordinate2.getX()][islandCoordinate1.getY()] > 0){
                        c.getGrid()[islandCoordinate2.getX()][islandCoordinate1.getY()] = max(c.getGrid()[islandCoordinate2.getX()][islandCoordinate1.getY()], sea);
                    }
                }
            }
        }
    }

    public static void oneRemainingInDiagonal(Container c){
        for(int i = 0; i < c.getIslands().size(); i++){
            Island island = c.getIslands().get(i);
            if(island.getPossibilities().size() == 2 && island.getSize() - island.getCoordinates().size() == 1){
                IslandCoordinate islandCoordinate1 = island.getPossibilities().get(0);
                IslandCoordinate islandCoordinate2 = island.getPossibilities().get(1);
                if(abs(islandCoordinate1.getX() - islandCoordinate2.getX()) == 1 && abs(islandCoordinate1.getY() - islandCoordinate2.getY()) == 1){
                    c.getGrid()[islandCoordinate1.getX()][islandCoordinate1.getY()] = max(c.getGrid()[islandCoordinate1.getX()][islandCoordinate1.getY()], sea);
                    c.getGrid()[islandCoordinate2.getX()][islandCoordinate2.getY()] = max(c.getGrid()[islandCoordinate2.getX()][islandCoordinate2.getY()], sea);
                }
            }
        }
    }

    public static void onlyOnePossibility(Container c){
        for(int i = 0; i < c.getIslands().size(); i++){
            Island island = c.getIslands().get(i);
            if(island.getPossibilities().size() == 1){
                IslandCoordinate islandCoordinate = island.getPossibilities().get(0);
                island.addCoordinate(islandCoordinate);
                c.getGrid()[islandCoordinate.getX()][islandCoordinate.getY()] = 9999;
            }
        }
    }

    public static void unreachableBlocks(Container c){
        for(int i = 0; i < grid_size; i++){
            for(int j = 0; j < grid_size; j++){
                if(c.getGrid()[i][j] == unknown){
                    boolean reachable = false;
                    for(int k = 0; k < c.getIslands().size(); k++){
                        Island island = c.getIslands().get(k);
                        int dist = abs(i - island.getX()) + abs(j - island.getY());
                        if(dist < island.getSize()){
                            reachable = true;
                            break;
                        }
                    }
                    if(!reachable){
                        c.getGrid()[i][j] = sea;
                    }
                }
            }
        }
    }

    public static void unreachableBlocks2(Container c){
        for(int i = 0; i < grid_size; i++){
            for(int j = 0; j < grid_size; j++){
                if(c.getGrid()[i][j] == unknown){
                    boolean reachable = false;
                    for(int k = 0; k < c.getIslands().size(); k++){
                        Island island = c.getIslands().get(k);
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
                        c.getGrid()[i][j] = sea;
                    }
                }
            }
        }
    }

    public static void islandOfOne(Container c){
        for(int i = 0; i < c.getIslands().size(); i++){
            if(c.getIslands().get(i).size == 1){
                int x = c.getIslands().get(i).x;
                int y = c.getIslands().get(i).y;

                if(x+1 < grid_size) {
                    c.getGrid()[x + 1][y] = sea;
                }
                if(x-1 >= 0) {
                    c.getGrid()[x-1][y] = sea;
                }
                if(y+1 < grid_size) {
                    c.getGrid()[x][y + 1] = sea;
                }
                if(y-1 >= 0) {
                    c.getGrid()[x][y - 1] = sea;
                }
            }
        }
    }

    public static void separatedByOneSquare(Container c){
        for(int i = 0; i < grid_size; i++){
            for(int j = 0; j < grid_size; j++){
                if(c.getGrid()[i][j] > 0 && i + 2 < grid_size && c.getGrid()[i+2][j] > 0){
                    if(c.getGrid()[i+1][j] <= 0){
                        c.getGrid()[i+1][j] = sea;
                    }
                }

                if(c.getGrid()[i][j] > 0 && j + 2 < grid_size && c.getGrid()[i][j+2] > 0){
                    if(c.getGrid()[i][j+1] <= 0) {
                        c.getGrid()[i][j+1] = sea;
                    }
                }
            }
        }
    }

    public static void diagonallyAdjacent(Container c){
        for(int i = 0; i < grid_size; i++){
            for(int j = 0; j < grid_size; j++){
                if(c.getGrid()[i][j] > 0 && i + 1 < grid_size &&  j + 1 < grid_size && c.getGrid()[i+1][j+1] > 0){
                    if(c.getGrid()[i+1][j] <= 0 && c.getGrid()[i][j+1] <= 0) {
                        c.getGrid()[i + 1][j] = sea;
                        c.getGrid()[i][j + 1] = sea;
                    }
                }
                if(c.getGrid()[i][j] > 0 && i - 1 > 0 &&  j + 1 < grid_size && c.getGrid()[i-1][j+1] > 0){
                    if(c.getGrid()[i-1][j] <= 0 && c.getGrid()[i][j+1] <= 0){
                        c.getGrid()[i-1][j] = sea;
                        c.getGrid()[i][j+1] = sea;
                    }
                }
            }
        }
    }

    public static void calculatePossibilities(Container c){
        for(int i = 0; i < c.getIslands().size(); i++){
            Island island = c.getIslands().get(i);
            island.getPossibilities().clear();
            for(int j = 0; j < island.getCoordinates().size(); j++){
                int x = island.getCoordinates().get(j).getX();
                int y = island.getCoordinates().get(j).getY();

                if(x+1 < grid_size) {
                    if(c.getGrid()[x + 1][y] == unknown){
                        island.addPossibility(new IslandCoordinate(x + 1, y));
                    }
                }
                if(x-1 >= 0) {
                    if(c.getGrid()[x - 1][y] == unknown){
                        island.addPossibility(new IslandCoordinate(x - 1, y));
                    }
                }
                if(y+1 < grid_size) {
                    if(c.getGrid()[x][y + 1] == unknown){
                        island.addPossibility(new IslandCoordinate(x, y + 1));
                    }
                }
                if(y-1 >= 0) {
                    if(c.getGrid()[x][y - 1] == unknown){
                        island.addPossibility(new IslandCoordinate(x, y - 1));
                    }
                }
            }
        }
    }

    public static boolean isError(Container c){
        // Pride do morja velikost 2x2
        for(int i = 0; i < grid_size-1; i++){
            for(int j = 0; j < grid_size-1; j++) {
                if(c.getGrid()[i][j] == sea && c.getGrid()[i+1][j] == sea && c.getGrid()[i][j+1] == sea && c.getGrid()[i+1][j+1] == sea){
                    return true;
                }
            }
        }

        // Otok se ne more več širiti
        for(int k = 0; k < c.getIslands().size(); k++){
            Island island = c.getIslands().get(k);
            if(island.getPossibilities().size() == 0 && island.getCoordinates().size() != island.getSize()){
                return true;
            }
        }

        // Voda se deli na 2 dela
        for(int x = 0; x < grid_size; x++) {
            for (int y = 0; y < grid_size; y++) {
                if (c.getGrid()[x][y] == sea) {
                    if (!bfs(c.getGrid(), x, y)) {
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
        int [][] grid = new int[grid_size][grid_size];
        Scanner sc = new Scanner(new BufferedReader(new FileReader(filename)));
        while(sc.hasNextLine()) {
            for (int i=0; i<grid_size; i++) {
                String[] line = sc.nextLine().trim().split(" ");
                for (int j=0; j<line.length; j++) {
                    grid[i][j] = Integer.parseInt(line[j]);
                }
            }
        }

        container = new Container(grid);
        for(int i = 0; i < grid_size; i++){
            for(int j = 0; j < grid_size; j++){
                if(container.getGrid()[i][j] > 0){
                    Island island = new Island(i, j, container.getGrid()[i][j]);
                    container.addIsland(island);
                }
            }
        }
    }

    public static Container copyContainer(Container c){
        Container c2 = new Container(copyArray(c.getGrid()));
        c2.setIslands(copyIslands(c));
        return c2;
    }

    public static int[][] copyArray(int[][] grid){
        int[][] copy = new int[grid_size][grid_size];
        for (int i = 0; i < grid_size; i++) {
            copy[i] = grid[i].clone();
        }
        return copy;
    }

    public static ArrayList<Island> copyIslands(Container c){
        ArrayList<Island> islands = new ArrayList<>();
        for(int i = 0; i < c.getIslands().size(); i++) {
            Island c_island = c.getIslands().get(i);
            islands.add(new Island(c_island.getX(), c_island.getY(), c_island.getSize(), copyCoordinates(c_island.getCoordinates())));
        }
        return islands;
    }

    public static ArrayList<IslandCoordinate> copyCoordinates(ArrayList<IslandCoordinate> ic){
        ArrayList<IslandCoordinate> ic2 = new ArrayList<IslandCoordinate>();
        for(IslandCoordinate i : ic){
            ic2.add(new IslandCoordinate(i.getX(), i.getY()));
        }
        return  ic2;
    }

    public static int getNumOfMoves(int[][] grid) {
        int num_moves = 0;
        for(int i = 0; i < grid_size; i++) {
            for (int j = 0; j < grid_size; j++) {
                if (grid[i][j] == unknown) {
                    num_moves += 1;
                }
            }
        }
        return num_moves;
    }

    public static boolean compareGrids(int[][] grid1, int[][] grid2) {
        for(int i = 0; i < grid_size; i++) {
            for (int j = 0; j < grid_size; j++) {
                if (grid1[i][j] != grid2[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void printGrid(Container c){
        for(int i = 0; i < grid_size; i++){
            for(int j = 0; j < grid_size; j++){
                System.out.print(c.grid[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    public static Container getContainer() {
        return container;
    }

    public static void setContainer(Container container) {
        Nurikabe.container = container;
    }
}
