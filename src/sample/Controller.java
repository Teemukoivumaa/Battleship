package sample;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Controller {
    // Ship placing buttons and ship rotation
    public Button Carrier;
    public Button Battleship;
    public Button Destroyer;
    public Button Submarine;
    public Button Patrol;
    public Button RotateButton;

    // Play area
    public SplitPane splitPane;
    public GridPane OwnGrid;
    public GridPane EnemyGrid;

    // Creates buttons at start()
    public Button StartButton;

    // Tells the state of the game and turns
    public Label stateLabel;
    public Label TurnCounter;

    // Start the game
    public Button StartGame;

    public boolean rotated = false;
    public boolean enemySets = false;
    public boolean GAME = true;
    public boolean yourTurn = true;

    public List <String> ships = new ArrayList<>();
    public List <String> shipLocations = new ArrayList<>();
    public List <String> enemyLocations = new ArrayList<>();
    public String chosenShip = "";
    public String lastPlacedShip = "";

    public int shipRow, shipColumn;
    public int shipLength;
    public int rotationRow, rotationColumn;
    public int turns = 0;

    public void Start(ActionEvent actionEvent) {
        SetButtons(OwnGrid, "Own");
        StartButton.setVisible(false);
        splitPane.setDisable(false);
        stateLabel.setText("Place ships to your board now");
    }

    public void StartGame(ActionEvent actionEvent) {
        SetButtons(EnemyGrid, "Enemy");
        OwnGrid.setDisable(true);
        StartGame.setVisible(false);
        RotateButton.setVisible(false);
        TurnCounter.setVisible(true);
        TurnCounter.setText("Turns: " + turns);

        enemySets = true;
        PlaceEnemyShips();
        stateLabel.setText("It's your turn! Click on enemy grid to shoot there");
    }

    public void Turns(ActionEvent actionEvent) {

    }

    public void PlaceEnemyShips() {

        for (int i = 0; i < 5; i++) {
            int columnNum = ThreadLocalRandom.current().nextInt(1, 10 + 1);
            int rowNum = ThreadLocalRandom.current().nextInt(1, 10 + 1);
            System.out.println("Col"+columnNum+" Row"+rowNum);

            String ship = switch (i) {
                case 0 -> "Carrier";
                case 1 -> "Battleship";
                case 2 -> "Destroyer";
                case 3 -> "Submarine";
                case 4 -> "Patrol";
                default -> throw new IllegalStateException("Unexpected value: " + i);
            };

            switch (ship) {
                case "Carrier" -> shipLength = 5;
                case "Battleship" -> shipLength = 4;
                case "Destroyer", "Submarine" -> shipLength = 3;
                case "Patrol" -> shipLength = 2;
            }

            EventHandler<ActionEvent> action = null;
            //SetShip();

        }
    }

    public void SetButtons(GridPane grid, String gridName) {
        EventHandler<ActionEvent> action = null;

        if (gridName.equals("Own")) { action = this::SetShip; }
        else { action = this::test; }

        System.out.println(gridName);
        for (int i = 1; i < 11; i++) {
            String columnChar = IntToChar(i);

            for (int j = 1; j < 11; j++) {
                Button setButton = new Button();
                setButton.setStyle("-fx-background-color: f4f4f4;");
                setButton.setOnAction(action);
                setButton.setPrefWidth(26);
                setButton.setId(columnChar + j);
                grid.add(setButton, i, j);
            }
        }
    }

    public void test(ActionEvent actionEvent) {
        String temp = actionEvent.toString();
        String mark = temp.substring(42, temp.length() - 23);
        System.out.println("Shot at: " + mark);
    }

    public void ChooseShip(ActionEvent actionEvent) {
        String temp = (actionEvent.toString());
        String ship = temp.substring(42, temp.length() - 23);
        System.out.println("Chosen ship: " + ship);

        boolean hasBeenSet = false;
        for (String s: ships) {
            System.out.println(s);
            if (ship.equals(s)) {
                hasBeenSet = true;
                stateLabel.setText(ship + " has been placed to the board already");
            }
        }

        if (!hasBeenSet) {
            stateLabel.setText("Set " + ship + " to board");
            chosenShip = ship;

            switch (ship) {
                case "Carrier" -> shipLength = 5;
                case "Battleship" -> shipLength = 4;
                case "Destroyer", "Submarine" -> shipLength = 3;
                case "Patrol" -> shipLength = 2;
            }
        }

        rotated = false;
    }

    public void SetShip(ActionEvent actionEvent) {
        System.out.println(actionEvent);
        if (!chosenShip.equals("")) {
            GridPane grid;

            if (enemySets) {
                grid = EnemyGrid;
                System.out.println("Enemy places boat");
            } else {
                grid = OwnGrid;
                System.out.println("You place a boat");
            }

            String temp = actionEvent.toString();
            String location = temp.substring(42, temp.length() - 23);
            System.out.println(location);
            stateLabel.setText(chosenShip + " set at: " + location);

            String columnChar;
            if (location.length() == 3) {
                columnChar = location.substring(0, location.length() - 2);
            } else {
                columnChar = location.substring(0, location.length() - 1);
            }

            shipRow = Integer.parseInt(location.substring(1));
            shipColumn = CharToInt(columnChar);

            boolean collision = FirstSet(columnChar, shipRow, shipLength, shipLocations);
            if (!collision) {
                ships.add(chosenShip); // add ship to list

                for (int i = 0; i < shipLength; i++) {
                    Pane shipLocation = new Pane();
                    shipLocation.setStyle("-fx-background-color: black;");
                    grid.add(shipLocation, shipColumn + i, shipRow);

                    columnChar = IntToChar(shipColumn + i);
                    shipLocations.add(columnChar + shipRow);
                }
                System.out.println(shipLocations);
                lastPlacedShip = chosenShip;
                chosenShip = "";
            } else {
                stateLabel.setText("Can't place a ship to " + columnChar + shipRow + "!");
            }
        } else {
            stateLabel.setText("Choose a ship first!");
        }
    }

    public void RotateShip(ActionEvent actionEvent) {
        if (rotated) {
            String columnChar = IntToChar(shipColumn);
            boolean collision = DoesItCollide(columnChar, shipRow, shipLength, false, shipLocations);
            if (!collision) {
                for (int i = 0; i < shipLength; i++) {
                    Pane emptyPane = new Pane();
                    emptyPane.setStyle("-fx-background-color: f4f4f4; -fx-border-color: black;");
                    OwnGrid.add(emptyPane, shipColumn, shipRow + i);
                    shipLocations.remove(shipLocations.size() - 1);
                }
                System.out.println(shipLocations);

                rotationColumn = shipColumn;
                for (int i = 0; i < shipLength; i++) {
                    columnChar = IntToChar(rotationColumn);

                    Pane shipPane = new Pane();
                    shipPane.setStyle("-fx-background-color: black;");

                    OwnGrid.add(shipPane, rotationColumn, shipRow);
                    shipLocations.add(columnChar + shipRow);
                    rotationColumn++;
                }
                System.out.println(shipLocations);
                stateLabel.setText(lastPlacedShip + " is now set vertically");
                rotated = false;
            } else {
                stateLabel.setText("Ship can't be rotated");
            }
        } else {
            String columnChar = IntToChar(shipColumn);
            boolean collision = DoesItCollide(columnChar, shipRow, shipLength, true, shipLocations);
            if (!collision) {
                for (int i = 0; i < shipLength; i++) {
                    Pane emptyPane = new Pane();
                    emptyPane.setStyle("-fx-background-color: f4f4f4; -fx-border-color: black;");
                    OwnGrid.add(emptyPane, shipColumn + i, shipRow);
                    shipLocations.remove(shipLocations.size() - 1);
                }
                System.out.println(shipLocations);

                rotationRow = shipRow;
                for (int i = 0; i < shipLength; i++) {
                    Pane shipPane = new Pane();
                    shipPane.setStyle("-fx-background-color: black;");

                    OwnGrid.add(shipPane, shipColumn, rotationRow);
                    shipLocations.add(columnChar + rotationRow);
                    rotationRow++;
                }
                System.out.println(shipLocations);
                stateLabel.setText(lastPlacedShip + " is now set horizontally");
                rotated = true;
            } else {
                stateLabel.setText("Ship can't be rotated");
            }
        }
    }

    public boolean DoesItCollide(String columnChar, int rowInt, int length, boolean rotation, List < String > shipLocations) {
        boolean collision = false;

        if (!rotation) { // set to default position >
            int columnInt = CharToInt(columnChar);
            columnInt++;
            for (int j = 0; j < length; j++) { // check if hits other ships
                String column = IntToChar(columnInt);
                columnInt++;
                for (int k = 0; k < shipLocations.size(); k++) {
                    if (shipLocations.get(k).equals(column + rowInt)) {
                        collision = true;
                        k = length;
                        j = length;
                    }
                }
            }
        } else {
            if (rowInt == 7 || rowInt == 8 || rowInt == 9 || rowInt == 10) {
                switch (rowInt) {
                    case 7: if (length > 4) { collision = true; } break;
                    case 8: if (length > 3) { collision = true; } break;
                    case 9: if (length > 2) { collision = true; } break;
                    case 10: collision = true; break;
                    default: throw new IllegalStateException("Unexpected value: " + columnChar);
                }
            }

            if (!collision) {
                int row = rowInt;
                for (int j = 0; j < length; j++) { // check if hits other ships
                    row++;
                    for (int k = 0; k < shipLocations.size(); k++) {

                        if (shipLocations.get(k).equals(columnChar + row)) {
                            collision = true;
                            k = length;
                            j = length;
                        }
                    }
                }
            }
        }

        System.out.println(collision);
        return collision;
    }

    public boolean FirstSet(String columnChar, int rowInt, int length, List < String > shipLocations) {
        boolean collision = false;


            if (columnChar.equals("G") || columnChar.equals("H") || columnChar.equals("I") || columnChar.equals("J")) {
                switch (columnChar) {
                    case "G": if (length > 4) { collision = true; } break;
                    case "H": if (length > 3) { collision = true; } break;
                    case "I": if (length > 2) { collision = true; } break;
                    case "J": collision = true; break;
                    default: throw new IllegalStateException("Unexpected value: " + columnChar);
                }
            }

            if (!collision) {
                int columnInt = CharToInt(columnChar);
                for (int j = 0; j < length; j++) { // check if hits other ships
                    String column = IntToChar(columnInt);
                    columnInt++;
                    for (int k = 0; k < shipLocations.size(); k++) {
                        if (shipLocations.get(k).equals(column + rowInt)) {
                            collision = true;
                            k = length;
                            j = length;
                        }
                    }
                }
            }


        System.out.println(collision);
        return collision;
    }

    public String IntToChar(int toChar) {
        return switch (toChar) {
            case 1 -> "A";
            case 2 -> "B";
            case 3 -> "C";
            case 4 -> "D";
            case 5 -> "E";
            case 6 -> "F";
            case 7 -> "G";
            case 8 -> "H";
            case 9 -> "I";
            case 10 -> "J";
            default -> throw new IllegalStateException("Unexpected value: " + toChar);
        };
    }

    public int CharToInt(String toInt) {
        return switch (toInt) {
            case "A" -> 1;
            case "B" -> 2;
            case "C" -> 3;
            case "D" -> 4;
            case "E" -> 5;
            case "F" -> 6;
            case "G" -> 7;
            case "H" -> 8;
            case "I" -> 9;
            case "J" -> 10;
            default -> throw new IllegalStateException("Unexpected value: " + toInt);
        };
    }
}