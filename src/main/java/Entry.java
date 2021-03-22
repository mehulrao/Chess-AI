import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;

public class Entry {
    public final long key;
    public final int value;
    public final Move move;
    public final byte depth;
    public final byte nodeType;

    public Entry(long key, int value, byte depth, byte nodeType, Move move) {
        this.key = key;
        this.value = value;
        this.depth = depth;
        this.nodeType = nodeType;
        this.move = move;
    }
    public Entry() {
        this.key = 0;
        this.value = 0;
        this.depth = 0;
        this.nodeType = 0;
        this.move = new Move(Square.NONE, Square.NONE);
    }
}
