package src;

public class Article {
    private String id;
    private String headline;

    public Article(String id, String headline) {
        this.id = id;
        this.headline = headline;
    }

    public String getHeadline() { return headline; }
    
    @Override
    public String toString() {
        return "[" + id + "] " + headline;
    }
}