import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveGenerator;

import java.util.List;

public class Searcher {

    final static int immediateMateScore = 100000;
    final int ttSize = 100000;
    final int posInf = 9999999;
    final int negInf = -posInf;

    TranspositionTable tt;

    Move bestMoveThisItr;
    int bestEvalThisItr;
    Move bestMove;
    int bestEval;

    Move invalidMove = new Move(Square.NONE, Square.NONE);
    MoveOrdering moveOrdering;
    Board board;
    Evaluation evaluation;

    public int numPos;
    public int numNodes;
    public int numSafePos;
    public int numPrunes;
    public int numTT;

    boolean useSecondSearch;

    public Searcher(Board board, boolean useSecondSearch) {
        this.board = board;
        tt = new TranspositionTable(board, ttSize);
        this.useSecondSearch = useSecondSearch;
        evaluation = new Evaluation();
        moveOrdering = new MoveOrdering(tt);
    }

    int doIterativeDeepeningSearch() {
        return 0;
    }

    int SearchMoves(int depth, int plyFromRoot, int alpha, int beta) {
        if (plyFromRoot > 0) {
            if(board.isRepetition(2)) {
                return 0;
            }

            alpha = Math.max(alpha, -immediateMateScore + plyFromRoot);
            beta = Math.min(beta, immediateMateScore - plyFromRoot);
            if(alpha >= beta) {
                return alpha;
            }
        }

        int ttEval = tt.lookupEvaluation(depth, plyFromRoot, alpha, beta);
        if(ttEval != tt.lookupFailed) {
            numTT++;
            if(plyFromRoot == 0) {
                bestMoveThisItr = tt.getStoredMove();
                bestEvalThisItr = tt.entries[tt.index()].value;
            }
            return ttEval;
        }
        if(depth == 0) {
            if(useSecondSearch){
                return searchCaptures(alpha, beta);
            }
            else {
                return evaluation.Evaluate(board);
            }
        }
        List<Move> moveList = board.legalMoves();
        moveOrdering.orderMoves(board, moveList, true);

        if(board.isMated()) {
            int mateScore = immediateMateScore - plyFromRoot;
            return -mateScore;
        }
        else if(board.isStaleMate()) {
            return 0;
        }

        int evalType = tt.upperBound;
        var bestMoveThisPos = invalidMove;

        for (Move move : moveList) {
            board.doMove(move);
            int eval = -SearchMoves(depth - 1, plyFromRoot + 1, -beta, -alpha);
            board.undoMove();
            numNodes++;

            if (eval >= beta) {
                tt.storeEval(depth, plyFromRoot, beta, tt.lowerBound, move);
                numPrunes++;
                return beta;
            }
            if (eval > alpha) {
                evalType = tt.exact;
                bestMoveThisPos = move;

                alpha = eval;
                if (plyFromRoot == 0) {
                    bestMoveThisItr = move;
                    bestEvalThisItr = eval;
                }
            }

        }

        tt.storeEval(depth, plyFromRoot, alpha, evalType, bestMoveThisPos);
        return alpha;

    }

    int searchCaptures(int alpha, int beta) {
        int eval = evaluation.Evaluate(board);
        numPos++;
        if(eval >= beta) {
            return beta;
        }
        if(eval > alpha) {
            alpha = eval;
        }
        var moveList = MoveGenerator.generatePseudoLegalCaptures(board);
        var itr = moveList.iterator();
        while (itr.hasNext()) {
            var next = (Move)itr.next();
            if(!board.isMoveLegal(next, true)) {
                itr.remove();
            }
        }
        moveOrdering.orderMoves(board, moveList, false);
        for (Move move : moveList) {
            board.doMove(move);
            eval = -searchCaptures(-beta, -alpha);
            board.undoMove();
            numSafePos++;
            if (eval >= beta) {
                numPrunes++;
                return beta;
            }
            if (eval > alpha) {
                alpha = eval;
            }
        }
        return alpha;
    }

    public static boolean isMateScore(int score) {
        final int maxMateDepth = 1000;
        return Math.abs(score) > immediateMateScore - maxMateDepth;
    }

    public void doSearch(int depth) {
        SearchMoves(depth, 0, negInf, posInf);
        bestMove = bestMoveThisItr;
        bestEval = bestEvalThisItr;
    }

    public void clearTT() {
        tt.clear();
    }

    public Move getBestMove() {
        return bestMove;
    }
    public int getBestEval() {
        return bestEval;
    }
}