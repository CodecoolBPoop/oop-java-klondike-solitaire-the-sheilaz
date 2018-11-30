package com.codecool.klondike;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Label;
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
        game.setTableBackground(new Image("/table/orangeish.png"));

        primaryStage.setTitle("The Sheilaz Solitaire");
        Scene scene = new Scene(game, WINDOW_WIDTH, WINDOW_HEIGHT);
        primaryStage.setScene(scene);
        scene.setOnKeyPressed(event -> { // TODO: javaFX timeline could be a solution
            if (event.getCode() == KeyCode.UP) {
                game.autoComplete();
            }
        });
        primaryStage.show();
    }
}
