package leapdroneapp;

import com.leapmotion.leap.*;
import java.io.IOException;

class LeapController extends Listener {

    UserInterface ui;
    SerialController sc;

    @Override
    public void onInit(Controller controller) {
        System.out.println("Initialized");
    }

    @Override
    public void onConnect(Controller controller) {
        System.out.println("Connected to Motion Sensor");
        controller.setPolicyFlags(Controller.PolicyFlag.POLICY_BACKGROUND_FRAMES);
    }

    @Override
    public void onDisconnect(Controller controller) {
        System.out.println("Motion Sensor Disconnected");
    }

    @Override
    public void onExit(Controller controller) {
        System.out.println("Exited");
    }

    // Leap Motion specific
    Frame frame;
    HandList hands;
    FingerList fingers;

    // Calculated angles
    int upDownAngle;
    int leftRightAngle;

    // Height
    int height;

    // Hand direction
    int direction;

    // getUpDownAngle()
    int palmY, middleY;
    int palmZ, middleZ;
    int upDown_a, upDown_b;

    // getLeftRightAngle()
    int indexX, pinkyX;
    int indexY, pinkyY;
    int leftRight_a, leftRight_b;

    // Only even values
    // Angle ranges
    int upLevel = 50;
    int downLevel = -50;
    int leftLevel = -50;
    int rightLevel = 50;

    // Only even values
    // Height range
    int topLevel = 260;
    int botLevel = 90;

    // Only even values
    // Hand direction range
    int leftDirLevel = -60;
    int rightDirLevel = 60;

    // Output range levels
    int outputLow = 1;
    int outputHigh = 255;

    // Leap values converted to servo values
    float servo1, servo2, servo3, servo4;

    // Expo constant given from slider
    float expoConst;

    @Override
    public void onFrame(Controller controller) {
        Frame frame = controller.frame();

        if (frame.hands().isEmpty()) {
            servo1 = 125;
            servo2 = 125;
            servo3 = 125;
            servo4 = 125;
            setServoValues();
            System.out.println("No Hands detected!" + frame.id());
        } else {
            HandList hands = frame.hands();
            for (Hand hand : hands) {
                getUpDownAngle(hand);
                getLeftRightAngle(hand);
                getHeight(hand);
                getHandDirection(hand);
                setExpoValues();
                setServoValues();
                try {
                    writeServoValues();
                } catch (IOException ex) {
                    System.exit(0);
                }
            }
        }

    }

    public void getUpDownAngle(Hand hand) {
        palmY = (int) hand.palmPosition().toFloatArray()[1];
        palmZ = (int) hand.palmPosition().toFloatArray()[2];
        fingers = hand.fingers();
        for (Finger finger : fingers) {
            switch (finger.type().toString()) {
                case "TYPE_MIDDLE":
                    middleY = (int) finger.tipPosition().toFloatArray()[1];
                    middleZ = (int) finger.tipPosition().toFloatArray()[2];
                    break;
            }
        }

        upDown_a = Math.abs(middleZ - palmZ);
        upDown_b = Math.abs(middleY - palmY);

        if (middleY > palmY) {
            upDownAngle = (int) Math.toDegrees(Math
                    .atan(((double) upDown_b / (double) upDown_a)));
            if (upDownAngle >= upLevel) {
                upDownAngle = upLevel;
            }
        } else {
            upDownAngle = (int) Math.toDegrees(Math
                    .atan(((double) upDown_b / (double) upDown_a))) * (-1);
            if (upDownAngle <= downLevel) {
                upDownAngle = downLevel;
            }
        }
    }

    public void getLeftRightAngle(Hand hand) {
        fingers = hand.fingers();
        for (Finger finger : fingers) {
            switch (finger.type().toString()) {
                case "TYPE_INDEX":
                    indexX = (int) finger.tipPosition().toFloatArray()[0];
                    indexY = (int) finger.tipPosition().toFloatArray()[1];
                    break;
                case "TYPE_PINKY":
                    pinkyX = (int) finger.tipPosition().toFloatArray()[0];
                    pinkyY = (int) finger.tipPosition().toFloatArray()[1];
                    break;
            }
        }

        leftRight_a = Math.abs(indexX - pinkyX);
        leftRight_b = Math.abs(indexY - pinkyY);

        if (indexY > pinkyY) {
            leftRightAngle = (int) Math.toDegrees(Math
                    .atan(((double) leftRight_b / (double) leftRight_a)));
            if (leftRightAngle >= rightLevel) {
                leftRightAngle = rightLevel;
            }
        } else {
            leftRightAngle = (int) Math.toDegrees(Math
                    .atan(((double) leftRight_b / (double) leftRight_a)))
                    * (-1);
            if (leftRightAngle <= leftLevel) {
                leftRightAngle = leftLevel;
            }
        }
    }

    public void getHeight(Hand hand) {
        height = (int) hand.palmPosition().toFloatArray()[1];
        if (height <= botLevel) {
            height = botLevel;
        }

        if (height >= topLevel) {
            height = topLevel;
        }
    }

    public void getHandDirection(Hand hand) {
        FingerList fingers = hand.fingers();
        for (Finger finger : fingers) {
            switch (finger.type().toString()) {
                case "TYPE_MIDDLE":
                    direction = Math
                            .round(finger.direction().toFloatArray()[0] * 100);
                    if (direction <= leftDirLevel) {
                        direction = leftDirLevel;
                    }
                    if (direction >= rightDirLevel) {
                        direction = rightDirLevel;
                    }
                    break;
            }

        }
    }

    public float convertRange(int oldMin, int oldMax, int newMin, int newMax,
            int value) {

        oldMin *= 1000;
        oldMax *= 1000;
        newMin *= 1000;
        newMax *= 1000;
        value *= 1000;

        int oldRange = (oldMax - oldMin);
        if (oldRange == 0) {
            return newMin;
        } else {
            int newRange = (newMax - newMin);
            return (((value - oldMin) * newRange) / oldRange) + newMin;
        }
    }

    public int convertServoRange(int oldMin, int oldMax, int newMin, int newMax,
            float value) {

        int oldRange = (oldMax - oldMin);
        if (oldRange == 0) {
            return newMin;
        } else {
            int newRange = (newMax - newMin);
            return (int) ((((value - oldMin) * newRange) / oldRange) + newMin);
        }
    }

    public void setExpoValues() {
        expoConst = Float.parseFloat(ui.expoConstTextField.getText()) / 100;

        servo1 = convertRange(downLevel, upLevel, -1, 1, upDownAngle) / 1000;
        servo2 = convertRange(leftLevel, rightLevel, -1, 1, leftRightAngle) / 1000;
        servo3 = convertRange(botLevel, topLevel, -1, 1, height) / 1000;
        servo4 = convertRange(leftDirLevel, rightDirLevel, -1, 1, direction) / 1000;

        servo1 = (float) ((1 - expoConst) * servo1 + expoConst
                * Math.pow(servo1, 3));

        servo2 = (float) ((1 - expoConst) * servo2 + expoConst
                * Math.pow(servo2, 3));

        servo3 = (float) ((1 - expoConst) * servo3 + expoConst
                * Math.pow(servo3, 3));

        servo4 = (float) ((1 - expoConst) * servo4 + expoConst
                * Math.pow(servo4, 3));
    }

    public void setServoValues() {
        servo1 = convertServoRange(-1, 1, 0, 250, servo1);
        servo2 = convertServoRange(-1, 1, 0, 250, servo2);
        servo3 = convertServoRange(-1, 1, 0, 250, servo3);
        servo4 = convertServoRange(-1, 1, 0, 250, servo4);
    }

    public void writeServoValues() throws IOException {
        sc.setServo(0, (int) servo3);
        sc.setServo(1, (int) servo2);
        sc.setServo(2, (int) servo1);
        sc.setServo(3, (int) servo4);
    }
}
