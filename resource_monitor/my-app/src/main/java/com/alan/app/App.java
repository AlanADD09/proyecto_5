//Proyecto 5 y 6 -- Domínguez Durán Alan Axel -- 4CM11 -- DSD
package com.alan.app;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class App {
    public static void main(String[] args) {
        if (args.length == 1) {
            String url_string = args[0];
            SwingUtilities.invokeLater(() -> {
            CPUUsageMonitor monitor = new CPUUsageMonitor("Resource Usage Monitor");
            monitor.pack();
            monitor.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            monitor.setVisible(true);
            monitor.startMonitoring("http://localhost:"+url_string+"/monitor");
        });
        } else {
            System.out.println("Provide a correct port");
        }
    }
}