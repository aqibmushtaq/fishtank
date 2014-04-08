package miniproject;

import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 *
 * @author Aqib
 */
public class FishRed extends Fish {

    public FishRed (String name, boolean agg, int age) {
        super.setName(name);
        super.setAgg(agg);
        super.setAge(age);

        super.setType((byte)3);
        super.setMouthPos(5);

        try {
            setImg1(ImageIO.read(new File("img/fish_red_right.png")));
            setImg2(ImageIO.read(new File("img/fish_red_left.png")));
        } catch (IOException ex) {
            System.out.println("Images of the fish where not found.");
        }

        super.initFish();
    }
}