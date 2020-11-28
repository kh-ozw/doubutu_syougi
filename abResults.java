
public class abResults {
    private int point;
    private String bestMove;

    public abResults(int point, String bestMove) {
        this.point = point;
        this.bestMove = bestMove;
    }

    public int getPoint() {
        return this.point;
    }

    public String getBestMove() {
        return this.bestMove;
    }
}
