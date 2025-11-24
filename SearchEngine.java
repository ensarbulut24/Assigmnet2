package src;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class SearchEngine {
    private MyHashTable<String, Article> articleMap;
    private MyHashTable<String, MyHashTable<String, Integer>> indexMap;
    private MyHashTable<String, String> stopWords;

    public SearchEngine() {
        this.articleMap = new MyHashTable<>(40000, 0.8, false, false);
        this.indexMap = new MyHashTable<>(100000, 0.8, false, false);
        this.stopWords = new MyHashTable<>(1000, 0.8, false, false);
    }

    public void resetEngine(double loadFactor, boolean usePAF, boolean useDH) {
        this.articleMap = new MyHashTable<>(40000, loadFactor, usePAF, useDH);
        this.indexMap = new MyHashTable<>(100000, loadFactor, usePAF, useDH);
        this.stopWords = new MyHashTable<>(1000, 0.8, false, false);
    }

    public void loadStopWords(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if(!line.trim().isEmpty())
                    stopWords.put(line.trim(), "ignored");
            }
        } catch (IOException e) {
            System.out.println("File error: " + filePath);
        }
    }

    public void loadArticles(String filePath) {
        System.out.println("Loading articles (LIMIT: 100)...");
        long startTime = System.currentTimeMillis();
        int count = 0;
        int limit = 100; 

        try (BufferedReader br = new BufferedReader(new FileReader(filePath), 1024 * 1024)) {
            String headerLine = br.readLine();
            if (headerLine != null) {
                ArrayList<String> headers = parseCSVLine(headerLine);
                System.out.println("Detected Columns: " + headers);
            }
            
            String line;
            while ((line = br.readLine()) != null && count < limit) {
                ArrayList<String> columns = parseCSVLine(line);
                
                if (columns.size() >= 5) {
                    String id = columns.get(0);
                    
                    // DÜZELTME: Index 2 (Tarih) yerine Index 4 (Başlık) deniyoruz
                    String headline = columns.get(4); 
                    
                    String text = columns.get(columns.size() - 1);

                    if(id.length() > 1 && id.charAt(0) == '"') id = id.substring(1, id.length()-1);
                    if(headline.length() > 1 && headline.charAt(0) == '"') headline = headline.substring(1, headline.length()-1);
                    if(text.length() > 1 && text.charAt(0) == '"') text = text.substring(1, text.length()-1);

                    Article article = new Article(id, headline);
                    articleMap.put(id, article);

                    String[] words = text.split("[^a-zA-Z0-9]+");
                    
                    for (String word : words) {
                        if (word.length() < 2) continue;
                        word = word.toLowerCase();
                        if (stopWords.containsKey(word)) continue;

                        if (!indexMap.containsKey(word)) {
                            indexMap.put(word, new MyHashTable<>(10, 0.8, false, false));
                        }

                        MyHashTable<String, Integer> postingList = indexMap.get(word);
                        int currentCount = 0;
                        Integer val = postingList.get(id);
                        if (val != null) currentCount = val;
                        
                        postingList.put(id, currentCount + 1);
                    }
                }
                count++;
            }
            System.out.println("\nFinished loading " + count + " articles in " + (System.currentTimeMillis() - startTime) + "ms.");
            
        } catch (IOException e) {
            System.out.println("File error: " + filePath);
        }
    }

    private ArrayList<String> parseCSVLine(String line) {
        ArrayList<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                inQuotes = !inQuotes;
                sb.append(c); 
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString());
        return tokens;
    }

    public Article searchById(String id) {
        return articleMap.get(id);
    }

    public void searchByText(String query) {
        String[] words = query.toLowerCase().split("[^a-zA-Z0-9]+");
        MyHashTable<String, Integer> articleScores = new MyHashTable<>(100, 0.8, false, false);
        
        boolean foundAnyWord = false;
        for (String word : words) {
            if (stopWords.containsKey(word)) continue;

            if (indexMap.containsKey(word)) {
                foundAnyWord = true;
                MyHashTable<String, Integer> postings = indexMap.get(word);
                ArrayList<String> ids = postings.getKeys();
                
                for (String artId : ids) {
                    int count = postings.get(artId);
                    int currentScore = 0;
                    Integer val = articleScores.get(artId);
                    if (val != null) currentScore = val;
                    articleScores.put(artId, currentScore + count);
                }
            }
        }

        if (!foundAnyWord) {
            System.out.println("No results found.");
            return;
        }

        ArrayList<SearchResult> results = new ArrayList<>();
        ArrayList<String> foundIds = articleScores.getKeys();
        for (String artId : foundIds) {
            results.add(new SearchResult(artId, articleScores.get(artId)));
        }
        Collections.sort(results);

        int count = 0;
        for (SearchResult res : results) {
            if (count >= 5) break;
            Article art = articleMap.get(res.articleId);
            if (art != null) {
                System.out.println(res.articleId + " | Score: " + res.score + " | " + art.getHeadline());
            }
            count++;
        }
    }

    public void runPerformanceTests() {
        double[] loadFactors = {0.5, 0.8};
        boolean[] hashFunctions = {false, true};
        boolean[] collisionTypes = {false, true};
        
        ArrayList<String> searchKeys = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("search.txt"))) {
            String line;
            while ((line = br.readLine()) != null) searchKeys.add(line.trim());
        } catch (IOException e) { 
            System.out.println("search.txt not found");
            return;
        }

        System.out.printf("%-10s %-10s %-10s %-15s %-20s %-20s\n", 
            "LoadFac", "HashFunc", "CollType", "Collisions", "IndexTime(ms)", "AvgSearch(ns)");
        
        for (double lf : loadFactors) {
            for (boolean isPaf : hashFunctions) {
                for (boolean isDh : collisionTypes) {
                    
                    System.out.println("\n--- TEST: LF=" + lf + ", PAF=" + isPaf + ", DH=" + isDh + " ---");
                    resetEngine(lf, isPaf, isDh);
                    loadStopWords("stop_words_en.txt");

                    long startIndex = System.nanoTime();
                    loadArticles("CNN_Articels.csv");
                    long endIndex = System.nanoTime();
                    
                    double indexTime = (endIndex - startIndex) / 1_000_000.0;

                    long startSearch = System.nanoTime();
                    for(String key : searchKeys) {
                        indexMap.get(key);
                    }
                    long endSearch = System.nanoTime();
                    double avgSearch = (double)(endSearch - startSearch) / searchKeys.size();

                    System.out.printf("RESULT: %-10.1f %-10s %-10s %-15d %-20.2f %-20.2f\n", 
                        lf, (isPaf ? "PAF" : "SSF"), (isDh ? "DH" : "LP"), 
                        indexMap.getCollisionCount(), indexTime, avgSearch);
                    
                    System.gc();
                }
            }
        }
    }
}