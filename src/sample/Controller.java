package sample;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

    // Game ships
    Ship carrier = new Ship();
    Ship battleship = new Ship();
    Ship destroyer = new Ship();
    Ship submarine = new Ship();
    Ship patrol = new Ship();

    Ship enemyCarrier = new Ship();
    Ship enemyBattleship = new Ship();
    Ship enemyDestroyer = new Ship();
    Ship enemySubmarine = new Ship();
    Ship enemyPatrol = new Ship();

    public List <String> ships = new ArrayList<>();
    public List <String> shipLocations = new ArrayList<>();
    public List <String> enemyShips = new ArrayList<>();
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

    public void ShootAtEnemy(ActionEvent actionEvent) {
        String temp = actionEvent.toString();
        String mark = temp.substring(42, temp.length() - 23);
        stateLabel.setText("Shot at: " + mark);

        turns++;
        TurnCounter.setText("Turns: " + turns);
        DidItHit(mark);
        //yourTurn = false;
    }

    public void SetButtons(GridPane grid, String gridName) {
        EventHandler<ActionEvent> action = null;

        if (gridName.equals("Own")) { action = this::SetShip; }
        else { action = this::ShootAtEnemy; }

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

    // Enemy ship placement ---------------------------------------------
    public void PlaceEnemyShips() {
        for (int i = 0; i < 5; i++) {
            int columnNum = ThreadLocalRandom.current().nextInt(1, 10 + 1);
            int rowNum = ThreadLocalRandom.current().nextInt(1, 10 + 1);

            chosenShip = switch (i) {
                case 0 -> "Carrier";
                case 1 -> "Battleship";
                case 2 -> "Destroyer";
                case 3 -> "Submarine";
                case 4 -> "Patrol";
                default -> throw new IllegalStateException("Unexpected value: " + i);
            };

            switch (chosenShip) {
                case "Carrier" ->       { shipLength = 5; enemyCarrier.SetShip(chosenShip, shipLength); }
                case "Battleship" ->    { shipLength = 4; enemyBattleship.SetShip(chosenShip, shipLength); }
                case "Destroyer" ->     { shipLength = 3; enemyDestroyer.SetShip(chosenShip, shipLength); }
                case "Submarine" ->     { shipLength = 3; enemySubmarine.SetShip(chosenShip, shipLength); }
                case "Patrol" ->        { shipLength = 2; enemyPatrol.SetShip(chosenShip, shipLength); }
            }

            String location = IntToChar(columnNum) + rowNum;
            shipRow = Integer.parseInt(location.substring(1));

            boolean collision = FirstSet(IntToChar(columnNum), shipRow, shipLength, enemyLocations);
            if (collision) { i--; }
            else { SetEnemyShip(location); }
        }
    }

    public void SetEnemyShip(String location) {
        if (!chosenShip.equals("")) {
            GridPane grid = EnemyGrid;

            String columnChar;
            if (location.length() == 3) { columnChar = location.substring(0, location.length() - 2); }
            else { columnChar = location.substring(0, location.length() - 1); }

            shipRow = Integer.parseInt(location.substring(1));
            shipColumn = CharToInt(columnChar);

            boolean collision = FirstSet(columnChar, shipRow, shipLength, enemyLocations);
            if (!collision) {
                enemyShips.add(chosenShip); // add ship to list
                List <String> locations = new ArrayList<>();
                EventHandler<ActionEvent> action = this::ShootAtEnemy;

                for (int i = 0; i < shipLength; i++) {
                    columnChar = IntToChar(shipColumn + i);

                    Pane shipLocation = new Pane();
                    shipLocation.setStyle("-fx-background-color: black;");
                    grid.add(shipLocation, shipColumn + i, shipRow);

                    Button setButton = new Button();
                    setButton.setStyle("-fx-background-color: black;");
                    setButton.setOnAction(action);
                    setButton.setPrefWidth(26);
                    setButton.setId(columnChar + shipRow);
                    grid.add(setButton, shipColumn + i, shipRow);

                    locations.add(columnChar + shipRow);
                }

                switch (chosenShip) {
                    case "Carrier" ->       enemyCarrier.SetShipLocation(locations);
                    case "Battleship" ->    enemyBattleship.SetShipLocation(locations);
                    case "Destroyer" ->     enemyDestroyer.SetShipLocation(locations);
                    case "Submarine" ->     enemySubmarine.SetShipLocation(locations);
                    case "Patrol" ->        enemyPatrol.SetShipLocation(locations);
                }
                enemyLocations.addAll(locations);

                lastPlacedShip = chosenShip;
                chosenShip = "";
            }
        }
    }
    // Enemy ship placement end ------------------------------------------

    // Ship placement ------------------------------------------
    public void ChooseShip(ActionEvent actionEvent) {
        String temp = (actionEvent.toString());
        String ship = temp.substring(42, temp.length() - 23);

        boolean hasBeenSet = false;
        for (String s: ships) {
            if (ship.equals(s)) {
                hasBeenSet = true;
                stateLabel.setText(ship + " has been placed to the board already");
            }
        }

        if (!hasBeenSet) {
            stateLabel.setText("Set " + ship + " to board");
            chosenShip = ship;

            switch (chosenShip) {
                case "Carrier" ->       { shipLength = 5; carrier.SetShip(chosenShip, shipLength); }
                case "Battleship" ->    { shipLength = 4; battleship.SetShip(chosenShip, shipLength); }
                case "Destroyer" ->     { shipLength = 3; destroyer.SetShip(chosenShip, shipLength); }
                case "Submarine" ->     { shipLength = 3; submarine.SetShip(chosenShip, shipLength); }
                case "Patrol" ->        { shipLength = 2; patrol.SetShip(chosenShip, shipLength); }
            }
        }

        rotated = false;
    }

    public void SetShip(ActionEvent actionEvent) {
        if (!chosenShip.equals("")) {
            GridPane grid = OwnGrid;

            String temp = actionEvent.toString();
            String location = temp.substring(42, temp.length() - 23);
            stateLabel.setText(chosenShip + " set at: " + location);

            String columnChar;
            if (location.length() == 3) { columnChar = location.substring(0, location.length() - 2); }
            else { columnChar = location.substring(0, location.length() - 1); }

            shipRow = Integer.parseInt(location.substring(1));
            shipColumn = CharToInt(columnChar);

            boolean collision = FirstSet(columnChar, shipRow, shipLength, shipLocations);
            if (!collision) {
                ships.add(chosenShip); // add ship to list
                List <String> locations = new ArrayList<>();

                for (int i = 0; i < shipLength; i++) {
                    Pane shipLocation = new Pane();
                    shipLocation.setStyle("-fx-background-color: black;");
                    grid.add(shipLocation, shipColumn + i, shipRow);

                    columnChar = IntToChar(shipColumn + i);
                    locations.add(columnChar + shipRow);
                }

                switch (chosenShip) { // update ship locations
                    case "Carrier" -> carrier.SetShipLocation(locations);
                    case "Battleship" -> battleship.SetShipLocation(locations);
                    case "Destroyer" -> destroyer.SetShipLocation(locations);
                    case "Submarine" -> submarine.SetShipLocation(locations);
                    case "Patrol" -> patrol.SetShipLocation(locations);
                }

                shipLocations.addAll(locations);

                System.out.println(shipLocations);
                lastPlacedShip = chosenShip;
                chosenShip = "";

            } else stateLabel.setText("Can't place a ship to " + columnChar + shipRow + "!");

        } else stateLabel.setText("Choose a ship first!");
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

                List <String> locations = new ArrayList<>();
                rotationColumn = shipColumn;
                for (int i = 0; i < shipLength; i++) {
                    columnChar = IntToChar(rotationColumn);

                    Pane shipPane = new Pane();
                    shipPane.setStyle("-fx-background-color: black;");

                    OwnGrid.add(shipPane, rotationColumn, shipRow);
                    locations.add(columnChar + shipRow);
                    rotationColumn++;
                }

                switch (lastPlacedShip) { // update ship locations
                    case "Carrier" -> carrier.SetShipLocation(locations);
                    case "Battleship" -> battleship.SetShipLocation(locations);
                    case "Destroyer" -> destroyer.SetShipLocation(locations);
                    case "Submarine" -> submarine.SetShipLocation(locations);
                    case "Patrol" -> patrol.SetShipLocation(locations);
                }
                shipLocations.addAll(locations);

                stateLabel.setText(lastPlacedShip + " is now set vertically");
                rotated = false;
            } else stateLabel.setText("Ship can't be rotated");
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

                List <String> locations = new ArrayList<>();
                rotationRow = shipRow;
                for (int i = 0; i < shipLength; i++) {
                    Pane shipPane = new Pane();
                    shipPane.setStyle("-fx-background-color: black;");

                    OwnGrid.add(shipPane, shipColumn, rotationRow);
                    locations.add(columnChar + rotationRow);
                    rotationRow++;
                }

                switch (lastPlacedShip) { // update ship locations
                    case "Carrier" -> carrier.SetShipLocation(locations);
                    case "Battleship" -> battleship.SetShipLocation(locations);
                    case "Destroyer" -> destroyer.SetShipLocation(locations);
                    case "Submarine" -> submarine.SetShipLocation(locations);
                    case "Patrol" -> patrol.SetShipLocation(locations);
                }
                shipLocations.addAll(locations);

                stateLabel.setText(lastPlacedShip + " is now set horizontally");
                rotated = true;
            } else stateLabel.setText("Ship can't be rotated");
        }
    }
    // Ship placement end ------------------------------------------

    // Helper functions --------------------------------------------
    public boolean DidItHit(String mark) {
        String columnChar;
        if (mark.length() == 3) { columnChar = mark.substring(0, mark.length() - 2); }
        else { columnChar = mark.substring(0, mark.length() - 1); }

        int column = CharToInt(columnChar);
        int rowNum = Integer.parseInt(mark.substring(1));

        boolean itHit = false;
        List<String> locations = enemyLocations;

        for (int i = 0; i < 5; i++) {
            if (yourTurn) {
                switch (i) { // update ship locations
                    case 0 -> locations = enemyCarrier.GetShipLocation();
                    case 1 -> locations = enemyBattleship.GetShipLocation();
                    case 2 -> locations = enemyDestroyer.GetShipLocation();
                    case 3 -> locations = enemySubmarine.GetShipLocation();
                    case 4 -> locations = enemyPatrol.GetShipLocation();
                }
            }

            for (int j = 0; j < locations.size(); j++) {
                if (mark.equals(locations.get(j))) {
                    if (yourTurn) {
                        boolean destroyed = false;
                        switch (i) { // update ship locations
                            case 0 -> destroyed = enemyCarrier.SetHits(mark);
                            case 1 -> destroyed = enemyBattleship.SetHits(mark);
                            case 2 -> destroyed = enemyDestroyer.SetHits(mark);
                            case 3 -> destroyed = enemySubmarine.SetHits(mark);
                            case 4 -> destroyed = enemyPatrol.SetHits(mark);
                        }
                    }
                    i = 4; j = locations.size();
                    itHit = true;
                }
            }
        }

        if (itHit) {
            ImageView hit = new ImageView(new Image(getClass().getResourceAsStream("images/hit.png")));
            hit.setFitHeight(26);
            hit.setFitWidth(26);
            EnemyGrid.add(hit, column, rowNum);
        } else {
            ImageView miss = new ImageView(new Image(getClass().getResourceAsStream("images/miss.png")));
            miss.setFitHeight(26);
            miss.setFitWidth(26);
            EnemyGrid.add(miss, column, rowNum);
        }

        return itHit;
    }

    public boolean DoesItCollide(String columnChar, int rowInt, int length, boolean rotation, List <String> shipLocations) {
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

    public boolean FirstSet(String columnChar, int rowInt, int length, List <String> shipLocations) {
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
    // Helper functions end -----------------------------------------
}

class Ship {
    public List <String> location = new ArrayList<>();
    public List <String> hits = new ArrayList<>();
    public String name;
    public int length;
    public boolean destroyed = false;

    public void SetShip (String givenName, int givenLength) { name = givenName; length = givenLength; }

    public void SetShipLocation (List <String> givenLocation) {
        location = givenLocation;
        printInfo();
    }

    public List<String> GetShipLocation () { return location; }

    public void printInfo() {
        System.out.println();
        System.out.println("~-~-~ Debug ship information start ~-~-~");
        System.out.println("Ship name: " + name + " | Ship length: " + length + " | Is ship destroyed?: " + destroyed);
        System.out.println("Ship location: " + location);
        System.out.println("Ship hits: " + hits);
        System.out.println("~-~-~ Debug ship information end ~-~-~");
        System.out.println();
    }

    public boolean SetHits (String hitLocation) {
        if (location.size() == 0) {
            destroyed = true;
        } else {
            if (location.size() == 1) destroyed = true;

            for (int i = 0; i < location.size(); i++) {
                if (hitLocation.equals(location.get(i))) {
                    location.remove(i);
                    hits.add(hitLocation);
                }
            }
        }

        printInfo();
        return destroyed;
    }
}