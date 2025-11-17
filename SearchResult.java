package src;

public class SearchResult implements Comparable<SearchResult> {
    String articleId;
    int score;

    public SearchResult(String articleId, int score) {
        this.articleId = articleId;
        this.score = score;
    }

    @Override
    public int compareTo(SearchResult other) {
        return Integer.compare(other.score, this.score);
    }
}
