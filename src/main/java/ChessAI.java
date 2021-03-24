import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveGeneratorException;

import java.util.List;
import java.util.Scanner;

import static org.junit.Assert.*;
import org.testng.annotations.Test;

public class ChessAI {
    static String input;

    public static void main(String[] args) {
        Board board = new Board();
        board.loadFromFen("r1bqk2r/ppp2ppp/2np1n2/1B2p3/1b2P3/2NP1N2/PPP2PPP/R1BQK2R w KQkq - 0 6");
        System.out.println(board);
        if(board.isMated()) {
            System.out.println("Mate");
            return;
        }

        testPerftInitialPosition();

        var searcher = new Searcher(board, true);
        searcher.tt.clear();

        while(!board.isMated()) {
            aiMove(searcher, board);
            System.out.println(board);
            System.out.println();
            printSearch(searcher);
            doPlayerMove(board);
            System.out.println(board);
        }
    }
    private static long perft(Board board, int depth, int ply) throws MoveGeneratorException {

        if (depth == 0) {
            return 1;
        }
        long nodes = 0;
        List<Move> moves = board.legalMoves();
        for (Move move : moves) {
            board.doMove(move);
            nodes += perft(board, depth - 1, ply + 1);
            board.undoMove();
        }
        return nodes;
    }

    @Test
    public static void testPerftInitialPosition() throws MoveGeneratorException {

        Board board = new Board();
        board.setEnableEvents(false);
        board.loadFromFen("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8");

        long nodes = perft(board, 4, 1);
        assertEquals(2103487, nodes);
    }

    private static void aiMove(Searcher searcher, Board board) {
        printSearch(searcher);
        board.doMove(searcher.getBestMove(), true);
    }

    private static void printSearch(Searcher searcher) {
        searcher.doIterativeDeepeningSearch(8);
        System.out.println(searcher.getBestMove());
        System.out.println(searcher.getBestEval());
        System.out.println("Positions: " + searcher.numPos);
        System.out.println("Nodes: " + searcher.numNodes);
        System.out.println("Prunes: " + searcher.numPrunes);
        System.out.println("Table Hits: " + searcher.numTT);
        System.out.println();
    }

    private static void doPlayerMove(Board board) {
        System.out.println("Enter Move: ");
        var cnsl = new Scanner(System.in);
        input = cnsl.nextLine();
        var moveFromInupt = new Move(input, board.getSideToMove());
        while (!board.isMoveLegal(moveFromInupt, true)) {
            System.out.println("Invalid Move");
            System.out.println("Try Again: ");
            input = cnsl.nextLine();
            moveFromInupt = new Move(input, board.getSideToMove());
        }
        board.doMove(moveFromInupt);
    }
}
