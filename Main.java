package src;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        SearchEngine engine = new SearchEngine();
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        System.out.println("### Haber Arama Motoru ###");

        while (running) {
            System.out.println("\nLütfen bir işlem seçiniz:");
            System.out.println("1. Verisetini Yükle ve İndeksle");
            System.out.println("2. ID ile Makale Ara");
            System.out.println("3. Metin (Kelime) ile Haber Ara");
            System.out.println("4. Performans Testlerini Çalıştır");
            System.out.println("0. Çıkış");
            System.out.print("Seçiminiz: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    System.out.println("Veriler yükleniyor...");
                    engine.loadStopWords("stop_words_en.txt");
                    // Dosya ismini projenizdeki gerçek dosya ismiyle eşleştirin
                    engine.loadArticles("CNN_Articels.csv"); 
                    System.out.println("Yükleme tamamlandı.");
                    break;
                case "2":
                    System.out.print("Aranacak Makale ID'si: ");
                    String id = scanner.nextLine();
                    Article article = engine.searchById(id);
                    if (article != null) {
                        System.out.println("Bulunan Makale: " + article);
                    } else {
                        System.out.println("Bu ID ile kayıtlı makale bulunamadı.");
                    }
                    break;
                case "3":
                    System.out.print("Aranacak Kelimeler: ");
                    String query = scanner.nextLine();
                    engine.searchByText(query);
                    break;
                case "4":
                    System.out.println("Performans testleri başlatılıyor (Bu işlem biraz sürebilir)...");
                    engine.runPerformanceTests();
                    break;
                case "0":
                    running = false;
                    System.out.println("Çıkış yapılıyor.");
                    break;
                default:
                    System.out.println("Geçersiz seçim, tekrar deneyin.");
            }
        }
        scanner.close();
    }
}
