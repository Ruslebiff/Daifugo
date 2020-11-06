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
    int cardNumb = 19;
    int boundsX = 500 - cardWidth + 15;
    int placementNumber = 20;
    int cardsOnDisplay = 0;


    public Player(String name, int playerID, String role, ArrayList<Card> cards) {
        this.name = name;
        this.playerID = playerID;
        this.role = role;
        this.hand = cards;
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
        cardsOnDisplay++;
        rearrangeCardsOnDisplay();
    }

    public void removeCardFromDisplay() {
        cardsOnDisplay--;
        System.out.println(cardsOnDisplay);
        Card temp = hand.get(++cardNumb);
        this.remove(temp);
        rearrangeCardsOnDisplay();
    }

    public void rearrangeCardsOnDisplay() {
        boundsX = 250 - (cardWidth/4);
        for (int i = 0; i < cardsOnDisplay; i++) {  // Fjerne alle eksisterende fra bordet
            Card temp = hand.get(cardNumb++);
            this.remove(temp);
            placementNumber += 10;
        }
        boundsX += placementNumber;

        for (int i = 0; i < cardsOnDisplay; i++) {
            if(i == 0) {
                placementNumber = 20;
            } else placementNumber = 10;
            Card temp = hand.get(cardNumb--);
            temp.setBounds(boundsX -= (2*placementNumber),0, cardWidth,cardHeight);
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