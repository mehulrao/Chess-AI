import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveGeneratorException;

import java.util.List;
import java.util.Scanner;
import java.util.Timer;

import static org.junit.Assert.*;
import org.testng.annotations.Test;

public class ChessAI {
    static String input;

    public static void main(String[] args) throws InterruptedException {
        Board board = new Board();
        board.loadFromFen("8/5pR1/5P1p/7b/2BP1k2/8/7P/1r4K1 w - - 12 49"); // Set baord fen
        System.out.println(board);
        if(board.isMated()) {
            System.out.println("Mate");
            return;
        }

        testPerftInitialPosition();

        var searcher = new Searcher(board, true);
        searcher.tt.clear();

        while(!board.isMated()) {
            aiMove(searcher, board); // AI plays white -- Switch this with doPlayerMove if you want the bot to play black
            System.out.println(board);
            System.out.println();
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

    private static void aiMove(Searcher searcher, Board board) throws InterruptedException {
        printSearch(searcher);
        board.doMove(searcher.getBestMove(), true);
    }

    private static void printSearch(Searcher searcher) throws InterruptedException {
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                searcher.doIterativeDeepeningSearch(0);
            }
        });
        t1.start();
        timedStop(7000, searcher);
        t1.join();
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

    static void timedStop(int time, Searcher searcher) {
        Timer t = new java.util.Timer();
        t.schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        searcher.endSearch();
                        t.cancel();
                    }
                },
                time
        );
    }
}
