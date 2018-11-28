package com.codecool.klondike;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;


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
        primaryStage.setScene(new Scene(game, WINDOW_WIDTH, WINDOW_HEIGHT));
        primaryStage.show();

        createButtons(game);
    }

    private  void createButtons(Game game) {
        Button newGameButton = new Button( "New Game" );
        game.getChildren().add( newGameButton );
        newGameButton.setLayoutX( 10 );
        newGameButton.setLayoutY( 850 );

        newGameButton.setOnAction((event) -> {

            game.newGame();
            createButtons(game);
        });
    }

}
