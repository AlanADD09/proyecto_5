//Proyecto 5 y 6 -- Domínguez Durán Alan Axel -- 4CM11 -- DSD
package com.alan.app;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.Headers;
import java.net.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import javax.json.*;

public class WebServer {
    private static final String STATUS_ENDPOINT = "/status";
    private static final String PROCESS_ENDPOINT = "/process";
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
        HttpContext processContext = server.createContext(PROCESS_ENDPOINT);
        HttpContext monitorContext = server.createContext(MONITOR_ENDPOINT);

        statusContext.setHandler(this::handleStatusCheckRequest);
        processContext.setHandler(this::handleProcessRequest);
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

    public void handleProcessRequest(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            System.out.println("process request");

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
                List<Book> finalList = new ArrayList<>();
                JsonReader jsonReader = Json.createReader(new StringReader(requestBodyContent.toString()));
                JsonObject jsonObject = jsonReader.readObject();
                jsonReader.close();
                String input = jsonObject.getString("input");
                CompletableFuture<List<Book>> request1 = solicitudAsincronica(input,"http://localhost:8081/search", 2000);
                CompletableFuture<List<Book>> request2 = solicitudAsincronica(input,"http://localhost:8082/search", 2000);
                CompletableFuture<List<Book>> request3 = solicitudAsincronica(input,"http://localhost:8083/search", 2000);
                CompletableFuture<Void> all_requests = CompletableFuture.allOf(request1);
                all_requests.thenRun(() -> {
                    try {
                        List<Book> response1 = request1.get();
                        List<Book> response2 = request2.get();
                        List<Book> response3 = request3.get();
                        finalList.addAll(response1);
                        finalList.addAll(response2);
                        finalList.addAll(response3);
        
                        System.out.println("Resultado de la Solicitud 1: " + response1);
                        System.out.println("Resultado de la Solicitud 2: " + response2);
                        System.out.println("Resultado de la Solicitud 3: " + response3);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
        
                // Bloquear hasta que todas las solicitudes se completen
                all_requests.join();
                Collections.sort(finalList, new Comparator<Book>() {
                    @Override
                    public int compare(Book libro1, Book libro2) {
                        // Ordenar de mayor a menor
                        return Double.compare(libro2.getTF_ITF(), libro1.getTF_ITF());
                    }
                });
                
                JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
                for (Book book : finalList) {
                    if (book.getTF_ITF() != 0) {
                        System.out.println("Book: "+book.getName()+", TF_ITF: "+book.getTF_ITF());
                        jsonArrayBuilder.add(book.getName());   
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

    private static CompletableFuture<List<Book>> solicitudAsincronica(String input, String url_string, int tiempoEspera) {
        return CompletableFuture.supplyAsync(() -> {
            List<Book> books = new ArrayList<>();
            try {
                URL url = new URL(url_string);
                JsonObject jsonObjectInput = Json.createObjectBuilder().add("input", input).build();
                
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");

                OutputStream outputStream = connection.getOutputStream();
                JsonWriter jsonWriter = Json.createWriter(new OutputStreamWriter(outputStream));

                jsonWriter.writeObject(jsonObjectInput);
                jsonWriter.close();

                int responseCode = connection.getResponseCode();
                System.out.println("Código de respuesta: " + responseCode);

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    StringBuilder response = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    connection.disconnect();
    
                    System.out.println("Respuesta del servidor: " + response.toString());
                    JsonReader jsonReader = Json.createReader(new StringReader(response.toString()));
                    JsonArray jsonArray = jsonReader.readArray();
                    jsonReader.close();
                    for (JsonObject jsonObject : jsonArray.getValuesAs(JsonObject.class)) {
                        // Mapeo de los valores del JSON a un objeto personalizado MyObject
                        String name = jsonObject.getString("name");
                        double TF_ITF = jsonObject.getJsonNumber("TF_ITF").doubleValue();
    
                        Book book = Book.createBook(name, TF_ITF);
                        books.add(book);
                    }
                    // return books;
                } finally {
                    connection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(tiempoEspera);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Funcion asincriona completada.");
            return books;

            // Devolver una lista de ejemplo como resultado
            
        });
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
