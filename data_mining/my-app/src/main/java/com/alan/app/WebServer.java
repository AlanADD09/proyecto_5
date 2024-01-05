package com.alan.app;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.Headers;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.Executors;

import javax.json.*;

public class WebServer {
    private static final String STATUS_ENDPOINT = "/status";
    private static final String SEARCH_ENDPOINT = "/search";
    private static final String MONITOR_ENDPOINT = "/monitor";

    private final int port;
    private HttpServer server;

    public WebServer(int port) {
        this.port = port;
    }

    public void startServer() {
        try {
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        HttpContext statusContext = server.createContext(STATUS_ENDPOINT);
        HttpContext searchContext = server.createContext(SEARCH_ENDPOINT);
        HttpContext monitorContext = server.createContext(MONITOR_ENDPOINT);

        statusContext.setHandler(this::handleStatusCheckRequest);
        searchContext.setHandler(this::handleSearchRequest);
        monitorContext.setHandler(this::handleMonitorRequest);

        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
    }

    private void handleStatusCheckRequest(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("get")) {
            exchange.close();
            return;
        }

        String responseMessage = "El servidor está vivo\n";
        sendResponse(responseMessage.getBytes(), exchange);
    }

    private void sendResponse(byte[] responseBytes, HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(200, responseBytes.length);
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(responseBytes);
        outputStream.flush();
        outputStream.close();
        exchange.close();
    }

    public void handleSearchRequest(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            System.out.println("search request");

            Headers headers = exchange.getResponseHeaders();
            headers.set("Access-Control-Allow-Origin", "http://localhost:3000");
            headers.set("Access-Control-Allow-Methods", "GET, POST");
            headers.set("Access-Control-Allow-Headers", "Content-Type");


            InputStream requestBody = exchange.getRequestBody();
            BufferedReader reader = new BufferedReader(new InputStreamReader(requestBody));

            StringBuilder requestBodyContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                requestBodyContent.append(line);
            }

            System.out.println("body: " + requestBodyContent.toString());
            try {
                JsonReader jsonReader = Json.createReader(new StringReader(requestBodyContent.toString()));
                JsonObject jsonObject = jsonReader.readObject();
                jsonReader.close();
                
                String input = jsonObject.getString("input");

                List<Book> books = Reader.Calculate_TF_ITF("src/main/resources/LIBROS_TXT/", input);
                
                JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
                for (Book book : books) {
                    if (book.getTF_ITF() != 0) {
                        System.out.println("Book: "+book.getName()+", TF_ITF: "+book.getTF_ITF());
                        // jsonArrayBuilder.add(book.getName());
                        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder()
                            .add("name", book.getName())
                            .add("TF_ITF", book.getTF_ITF());
                        jsonArrayBuilder.add(jsonObjectBuilder.build());
                    }
                }
                JsonArray jsonArray = jsonArrayBuilder.build();

                // Convertir el JsonArray a formato String
                String response = jsonArray.toString();
                sendResponse(response.getBytes(), exchange);
            } catch (JsonException e) {
                e.printStackTrace();
            }
        } else {
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_METHOD, 0);
        }
    }

    public void handleMonitorRequest(HttpExchange exchange) throws IOException{
        if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            System.out.println("monitor request");
            InputStream requestBody = exchange.getRequestBody();
            BufferedReader reader = new BufferedReader(new InputStreamReader(requestBody));

            StringBuilder requestBodyContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                requestBodyContent.append(line);
            }

            double cpuUsage = Monitoring.getCPUUsage();
            double ramUsage = Monitoring.getRAMUsage();

            JsonObjectBuilder jsonBuilder = Json.createObjectBuilder()
                .add("cpuUsage", cpuUsage)
                .add("ramUsage", ramUsage);
            JsonObject jsonObject = jsonBuilder.build();
            String jsonString = jsonObject.toString();
            sendResponse(jsonString.getBytes(), exchange);

        } else {
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_METHOD, 0);
        }
    }

}
