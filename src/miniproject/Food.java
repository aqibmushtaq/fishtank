package miniproject;

import java.io.File;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.Random;
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class Food implements FishTankProperties {

    private int posX;
    private int posY;
    private int velocity;

    private BufferedImage img;

    public Food (int posX) {
        this.posX = posX;
        this.posY = 0;
        
        Random r = new Random();
        velocity  = r.nextInt(2);
        while (velocity == 0) {
            velocity = r.nextInt(2);
        }
        
        try {
            this.img = ImageIO.read(new File("img/fish_food.png"));
        } catch (IOException ex) {
            System.out.println("Fish food image not found.");
        }        
    }

    public BufferedImage getImg () { return img; }
    public int getPosX () { return posX; }
    public int getPosY () { return posY; }

    public void move () {
        if (posY < TANKHEIGHT - HUDHEIGHT) {     //while the food hasn't touched the ground, it will remain falling
            posY += velocity;
            if (posY > 10) {
                posY += (int) ((1/posY)*velocity);
            }

            /*if (posX < 0) {
                posX++;
            } else if (posX > scrnsize.width - getImg().getWidth()) {
                posX--;
            } else {
                double r = (new Random()).nextDouble();
                this.posX = (r > 0.5 ? posX + 1 : posX - 1);
            }*/
        }
    }

    public JLabel displayFoodFirst () {
        JLabel foodImage = new JLabel();
        foodImage.setIcon(new ImageIcon(getImg()));
        return foodImage;
    }

    public synchronized void eatFood () {
        
    }
}