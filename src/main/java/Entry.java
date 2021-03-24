import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;

public final class Entry {
    public long key;
    public int value;
    public Move move;
    public byte depth;
    public byte nodeType;

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
