//Proyecto 5 y 6 -- Domínguez Durán Alan Axel -- 4CM11 -- DSD
package com.alan.app;

import org.jfree.data.xy.XYSeries;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;

public class Monitoring {
    public static double getCPUUsage() {
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        double cpuUsage = operatingSystemMXBean.getSystemLoadAverage() * 100; // Get CPU usage as a percentage
        return cpuUsage;
    }

    public static double getRAMUsage() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage memoryUsage = memoryMXBean.getHeapMemoryUsage();
        double usedMemory = memoryUsage.getUsed();
        double maxMemory = memoryUsage.getMax();
        double ramUsage = (usedMemory / maxMemory) * 100; // Get RAM usage as a percentage
        return ramUsage;
    }

    public static double calculateAreaUnderCurve(XYSeries series) {
        double area = 0.0;
        int itemCount = series.getItemCount();
        for (int i = 1; i < itemCount; i++) {
            double x1 = series.getX(i - 1).doubleValue();
            double x2 = series.getX(i).doubleValue();
            double y1 = series.getY(i - 1).doubleValue();
            double y2 = series.getY(i).doubleValue();
            area += ((x2 - x1) * (y1 + y2)) / 2.0;
        }
        return area;
    }
}
