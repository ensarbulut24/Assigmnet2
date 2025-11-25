package dataodev;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        SearchEngine engine = new SearchEngine();
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n--- MAIN MENU ---");
            System.out.println("1. Load Data (Select Settings)");
            System.out.println("2. Search by ID");
            System.out.println("3. Search by Keyword");
            System.out.println("4. Run Performance Tests (Table 1)");
            System.out.println("0. Exit");
            System.out.print("Choice: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    // --- 1. Soru: Hash Fonksiyonu ---
                    System.out.println("\n[Settings] Select Hash Function:");
                    System.out.println("1. Simple Summation Function (SSF)");
                    System.out.println("2. Polynomial Accumulation Function (PAF)");
                    System.out.print("Selection: ");
                    String hashChoice = scanner.nextLine();
                    boolean usePAF = hashChoice.equals("2");

                    // --- 2. Soru: Çakışma Yönetimi ---
                    System.out.println("\n[Settings] Select Collision Handling:");
                    System.out.println("1. Linear Probing (LP)");
                    System.out.println("2. Double Hashing (DH)");
                    System.out.print("Selection: ");
                    String collChoice = scanner.nextLine();
                    boolean useDH = collChoice.equals("2");
                    
                    // --- 3. Soru: Limit ---
                    System.out.println("\n[Settings] How many articles to load?");
                    System.out.println("(Enter '0' for ALL 28645 articles, or '100' for testing)");
                    System.out.print("Count: ");
                    
                    String limitStr = scanner.nextLine();
                    int limit = 0;
                    try {
                        limit = Integer.parseInt(limitStr);
                    } catch (NumberFormatException e) {
                        limit = 0; 
                    }

                    System.out.println("\nApplying settings: " + (usePAF ? "PAF" : "SSF") + 
                                       " & " + (useDH ? "DH" : "LP") + 
                                       " | Limit: " + (limit == 0 ? "ALL" : limit));
                    
                    // Motoru sıfırla
                    engine.resetEngine(0.8, usePAF, useDH);
                    engine.loadStopWords("stop_words_en.txt");

                    // GÜNCELLEME BURADA:
                    // loadArticles artık 3 parametre alıyor.
                    // Manuel yükleme olduğu için cache kullanma (false), diskten oku.
                    engine.loadArticles("CNN_Articels.csv", limit);
                    break;
                    
                case "2":
                    System.out.print("Article ID: ");
                    String id = scanner.nextLine();
                    Article article = engine.searchById(id);
                    if (article != null) {
                        System.out.println(article);
                    } else {
                        System.out.println("Not found.");
                    }
                    break;
                    
                case "3":
                    System.out.print("Keywords: ");
                    String query = scanner.nextLine();
                    engine.searchByText(query);
                    break;
                    
                case "4":
                    System.out.println("Running performance tests (Index creation from RAM cache)...");
                    // SearchEngine içindeki runPerformanceTests kendi içinde 
                    // loadArticles(..., true) çağırdığı için buraya dokunmuyoruz.
                    engine.runPerformanceTests();
                    break;
                    
                case "0":
                    running = false;
                    System.out.println("Exiting...");
                    break;
                    
                default:
                    System.out.println("Invalid choice.");
            }
        }
        scanner.close();
    }
}