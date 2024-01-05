//Proyecto 5 y 6 -- Domínguez Durán Alan Axel -- 4CM11 -- DSD
package com.alan.app;

public class CPUData {
    private double cpuUsage;
    private double ramUsage;

    public CPUData(double cpuUsage, double ramUsage) {
        this.cpuUsage = cpuUsage;
        this.ramUsage = ramUsage;
    }
    
    public double getCpuUsage() {
        return cpuUsage;
    }
    
    public void setCpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }
    
    public double getRamUsage() {
        return ramUsage;
    }
    
    public void setRamUsage(double ramUsage) {
        this.ramUsage = ramUsage;
    }
}
