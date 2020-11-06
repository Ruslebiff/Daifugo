package client;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ClientMain {
    public static void main(String[] args) {
        System.out.println("Hello, I'm the client!");

        // eksempel på loading av bilde fra resources:
        try {
            BufferedImage imageExample = ImageIO.read(
                    ClientMain.class.getResourceAsStream("/cardimages/C2.png")
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        GameLobby lobby = new GameLobby();

    }
}
