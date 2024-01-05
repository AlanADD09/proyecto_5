//Proyecto 5 y 6 -- Domínguez Durán Alan Axel -- 4CM11 -- DSD
package com.alan.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Reader {

    public static Map<String, Double> wordCountITF = new HashMap<>();
    // public static void main(String[] args) {
    //     List<Book> books = Calculate_TF_ITF("LIBROS_TXT/", "Los hombres grises");
    //     for (Book book : books) {
    //         System.out.println("Book: "+book.getName()+", TF_ITF: "+book.getTF_ITF());
    //     }
    // }

    public static List<String> StringToList(String s){
        List<String> words = Arrays.stream(s.split(" ")).map(String::toLowerCase).collect(Collectors.toList());
        for (String string : words) {
            System.out.println("Palabra: "+string);
        }
        return words;
    }

    public static Book WordCounter(List<String> searchWords, String filename, String path){
        Map<String, Double> wordCount = new HashMap<>();

        try {
            FileReader fileReader = new FileReader(path+filename);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            StringBuilder wordSb = new StringBuilder();
            String word;
            int character;
            char c;
            int count = 0;
            while ((character = bufferedReader.read()) != -1) {
                c = (char) character;
                if (Character.isLetterOrDigit(c)) {
                    wordSb.append(c);
                } else if (wordSb.length() > 0) {
                    word = wordSb.toString().toLowerCase();
                    count++;
                    // System.out.println("Word: "+word);
                    if (searchWords.contains(word)){
                        if (wordCount.containsKey(word)) {
                            wordCount.put(word, wordCount.get(word) + 1);
                        } else {
                            wordCount.put(word, 1.0);
                        }
                    }
                    wordSb.setLength(0);
                    word = null;
                }
            }
            System.out.println("Libro: "+filename);
            for (Map.Entry<String, Double> entry : wordCount.entrySet()) {
                System.out.println("Palabra: '" + entry.getKey() + "' - Apariciones: " + entry.getValue() + ", TF: "+entry.getValue()/count);
                if (entry.getValue() > 0) {
                    if (wordCountITF.get(entry.getKey()) != null) {
                        wordCountITF.put(entry.getKey(), wordCountITF.get(entry.getKey()) + 1);   
                    } else {
                        wordCountITF.put(entry.getKey(), 1.0);
                    }
                }
                wordCount.put(entry.getKey(), entry.getValue()/count);   
            }
            System.out.println("Numero total de palabras: "+count);
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Book book = Book.createBook(filename, wordCount, 0.0);
        return book;
    }

    public static List<Book> ListFiles(String path, String input){
        List<String> words = StringToList(input);
        List<Book> books = new ArrayList<>();
        List<Book> books_TF_ITF = new ArrayList<>();
        Double book_count = 0.0;

        // Crear un objeto File que represente el directory
        File directory = new File(path);

        // Verificar si es un directory válido
        if (directory.isDirectory()) {
            // Array para almacenar los nombres de los archivos
            File[] archivos = directory.listFiles();

            // Recorrer los archivos y agregar sus nombres a la lista
            if (archivos != null) {
                for (File archivo : archivos) {
                    if (archivo.isFile()) {
                        Book book;
                        book = WordCounter(words, archivo.getName(), path);
                        books.add(book);
                        book_count++;
                    }
                }
            }
            System.out.println("wordCountITF: "+wordCountITF);
            System.out.println("Numero de libros: "+book_count);
            for (Book book : books) {
                Double value = 0.0;
                for (Map.Entry<String, Double> entry : book.getWordCount().entrySet()) {
                    value = value + entry.getValue()*Math.log10(book_count/wordCountITF.get(entry.getKey()));   
                }
                book.setTF_ITF(value);
                books_TF_ITF.add(book);
            }
            wordCountITF.clear();
        } else {
            System.out.println("La ruta especificada no es un directorio válido.");
        }
        return books_TF_ITF;
    }

    public static List<Book> Calculate_TF_ITF(String path, String input){
        List<Book> listaLibros = new ArrayList<>();
        listaLibros = ListFiles(path, input);
        Collections.sort(listaLibros, new Comparator<Book>() {
            @Override
            public int compare(Book libro1, Book libro2) {
                // Ordenar de mayor a menor
                return Double.compare(libro2.getTF_ITF(), libro1.getTF_ITF());
            }
        });
        return listaLibros;
    }
}
