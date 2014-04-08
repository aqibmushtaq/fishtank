package miniproject;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.JLabel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import javax.swing.filechooser.*;
import java.util.ArrayList;

public class FishTank implements FishTankProperties {

    private ArrayList<Fish> f = new ArrayList<Fish>();
    private ArrayList<Thread> t = new ArrayList<Thread>();
    private ArrayList<Timer> hungerTimer = new ArrayList<Timer>();
    private ArrayList<Timer> ageTimer = new ArrayList<Timer>();

    private ArrayList<Food> food = new ArrayList<Food>();
    private ArrayList<Thread> tFood = new ArrayList<Thread>();

    private int fishStatus; //0 = running normally, 1 = paused, 2 = racing
    
    private JFrame frame;
    private JLayeredPane Tank;
    private JLabel TankBg;
    private JPanel Controls; //Container for HUD and StatusBar
    private JPanel HUD;
    private JPanel HUDInfo;
    private JPanel HUDControls;
    private JPanel StatusBar;
    //Fish Image Label/Container
    private ArrayList<JLabel> lblImages = new ArrayList<JLabel>();
    //Food Image Label/Container
    private ArrayList<JLabel> lblImagesFood = new ArrayList<JLabel>();
    //HUD - Current fish index
    private int HUDIndex;
    private Timer HUDUpdateTimer;
    //HUD Labels
    private JLabel lblFishName;
    private JLabel lblFishType;
    private JLabel lblFishAge;
    private JLabel lblFishAggressive;
    private JLabel lblFishHunger;
    private JLabel lblFishVelocity;
    //HUD Inputs
    private JTextField txtNewFishName;
    private JComboBox txtNewFishType;
    private JCheckBox txtNewFishAgg;
    private JButton btnCreateNewFish;
    private JButton btnAddFood;
    private boolean addFoodToggle;
    //Status Bar Labels
    private JLabel lblFoodShortage;
    private JLabel lblTotalAliveFish;
    private JLabel lblTotalDeadFish;
    private JLabel lblUserData;
    //Timer to update Status Bar
    private Timer StatusBarUpdater;


    private JFileChooser fileChooser;
    private File saveDataFile;  //store the last saved or opened file for easy access (when quick saving)
    private String[] fileDetails;

    public FishTank() {
        this.initFrame();
        //this.initHUD();
        this.initControls();
        this.initTank();

        this.addFoodToggle = false;

        frame.setVisible(true);
    }

    public final void initFrame() {
        frame = new JFrame("Fish Tank");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //set window size
        frame.setSize(SCRNSIZE.width, SCRNSIZE.height - 50);
        frame.setResizable(false);
        BorderLayout layout = new BorderLayout();
        frame.setLayout(layout);

        initMenu();
    }

    public final void initMenu () {
        //Create a menu bar
        JMenuBar frameMenuBar = new JMenuBar();

        //Create first menu in menubar
        JMenu mFile = new JMenu("File");

        //Create first menu in menubar
        JMenu mFish = new JMenu("Fish");

        //Create second menu in menubar
        JMenu mHelp = new JMenu("Help");

        //Create first menu item for first menu
        JMenuItem mFile_Open = new JMenuItem("Open");
        fileChooser = new JFileChooser();
        mFile_Open.addActionListener(new  ActionListener () {
            public void actionPerformed( ActionEvent event) {

                File dir = null;
                try {
                    dir = new File(".").getCanonicalFile();
                } catch (IOException ex) {
                    System.out.println("Unexpected Error.");
                }
                fileChooser.setCurrentDirectory(dir);
                fileChooser.addChoosableFileFilter(new TextFilter());
                fileChooser.setAcceptAllFileFilterUsed(false);

                int status = fileChooser.showDialog(frame, "Open File");

                try {
                    if ( status == 0 ) {
                        File file = fileChooser.getSelectedFile();
                        String extension = getExtension(file);
                        if(file.toString().matches(FISHFILEREGEX)) {
                        //if(extension.equals("fish")) {
                            //Correct file extension found.
                            boolean format = checkFileFormat(file);
                            if (format) {
                                System.out.println("Good format");

                                //Reset all the fishes (delete old ones)
                                for (int i = 0; i < f.size(); i++) {
                                    f.get(i).setStatus(-1);
                                    //t.get(i).stop();
                                    hungerTimer.get(i).stop();
                                    ageTimer.get(i).stop();
                                    lblImages.get(i).setBounds(0, 0, 0, 0);
                                }
                                f = new ArrayList<Fish>();
                                t = new ArrayList<Thread>();
                                hungerTimer = new ArrayList<Timer>();
                                ageTimer = new ArrayList<Timer>();
                                lblImages = new ArrayList<JLabel>();
                                //Finished cleaning up the fish tank for the new fishes

                                boolean loaded =  loadFile(file);

                                if (!loaded) {
                                    JOptionPane.showMessageDialog(null, "Error. No fishes in file..");
                                } else {
                                    saveDataFile = file;
                                }
                            } else {
                                System.out.println("BAD format");
                            }
                            
                        } else {
                            System.out.println("Invalid extension.");
                        }
                    } else if (status == 1) {
                        System.out.println("User cancelled open operation");
                    } else if (status == -1) {
                        System.out.println("An error occured");
                    }
                } catch (NullPointerException e) {
                    System.out.println("No file found.");
                }

            }
        });

        //Create first menu item for second menu
        JMenuItem mFile_Save = new JMenuItem("Save");
        saveDataFile = null;    //initialise to avoid NullPointerException
        mFile_Save.addActionListener(new  ActionListener () {
            public void actionPerformed( ActionEvent event) {
                if (saveDataFile != null) {
                    //save data file set, so automatically overwrite the current data file
                    try {
                        saveFile();
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(null, "File not found. Try again.");
                    }
                } else {
                    saveAsFile();
                }
            }
        });

        //Create second menu item for second menu
        JMenuItem mFile_SaveAs = new JMenuItem("Save As");
        mFile_SaveAs.addActionListener(new ActionListener() {
            public void actionPerformed (ActionEvent event) {
                saveAsFile ();
            }
        });

        JMenuItem mFish_Pause = new JMenuItem("Pause All Fish");
        mFish_Pause.addActionListener(new ActionListener() {
            public void actionPerformed (ActionEvent event) {
                pauseAllFish();
            }
        });

        JMenuItem mFish_Start = new JMenuItem("Start All Fish");
        mFish_Start.addActionListener(new ActionListener() {
            public void actionPerformed (ActionEvent event) {
                startAllFish();
            }
        });

        JMenuItem mFish_Hide = new JMenuItem("Hide All Fish");
        mFish_Hide.addActionListener(new ActionListener() {
            public void actionPerformed (ActionEvent event) {
                hideAllFish();
            }
        });
        JMenuItem mFish_Show = new JMenuItem("Show All Fish");
        mFish_Show.addActionListener(new ActionListener() {
            public void actionPerformed (ActionEvent event) {
                showAllFish();
            }
        });
        JMenuItem mFish_Race = new JMenuItem("Race Fishes");
        mFish_Race.addActionListener(new ActionListener() {
            public void actionPerformed (ActionEvent event) {
                pauseAllFish();
            }
        });
        JMenuItem mFish_RaceStart = new JMenuItem("Start Racing Fishes");
        mFish_RaceStart.addActionListener(new ActionListener() {
            public void actionPerformed (ActionEvent event) {
                for (int i = 0; i < f.size(); i++) {
                    if (f.get(i) != null) {
                        f.get(i).setStatus(3);
                    }
                }
            }
        });

        //Create first menu item for third menu
        JMenuItem mHelp_About = new JMenuItem("About");
        mHelp_About.addActionListener(new ActionListener() {
            public void actionPerformed (ActionEvent e) {
                JFrame about = new JFrame ("About");
                about.setLayout(new BorderLayout());
                about.setSize(300, 400);
                about.setLocation(SCRNSIZE.width/2 - 150, SCRNSIZE.height/2 - 200);
                about.setResizable(false);
                //about.setDefaultCloseOperation(about.EXIT_ON_CLOSE);
                about.setAlwaysOnTop(true);

                JPanel infoPanel = new JPanel (new GridLayout(3, 1));
                JLabel infoAppName = new JLabel(APPNAME);
                JLabel infoDeveloper = new JLabel("Developer: " + DEVELOPER);
                JLabel infoVersion = new JLabel("Application Version: v" + VERSION);
                infoAppName.setFont(new Font("Serif", Font.BOLD, 40));
                infoPanel.add(infoAppName);
                infoPanel.add(infoDeveloper);
                infoPanel.add(infoVersion);

                infoPanel.setPreferredSize(new Dimension(300, 200));
                
                about.add(infoPanel, BorderLayout.SOUTH);

                about.setVisible(true);
            }
        });

        //add first menu item into first menu
        mFile.add(mFile_Open);
        mFile.add(mFile_Save);
        mFile.add(mFile_SaveAs);

        //add fish items to fish menu
        mFish.add(mFish_Start);
        mFish.add(mFish_Pause);
        mFish.add(mFish_Hide);
        mFish.add(mFish_Show);
        mFish.add(mFish_Race);

        //add first menu item into second menu
        mHelp.add(mHelp_About);

        //add first menu into menu bar
        frameMenuBar.add(mFile);

        //add fish menu to menu bar
        frameMenuBar.add(mFish);

        //add second menu into menu bar
        frameMenuBar.add(mHelp);

        //Set menu bar for JFrame
        frame.setJMenuBar(frameMenuBar);
    }

    public final void initControls () {
        Controls = new JPanel(new BorderLayout());

        Controls.setPreferredSize(new Dimension(0, CONTROLSHEIGHT));

        initHUD();
        initStatusBar();

        frame.add(Controls, BorderLayout.SOUTH);
    }

    public final void initHUD () {
        HUD = new JPanel(new BorderLayout());
        //frame.add(HUD, BorderLayout.SOUTH);
        HUD.setBackground(Color.WHITE);
        HUD.setPreferredSize(new Dimension(0, HUDHEIGHT));

        GridLayout HUDInfoL = new GridLayout(2, 3, 30, 25);
        HUDInfo = new JPanel(HUDInfoL);
        HUDInfo.setPreferredSize(new Dimension((4 * SCRNSIZE.width) / 10, 0));
        //HUD.add(HUDInfo, BorderLayout.WEST);


        GridLayout HUDControlsL = new GridLayout(1, 4, 30, 0);
        HUDControls = new JPanel(HUDControlsL);
        HUDControls.setPreferredSize(new Dimension((6 * SCRNSIZE.width) / 10, 0));
        //HUD.add(HUDControls, BorderLayout.EAST);


        //HUDINFO
        lblFishName = new JLabel("Name: ");
        lblFishType = new JLabel("Type: ");
        lblFishAge = new JLabel("Age: ");
        lblFishAggressive = new JLabel("Aggressive: ");
        lblFishHunger = new JLabel("Hunger: ");
        lblFishVelocity = new JLabel("Velocity: ");

        //Add HUDInfo
        HUDInfo.add(lblFishName);
        HUDInfo.add(lblFishType);
        HUDInfo.add(lblFishAge);
        HUDInfo.add(lblFishAggressive);
        HUDInfo.add(lblFishHunger);
        HUDInfo.add(lblFishVelocity);

        //HUDCONTROLS
        JPanel newFishName = new JPanel();
        JLabel lblNewFishName = new JLabel("New Fish Name: ");
        txtNewFishName = new JTextField(10);
        newFishName.add(lblNewFishName);
        newFishName.add(txtNewFishName);

        JPanel newFishType = new JPanel();
        JLabel lblNewFishType = new JLabel("New Fish Type: ");
        txtNewFishType = new JComboBox(new String[]{"Nemo", "Tropical 1", "Tropical 2", "Red"});
        newFishType.add(lblNewFishType);
        newFishType.add(txtNewFishType);

        JPanel newFishAgg = new JPanel();
        JLabel lblNewFishAgg = new JLabel("New Fish Aggression: ");
        txtNewFishAgg = new JCheckBox(/*"New Fish Aggression", false*/);
        newFishAgg.add(lblNewFishAgg);
        newFishAgg.add(txtNewFishAgg);

        btnCreateNewFish = new JButton("Create new fish");
        btnCreateNewFish.addActionListener(new Listener());

        btnAddFood = new JButton("Add More Food");
        btnAddFood.addActionListener(new Listener());

        //Add HUDControls
        HUDControls.add(newFishName);
        HUDControls.add(newFishType);
        HUDControls.add(newFishAgg);
        HUDControls.add(btnCreateNewFish);

        HUDControls.add(btnAddFood);

        HUD.add(HUDInfo, BorderLayout.WEST);
        HUD.add(HUDControls, BorderLayout.EAST);

        Controls.add(HUD, BorderLayout.NORTH);
    }

    public final void initStatusBar () {
        StatusBar = new JPanel(new GridLayout(1, 4));
        lblFoodShortage = new JLabel("Hungry Fishes: 0");
        lblTotalAliveFish = new JLabel("Total Alive Fish: 0");
        lblTotalDeadFish = new JLabel("Total Dead Fish: 0");
        lblUserData = new JLabel("User Data: 0");

        lblUserData.setToolTipText("No file loaded.");

        StatusBar.add(lblFoodShortage);
        StatusBar.add(lblTotalAliveFish);
        StatusBar.add(lblTotalDeadFish);
        StatusBar.add(lblUserData);

        StatusBar.setBackground(Color.WHITE);
        Controls.add(StatusBar, BorderLayout.SOUTH);

        StatusBarUpdater = new Timer(100, new ActionListener () {
            public void actionPerformed (ActionEvent event) {
                //update total alive, dead fish and hungry fishes
                int hungryFishes = 0;
                int aliveFish = 0;
                int deadFish = 0;
                for (int i = 0; i < f.size(); i++) {
                    if (f.get(i) == null) {
                        deadFish++;
                    } else {
                        aliveFish++;
                        if (f.get(i).getHunger() <= 3) {
                            hungryFishes++;
                        }
                    }
                }
                if (hungryFishes > 0) {
                    lblFoodShortage.setForeground(Color.RED);
                } else {
                    lblFoodShortage.setForeground(Color.BLACK);
                }
                lblFoodShortage.setText("Hungry Fishes: " + hungryFishes);
                lblTotalAliveFish.setText("Total Alive Fish: " + aliveFish);
                lblTotalDeadFish.setText("Total Dead Fish: " + deadFish);

                //update user data
                try {
                    if (saveDataFile != null) {
                        lblUserData.setText("User Data: " + saveDataFile);
                        lblUserData.setToolTipText(saveDataFile.toString());
                    }
                } catch (NullPointerException ex) {
                    lblUserData.setText("User Data: 00");
                }
            }
        });
        StatusBarUpdater.start();
    }

    public final void initTank() {

        Tank = new JLayeredPane();
        Tank.setPreferredSize(new Dimension(SCRNSIZE.width, SCRNSIZE.height));
        //Tank.setBackground(Color.CYAN);
        addBg();

        Tank.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (addFoodToggle) {
                    int x = e.getX();
                    food.add(new Food(x));
                    tFood.add(new Thread(new RepaintFood(food.size() - 1)));

                    food.get(food.size() - 1).move(); //make the food move
                    moveFood(true, food.size() - 1);  //add the food to the tank

                    tFood.get(tFood.size() - 1).start();
                }
            }
        });

        frame.add(Tank, BorderLayout.NORTH);

    }

    public class TextFilter extends FileFilter {


        //Accept all directories and all txt files.
        public boolean accept(File f) {

            String extension = getExtension(f);

            if (f.isDirectory()) {
                return true;
            } else if (extension != null) {
                if (extension.equals("fish")) {
                    return true;
                }
            }

            return false;
        }

        //The description of this filter
        public String getDescription() {
            return "Fish files (plain text) (.fish)";
        }


    }

    public String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }

    public boolean checkFileFormat(File f) {
        try {
            FileInputStream fis = new FileInputStream(f);
            BufferedInputStream bis = new BufferedInputStream(fis);
            DataInputStream dis = new DataInputStream(bis);

            if (count(f) < 2 ) {
                System.out.println(count(f));
                System.out.println("Need three lines");
                return false;
            }

            String[] fileHeader = new String[3];
            fileDetails = new String[2]; //datecreated, datelastsaved
            for(int i = 0; i <= 2 && dis.available() != 0; i++) {
                fileHeader[i] = dis.readLine();
                if (i == 0) {
                    if (!fileHeader[i].equals(SAVEFILEHEADERS[0])) {
                        fis.close();
                        bis.close();
                        dis.close();
                        System.out.println("fistank not found");
                        return false;
                    }
                }
                try {
                    //String dateRegex = "^(0[1-9]|[12][0-9]|3[01])[/](0[1-9]|1[012])[/](19|20)[\\d]{2}$";
                    String dateRegex = "[\\d]{13,}"; //1298195809785
                    if (i == 1) {
                        String[] date = fileHeader[i].split(" - ");
                        if (!date[0].equals(SAVEFILEHEADERS[1]) || !date[1].matches(dateRegex) || Long.parseLong(date[1]) > Math.pow(2,64)) {
                            fis.close();
                            bis.close();
                            dis.close();
                            System.out.println("date created not found");
                            return false;
                        } else {
                            fileDetails[i - 1] = date[1];
                        }
                    }
                    if (i == 2) {
                        String[] date = fileHeader[i].split(" - ");
                        if (!date[0].equals(SAVEFILEHEADERS[2]) || !date[1].matches(dateRegex) || Long.parseLong(date[1]) > Math.pow(2,64)) {
                            fis.close();
                            bis.close();
                            dis.close();
                            System.out.println("date saved not found");
                            return false;
                        } else {
                            fileDetails[i - 1] = date[1];
                        }
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Date format mismatch.");
                    dis.close();
                    bis.close();
                    fis.close();
                }
            }

        } catch (FileNotFoundException e) {
            System.out.println("File Not Found");
        } catch (IOException e) {
            System.out.println("File could not be opened.");
        }

        return true;
    }
    
    public boolean loadFile (File f) {
        //fish details
        ArrayList<String[]> fd = new ArrayList<String[]>();

        try {
            FileInputStream fis = new FileInputStream(f);
            BufferedInputStream bis = new BufferedInputStream(fis);
            DataInputStream dis = new DataInputStream(bis);

            //eat the headers
            for (int i = 0; i <= 4; i++) {
                dis.readLine();
            }           
            
            //get the useful information
            for (int i = 0; dis.available() != 0; i++) {
                String[] details = dis.readLine().split(",", 4);
                if (
                        (details[0].matches("[\\w]{1,30}")) &&
                        (details[1].matches("[\\d]*")) &&
                        (details[2].matches("[\\d]{1,2}")) &&
                        (details[3].equalsIgnoreCase("true") || details[3].equalsIgnoreCase("false"))
                    ) {
                    //good match
                    fd.add(details);
                } else {
                    //skip any invalid lines
                }                
            }

            for (int i = 0; i < fd.size(); i++) {
                for (String Fd : fd.get(i)) {
                    //System.out.print(Fd + " : ");
                }
                //System.out.println();
            }
            
            fis.close();
            bis.close();
            dis.close();
        } catch (FileNotFoundException e) {
            System.out.println("File Not Found");
        } catch (IOException e) {
            System.out.println("File could not be opened.");
        }
        
        if (!(fd.size() > 0)) {
            return false;
        }

        for (int i = 0; i < fd.size(); i++) {
            String[] details = fd.get(i);
            System.out.println(details[0] + " " + details[1] + " " + details[2] + " " + details[3]);
            addFish(details[0], Integer.parseInt(details[2]) - 1, (details[3].equalsIgnoreCase("true") ? true : false), Integer.parseInt(details[1]));
        }
        
        return true;
    }

    public void saveAsFile () {
        File dir = null;
        try {
            dir = new File(".").getCanonicalFile();
        } catch (IOException ex) {
            System.out.println("Unexpected Error.");
        }
        fileChooser.setCurrentDirectory(dir);
        fileChooser.addChoosableFileFilter(new TextFilter());
        fileChooser.setAcceptAllFileFilterUsed(false);

        int status = fileChooser.showDialog(frame, "Save As");

        try {
            if ( status == 0 ) {
                File file = fileChooser.getSelectedFile();
                System.out.println(file);
                if (file.toString().matches(FISHFILEREGEX)) {
                //String extension = getExtension(file);
                //if(extension.equals("fish")) {
                    //Correct file extension found.
                    try {
                        //Create the new file if it doesn't exist
                        boolean newFileCreated = file.createNewFile();
                        if (newFileCreated) {
                            saveDataFile = file;
                            //save the data to it
                            saveFile();
                        } else {
                            //the file already exists, ask user if they would like to overwrite
                            int overwrite = JOptionPane.showConfirmDialog(null, "Overwrite existing file?", "Overwrite?", JOptionPane.YES_OPTION);
                            if (overwrite == JOptionPane.YES_OPTION) {
                                //overwrite the selected file
                                saveDataFile = file;
                                try {
                                    saveFile();
                                } catch (IOException e) {
                                    JOptionPane.showMessageDialog(null, "File not found. Try again.");
                                }
                            } else {
                                //dont overwrite.
                            }
                        }
                    } catch (IOException ex) {
                        System.out.println("Unexpected Error.");
                    }
                /*} else {
                    System.out.println("Invalid extension.");
                }*/
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid file extension.");
                }
            } else if (status == 1) {
                System.out.println("User cancelled open operation");
            } else if (status == -1) {
                System.out.println("An error occured");
            }
        } catch (NullPointerException e) {
            System.out.println("No file found.");
        }
    }

    public boolean saveFile () throws IOException {
        //try {
        FileWriter fw = new FileWriter(saveDataFile);   //opens the file for byte writing
        BufferedWriter bw = new BufferedWriter(fw);     //buffers the file for faster writing
        PrintWriter pw = new PrintWriter(bw);           //loaded file for character writing

        //write the headers
        String headers;

        headers = SAVEFILEHEADERS[0] + "\n";
        if (fileDetails != null){
            headers += SAVEFILEHEADERS[1] + " - " + fileDetails[0] + "\n";
        } else {
            headers += SAVEFILEHEADERS[1] + " - " + System.currentTimeMillis() + "\n";
        }
        headers += SAVEFILEHEADERS[2] + " - " + System.currentTimeMillis() + "\n";
        headers += "----------\n";
        headers += "FishName,Age,Type,Aggression\n";

        pw.write(headers);

        if (f.size() > 0) {
            //go through each fish and write it's details to the file
            for (int i = 0; i < f.size(); i++) {
                if (f.get(i) != null) {
                    Fish myF = f.get(i);
                    pw.write( myF.getName() + "," + myF.getAge() + "," + myF.getType() + "," + myF.getAgg() );
                    pw.write("\n");
                }
            }
        }

        pw.close();
        bw.close();
        fw.close();

        /*} catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "File: " + saveDataFile + "\n Cannot be found.");
            return false;
        }*/

        return true;
    }

    public void addFish (String name, int type, boolean agg, int age) {
        if (name.equals("")) {
            JOptionPane.showMessageDialog(null, "Fish needs a name!");
            return;
        }
        if (type == 0) {
            f.add(new FishNemo(name, agg, age));
        } else if (type == 1) {
            f.add(new FishTropical1(name, agg, age));
        } else if (type == 2) {
            f.add(new FishTropical2(name, agg, age));
        } else if (type == 3) {
            f.add(new FishRed(name, agg, age));
        }

        final int i = f.size() - 1;
        t.add(new Thread(new RepaintFrame(i)));

        //start updating the HUD if the first fish is created
        if (i == 0) {
            HUDUpdateTimer = new Timer(100, new ActionListener () {
                public void actionPerformed (ActionEvent e) {
                    setInfoHUD();
                }
            });
            HUDUpdateTimer.start();
        }

        HUDIndex = i;

        //setInfoHUD(i);   //update the HUD with the information of the new fish
        f.get(i).move(); //make the fish move
        moveFish(true, i);  //add the fish to the tank
        t.get(i).start();    //start the thread for the fish movement

        hungerTimer.add(new Timer(2000, new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                if (f.get(i).getHunger() <= 0) {
                    f.get(i).setStatus(-1);
                    lblImages.get(i).setBounds(0, 0, 0, 0);
                    lblImages.set(i, null);
                    //t.get(i).stop();
                    t.set(i, null);
                    hungerTimer.get(i).stop();
                    ageTimer.get(i).stop();
                    //f.set(i, null);

                    //if the fish was updating the HUD, then change the HUD to display another fish
                    if (i == HUDIndex) {
                        for (int x = 0; x < f.size(); x++) {
                            if (f.get(x) != null) {
                                HUDIndex = x;
                            }
                        }
                    }
                } else {
                    f.get(i).decreaseHunger();
                }
            }
        }));

        ageTimer.add(new Timer(1000, new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                f.get(i).setAge((long) (f.get(i).getAge() + 1));
            }
        }));

        hungerTimer.get(hungerTimer.size() - 1).start();
        ageTimer.get(ageTimer.size() - 1).start();

        //add mouse listener to the label, so when it is clicked the HUD shows its information
        lblImages.get(lblImages.size() - 1).addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                HUDIndex = i;
                if (f.get(i).getStatus() == 1) {
                    f.get(i).setStatus(2);
                } else if (f.get(i).getStatus() == 2) {
                    f.get(i).setStatus(1);
                }
                //System.out.println("Fish " + i + " has status: " + f.get(i).getStatus());
            }
        });
    }

    public void pauseAllFish () {
        for (int i = 0; i < f.size(); i++) {
            if (f.get(i) != null) {
                f.get(i).setStatus(1);
                hungerTimer.get(i).stop();
                ageTimer.get(i).stop();
                fishStatus = 1;
            }
        }
    }

    public void hideAllFish () {
        for (int i = 0; i < lblImages.size(); i++) {            
            if (f.get(i) != null) {                
                lblImages.get(i).setBounds(0,0,0,0);
            }
        }
    }

    public void startAllFish () {
        fishStatus = 0;
        for (int i = 0; i < f.size(); i++) {
            if (f.get(i) != null) {
                System.out.println("Starting fish " + i);
                f.get(i).setStatus(0);
                hungerTimer.get(i).start();
                ageTimer.get(i).start();                
            }
        }        
    }

    public void showAllFish () {
        for (int i = 0; i < lblImages.size(); i++) {
            if (f.get(i) != null) {
                lblImages.get(i).setBounds(f.get(i).getPosX(),f.get(i).getPosY(),f.get(i).getImg1().getWidth(),f.get(i).getImg1().getHeight());
            }
        }
    }

    public int count (File filename) throws IOException {
        DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(filename)));
        byte[] c = new byte[1024];
        int count = 0;
        int readChars = 0;
        while ((readChars = dis.read(c)) != -1) {
            for (int i = 0; i < readChars; ++i) {
                if (c[i] == '\n')
                    ++count;
            }
        }
        dis.close();
        return count;
    }
    
    private void addBg () {
        TankBg = new JLabel(new ImageIcon("img/ws_Fish_tank_water_2048x768_reflect.jpg"));
        Tank.add(TankBg, 10000);
        TankBg.setBounds(new Rectangle(0, 0, 2048, 768));
    }

    public void setInfoHUD () {
        int i = this.HUDIndex;

        try {
            lblFishName.setText("Name: " + f.get(i).getName());
            lblFishType.setText("Type: " + f.get(i).getType());
            lblFishAge.setText("Age: " + f.get(i).getAge());
            lblFishAggressive.setText("Aggression: " + f.get(i).getAgg());
            lblFishHunger.setText("Hunger: " + f.get(i).getHunger());
            lblFishVelocity.setText("Velocity: " + f.get(i).getHVelocity());
        } catch (NullPointerException e) {
            //System.out.println("Fish not found to update HUD.");
        } catch (IndexOutOfBoundsException e) {
            //System.out.println("Fish not found to update HUD.");
        }
    }

    public void moveFish (final boolean newFish, final int i) {
        Fish f1 = f.get(i);

        int x = f1.getPosX();       //fish's X coordinate on the tank
        int y = f1.getPosY();       //fish's Y coordinate on the tank
        int width = f1.getImg1().getWidth();    //fish's width in pixels
        int height = f1.getImg1().getHeight();    //fish's height in pixels

        if (newFish) {
            lblImages.add(f1.displayFishFirst());       //set the label of the fish
            Tank.add(lblImages.get(f.size() - 1), 0);      //add the label to the tank
        }

        if (!newFish) {
            lblImages.get(i).setIcon(f1.displayFish());     //update the fish's image with the current one (depending on its velocity)
        }

        lblImages.get(i).setBounds(x, y, width, height);    //update the fish's position on the tank

        HUD.repaint();  //incase the fish moves over the HUD, it will wont cover the information
    }

    public void initRace () {
        
    }

    public void moveFood (final boolean newFood, final int i) {
        Food f1 = food.get(i);

        int x = f1.getPosX();       //food's X coordinate on the tank
        int y = f1.getPosY();       //food's Y coordinate on the tank
        int width = f1.getImg().getWidth();    //food's width in pixels
        int height = f1.getImg().getHeight();    //food's height in pixels

        if (newFood) {
            lblImagesFood.add(f1.displayFoodFirst());       //set the label of the food
            Tank.add(lblImagesFood.get(i), 0);      //add the label to the tank
        }

        lblImagesFood.get(i).setBounds(x, y, width, height);    //update the food's position on the tank

        HUD.repaint();  //incase the food moves over the HUD, it will wont cover the information
    }

    class Listener implements ActionListener {

        public void actionPerformed(final ActionEvent event) {

            Object source = event.getSource();

            if (source == btnCreateNewFish) {
                if (fishStatus == 1) {  //if the fish have been paused, adding a new one will start them back up
                    startAllFish();
                }
                fishStatus = 0; 
                addFish(txtNewFishName.getText(), txtNewFishType.getSelectedIndex(), txtNewFishAgg.isSelected(), 0);
            } else if (source == btnAddFood) {
                addFoodToggle = !addFoodToggle;
            }
        }
    }

    public class RepaintFrame implements Runnable {

        private int i;
        private int foodI;

        public RepaintFrame(final int index) {
            this.i = index;
            foodI = -1;
        }

        @Override
        public void run() {
            while (f.get(i).getStatus() != -1) {
                try {
                    t.get(i).sleep(30);
                } catch (InterruptedException ex) {
                    System.out.println("Fish thread for index " + i + " failed to sleep.");
                }
                if (f.get(i).getStatus() == 0) {
                    //System.out.println("Fish is move " + i);
                    
                    if (f.get(i).getHunger() <= 3) {    //if the fish is hungry
                        int foodIndex = getFood();

                        if (foodIndex >= 0) {   //if there is food available then move the fish towards the food
                            int x = food.get(foodIndex).getPosX();
                            int y = food.get(foodIndex).getPosY();

                            boolean foodEaten;

                            foodEaten = f.get(i).moveTowardsFood(x, y);

                            if (foodEaten) {
                                foodI = -1;
                                removeFood(foodIndex);
                            }
                        } else {    //otherwise if there isn't any food, keep the fish moving as it was
                            f.get(i).move();
                        }
                    } else {    //if the fish isn't hungry
                        f.get(i).move();
                    }
                    moveFish(false, this.i);
                } else if (f.get(i).getStatus() == 3) {
                    //race here
                }
            }
            f.set(i, null);     //once it has died, clean it off the system
        }

        public int getFood() {

            //check if the food arraylist is empty, if so then return -1
            boolean empty = false;
            for (int a = 0; a < food.size() && !empty; a++) {   //until a food item is found, keep searching
                if (food.get(a) != null) {  //if food is found, then break the loop and continue with the method
                    empty = true;
                }

                if (a == food.size() - 1) { //if it is the last loop, then check if the last food item is null, if so then there is no food available in the fishtank
                    if (food.get(a) == null) {
                        return -1;
                    }
                }
            }

            int foodCount = 0;
            int lastIndex = -1;
            for (int a = 0; a < food.size(); a++) {
                if (food.get(a) != null) {
                        foodCount++;
                        lastIndex = a;
                }
            }
            if (foodCount == 1) {
                return lastIndex;
            }

            //calculate the closest food using pythagorus thereom
            double coordinates[][] = new double[food.size()][2];
            for (int a = 0; a < food.size(); a++) {
                if (food.get(a) != null) {
                    double dx = f.get(i).getPosX() - food.get(a).getPosX();
                    double dy = f.get(i).getPosY() - food.get(a).getPosY();
                    coordinates[a][1] = Math.sqrt((Math.pow(dx, 2)) + (Math.pow(dy, 2)));
                    coordinates[a][0] = a;
                }
            }

            //if there is only one peice of food available, then there is no need to sort the results
            if (coordinates.length == 1) {
                System.out.print("only one available.");
                return 0;
            } else if (coordinates.length != 0) {
                //Sort results using bubble sort
                double[] temp = new double[3];
                for (int a = 0; a < (coordinates.length - 1); a++) {
                    for (int b = 0; b < (coordinates.length - 1); b++) {
                        if (coordinates[b][1] > coordinates[b + 1][1]) {
                            temp[0] = coordinates[b][0];
                            temp[1] = coordinates[b][1];

                            coordinates[b][0] = coordinates[b + 1][0];
                            coordinates[b][1] = coordinates[b + 1][1];

                            coordinates[b + 1][0] = temp[0];
                            coordinates[b + 1][1] = temp[1];
                        }
                    }
                } //Done sorting

                //return the index of the closest piece of food from the fish
                for (int a = 0; a < coordinates.length; a++) {
                    if (coordinates[a][0] != 0) {

                        //These if statements check if the the fish will go for the same food as it was before,
                        //or if it isn't available or another is closer, then it will be able to change
                        //its direction for another peice of food.
                        if (foodI == -1) {
                            foodI = (int) coordinates[a][0];
                            f.get(i).resetTurnForFood();
                        } else if (foodI != coordinates[a][0]) {
                            foodI = (int) coordinates[a][0];
                            f.get(i).resetTurnForFood();
                        }
                        return (int) coordinates[a][0];
                    }
                }
            }

            //program shouldn't get here, but java requires a return
            return -1;
        }

        public void removeFood(int index) {
            lblImagesFood.get(index).setBounds(0, 0, 0, 0);
            
            food.set(index, null);
            tFood.get(index).stop();
            
            f.get(i).resetTurnForFood();

            foodI = -1;
        }
    }

    public class RepaintFood implements Runnable {

        private int i;

        public RepaintFood(final int index) {
            this.i = index;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    tFood.get(i).sleep(50);
                } catch (InterruptedException ex) {
                    System.out.println("Food thread for index " + i + " failed to sleep.");
                }

                food.get(i).move();
                moveFood(false, this.i);
            }
        }
    }
}