package com.codecool.klondike;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.scene.control.Button;




public class Klondike extends Application {

    private static final double WINDOW_WIDTH = 1400;
    private static final double WINDOW_HEIGHT = 900;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Card.loadCardImages();
        Game game = new Game();
        game.setTableBackground(new Image("/table/green.png"));

        primaryStage.setTitle("Klondike Solitaire");
        Scene scene = new Scene(game, WINDOW_WIDTH, WINDOW_HEIGHT);
        primaryStage.setScene(scene);
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.UP && game.canBeAutoCompleted()) {
                game.autoComplete();
            }
        });
        primaryStage.show();
        createButtons(game);
    }

    public  void createButtons(Game game) {
        Button quitGameButton = new Button("Quit Game");
        game.getChildren().add( quitGameButton);
        quitGameButton.setLayoutX( 475 );
        quitGameButton.setLayoutY( 30 );
        quitGameButton.setStyle("-fx-font: 15 arial; -fx-base: red;");

        quitGameButton.setOnAction((event) -> {
           game.quitGame();
        });

        Button newGameButton = new Button( "New Game" );
        game.getChildren().add( newGameButton );
        newGameButton.setLayoutX( 475 );
        newGameButton.setLayoutY( 80 );

        newGameButton.setStyle("-fx-font: 15 arial; -fx-base: blue;");
        newGameButton.setOnAction((event) -> {
            game.newGame();
            createButtons(game);
        });
    }
}
