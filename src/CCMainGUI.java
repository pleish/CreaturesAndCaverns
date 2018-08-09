
import modules.*;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;


public class CCMainGUI extends JFrame implements ActionListener {
    private static final int serverPort = 8989;

    private Game game = new Game();
    private BufferedReader in;
    private PrintWriter out;


    private JFrame frame = new JFrame("Caverns and Creatures");
    private JLayeredPane contentPane = new JLayeredPane();
    private JTextArea chatFieldTXT;
    private JLabel scoreBoardLBL = new JLabel();
    private JLabel imgBackground = new JLabel();
    private JLabel topBackground = new JLabel();
    private JLabel dragonIconLBL = new JLabel();
    private JLabel player1LBL = new JLabel();
    private JLabel player2LBL = new JLabel();
    private JLabel player3LBL = new JLabel();
    private JLabel creature1LBL= new JLabel();
    private JScrollPane scrollChatTxt;
    private JTextField submitFieldTXT = new JTextField(75);
    private JButton sendButton = new JButton("Send");
    private JButton attackButton = new JButton("Roll Attack", createImageIcon("sword.png"));
    private JButton rollButton = new JButton("Roll", createImageIcon("d20-blank.png"));
    private JButton addCreatureButton = new JButton("Add Creature");
    private JButton startGameButton, damageButton, initiateTurnButton;
    private JComboBox<String> playerComboBox;
    private Actor playerActor;
    private int attackRoll = 0, damageRoll = 0;
    private String username, playerCharacter, target;
    private boolean playerDeath = false;
    private boolean playerTurn = false;

    public CCMainGUI() {
        setTitle("Caverns and Creatures");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(10, 10, 905, 700);
        setIconImage((createImageIcon("dragonicon.png")).getImage());
        contentPane.setBorder(new EmptyBorder(0, 5, 5, 5));
        contentPane.setBackground(new Color( 216,234,240));
        setContentPane(contentPane);
        contentPane.setLayout(null);
        contentPane.setOpaque(true);

        imgBackground.setOpaque(true);

        imgBackground.setIcon(createImageIcon("dark_field.jpg"));
        topBackground.setIcon(createImageIcon("dragonbackground.png"));
        dragonIconLBL.setIcon(createImageIcon("dragonicon.png"));
        scoreBoardLBL.setForeground(Color.WHITE);

        topBackground.setBounds(0, 0, 905, 140);
        imgBackground.setBounds(0, 0, 905, 700);
        dragonIconLBL.setBounds(840, 102, 50, 50);
        scoreBoardLBL.setBounds(0, 0, 905, 140);

        contentPane.add(topBackground,JLayeredPane.PALETTE_LAYER);
        contentPane.add(imgBackground,JLayeredPane.DEFAULT_LAYER);
        contentPane.add(dragonIconLBL,JLayeredPane.MODAL_LAYER);

        displayChat();
        displayGameBoard();
        createGameControls();

    }

    private void displayGameBoard(){
        attackButton.addActionListener(evt -> {
            if(attackRoll == 0) {
                attackRoll = playerActor.rollAttack();
                rollButton.setFont(new Font("Arial", Font.BOLD, 20));
                rollButton.setText(Integer.toString(attackRoll));
                chatFieldTXT.append("Attack Roll: " + attackRoll + "\n");
                attackButton.setText("Roll Damage");
            }
            else {
                damageRoll = playerActor.rollDamage();
                chatFieldTXT.append("Damage Roll: " + damageRoll + "\n");
                rollButton.setFont(new Font("Arial", Font.BOLD, 20));
                rollButton.setText(Integer.toString(attackRoll));
                attackButton.setVisible(false);
                attackButton.setText("Roll Attack");
                attackRoll = 0;
            }
        });
        rollButton.addActionListener(evt -> {
            Die d20 = new Die(20);
            int roll = d20.rollDie();
            chatFieldTXT.append("You rolled a " + roll + "!\n");
            rollButton.setFont(new Font("Arial", Font.BOLD, 20));
            rollButton.setText(Integer.toString(roll));
        });
        addCreatureButton.addActionListener(evt -> {
            sendJson(JSONLibrary.sendAddCreature());
        });
        scoreBoardLBL.setOpaque(false);
        player1LBL.setOpaque(false);
        rollButton.setOpaque(false);

        attackButton.setEnabled(true);
        attackButton.setBorder(BorderFactory.createEmptyBorder());
        attackButton.setForeground(new Color( 161,81,55));
        attackButton.setVerticalTextPosition(AbstractButton.BOTTOM);
        attackButton.setHorizontalTextPosition(AbstractButton.CENTER);
        rollButton.setEnabled(true);
        rollButton.setBorder(BorderFactory.createEmptyBorder());
        rollButton.setContentAreaFilled(false);
        rollButton.setBorderPainted(false);
        rollButton.setForeground(new Color( 255,255,255));
        rollButton.setFont(new Font("Arial", Font.PLAIN, 38));
        rollButton.setVerticalTextPosition(AbstractButton.CENTER);
        rollButton.setHorizontalTextPosition(AbstractButton.CENTER);
        player1LBL.setVerticalTextPosition(AbstractButton.CENTER);
        player1LBL.setHorizontalTextPosition(AbstractButton.CENTER);
        player1LBL.setForeground(Color.WHITE);
        player2LBL.setVerticalTextPosition(AbstractButton.BOTTOM);
        player2LBL.setHorizontalTextPosition(AbstractButton.CENTER);
        player2LBL.setForeground(Color.WHITE);
        player3LBL.setVerticalTextPosition(AbstractButton.BOTTOM);
        player3LBL.setHorizontalTextPosition(AbstractButton.CENTER);
        player3LBL.setForeground(Color.WHITE);
        addCreatureButton.setEnabled(true);

        attackButton.setBounds(0,140,100,100);
        rollButton.setBounds(0,240,100,109);
        addCreatureButton.setBounds(5,492,175,23);
        player1LBL.setBounds(440,250,240,160);
        player2LBL.setBounds(540,275,240,160);
        player3LBL.setBounds(640,300,240,160);
        creature1LBL.setBounds(200,275,240,160);

        contentPane.add(attackButton,JLayeredPane.MODAL_LAYER);
        contentPane.add(rollButton,JLayeredPane.MODAL_LAYER);
        contentPane.add(addCreatureButton,JLayeredPane.MODAL_LAYER);
        contentPane.add(player1LBL,JLayeredPane.MODAL_LAYER);
        contentPane.add(player2LBL,JLayeredPane.MODAL_LAYER);
        contentPane.add(player3LBL,JLayeredPane.MODAL_LAYER);
        contentPane.add(creature1LBL,JLayeredPane.MODAL_LAYER);
    }
    private void displayChat(){
        chatFieldTXT = new JTextArea(20, 75);
        scrollChatTxt = new JScrollPane(chatFieldTXT,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        submitFieldTXT.setBounds(5, 627, 795, 25);
        sendButton.setBounds(800, 627, 84, 23);
        scrollChatTxt.setBounds(5,515,880,110);

        chatFieldTXT.setEditable(false);
        sendButton.addActionListener(this);
        submitFieldTXT.addActionListener(this);
        sendButton.setEnabled(true);

        contentPane.add(scoreBoardLBL,JLayeredPane.MODAL_LAYER);
        contentPane.add(scrollChatTxt,JLayeredPane.MODAL_LAYER);
        contentPane.add(submitFieldTXT,JLayeredPane.MODAL_LAYER);
        contentPane.add(sendButton,JLayeredPane.MODAL_LAYER);
    }
    private void createGameControls() {
        startGameButton = new JButton("Start Game");
        damageButton = new JButton("Roll Damage");
        playerComboBox = new JComboBox<>();
        playerComboBox.addItem("--Target--");
        initiateTurnButton = new JButton("Initiate Turn");

        damageButton.setVisible(false);
        playerComboBox.setVisible(false);
        initiateTurnButton.setVisible(false);

        startGameButton.addActionListener(e-> {
            sendJson(JSONLibrary.sendStartGame());
            startGameButton.setVisible(false);
        });

        startGameButton.addActionListener(e->{
            sendJson(JSONLibrary.sendStartGame());
            startGameButton.setEnabled(false);
            attackButton.setVisible(true);
            damageButton.setVisible(true);
            playerComboBox.setVisible(true);
            initiateTurnButton.setVisible(true);
        });

        attackButton.addActionListener(e->{
            attackRoll = playerActor.rollAttack();
            chatFieldTXT.append("Attack Roll: " + attackRoll + "\n");
            attackButton.setEnabled(false);
        });

        damageButton.addActionListener(e->{
            damageRoll = playerActor.rollDamage();
            chatFieldTXT.append("Damage Roll: " + damageRoll + "\n");
            damageButton.setEnabled(false);
        });

        playerComboBox.addActionListener(e->{
            JComboBox cb = (JComboBox)e.getSource();
            if(!cb.getSelectedItem().equals("--Target--")){
                target = (String)cb.getSelectedItem();
                chatFieldTXT.append("Target : " + target + "\n");
            }
        });

        initiateTurnButton.addActionListener(e-> {
            if (!attackButton.isEnabled() && !damageButton.isEnabled() && !playerComboBox.getSelectedItem().equals("--Target--")) {
                sendJson(JSONLibrary.sendInitiateTurn(username, target, attackRoll, damageRoll));
                if (!attackButton.isEnabled() && !damageButton.isEnabled()) {
                    sendJson(JSONLibrary.sendInitiateTurn(username, target, attackRoll, damageRoll));
                    initiateTurnButton.setEnabled(false);
                    playerTurn = false;
                } else {
                    chatFieldTXT.append("You must roll attack and damage and select a target to initiate combat.\n");
                }
            }
        });

        addCreatureButton.addActionListener(e->{
            sendJson(JSONLibrary.sendAddCreature());
        });

        startGameButton.setBounds(105, 175, 300, 25);

        damageButton.setBounds(255, 200, 150, 25);
        playerComboBox.setBounds(105, 225, 300, 25);
        initiateTurnButton.setBounds(105, 250, 300, 25);

        contentPane.add(startGameButton,JLayeredPane.MODAL_LAYER);
        contentPane.add(damageButton,JLayeredPane.MODAL_LAYER);
        contentPane.add(playerComboBox,JLayeredPane.MODAL_LAYER);
        contentPane.add(initiateTurnButton,JLayeredPane.MODAL_LAYER);
    }
    private void setupGame(){
        //Just a test//
        ActorPresets actorPresets = new ActorPresets();
        Actor player1 = actorPresets.playerPresets.get(playerCharacter);
        game.addPlayer(username, player1.getType());
        game.addRandomMonster();
        //String[] nameActors = game.getNames();
        //String[] actorStats = game.getScoreboard();
        //displayScoreboard(nameActors,actorStats);

        //populate Actor test
        //player1LBL.setText(username);
        player1LBL.setIcon(createImageIcon(player1.getType()+".gif"));
        player2LBL.setIcon(createImageIcon("Mage.gif")); //hard coded but you get the idea
        player3LBL.setIcon(createImageIcon("Rogue.gif")); //hard coded but you get the idea
        creature1LBL.setIcon(createImageIcon("Dragon.gif"));

    }

    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = CCMainGUI.class.getClassLoader().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    private String getUser() {
        String username = JOptionPane.showInputDialog(
                contentPane,
                "Choose a screen name:",
                "Screen name selection",
                JOptionPane.PLAIN_MESSAGE);
        chatFieldTXT.append("Your username is now " + username + "\n");
        return username;
    }

    private String getPlayerCharacter(){
        String[] options = new String[]{"Fighter", "Rogue", "Mage"};
        int response = JOptionPane.showOptionDialog(contentPane, "Choose Your Character!",
                "Player Character Selection", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                options, options[0]);
        switch(response){
            case 0 : playerActor = new ActorPresets().playerPresets.get("Fighter");
                return "Fighter";
            case 1 : playerActor = new ActorPresets().playerPresets.get("Rogue");
                return "Rogue";
            case 2 : playerActor = new ActorPresets().playerPresets.get("Mage");
                return "Mage";
        }
        return null;
    }


    private String getServerAddress() {
        String serverName = JOptionPane.showInputDialog(contentPane,
                "Server name or IP", "ec2-18-207-150-67.compute-1.amazonaws.com");
        return  serverName;
    }

    private boolean connectToServer(){
        String serverAddress = getServerAddress();
        Socket socket = null;

        try {
            socket = new Socket(serverAddress, serverPort);
            in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

        } catch (IOException e) {
            e.printStackTrace();
            chatFieldTXT.append("Could not connect to " + serverAddress + ". Continuing anyway for testing. \n");
            return false;
        }
        chatFieldTXT.append("Connected to " + serverAddress + " successfully.\n");
        return true;
    }

    //Takes arrays of player names and player scoreboards from game running on server
    private void displayScoreboard(String[] playerNames, String[] colorActorStats) {
        String scoreboard = "<HTML><TABLE ALIGN=TOP BORDER=0  cellspacing=2 cellpadding=2><TR>";
        for(String actorName: playerNames){
            scoreboard += "<TH><H2>" + actorName + "</H2></TH>";
        }
        scoreboard += "</TR><TR>";
        int count = 0;
        for(String anActor: colorActorStats){
            if(count % 2 == 0){
                scoreboard += "<TD BGCOLOR=" + anActor + ">";
            }
            else {
                scoreboard += anActor + "</TD>";
            }
            count++;
        }
        scoreboard += "</TR></TABLE></HTML>";
        scoreBoardLBL.setText(scoreboard);
    }
    private void sendJson(String json){
        out.println(json);
        out.flush();
    }

    public void run() {

        boolean isConnected = connectToServer();
        username = getUser();
        sendJson(JSONLibrary.sendUser(username));

        playerCharacter = getPlayerCharacter();
        sendJson(JSONLibrary.sendPlayerCharacter(playerCharacter, username));

        setupGame();

        Runnable messageHandler = () -> {
            while(true){
                


            }
        }; new Thread(messageHandler).start();
    }



    @Override
    public void actionPerformed(ActionEvent e) {
        String text = submitFieldTXT.getText();
        chatFieldTXT.append(username + ": " + text + "\n");
        submitFieldTXT.selectAll();
        chatFieldTXT.setCaretPosition(chatFieldTXT.getDocument().getLength());
    }

    public static void main(String[] args) throws IOException {
        try {
            CCMainGUI frame = new CCMainGUI();
            frame.setVisible(true);
            frame.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}