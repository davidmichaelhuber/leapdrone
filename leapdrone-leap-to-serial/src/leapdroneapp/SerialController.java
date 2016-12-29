package leapdroneapp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;

public class SerialController implements SerialPortEventListener {

    UserInterface ui;
    LeapController lc;

    SerialPort serialPort;

    private BufferedReader input;
    private OutputStream output;
    private static final int TIME_OUT = 2000;
    private static final int DATA_RATE = 115200;

    public ArrayList<String> comPortNames;
    public ArrayList<CommPortIdentifier> comPortIds;

    public CommPortIdentifier selectedPortId;

    public ArrayList<String> getComPortList() {

        comPortNames = new ArrayList<>();
        comPortIds = new ArrayList<>();

        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

        CommPortIdentifier currPortId;

        while (portEnum.hasMoreElements()) {
            currPortId = (CommPortIdentifier) portEnum.nextElement();
            comPortNames.add(currPortId.getName());
            comPortIds.add(currPortId);
        }

        return comPortNames;
    }

    public void connect() {
        try {
            selectedPortId = comPortIds.get(ui.comPortList.getSelectedIndex());

            // open serial port, and use class name for the appName.
            serialPort = (SerialPort) selectedPortId.open(this.getClass().getName(),
                    TIME_OUT);

            // set port parameters
            serialPort.setSerialPortParams(DATA_RATE,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);

            // open the streams
            input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
            output = serialPort.getOutputStream();

            // add event listeners
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);

            ui.connectionStatusLabel.setText("Connected...");

        } catch (Exception e) {
            ui.connectionStatusLabel.setText("Failed to connect! " + e.toString());
            System.err.println();
        }
    }

    public synchronized void disconnect() {
        if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.close();
            ui.connectionStatusLabel.setText("Disconnected...");
        }
    }

    @Override
    public synchronized void serialEvent(SerialPortEvent oEvent) {
        if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                String inputLine = input.readLine();
                System.out.println(inputLine);
            } catch (Exception e) {
                System.err.println(e.toString());
            }
        }
    }

    public synchronized void setServo(int chNr, int value) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append('#');
        builder.append(chNr);
        output.write(builder.toString().getBytes());
        output.write(value);
    }
}