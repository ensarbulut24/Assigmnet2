package src;

public class Article {
    private String id;
    private String category;
    private String section;
    private String headline;

    public Article(String id, String category, String section, String headline) {
        this.id = id;
        this.category = category;
        this.section = section;
        this.headline = headline;
    }

    public String getHeadline() { return headline; }
    
    @Override
    public String toString() {
        // ID araması yapıldığında ekrana böyle basılacak
        return String.format("[%s] %s | %s | %s", id, category, section, headline);
    }

}
