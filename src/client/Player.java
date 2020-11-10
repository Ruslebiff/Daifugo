package client;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Player extends JPanel {
    private BufferedImage image;
    private final String filePath;
    private final String name;
    private final int playerID;
    private final String role;
    private final ArrayList<Card> hand;
    private JButton removeCard;
    private JButton addCard;
    private final int cardWidth = 80;
    private final int cardHeight = 120;
    private int widthOfComponent = 500;
    int cardNumb = 18;
    int boundsX = 0;
    int cardsOnDisplay = 0;
    int internalMargin = 5;
    private int space = 24; // Space between cards when a player has maximum cards
    private final int maxCards = 18;


    public Player(String name, int playerID, String role, ArrayList<Card> cards, int width) {
        this.name = name;
        this.playerID = playerID;
        this.role = role;
        this.hand = cards;
        this.widthOfComponent = width;
        sortHand(); // Sorts the players hand with respect to the game rules

        setLayout(null);
        setOpaque(true);
        this.filePath = "./resources/green_fabric.jpg"; // Filepath
        try {
            image = ImageIO.read(new File(filePath));       // Read the image
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        addCard = new JButton("Add");
        addCard.setBounds(0,125,100,50);
        add(addCard);
        addCard.addActionListener( e -> {
            addCardToDisplay();
        });

        removeCard = new JButton("Remove");
        removeCard.setBounds(100,125,100,50);
        add(removeCard);
        removeCard.addActionListener(e -> {
            removeCardFromDisplay();
        });

        setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));  // Create border
    }

    public void addCardToDisplay() {
//        cardsOnDisplay++;
//        rearrangeCardsOnDisplay();
        addAll18();
    }

    public void removeCardFromDisplay() {
        cardsOnDisplay--;
        System.out.println(cardNumb);
        Card temp = hand.get(++cardNumb);
        this.remove(temp);
        rearrangeCardsOnDisplay();
    }

    public void addAll18() {
        boundsX = (widthOfComponent) - (cardWidth) - internalMargin;
        int bort = 0;
        for (int i = 0; i < 18; i++) {
            Card temp = hand.get(cardNumb--);
            bort = i*space;
            temp.setBounds(boundsX - bort, 0, cardWidth, cardHeight);
            add(temp);
        }
        repaint();
        cardsOnDisplay = 18;
    }

    public void rearrangeCardsOnDisplay() {
        int maxWidthOfCards = (int) (widthOfComponent*1.5);
        for (int i = 0; i < cardsOnDisplay; i++) {  // Fjerne alle eksisterende fra bordet
            Card temp = hand.get(cardNumb++);
            System.out.println(cardNumb);
            this.remove(temp);
        }

//        int spread = (maxWidthOfCards/(cardsOnDisplay+1))*(1+(1/(cardsOnDisplay + 10)));
//        boundsX = (widthOfComponent/2) - ((spread * cardsOnDisplay)/2) + (widthOfComponent/2) - (cardWidth/2);
////        boundsX = widthOfComponent - cardWidth/2;
//        System.out.println("Bounds x "  + boundsX);
//        for (int i = 0; i < cardsOnDisplay; i++) {
//            Card temp = hand.get(cardNumb--);
//            temp.setBounds(boundsX -= spread, 0, cardWidth, cardHeight);
//            this.add(temp);
////            System.out.println("Added " + temp.getSuit() + Integer.toString(temp.getNumber()));
//        }


        space = space + ((maxCards - cardsOnDisplay)/2);
        boundsX = (widthOfComponent/2)  - (cardWidth/2) + (((cardsOnDisplay-1)/2) * space);
        for (int i = 0; i < cardsOnDisplay; i++) {
            Card temp = hand.get(cardNumb--);
            temp.setBounds(boundsX - (i*space), 0, cardWidth, cardHeight);
            this.add(temp);
        }
        repaint();
    }

    
    // Sorts the players hand by using a quicksort
    public void sortHand() {
        QuickSort.sort(this.hand, 0, this.hand.size() - 1);
    }

    public String getName(){
        return name;
    }

    public String getRole() {
        return role;
    }

    public String getPlayedID() {
        return Integer.toString(this.playerID);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, this); // Draws the image of onto the JPanel
    }
}

//    setLayout(new GridBagLayout()); // The row where the cards will show
//        GridBagConstraints gbc = new GridBagConstraints();  // Constraints of the grid
//        gbc.gridwidth = 1;   // Set the width of the cell of the cards
//        gbc.fill = GridBagConstraints.BOTH;  // Make the component fill out the cell
//        gbc.gridx = 0;  // Where the first component will be placed
//        gbc.gridy = 0;
//        gbc.weightx = 1;
//        gbc.weighty = 1;
//        gbc.anchor = GridBagConstraints.CENTER;


//        sortHand();     // HER SKAL DET EGT VÆRE hand.size()
//        for (int i = 0; i < 1; i++) {
////            if(i == (hand.size() - 1)) {
////                gbc.weightx = 5;
////            }
//            this.add(hand.get(i), gbc);     // Add each card to the hand
//            gbc.gridx += i;                 // Set where the card should be set
//            System.out.println("BREDDE PÅ ETT KORT " + hand.get(i).getBounds().width);
//        }
//
//        gbc.gridx = 0;
//        gbc.gridy = 2;
//        gbc.weightx = 2;
//        add(addCard);
//        gbc.gridx += 1;
//        add(removeCard);