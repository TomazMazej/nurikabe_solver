package nurikabe;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class GridController {

    @FXML
    Pane pane;

    // Velikost okna
    private int size = 420;
    // Število kvadratkov v vrstici
    private int spots = Nurikabe.size;
    // Velikost kvadratkov
    private int squareSize = size / spots;
    // Centriramo številko
    private int center = squareSize/2;
    // Grid
    int [][] grid;
    // Položaj v gridu
    int gridX = 0;
    int gridY = 0;

    @FXML
    public void initialize() {
        grid = Nurikabe.getGrid();
        Nurikabe.printGrid();
        for (int i = 0; i < size; i += squareSize) {
            for (int j = 0; j < size; j += squareSize) {
                if(grid[gridY][gridX] == 0){ // Če je prazno pobarvamo belo
                    Rectangle r = new Rectangle(i, j ,squareSize, squareSize);
                    r.setFill(Color.WHITE);
                    r.setStroke(Color.BLACK);
                    pane.getChildren().add(r);
                } else if(grid[gridY][gridX] == -1){ // Če je morje pobarvamo črno
                    Rectangle r = new Rectangle(i, j ,squareSize, squareSize);
                    r.setFill(Color.BLACK);
                    r.setStroke(Color.BLACK);
                    pane.getChildren().add(r);
                } else{ // Če je številka
                    Text text = new Text(i + center, j+squareSize - center, "" + grid[gridY][gridX]);
                    text.setScaleX(2);
                    text.setScaleY(2);
                    Rectangle r = new Rectangle(i, j ,squareSize, squareSize);
                    r.setFill(Color.WHITE);
                    r.setStroke(Color.BLACK);
                    pane.getChildren().add(r);
                    pane.getChildren().add(text);
                }
                gridY++;
            }
            gridX++;
            gridY = 0;
        }
    }
}