import com.github.bhlangonijr.chesslib.*;


public final class Evaluation {
    private static final int[] pawns = {
            0,  0,  0,  0,  0,  0,  0,  0,
            50, 50, 50, 50, 50, 50, 50, 50,
            10, 10, 20, 30, 30, 20, 10, 10,
            5,  5, 10, 25, 25, 10,  5,  5,
            0,  0,  0, 20, 20,  0,  0,  0,
            5, -5,-10,  0,  0,-10, -5,  5,
            5, 10, 10,-20,-20, 10, 10,  5,
            0,  0,  0,  0,  0,  0,  0,  0
    };

    private static final int[] knights = {
            -50,-40,-30,-30,-30,-30,-40,-50,
            -40,-20,  0,  0,  0,  0,-20,-40,
            -30,  0, 10, 15, 15, 10,  0,-30,
            -30,  5, 15, 20, 20, 15,  5,-30,
            -30,  0, 15, 20, 20, 15,  0,-30,
            -30,  5, 10, 15, 15, 10,  5,-30,
            -40,-20,  0,  5,  5,  0,-20,-40,
            -50,-40,-30,-30,-30,-30,-40,-50,
    };

    private static final int[] bishops = {
            -20,-10,-10,-10,-10,-10,-10,-20,
            -10,  0,  0,  0,  0,  0,  0,-10,
            -10,  0,  5, 10, 10,  5,  0,-10,
            -10,  5,  5, 10, 10,  5,  5,-10,
            -10,  0, 10, 10, 10, 10,  0,-10,
            -10, 10, 10, 10, 10, 10, 10,-10,
            -10,  5,  0,  0,  0,  0,  5,-10,
            -20,-10,-10,-10,-10,-10,-10,-20,
    };

    private static final int[] rooks = {
            0,  0,  0,  0,  0,  0,  0,  0,
            5, 10, 10, 10, 10, 10, 10,  5,
            -5,  0,  0,  0,  0,  0,  0, -5,
            -5,  0,  0,  0,  0,  0,  0, -5,
            -5,  0,  0,  0,  0,  0,  0, -5,
            -5,  0,  0,  0,  0,  0,  0, -5,
            -5,  0,  0,  0,  0,  0,  0, -5,
            0,  0,  0,  5,  5,  0,  0,  0
    };

    private static final int[] queens = {
            -20,-10,-10, -5, -5,-10,-10,-20,
            -10,  0,  0,  0,  0,  0,  0,-10,
            -10,  0,  5,  5,  5,  5,  0,-10,
            -5,  0,  5,  5,  5,  5,  0, -5,
            0,  0,  5,  5,  5,  5,  0, -5,
            -10,  5,  5,  5,  5,  5,  0,-10,
            -10,  0,  5,  0,  0,  0,  0,-10,
            -20,-10,-10, -5, -5,-10,-10,-20
    };

    private static final int[] kingMiddle = {
            -30,-40,-40,-50,-50,-40,-40,-30,
            -30,-40,-40,-50,-50,-40,-40,-30,
            -30,-40,-40,-50,-50,-40,-40,-30,
            -30,-40,-40,-50,-50,-40,-40,-30,
            -20,-30,-30,-40,-40,-30,-30,-20,
            -10,-20,-20,-20,-20,-20,-20,-10,
            20, 20,  0,  0,  0,  0, 20, 20,
            20, 30, 10,  0,  0, 10, 30, 20
    };

    private static final int[] kingEnd = {
            -50,-40,-30,-20,-20,-30,-40,-50,
            -30,-20,-10,  0,  0,-10,-20,-30,
            -30,-10, 20, 30, 30, 20,-10,-30,
            -30,-10, 30, 40, 40, 30,-10,-30,
            -30,-10, 30, 40, 40, 30,-10,-30,
            -30,-10, 20, 30, 30, 20,-10,-30,
            -30,-30,  0,  0,  0,  0,-30,-30,
            -50,-30,-30,-30,-30,-30,-30,-50
    };

    public final int pawnValue = 100;
    public final int knightValue = 320;
    public final int bishopValue = 330;
    public final int rookValue = 500;
    public final int queenValue = 900;

    final float endgameMaterialStart = rookValue * 2 + bishopValue + knightValue;
    Board board;

    public final int Evaluate (Board board) {
        this.board = board;
        int whiteEval = 0;
        int blackEval = 0;
        int perspective = 0;
        int whiteMaterial = CountMaterial(Side.WHITE);
        int blackMaterial = CountMaterial(Side.BLACK);

        /*
        int whiteMaterialWithoutPawns = whiteMaterial - board.pawns[Board.WhiteIndex].Count * pawnValue;
        int blackMaterialWithoutPawns = blackMaterial - board.pawns[Board.BlackIndex].Count * pawnValue;
        float whiteEndgamePhaseWeight = EndgamePhaseWeight (whiteMaterialWithoutPawns);
        float blackEndgamePhaseWeight = EndgamePhaseWeight (blackMaterialWithoutPawns);
        */

        whiteEval += whiteMaterial;
        whiteEval += evaluateTablesForSide(Side.WHITE);
        blackEval += blackMaterial;
        blackEval += evaluateTablesForSide(Side.BLACK);
        if(board.getSideToMove() == Side.WHITE && board.isKingAttacked()) {
            whiteEval -= 25;
        }

        if(board.getSideToMove() == Side.BLACK && board.isKingAttacked()) {
            blackEval -= 25;
        }

        int eval = whiteEval - blackEval;

        if(board.getSideToMove() == Side.WHITE) {
            perspective = 1;

        }
        if(board.getSideToMove() == Side.BLACK) {
            perspective = -1;
        }
        return eval * perspective;
    }

    private float EndgamePhaseWeight (int materialCountWithoutPawns) {
        final float multiplier = 1 / endgameMaterialStart;
        return 1 - Math.min(1, materialCountWithoutPawns * multiplier);
    }

    private int CountMaterial (Side side) {
        int material = 0;
        if(side == Side.WHITE) {
            material += board.getPieceLocation(Piece.WHITE_PAWN).size() * pawnValue;
            material += board.getPieceLocation(Piece.WHITE_KNIGHT).size() * knightValue;
            material += board.getPieceLocation(Piece.WHITE_BISHOP).size() * bishopValue;
            material += board.getPieceLocation(Piece.WHITE_ROOK).size() * rookValue;
            material += board.getPieceLocation(Piece.WHITE_QUEEN).size() * queenValue;
        }
        if(side == Side.BLACK) {
            material += board.getPieceLocation(Piece.BLACK_PAWN).size() * pawnValue;
            material += board.getPieceLocation(Piece.BLACK_KNIGHT).size() * knightValue;
            material += board.getPieceLocation(Piece.BLACK_BISHOP).size() * bishopValue;
            material += board.getPieceLocation(Piece.BLACK_ROOK).size() * rookValue;
            material += board.getPieceLocation(Piece.BLACK_QUEEN).size() * queenValue;
        }
        return material;
    }

    private int evaluateTables(Side side) {
        int score = 0;
        var board_array = board.boardToArray();
        for(int i = 0; i < board_array.length; i++) {
            if(board_array[i].getPieceSide() == side) {
                score += getTablePos(board_array[i], i, side);
            }
        }
        return score;
    }

    private int getTablePos(Piece piece, int i, Side side) {
        if(side == Side.WHITE) {i = 63 - i;}
        return switch (piece.getPieceType()) {
            case QUEEN -> queens[i];
            case PAWN -> pawns[i];
            case ROOK -> rooks[i];
            case KNIGHT -> knights[i];
            case BISHOP -> bishops[i];
            case KING -> kingMiddle[i];
            default -> 0;
        };
    }
    private int evaluateTablesForSide(Side side) {
        int value = 0;
        if(side == Side.WHITE) {
            value += evaluateTables(Side.WHITE);
        }
        if(side == Side.BLACK) {
            value += evaluateTables(Side.BLACK);
        }
        return value;
    }
}
