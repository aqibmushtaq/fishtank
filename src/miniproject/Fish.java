package miniproject;

import java.awt.image.BufferedImage;
import java.util.Random;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class Fish implements FishTankProperties {


    private int status;     // 0 - playing, 1 - paused, 2 - racing (select mode), 3 - racing

    private String name;
    private long age;
    private byte type;
    private boolean aggressive;
    private byte hunger;        // 1 < hunger < 10
    private byte hVelocity;      // -5 < velocity < 5
    private byte vVelocity;      // -5 < velocity < 5

    private boolean turnedToFood;

    private int posX;
    private int posY;

    private BufferedImage img1;
    private BufferedImage img2;
    private int mouthPos;  //this will be set in the sub classes for each fish

    public Fish () {

    }

    public void initFish () {
        double r = (new Random()).nextDouble();
        this.hVelocity = (byte) (r > 0.5 ? (aggressive?-2:-1) : (aggressive?2:1));
        r = (new Random()).nextDouble();
        this.vVelocity = ((new Random()).nextBoolean() == true) ? (byte) 1 : -1;
        this.posX = (this.hVelocity > 0 ? (0) : (SCRNSIZE.width - img1.getWidth(null)));
        this.posY = 20 + (new Random()).nextInt(SCRNSIZE.height - 100 - img1.getHeight(null) - 20);
        
        this.turnedToFood = false;
        this.hunger = (byte) 10;

        this.status = 0;
    }

    public int getStatus () { return status; }
    public String getName () { return name; }
    public long getAge () { return age; }
    public byte getType ()  { return this.type; }
    public boolean getAgg () { return aggressive; }
    public byte getHunger () { return hunger; }
    public byte getHVelocity () {
        if (hVelocity > 0) {
            return (byte)(hVelocity + (hunger/4));
        } else {
            return (byte)(hVelocity - (hunger/4));
        }
    }
    public BufferedImage getImg1 () { return img1; }
    public BufferedImage getImg2 () { return img2; }
    public int getPosX () { return this.posX; }
    public int getPosY () { return this.posY; }

    public void setStatus (int status) { this.status = status; }
    public void setName (String name) { this.name = name; }
    public void setAge (long age) { this.age = age; }
    public void setType (byte type) { this.type = type; }
    public void setAgg (boolean agg) { this.aggressive = agg; }
    public void setHunger (byte hunger) { this.hunger = hunger; }
    public void setHVelocity (byte hVelocity) { this.hVelocity = hVelocity; }
    public void setVVelocity (byte vVelocity) { this.vVelocity = vVelocity; }
    public void setImg1 (BufferedImage img1) { this.img1 = img1; }
    public void setImg2 (BufferedImage img2) { this.img2 = img2; }
    public void setPosX (int posX) { this.posX = posX; }
    public void setPosY (int posY) { this.posY = posY; }
    public void setMouthPos (int pos) { this.mouthPos = pos; }

    public boolean moveTowardsFood (int x, int y) {
        //Calculate the difference in coordinates between the fish and its food
        int dx;
        if (hVelocity > 0) {
            dx = this.posX - x + getImg1().getWidth();
        } else {
            dx = this.posX - x;
        }
        int dy = this.posY - y + mouthPos;

        //once the fish reaches the food, reset its hunger and return true so the fishtank can delete the peice of food
        if (dx <= 10 && dx >= -10 && dy <= 10 && dy >= -10) {
            hunger = 10;
            return true;
        }

        if (!(dx < 9 && dx > -9)) {
            if (dx > 0) {
                this.posX += -4;
                dx += -4;
            } else {
                this.posX += 4;
                dx += 4;
            }
        }
        if (!(dy < 9 && dy > -9)) {
            if (dy > 0) {
                this.posY += -4;
                dy += -4;
            } else {
                this.posY += 4;
                dy += 4;
            }//once the fish reaches the food, reset its hunger and return true so the fishtank can delete the peice of food
        if (dx <= 10 && dx >= -10 && dy <= 10 && dy >= -10) {
            hunger = 10;
            return true;
        }

        }
        
        //if the fish has already turned then it doesnt need to turn again
        if (!this.turnedToFood) {
            //if the food is in the opposite direction of velocity, get turn the fish around
            if ((dx > 0 && hVelocity > 0) || (dx < 0 && hVelocity < 0)) {
                hVelocity = (byte) (hVelocity * (-1));
                this.turnedToFood = true;
            }
        }

        return false;
    }

    public void resetTurnForFood () {
        this.turnedToFood = false;
    }

    public void move () {

        if (this.hVelocity > 0) {
            if (this.posX > (SCRNSIZE.width - img1.getWidth())) {
                hVelocity = (byte) (hVelocity * (-1));
            } else {
                this.posX += (byte) (hVelocity + (hunger/4));
            }
        } else {
            if (this.posX < 0) {
                hVelocity = (byte) (hVelocity * (-1));
            } else {
                this.posX += (byte) (hVelocity - (hunger/4));
            }
        }

        if (this.vVelocity > 0) {
            if (this.posY > (TANKHEIGHT - img1.getHeight() )) {
                vVelocity = (byte) (vVelocity * (-1));
            } else {
                this.posY += (byte) (vVelocity + (hunger/4));
            }
        } else {
            if (this.posY < 0) {
                vVelocity = (byte) (vVelocity * (-1));
            } else {
                this.posY += (byte) (vVelocity - (hunger/4));
            }
        }
        
        
        Random r = new Random();
        double n = r.nextDouble();
        double turnN = 0.998;

        //if the fish is on the top half of the screen, bring it down randomly and vice versa if it is at the bottom
        if (n > turnN) {
            if (this.posY < TANKHEIGHT/2 && this.vVelocity < 0) {
                this.vVelocity *= -1;
            } else if (this.posY > TANKHEIGHT/2 && this.vVelocity > 0) {
                this.vVelocity *= -1;
            }
        }
    }

    public void moveRace (int y) {

    }

    public void decreaseHunger () {
        this.hunger = (byte) (this.hunger - 1);
    }

    public Icon displayFish()
    {
        Icon img = new ImageIcon(getHVelocity() > 0 ? getImg1() : getImg2());
        return img;
    }

    public JLabel displayFishFirst () {
        JLabel fishImage = new JLabel();
        fishImage.setIcon(new ImageIcon(getHVelocity() > 0 ? getImg1() : getImg2()));
        return fishImage;
    }
}