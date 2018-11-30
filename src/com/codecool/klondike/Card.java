package com.codecool.klondike;

import javafx.collections.FXCollections;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

import java.util.*;

public class Card extends ImageView {

    private Suit suit;
    private Rank rank;
    private boolean faceDown;

    private Image backFace;
    private Image frontFace;
    private Pile containingPile;
    private DropShadow dropShadow;

    static Image cardBackImage;
    private static final Map<String, Image> cardFaceImages = new HashMap<>();
    public static final int WIDTH = 150;
    public static final int HEIGHT = 215;

    public Card(Suit suit, Rank rank, boolean faceDown) {
        this.suit = suit;
        this.rank = rank;
        this.faceDown = faceDown;
        this.dropShadow = new DropShadow(2, Color.gray(0, 0.75));
        backFace = cardBackImage;
        frontFace = cardFaceImages.get(getShortName());
        setImage(faceDown ? backFace : frontFace);
        setEffect(dropShadow);
    }

    public Suit getSuit() {
        return suit;
    }

    public Rank getRank() {
        return rank;
    }

    public boolean isFaceDown() {
        return faceDown;
    }

    public String getShortName() {
        return "S " + suit.toString() + " R " + rank.toString();
    }

    public DropShadow getDropShadow() {
        return dropShadow;
    }

    public Pile getContainingPile() {
        return containingPile;
    }

    public void setContainingPile(Pile containingPile) {
        this.containingPile = containingPile;
    }

    public void moveToPile(Pile destPile) {
        this.getContainingPile().getCards().remove(this);
        destPile.addCard(this);
    }

    public void flip() {
        faceDown = !faceDown;
        setImage(faceDown ? backFace : frontFace);
    }

    @Override
    public String toString() {
        return "The Rank " + rank + " of Suit " + suit;
    }

    public static boolean isOppositeColor(Card card1, Card card2) {
        boolean result = false;

        if (card1.suit == Suit.HEARTS || card1.suit == Suit.DIAMONDS) {
            if (card2.suit == Suit.SPADES || card2.suit == Suit.CLUBS) {
                result = true;
            }
        } else {
            if (card2.suit == Suit.HEARTS || card2.suit == Suit.DIAMONDS) {
                result = true;
            }
        }

        return result;
    }

    public static boolean isSameSuit(Card card1, Card card2) {
        return card1.getSuit() == card2.getSuit();
    }

    public static List<Card> createNewDeck() {
        List<Card> result = new ArrayList<>();
        for (Suit suit: Suit.values()) {
            for (Rank rank: Rank.values()) {
                result.add(new Card(suit, rank, true));
            }
        }
        return result;
    }

    public static void loadCardImages() {
        cardBackImage = new Image("card_images/back.png");
        String suitName = "";
        for (Suit suit: Suit.values()) {
            suitName = suit.toString();

            for (Rank rank: Rank.values()) {
                String cardName = suitName + rank.value;
                String cardId = "S " + suit.toString() + " R " + rank.toString();
                String imageFileName = "card_images/" + cardName + ".png";
                cardFaceImages.put(cardId, new Image(imageFileName));
            }
        }
    }

    enum Suit {
        HEARTS, DIAMONDS, SPADES, CLUBS;

        @Override
        public String toString() {
            return name().toLowerCase();
        }

    }

    enum Rank {
        ACE(1), TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7), EIGHT(8), NINE(9), TEN(10), JACK(11), QUEEN(12), KING(13);
        private int value;

        Rank(int value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return name().toLowerCase();
        }

        //TODO: useful method to add: public boolean follows(Rank other, Pile pileType)

        public int getValue() {
            return this.value;
        }

    }

    public List<Card> getCardsOnTop() {
        Pile pile = this.getContainingPile();
        int cardIndex = pile.getCards().indexOf(this);
        List<Card> topCards = FXCollections.observableArrayList();
        for (int i = cardIndex; i < pile.numOfCards(); i++) {
            topCards.add(pile.getCards().get(i));
        }
        return topCards;
    }

}
