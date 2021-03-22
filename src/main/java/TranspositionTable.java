import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;

public class TranspositionTable {
    public final int lookupFailed = Integer.MIN_VALUE;
    public final int exact = 0;
    public final int lowerBound = 1;
    public final int upperBound = 2;

    public Entry[] entries;

    public final long size;
    Board board;

    public TranspositionTable(Board board, int size) {
        this.board = board;
        this.size = size;

        entries = new Entry[size];
    }

    public void clear() {
        for (int i  = 0; i < entries.length; i++) {
            entries[i] = new Entry();
        }
    }

    public int index() {
        long key = Math.abs(board.getZobristKey());
        return (int) (key % size);
    }

    public Move getStoredMove() {
        return entries[index()].move;
    }

    public int lookupEvaluation(int depth, int plyFromRoot, int alpha, int beta) {
        Entry entry = entries[index()];

        if(entry.key == board.getZobristKey()) {
            if(entry.depth >= depth) {
                int correctScore = correctRetrievedMateScore(entry.value, plyFromRoot);
                if(entry.nodeType == exact) {
                    return correctScore;
                }
                if(entry.nodeType == upperBound && correctScore <= alpha) {
                    return correctScore;
                }
                if(entry.nodeType == lowerBound && correctScore >= beta) {
                    return correctScore;
                }
            }
        }
        return lookupFailed;
    }

    public void storeEval(int depth, int numPlySearched, int eval, int evalType, Move move) {
        Entry entry = new Entry(board.getZobristKey(), correctScoreToStore(eval, numPlySearched), (byte) depth, (byte) evalType, move);
        entries[(int)index()] = entry;
    }

    int correctScoreToStore(int score, int numPlySearched) {
        if(Searcher.isMateScore(score)) {
            int sign = (int) Math.signum(score);
            return(score * sign + numPlySearched) * sign;
        }
        return score;
    }

    int correctRetrievedMateScore(int score, int numPlySearched) {
        if(Searcher.isMateScore(score)) {
            int sign = (int) Math.signum(score);
            return(score * sign - numPlySearched) * sign;
        }
        return score;
    }
}
