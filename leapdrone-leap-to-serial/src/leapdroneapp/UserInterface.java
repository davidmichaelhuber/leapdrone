package leapdroneapp;

import com.leapmotion.leap.Controller;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.*;

public class UserInterface extends JFrame implements ActionListener {

    SerialController sc;
    LeapController lc;
    Controller controller;

    JFrame frame;
    JPanel panel;

    ArrayList<String> comPortNames;
    JComboBox<String> comPortList;

    JButton connectToPortButton;
    JButton disconnectFromPortButton;
    JLabel connectionStatusLabel;

    JLabel expoConstLabel;
    JTextField expoConstTextField;

    JButton startProcessingButton, stopProcessingButton;

    public void createFrame() {

        frame = new JFrame();
        frame.setTitle("LeapDrone");
        frame.setSize(600, 400);

        panel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        comPortList = new JComboBox<>();
        updateComPortList();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        panel.add(comPortList, c);

        connectToPortButton = new JButton("Connect");
        connectToPortButton.addActionListener(this);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 0;
        panel.add(connectToPortButton, c);

        disconnectFromPortButton = new JButton("Disconnect");
        disconnectFromPortButton.addActionListener(this);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 2;
        c.gridy = 0;
        panel.add(disconnectFromPortButton, c);

        connectionStatusLabel = new JLabel("Disconnected...");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 3;
        connectionStatusLabel.setHorizontalAlignment(JLabel.CENTER);
        panel.add(connectionStatusLabel, c);

        expoConstLabel = new JLabel("Expo Constant");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        expoConstLabel.setHorizontalAlignment(JLabel.CENTER);
        panel.add(expoConstLabel, c);

        expoConstTextField = new JTextField("40");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 2;
        c.gridwidth = 1;
        panel.add(expoConstTextField, c);

        startProcessingButton = new JButton("Start Processing");
        startProcessingButton.addActionListener(this);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 1;
        panel.add(startProcessingButton, c);

        stopProcessingButton = new JButton("Stop Processing");
        stopProcessingButton.addActionListener(this);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 3;
        c.gridwidth = 1;
        panel.add(stopProcessingButton, c);

        frame.add(panel);
        frame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == connectToPortButton) {
            sc.connect();
        } else if (e.getSource() == disconnectFromPortButton) {
            sc.disconnect();
        } else if (e.getSource() == startProcessingButton) {
            startProcessing();
        } else if (e.getSource() == stopProcessingButton) {
            stopProcessing();
        }
    }

    public void updateComPortList() {
        comPortNames = new ArrayList<>();
        comPortNames = sc.getComPortList();
        for (String portName : comPortNames) {
            comPortList.addItem(portName);
        }
    }

    public void startProcessing() {
        controller = new Controller();

        controller.addListener(lc);
    }

    public void stopProcessing() {
        controller.removeListener(lc);
    }
}
