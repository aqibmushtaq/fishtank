package miniproject;

import java.awt.Dimension;
import java.awt.Toolkit;

/**
 *
 * @author am303
 */
public interface FishTankProperties {
    public final String APPNAME = "Aq(ib)uarium";
    public final String VERSION = "1.0.01";
    public final String DEVELOPER = "Aqib Mushtaq";

    public final String[] SAVEFILEHEADERS = new String[] {"fishtank", "datecreated", "datelastsaved"};
    public final String FISHFILEREGEX  = "((([a-zA-Z][:]([\\\\]{1})){1}([\\w- ]{1,}[\\\\]{1}){0,})|([/]{1}([\\w- ]{1,}[/]){0,}))([\\w- ]{1,}.fish){1}";

    public final Dimension SCRNSIZE = (Toolkit.getDefaultToolkit()).getScreenSize(); // Get the current screen size
    public final int STATUSBARHEIGHT = 20;
    public final int HUDHEIGHT = 60;
    public final int CONTROLSHEIGHT = HUDHEIGHT + STATUSBARHEIGHT;

    public final int TANKHEIGHT = (SCRNSIZE.height > 768) ? 768 - HUDHEIGHT : SCRNSIZE.height - CONTROLSHEIGHT;
}
