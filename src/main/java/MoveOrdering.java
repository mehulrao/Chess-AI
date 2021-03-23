import com.github.bhlangonijr.chesslib.*;
import com.github.bhlangonijr.chesslib.move.Move;

import java.util.*;

public final class MoveOrdering {
    int[] moveScores;
    final int maxMoveCnt = 100;

    final int squareControlledByOpponentPawnPen = 350;
    final int capPieceValueMultiplier = 10;

    TranspositionTable tt;
    Move invalidMove;

    Evaluation eval;

    public MoveOrdering(TranspositionTable tt) {
        moveScores = new int[maxMoveCnt];
        this.tt = tt;
        invalidMove = new Move(Square.NONE, Square.NONE);
        eval = new Evaluation();
    }

    public final void orderMoves(Board board, List<Move> moves, boolean useTT) {
        Move hashMove = invalidMove;
        if(useTT) {
            hashMove = tt.getStoredMove();
        }
        for (int i = 0; i < moves.size(); i++) {
            int score = 0;
            var currentMove = moves.get(i);
            var movePieceType = board.getPiece(currentMove.getFrom());
            var capturePieceType = board.getPiece(currentMove.getTo());

            if (capturePieceType != Piece.NONE) {
                score = capPieceValueMultiplier * getPieceValue(capturePieceType) - getPieceValue(movePieceType);
            }

            if (movePieceType.getPieceType() == PieceType.PAWN) {
                if(currentMove.getPromotion().getPieceType() != null) {
                    var promotionMove = currentMove.getPromotion().getPieceType();
                    score += getPromotionValue(promotionMove);
                }
            } else {
                if (board.squareAttackedByPieceType(currentMove.getTo(), Side.WHITE, PieceType.PAWN) != 0) {
                    score -= squareControlledByOpponentPawnPen;
                }
            }
            if (currentMove.equals(hashMove)) {
                score += 10000;
            }
            moveScores[i] = score;
        }
        Sort(moves);
    }

    private int getPieceValue(Piece piece) {
        return switch (piece.getPieceType()) {
            case QUEEN -> eval.queenValue;
            case PAWN -> eval.pawnValue;
            case ROOK -> eval.rookValue;
            case KNIGHT -> eval.knightValue;
            case BISHOP -> eval.bishopValue;
            default -> 0;
        };
    }

    private int getPromotionValue(PieceType pt) {
        return switch (pt) {
            case QUEEN -> eval.queenValue;
            case PAWN -> eval.pawnValue;
            case ROOK -> eval.rookValue;
            case KNIGHT -> eval.knightValue;
            case BISHOP -> eval.bishopValue;
            default -> 0;
        };
    }

    private void Sort(List<Move> moves) {
        for (int i = 0; i < moves.size() - 1; i++) {
            for (int j = i + 1; j > 0; j--) {
                int swapIndex = j - 1;
                if (moveScores[swapIndex] < moveScores[j]) {
                    var tempMove = moves.get(j);
                    moves.set(j, moves.get(swapIndex));
                    moves.set(swapIndex, tempMove);
                    var tempMoveScores = moveScores[j];
                    moveScores[j] = moveScores[swapIndex];
                    moveScores[swapIndex] = tempMoveScores;
                }
            }
        }
    }
}