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
        // Varsayılan olarak SSF ve Linear Probing ile başlatıyoruz
    	this.articleMap = new MyHashTable<>(50000, 0.8, false, false);
        this.indexMap = new MyHashTable<>(300000, 0.8, false, false); 
        this.stopWords = new MyHashTable<>(1000, 0.8, false, false);
    }
    
    // Testler için tabloyu özel parametrelerle yeniden başlatan metot
    public void resetEngine(double loadFactor, boolean usePAF, boolean useDH) {
        this.articleMap = new MyHashTable<>(1000, loadFactor, usePAF, useDH);
        this.indexMap = new MyHashTable<>(1000, loadFactor, usePAF, useDH);
    }

    public void loadStopWords(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                stopWords.put(line.trim(), "dummy");
            }
        } catch (IOException e) {
            System.out.println("Stop words dosyası bulunamadı: " + filePath);
        }
    }

    public void loadArticles(String filePath) {
        String delimiterRegex = "[^a-zA-Z0-9]+";
        
        long startTime = System.currentTimeMillis(); // Süre ölçümü için
        int count = 0; // Sayaç

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine(); // Başlığı atla
            
            System.out.println("Veri okuma başladı, lütfen bekleyin...");
            
            while ((line = br.readLine()) != null) {
                // PDF'teki Regex (Bu işlem yavaştır, sabır gerektirir)
                String[] columns = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                //String[] columns = line.split(",");
                
                if (columns.length >= 3) {
                    String id = columns[0];
                    String headline = columns[2];
                    String text = columns[columns.length - 1];

                    Article article = new Article(id, headline);
                    articleMap.put(id, article);

                    String[] words = text.split(delimiterRegex);
                    for (String word : words) {
                        word = word.toLowerCase();
                        if (word.length() < 2 || stopWords.containsKey(word)) continue;

                        if (!indexMap.containsKey(word)) {
                            indexMap.put(word, new MyHashTable<>(10, 0.8, false, false));
                        }

                        MyHashTable<String, Integer> postingList = indexMap.get(word);
                        int currentCount = 0;
                        if (postingList.containsKey(id)) {
                            currentCount = postingList.get(id);
                        }
                        postingList.put(id, currentCount + 1);
                    }
                }

                // --- İLERLEME GÖSTERGESİ ---
                count++;
                if (count % 1000 == 0) {
                    // Her 1000 makalede bir ekrana bilgi yaz
                    System.out.println(count + " makale işlendi... (" + (System.currentTimeMillis() - startTime) / 1000 + " sn)");
                }
            }
        } catch (IOException e) {
            System.out.println("Dosya okuma hatası: " + filePath);
        }
        System.out.println("Tüm makaleler yüklendi! Toplam: " + count);
    }

    public Article searchById(String id) {
        return articleMap.get(id);
    }

    public void searchByText(String query) {
        String[] words = query.toLowerCase().split("[^a-zA-Z0-9]+");
        MyHashTable<String, Integer> articleScores = new MyHashTable<>(100, 0.8, false, false);
        
        System.out.println("Aranan: " + query);
        
        for (String word : words) {
            if (stopWords.containsKey(word)) continue;

            if (indexMap.containsKey(word)) {
                MyHashTable<String, Integer> postings = indexMap.get(word);
                ArrayList<String> ids = postings.getKeys();
                
                for (String artId : ids) {
                    int count = postings.get(artId);
                    int currentScore = 0;
                    if (articleScores.containsKey(artId)) {
                        currentScore = articleScores.get(artId);
                    }
                    articleScores.put(artId, currentScore + count);
                }
            }
        }

        // Sonuçları sıralama
        ArrayList<SearchResult> results = new ArrayList<>();
        ArrayList<String> foundIds = articleScores.getKeys();
        for (String artId : foundIds) {
            results.add(new SearchResult(artId, articleScores.get(artId)));
        }
        Collections.sort(results);

        System.out.println("--- En Alakalı 5 Haber ---");
        int count = 0;
        for (SearchResult res : results) {
            if (count >= 5) break;
            Article art = articleMap.get(res.articleId);
            if (art != null) {
                System.out.println("ID: " + res.articleId + " | Puan: " + res.score + " | Başlık: " + art.getHeadline());
            }
            count++;
        }
        if (count == 0) System.out.println("Sonuç bulunamadı.");
        System.out.println("--------------------------");
    }

    public void runPerformanceTests() {
        double[] loadFactors = {0.5, 0.8};
        boolean[] hashFunctions = {false, true}; // false=SSF, true=PAF
        boolean[] collisionTypes = {false, true}; // false=LP, true=DH
        
        // Arama kelimelerini yükle
        ArrayList<String> searchKeys = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("search.txt"))) {
            String line;
            while ((line = br.readLine()) != null) searchKeys.add(line.trim());
        } catch (IOException e) { e.printStackTrace(); }

        System.out.println(String.format("%-10s %-10s %-10s %-15s %-20s %-20s", 
            "LoadFac", "HashFunc", "CollType", "Collisions", "IndexTime(ms)", "AvgSearch(ns)"));
        
        for (double lf : loadFactors) {
            for (boolean isPaf : hashFunctions) {
                for (boolean isDh : collisionTypes) {
                    
                    resetEngine(lf, isPaf, isDh); // Motoru yeni ayarlarla sıfırla
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

                    System.out.println(String.format("%-10.1f %-10s %-10s %-15d %-20.2f %-20.2f", 
                        lf, (isPaf ? "PAF" : "SSF"), (isDh ? "DH" : "LP"), 
                        indexMap.getCollisionCount(), indexTime, avgSearch));
                }
            }
        }
    }
}