//Proyecto 5 y 6 -- Domínguez Durán Alan Axel -- 4CM11 -- DSD
package com.alan.app;

import java.util.Map;

public class Book {
    private String name;
    private Map<String, Double> wordCount;
    private Double TF_ITF;

    // Constructor
    public Book(String name, Map<String, Double> wordCount, Double TF_ITF) {
        this.name = name;
        this.wordCount = wordCount;
        this.TF_ITF = TF_ITF;
    }

    // Getter y Setter para name
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Getter y Setter para wordCount
    public Map<String, Double> getWordCount() {
        return wordCount;
    }

    public void setWordCount(Map<String, Double> wordCount) {
        this.wordCount = wordCount;
    }

    // Getter y Setter para TF_ITF
    public Double getTF_ITF() {
        return TF_ITF;
    }

    public void setTF_ITF(Double TF_ITF) {
        this.TF_ITF = TF_ITF;
    }

    // Método creacional para crear un nuevo libro con nombre, conteo de palabras y TF_ITF dados
    public static Book createBook(String name, Map<String, Double> wordCount, Double TF_ITF) {
        return new Book(name, wordCount, TF_ITF);
    }
}
