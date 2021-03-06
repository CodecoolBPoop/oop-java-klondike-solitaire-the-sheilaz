package com.codecool.klondike;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import java.util.Optional;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Game extends Pane {

    private List<Card> deck = new ArrayList<>();

    private Pile stockPile;
    private Pile discardPile;
    private List<Pile> foundationPiles = FXCollections.observableArrayList();
    private List<Pile> tableauPiles = FXCollections.observableArrayList();

    private double dragStartX, dragStartY;
    private List<Card> draggedCards = FXCollections.observableArrayList();

    private static double STOCK_GAP = 1;
    private static double FOUNDATION_GAP = 0;
    private static double TABLEAU_GAP = 30;


    private EventHandler<MouseEvent> onMouseClickedHandler = e -> {
        Card card = (Card) e.getSource();
        Pile containingPile = card.getContainingPile();
        if (e.getClickCount() == 2 && !e.isConsumed()) {
            putToFoundation(card);
            e.consume();
        }
        if (containingPile.getPileType() == Pile.PileType.STOCK && card == containingPile.getTopCard()) {
            card.moveToPile(discardPile);
            card.flip();
            card.setMouseTransparent(false);
            System.out.println("Placed " + card + " to the waste.");
        } else if (containingPile.getPileType() == Pile.PileType.TABLEAU && card.isFaceDown() && card == containingPile.getTopCard()) {
            card.flip();
        }

    };

    private EventHandler<MouseEvent> stockReverseCardsHandler = e -> {
        refillStockFromDiscard();
    };

    private EventHandler<MouseEvent> onMousePressedHandler = e -> {
        dragStartX = e.getSceneX();
        dragStartY = e.getSceneY();
    };

    private EventHandler<MouseEvent> onMouseDraggedHandler = e -> {
        Card card = (Card) e.getSource();
        Pile activePile = card.getContainingPile();
        if (activePile.getPileType() == Pile.PileType.STOCK ||
                (activePile.getPileType() == Pile.PileType.DISCARD && card != activePile.getTopCard()) ||
                (activePile.getPileType() == Pile.PileType.TABLEAU && card.isFaceDown()))
            return;
        double offsetX = e.getSceneX() - dragStartX;
        double offsetY = e.getSceneY() - dragStartY;

        draggedCards.clear();
        List<Card> cardsList = card.getCardsOnTop();

        if(activePile.getPileType() == Pile.PileType.TABLEAU && !card.equals(activePile.getTopCard())){
            for (int i = 0; i < cardsList.size(); i++) {
                Card currentCard = cardsList.get(i);
                draggedCards.add(currentCard);
                currentCard.getDropShadow().setRadius(20);
                currentCard.getDropShadow().setOffsetX(10);
                currentCard.getDropShadow().setOffsetY(10);

                currentCard.toFront();
                currentCard.setTranslateX(offsetX);
                currentCard.setTranslateY(offsetY);
            }
        } else {

            draggedCards.add(card);

            card.getDropShadow().setRadius(20);
            card.getDropShadow().setOffsetX(10);
            card.getDropShadow().setOffsetY(10);

            card.toFront();
            card.setTranslateX(offsetX);
            card.setTranslateY(offsetY);
        }
    };

    private EventHandler<MouseEvent> onMouseReleasedHandler = e -> {
        if (draggedCards.isEmpty())
            return;
        Card card = (Card) e.getSource();
        List<Pile> pilesToDragTo = FXCollections.concat( (ObservableList<Pile>) tableauPiles, (ObservableList<Pile>) foundationPiles);
        Pile pile = getValidIntersectingPile(card, pilesToDragTo);
        if (pile != null) {
            handleValidMove(card, pile);
            if (isGameWon()) {
                displayAlert();
            }
        } else {
            draggedCards.forEach(MouseUtil::slideBack);
            draggedCards.clear();
        }
    };

    public void autoComplete() {
        List<Pile> pilesToCheck = FXCollections.concat((ObservableList<Pile>) tableauPiles, (ObservableList<Pile>) asList(discardPile));
        for (Pile origPile: pilesToCheck) {
            if (origPile.isEmpty()) {
                if (isAutoGameWon()) {
                    displayAlert();
                }
                continue;
            }
            Card card = origPile.getTopCard();
            putToFoundation(card);
            // TODO: thread should wait until animation finishes!
        }
    }

    private void putToFoundation(Card card) {
        List<Card> cardAsList = asList(card);
        for (Pile foundationPile: foundationPiles) {
            if (foundationPile.isEmpty()) {
                if (card.getRank() == Card.Rank.ACE) {
                    MouseUtil.slideToDest(cardAsList, foundationPile);
                }
                continue;
            }
            if (foundationPile.getTopCard().getSuit() == card.getSuit() &&
                    foundationPile.getTopCard().getRank().getValue() + 1 == card.getRank().getValue()) {
                MouseUtil.slideToDest(cardAsList, foundationPile);
                break;
            }
        }
    }

    private List<Card> asList(Card object) {
        List<Card> cardAsList = FXCollections.observableArrayList();
        cardAsList.add(object);
        return cardAsList;
    }

    private List<Pile> asList(Pile object) {
        List<Pile> cardAsList = FXCollections.observableArrayList();
        cardAsList.add(object);
        return cardAsList;
    }

    public boolean isGameWon() {
        int completedPiles = 0;
        int lastPile = 0;
        for (Pile pile : foundationPiles) {
            if (pile.numOfCards() == 13) {
                completedPiles++;
            } else if (pile.numOfCards() == 12) {
                lastPile++;
            }
        }
        return (completedPiles == 3 && lastPile == 1);
    }

    public boolean isAutoGameWon() {
        int completePiles = 0;
        for (Pile pile : foundationPiles) {
            if (pile.numOfCards() == 13) {
                completePiles++;
            }
        }
        return (completePiles == 4);
    }

    public void displayAlert() {
        clearStage();

        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setHeaderText("Congratulations, you won!");
        alert.setContentText("Start a new game?");

        ButtonType buttonTypeOne = new ButtonType("Yes");
        ButtonType buttonTypeTwo = new ButtonType("No");

        alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo);

      Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == buttonTypeOne) {
            newGame();
        } else if (result.isPresent() && result.get() == buttonTypeTwo) {
            quitGame();
        }
    }

    public  void createButtons( ) {
        Button quitGameButton = new Button();
        getChildren().add( quitGameButton);

        quitGameButton.setLayoutX( 10 );
        quitGameButton.setLayoutY( 10 );
        quitGameButton.setPrefSize( 50, 50 );
        quitGameButton.setStyle("-fx-background-image: url('buttons/redx.png')");

        quitGameButton.setOnAction((event) -> {
            quitGame();
        });

        Button newGameButton = new Button();
        getChildren().add( newGameButton );

        newGameButton.setLayoutX( 10 );
        newGameButton.setLayoutY( 110 );
        newGameButton.setPrefSize( 50, 50 );
        newGameButton.setStyle("-fx-background-image: url('buttons/restart.jpg')");

        newGameButton.setOnAction((event) -> {
            newGame();
        });
    }

    public void newGame() {
        clearStage();
        createButtons();
        deck = Card.createNewDeck();
        Collections.shuffle(deck);
        initPiles();
        dealCards();
    }

    public void quitGame() {
        Platform.exit();
    }

    public void clearStage() {
        deck.clear();
        foundationPiles.clear();
        tableauPiles.clear();
        stockPile.clear();
        discardPile.clear();
        getChildren().clear();

    }

    public Game() {
        createButtons();
        deck = Card.createNewDeck();
        Collections.shuffle(deck);
        initPiles();
        dealCards();
    }

    public void addMouseEventHandlers(Card card) {
        card.setOnMousePressed(onMousePressedHandler);
        card.setOnMouseDragged(onMouseDraggedHandler);
        card.setOnMouseReleased(onMouseReleasedHandler);
        card.setOnMouseClicked(onMouseClickedHandler);
    }

    public void refillStockFromDiscard() {
        stockPile.clear();
        for (int i = discardPile.numOfCards()-1 ; i > -1; i--) {
            Card currentCard = discardPile.getCards().get(i);
            currentCard.flip();
            stockPile.addCard(currentCard);
        }
        discardPile.clear();
        System.out.println("Stock refilled from discard pile.");
    }

    public boolean isMoveValid(Card card, Pile destPile) {
        boolean isValid = false;
        Card destTopCard = destPile.getTopCard();
        if (destPile.getPileType() == Pile.PileType.FOUNDATION) {
            if (destPile.isEmpty()) {
                if (card.getRank() == Card.Rank.ACE) {
                    isValid = true;
                }
            } else {
                if (Card.isSameSuit(destTopCard, card) &&
                        destTopCard.getRank().getValue() + 1 == card.getRank().getValue()) {
                    isValid = true;
                }
            }
        } else if (destPile.getPileType() == Pile.PileType.TABLEAU) {
            if (destPile.isEmpty()) {
                if (card.getRank() == Card.Rank.KING) {
                    isValid = true;
                }
            } else {
                if (Card.isOppositeColor(card, destTopCard) && destTopCard.getRank().getValue() - 1 == card.getRank().getValue()) {
                    isValid = true;
                }
            }
        }
        return isValid;
    }
    private Pile getValidIntersectingPile(Card card, List<Pile> piles) {
        Pile result = null;
        for (Pile pile : piles) {
            if (!pile.equals(card.getContainingPile()) &&
                    isOverPile(card, pile) &&
                    isMoveValid(card, pile))
                result = pile;
        }
        return result;
    }

    private boolean isOverPile(Card card, Pile pile) {
        if (pile.isEmpty())
            return card.getBoundsInParent().intersects(pile.getBoundsInParent());
        else
            return card.getBoundsInParent().intersects(pile.getTopCard().getBoundsInParent());
    }

    private void handleValidMove(Card card, Pile destPile) {
        String msg = null;
        if (destPile.isEmpty()) {
            if (destPile.getPileType().equals(Pile.PileType.FOUNDATION))
                msg = String.format("Placed %s to the foundation.", card);
            if (destPile.getPileType().equals(Pile.PileType.TABLEAU))
                msg = String.format("Placed %s to a new pile.", card);
        } else {
            msg = String.format("Placed %s to %s.", card, destPile.getTopCard());
        }
        System.out.println(msg);
        MouseUtil.slideToDest(draggedCards, destPile);
        draggedCards.clear();
    }


    private void initPiles() {
        stockPile = new Pile(Pile.PileType.STOCK, "Stock", STOCK_GAP);
        stockPile.setBlurredBackground();
        stockPile.setLayoutX(95);
        stockPile.setLayoutY(20);
        stockPile.setOnMouseClicked(stockReverseCardsHandler);
        getChildren().add(stockPile);

        discardPile = new Pile(Pile.PileType.DISCARD, "Discard", STOCK_GAP);
        discardPile.setBlurredBackground();
        discardPile.setLayoutX(285);
        discardPile.setLayoutY(20);
        getChildren().add(discardPile);

        for (int i = 0; i < 4; i++) {
            Pile foundationPile = new Pile(Pile.PileType.FOUNDATION, "Foundation " + i, FOUNDATION_GAP);
            foundationPile.setBlurredBackground();
            foundationPile.setLayoutX(610 + i * 180);
            foundationPile.setLayoutY(20);
            foundationPiles.add(foundationPile);
            getChildren().add(foundationPile);
        }
        for (int i = 0; i < 7; i++) {
            Pile tableauPile = new Pile(Pile.PileType.TABLEAU, "Tableau " + i, TABLEAU_GAP);
            tableauPile.setBlurredBackground();
            tableauPile.setLayoutX(95 + i * 180);
            tableauPile.setLayoutY(275);
            tableauPiles.add(tableauPile);
            getChildren().add(tableauPile);
        }
    }

    public void dealCards() {
        Iterator<Card> deckIterator = deck.iterator();
        Iterator<Pile> tableauPileIterator = tableauPiles.iterator();
        int numberOfCards = 1;
        while (tableauPileIterator.hasNext()){
            Pile currentPile = tableauPileIterator.next();
            for (int i = 0; i < numberOfCards; i++) {
                Card currentCard = deckIterator.next();
                currentPile.addCard(currentCard);
                addMouseEventHandlers(currentCard);
                getChildren().add(currentCard);
            }
            currentPile.getTopCard().flip();
            numberOfCards++;
        }
        deckIterator.forEachRemaining(card -> {
            stockPile.addCard(card);
            addMouseEventHandlers(card);
            getChildren().add(card);
        });
    }

    public void setTableBackground(Image tableBackground) {
        setBackground(new Background(new BackgroundImage(tableBackground,
                BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
                BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
    }

}
