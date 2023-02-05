package nurikabe;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import static nurikabe.Nurikabe.sea;
import static nurikabe.Nurikabe.unknown;

public class GridController {

    @FXML
    Pane pane;

    private final int size = 500; // Velikost okna
    private final int spots = Nurikabe.grid_size; // Število kvadratkov v vrstici
    private final int squareSize = size / spots; // Velikost kvadratkov
    private final int center = squareSize/2; // Centriramo številko

    int [][] grid; // Grid
    // Položaj v gridu
    int gridX = 0;
    int gridY = 0;

    @FXML
    public void initialize() {
        grid = Nurikabe.getContainer().getGrid();
        Nurikabe.printGrid();
        for (int i = 0; i < size; i += squareSize) {
            for (int j = 0; j < size; j += squareSize) {
                if(grid[gridY][gridX] == unknown){ // Če je prazno pobarvamo belo
                    Rectangle r = new Rectangle(i, j ,squareSize, squareSize);
                    r.setFill(Color.GREY);
                    r.setStroke(Color.BLACK);
                    pane.getChildren().add(r);
                } else if(grid[gridY][gridX] == sea){ // Če je morje pobarvamo črno
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
                    if(grid[gridY][gridX] != 9999){
                        pane.getChildren().add(text);
                    }
                }
                gridY++;
            }
            gridX++;
            gridY = 0;
        }
    }
}