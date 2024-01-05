//Proyecto 5 y 6 -- Domínguez Durán Alan Axel -- 4CM11 -- DSD
package com.alan.app;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.*;

public class CPUUsageMonitor extends JFrame {
    private XYSeries cpuSeries;
    private XYSeries ramSeries;
    private double cpuCost;
    private double ramCost;
    private Timer timer;
    private double f1 = 1.0; // Factor para el costo de CPU
    private double f2 = 1.0; // Factor para el costo de memoria
    private JLabel cpuCostLabel;
    private JLabel ramCostLabel;
    private JLabel totalCostLabel;

    public CPUUsageMonitor(String title) {
        super(title);
        cpuSeries = new XYSeries("CPU Usage");
        ramSeries = new XYSeries("RAM Usage");
    
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(cpuSeries);
        dataset.addSeries(ramSeries);
    
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Resource Usage Monitor",
                "Time",
                "Usage (%)",
                dataset
        );
    
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));
        setLayout(new BorderLayout());
        add(chartPanel, BorderLayout.CENTER);
        
        JPanel costPanel = new JPanel(new GridLayout(1, 3)); // Aumenta a 3 filas para incluir el totalCostLabel
        cpuCostLabel = new JLabel("CPU Cost: ");
        ramCostLabel = new JLabel("RAM Cost: ");
        totalCostLabel = new JLabel("Total Cost: ");
        costPanel.add(cpuCostLabel);
        costPanel.add(ramCostLabel);
        costPanel.add(totalCostLabel);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(costPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void startMonitoring(String url_string) {
        timer = new Timer(1000, e -> {
            CPUData data = getServerData(url_string);
            double cpuUsage = data.getCpuUsage();
            double ramUsage = data.getRamUsage();
            double cpuArea = calculateAreaUnderCurve(cpuSeries);
            double ramArea = calculateAreaUnderCurve(ramSeries);
            double totalCost = 0.0;

            cpuCost = cpuArea * f1;
            ramCost = ramArea * f2;
            totalCost += cpuCost + ramCost;

            cpuSeries.addOrUpdate(System.currentTimeMillis(), cpuUsage);
            ramSeries.addOrUpdate(System.currentTimeMillis(), ramUsage);

            String cpuText = String.format("%.3f", cpuCost/1000000);
            String ramText = String.format("%.3f", ramCost/1000000);
            String totalText = String.format("%.3f", totalCost/1000000);
            System.out.println("cpuCost: "+cpuCost);
            System.out.println("ramCost: "+ramCost);
            System.out.println("totalCost: "+totalCost);
            cpuCostLabel.setText("CPU Cost: " + cpuText);
            ramCostLabel.setText("RAM Cost: " + ramText);
            totalCostLabel.setText("Total Cost: " + totalText);
        });
        timer.start();
    }

    private double calculateAreaUnderCurve(XYSeries series) {
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

    public CPUData getServerData(String url_string){
        try {
            CPUData data;
            URL url = new URL(url_string);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            int responseCode = connection.getResponseCode();
            System.out.println("Código de respuesta: " + responseCode);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                Double cpuUsage;
                Double ramUsage;
                StringBuilder response = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                connection.disconnect();

                System.out.println("Respuesta del servidor: " + response.toString());
                JsonReader jsonReader = Json.createReader(new StringReader(response.toString()));
                JsonObject jsonObject = jsonReader.readObject();
                jsonReader.close();
                cpuUsage = jsonObject.getJsonNumber("cpuUsage").doubleValue();
                ramUsage = jsonObject.getJsonNumber("ramUsage").doubleValue();
                data = new CPUData(cpuUsage, ramUsage);

                System.out.println("data: " + data.getCpuUsage()+", "+data.getRamUsage());
            }
            return data;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
