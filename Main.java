package src;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        SearchEngine engine = new SearchEngine();
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n1. Load Data");
            System.out.println("2. Search by ID");
            System.out.println("3. Search by Keyword");
            System.out.println("4. Run Performance Tests");
            System.out.println("0. Exit");
            System.out.print("Choice: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    engine.loadStopWords("stop_words_en.txt");
                    engine.loadArticles("CNN_Articels.csv"); 
                    System.out.println("Data loaded.");
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
                    System.out.println("Running tests...");
                    engine.runPerformanceTests();
                    break;
                case "0":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
        scanner.close();
    }
}