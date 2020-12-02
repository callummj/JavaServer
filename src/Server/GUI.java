package Server;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class GUI implements Runnable, ActionListener {

    public static JTextArea consoleArea;
    public static JTextPane connectionsArea;
    public static JTextArea stockManager;
    private JScrollPane scrollPane;

    private ArrayList<String> connectedIDs = new ArrayList<>();

    public GUI(){}


    public void drawGUI(){
        JFrame frame = new JFrame("Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600,600);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }




        //Creating the MenuBar and adding components
        JMenuBar menuBar = new JMenuBar();
        JMenu menuButton = new JMenu("Menu");
        menuBar.add(menuButton);
        JMenuItem disconnectMenuButton = new JMenuItem("Close server");
        JMenuItem helpMenuButton = new JMenuItem("Help");

        menuButton.add(disconnectMenuButton);
        menuButton.add(helpMenuButton);

        connectionsArea = new JTextPane();
        connectionsArea.setEditable(false);
        connectionsArea.setText("Connections");
        connectionsArea.setBackground(Color.LIGHT_GRAY);
        connectionsArea.setForeground(Color.BLACK);

        JScrollPane scroll = new JScrollPane(connectionsArea);


        stockManager = new JTextArea();
        stockManager.setEditable(false);

        consoleArea = new JTextArea();
        consoleArea.setEditable(false);
        consoleArea.setText("Console");



        scrollPane = new JScrollPane(consoleArea);
        frame.setVisible(true);
        frame.getContentPane().add(BorderLayout.NORTH, menuBar);
        frame.getContentPane().add(BorderLayout.CENTER, consoleArea);
        frame.getContentPane().add(BorderLayout.EAST, scroll);
        frame.getContentPane().add(BorderLayout.SOUTH, stockManager);


    }

    public void removeConnection(String ID){
        connectedIDs.remove(ID);
        connectionsArea.setText("Connections: " + connectionsToString());
    }

    public void newConnection(String ID){
        connectedIDs.add(ID);

        connectionsArea.setText("Connections: " + connectionsToString());
    }

    private String connectionsToString(){
        String result = "";
        for(String ID: connectedIDs){
            result += "\n" + ID;
        }
        return result;
    }

    protected void updateStockOwner(String newOwnerID){
        System.out.println("Updading stock owner label;");
        stockManager.setText("Current owner of stock: " + newOwnerID);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }

    @Override
    public void run() {
        drawGUI();
    }

    public void updateConsole(String updateMsg) {
        consoleArea.append("\n>"+updateMsg);
    }
}
