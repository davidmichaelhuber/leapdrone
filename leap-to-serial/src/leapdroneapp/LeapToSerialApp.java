/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package leapdroneapp;

/**
 *
 * @author davidmichaelhuber
 */
public class LeapToSerialApp {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        UserInterface ui = new UserInterface();
        SerialController sc = new SerialController();
        LeapController lc = new LeapController();
        
        ui.sc = sc;
        ui.lc = lc;
        sc.ui = ui;
        sc.lc = lc;
        lc.ui = ui;
        lc.sc = sc;
        
        ui.createFrame();
    }
}