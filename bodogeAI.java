import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

//改善点
//promLineまとめる
//475 delete必要？
//184,,
//458 手持ちから進化する可能性？

/**
 * bodogeAI
 */
public class bodogeAI {
    private Map<String, List<String>> moveList;
    private Map<String, Integer> evaList1;
    private Map<String, Integer> evaList2;
    private String MYTURN;
    private String YOURTURN;

    public bodogeAI(Map<String, List<String>> moveList, Map<String, Integer> evaList1, Map<String, Integer> evaList2,
            String MYTURN, String YOURTURN) {
        this.moveList = moveList;
        this.evaList1 = evaList1;
        this.evaList2 = evaList2;
        this.MYTURN = MYTURN;
        this.YOURTURN = YOURTURN;
    }

    public static void main(String[] args) {
        Map<String, List<String>> moveList = makeHashMap.makemoveList();
        Map<String, Integer> evaList1 = makeHashMap.evaluationList1();
        Map<String, Integer> evaList2 = makeHashMap.evaluationList2();
        String sevName = "localHost";
        int sevPort = 4444;
        long testStart = System.nanoTime();
        long testEnd = System.nanoTime();
        System.out.println((testEnd - testStart) + "ns");
        testStart = System.nanoTime();
        testEnd = System.nanoTime();
        System.out.println((testEnd - testStart) + "ns");
        try {
            Socket socket = new Socket(sevName, sevPort);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            String firstSend = reader.readLine();
            System.out.println(firstSend);
            String myTurn = firstSend.substring(14, 15);
            String yourTurn = "2";
            if (myTurn.equals("2")) {
                yourTurn = "1";
                evaList1 = makeHashMap.evaluationList2();
                evaList2 = makeHashMap.evaluationList1();
            } else if (!myTurn.equals("1")) {
                System.out.println("Player is full");
                System.exit(0);
            }
            // System.out.println(myTurn);

            String checkTurn, checkBoard, checkMove;
            HashMap<String, String> boardMap = new HashMap<String, String>();
            bodogeAI AI = new bodogeAI(moveList, evaList1, evaList2, myTurn, yourTurn);
            while (true) {
                writer.println("turn");
                checkTurn = reader.readLine();
                // System.out.println(checkTurn);
                if (checkTurn.substring(6, 7).equals(myTurn)) {
                    long startTime = System.nanoTime();
                    writer.println("board");
                    checkBoard = reader.readLine();
                    if (!checkBoard.equals("error")) {
                        // System.out.println(checkBoard);
                        boardMap = makeMap(checkBoard);
                    }
                    // System.out.println(boardMap);

                    abResults nextMove = AI.execute(boardMap);
                    Integer WorL = AI.winOrLose(boardMap, myTurn);

                    System.out.println("mv " + nextMove.getBestMove() + ", point = " + nextMove.getPoint());
                    writer.println("mv " + nextMove.getBestMove());
                    checkMove = reader.readLine();
                    long endTime = System.nanoTime();
                    System.out.print((endTime - startTime) / 1000000000 + "s ");
                    System.out.println(checkMove);
                    if (WorL != null) {
                        if (WorL == 10000) {
                            System.out.println("you win!");
                            break;
                        } else if (WorL == -10000) {
                            System.out.println("you lose!");
                            break;
                        }

                    }
                    if (checkMove.equals("Error.")) {
                        System.exit(0);
                    }
                }
                // loop for the duration of the enemy turn
                while (true) {
                    writer.println("turn");
                    checkTurn = reader.readLine();
                    if (checkTurn.substring(6, 7).equals(myTurn)) {
                        break;
                    }
                }
            }
            socket.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public abResults execute(HashMap<String, String> boardMap) {
        // int havePieceSize = 0;
        // for (Entry<String, String> entry : boardMap.entrySet()) {
        // String board = entry.getKey();
        // if (board.substring(0, 1).equals("D") || board.substring(0, 1).equals("E")) {
        // havePieceSize++;
        // }
        // }
        // if (havePieceSize > 5) {
        // return negascout(boardMap, MYTURN, YOURTURN, 5, -500000, 500000);
        // } else if (havePieceSize > 3) {
        // return negascout(boardMap, MYTURN, YOURTURN, 5, -500000, 500000);
        // } else if (havePieceSize > 2) {
        // return negascout(boardMap, MYTURN, YOURTURN, 6, -500000, 500000);
        // }
        return negascout(boardMap, MYTURN, YOURTURN, 7, -500000, 500000);
    }

    // negaalpha method
    private abResults negascout(HashMap<String, String> boardMap, String myTurn, String yourTurn, int depth, int alpha,
            int beta) {
        // static evaluation if the edge
        if (depth == 0) {
            // int point = judge(boardMap, myTurn);
            // int point2 = judge2(boardMap, myTurn);
            // if (point != point2) {
            // int aa = 0;
            // }
            int point = judge2(boardMap, myTurn);
            return new abResults(point, "");
        }

        // dynamic evaluation if not the edge
        String bestMove = "";
        Integer WorL = winOrLose(boardMap, myTurn);
        if (WorL != null) {
            if (WorL == 10000) {
                return new abResults(WorL + depth, "");
            } else {
                return new abResults(WorL - depth, "");
            }

        }
        // String WorL = winOrLose(boardMap, myTurn);
        // if (WorL.equals("win")) {
        // return new abResults(10001 + depth, "");
        // } else if (WorL.equals("lose")) {
        // return new abResults(-10001 - depth, "");
        // }

        // searching the best move in a shallow move
        ArrayList<String> nextMoveList = Nextmv(boardMap, myTurn, moveList);
        int maxPoint = -100000;
        int maxIdx = 0;
        int s = nextMoveList.size();
        if (s > 10) {
            int i;
            for (i = 0; i < s; i++) {
                String nextMove = nextMoveList.get(i);
                HashMap<String, String> nextBoard = makeNextBoard(boardMap, nextMove, myTurn);
                abResults shallowResults = new abResults(judge2(nextBoard, myTurn), "");
                if (maxPoint < shallowResults.getPoint()) {
                    maxPoint = shallowResults.getPoint();
                    maxIdx = i;
                }
            }
            Collections.swap(nextMoveList, 0, maxIdx);
        }

        // if alpha >= beta, no more looking into possible moves
        int score = 0;
        int i = 0, len = nextMoveList.size();
        for (i = 0; i < len; i++) {
            String nextMove = nextMoveList.get(i);
            HashMap<String, String> nextBoard = makeNextBoard(boardMap, nextMove, myTurn);
            if (i == 0) {
                // explore first moves (probably the largest value)
                abResults tempResults = negascout(nextBoard, yourTurn, myTurn, depth - 1, -beta, -alpha);
                score = -tempResults.getPoint();
            } else {
                // search with a null window
                abResults tempResults = negascout(nextBoard, yourTurn, myTurn, depth - 1, -alpha - 1, -alpha);
                score = -tempResults.getPoint();
                // if it failed high, do a full re-search
                if (alpha < score && score < beta) {
                    tempResults = negascout(nextBoard, yourTurn, myTurn, depth - 1, -beta, -score);
                    score = -tempResults.getPoint();
                }
            }
            // if evaluated point > alpha, uprade alpha
            if (score > alpha) {
                alpha = score;
                bestMove = nextMove;
            }
            // if alpha >= beta, no more looking into possible moves
            if (alpha >= beta) {
                break;
            }
        }
        return new abResults(alpha, bestMove);
    }

    // // negaalpha method
    // private abResults negaalpha(HashMap<String, String> boardMap, String myTurn,
    // String yourTurn, int depth, int alpha,
    // int beta) {
    // // static evaluation if the edge
    // if (depth == 0) {
    // return new abResults(judge(boardMap, myTurn), "");
    // }

    // // dynamic evaluation if not the edge
    // String bestMove = "";
    // String WorL = winOrLose(boardMap, myTurn);
    // if (WorL.equals("win")) {
    // return new abResults(10001 + depth, "");
    // } else if (WorL.equals("lose")) {
    // return new abResults(-10001 - depth, "");
    // }
    // // explore all possible moves
    // ArrayList<String> nextMoveList = new ArrayList<String>();
    // nextMoveList = Nextmv(boardMap, myTurn, moveList);
    // for (String nextMove : nextMoveList) {
    // HashMap<String, String> nextBoard = makeNextBoard(boardMap, nextMove,
    // myTurn);
    // abResults tempResults = negaalpha(nextBoard, yourTurn, myTurn, depth - 1,
    // -beta, -alpha);
    // // if evaluated point > alpha, uprade alpha
    // if (-tempResults.getPoint() > alpha) {
    // alpha = -tempResults.getPoint();
    // bestMove = nextMove;
    // }
    // // if alpha >= beta, no more looking into possible moves
    // if (alpha >= beta) {
    // break;
    // }
    // }
    // return new abResults(alpha, bestMove);
    // }

    private int judge(HashMap<String, String> boardMap, String myTurn) {
        int point = 0;
        String myHandAlf = "D";
        String yourHandAlf = "E";
        if (myTurn.equals("2")) {
            myHandAlf = "E";
            yourHandAlf = "D";
        }
        Integer WorL = winOrLose(boardMap, myTurn);
        if (WorL != null) {
            return WorL;
        }
        // String WorL = winOrLose(boardMap, myTurn);
        // if (WorL.equals("win")) {
        // return 10000;
        // } else if (WorL.equals("lose")) {
        // return -10000;
        // }

        for (Entry<String, String> entry : boardMap.entrySet()) {
            String board = entry.getKey();
            String piece = entry.getValue();

            // score according to the piece. ex "A2 g1"
            String boardAlf = board.substring(0, 1); // boardAlf = A
            // String boardNum = board.substring(1, 2); // boardNum = 2
            String pieceAlf = piece.substring(0, 1); // pieceAlf = g
            String pieceNum = piece.substring(1, 2); // pieceNum = 1

            if (pieceAlf.equals("c")) {
                if (pieceNum.equals(myTurn)) {
                    point += 1;
                    if (boardAlf.equals(myHandAlf)) {
                        point += 2;
                    }
                } else {
                    point -= 1;
                    if (boardAlf.equals(yourHandAlf)) {
                        point -= 2;
                    }
                }
            } else if (pieceAlf.equals("h")) {
                if (pieceNum.equals(myTurn)) {
                    point += 6;
                } else {
                    point -= 6;
                }
            } else if (pieceAlf.equals("e")) {
                if (pieceNum.equals(myTurn)) {
                    point += 6;
                    if (boardAlf.equals(myHandAlf)) {
                        point += 2;
                    }
                } else {
                    point -= 6;
                    if (boardAlf.equals(yourHandAlf)) {
                        point -= 2;
                    }
                }
            } else if (pieceAlf.equals("g")) {
                if (pieceNum.equals(myTurn)) {
                    point += 5;
                    if (boardAlf.equals(myHandAlf)) {
                        point += 2;
                    }
                } else {
                    point -= 5;
                    if (boardAlf.equals(yourHandAlf)) {
                        point -= 2;
                    }
                }
            }
        }
        return point;
    }

    private int judge2(HashMap<String, String> boardMap, String myTurn) {
        int point = 0;
        Integer WorL = winOrLose(boardMap, myTurn);
        if (WorL != null) {
            return WorL;
        }
        // String WorL = winOrLose(boardMap, myTurn);
        // if (WorL.equals("win")) {
        // return 10000;
        // } else if (WorL.equals("lose")) {
        // return -10000;
        // }
        Map<String, Integer> evaList = evaList1;
        if (!myTurn.equals(MYTURN)) {
            evaList = evaList2;
        }

        for (Entry<String, String> entry : boardMap.entrySet()) {
            String board = entry.getKey();
            String piece = entry.getValue();
            point += evaList.get(board + " " + piece);
        }
        return point;
    }

    private Integer winOrLose(HashMap<String, String> boardMap, String myTurn) {
        // if myTurn = 1
        String yourTurn = "2";
        String myLion = "l1";
        String yourLion = "l2";
        String winLineA = "A1";
        String winLineB = "B1";
        String winLineC = "C1";
        String loseLineA = "A4";
        String loseLineB = "B4";
        String loseLineC = "C4";

        // if myTurn = 2
        if (myTurn.equals("2")) {
            yourTurn = "1";
            myLion = "l2";
            yourLion = "l1";
            winLineA = "A4";
            winLineB = "B4";
            winLineC = "C4";
            loseLineA = "A1";
            loseLineB = "B1";
            loseLineC = "C1";
        }

        // if the lion does not exist
        if (!boardMap.values().contains(yourLion)) {
            return 10000;
        } else if (!boardMap.values().contains(myLion)) {
            return -10000;
        }
        // our try decision
        if ((boardMap.containsKey(winLineA) && boardMap.get(winLineA).equals(myLion))
                || (boardMap.containsKey(winLineB) && boardMap.get(winLineB).equals(myLion))
                || (boardMap.containsKey(winLineC) && boardMap.get(winLineC).equals(myLion))) {
            ArrayList<String> nextMoveList = Nextmv(boardMap, yourTurn, moveList);
            for (String nextMove : nextMoveList) {
                HashMap<String, String> nextBoard = makeNextBoard(boardMap, nextMove, yourTurn);
                if (!nextBoard.containsValue(myLion)) {
                    return null;
                }
            }
            return 10000;
        }
        // enemy try decision
        if ((boardMap.containsKey(loseLineA) && boardMap.get(loseLineA).equals(yourLion))
                || (boardMap.containsKey(loseLineB) && boardMap.get(loseLineB).equals(yourLion))
                || (boardMap.containsKey(loseLineC) && boardMap.get(loseLineC).equals(yourLion))) {
            ArrayList<String> nextMoveList = Nextmv(boardMap, myTurn, moveList);
            for (String nextMove : nextMoveList) {
                HashMap<String, String> nextBoard = makeNextBoard(boardMap, nextMove, myTurn);
                if (!nextBoard.containsValue(yourLion)) {
                    return null;
                }
            }
            return -10000;
        }
        return null;
    }

    private HashMap<String, String> makeNextBoard(HashMap<String, String> boardMap0, String nextMove, String turn) {
        HashMap<String, String> boardMap = new HashMap<String, String>(boardMap0);
        // if myTurn = 1
        String handAlf = "D";
        String handAlf1 = "D1";
        String handAlf2 = "D2";
        String handAlf3 = "D3";
        String handAlf4 = "D4";
        String handAlf5 = "D5";
        String handAlf6 = "D6";
        String promLineA = "A1";
        String promLineB = "B1";
        String promLineC = "C1";
        // if myTurn = 2
        if (turn.equals("2")) {
            handAlf = "E";
            handAlf1 = "E1";
            handAlf2 = "E2";
            handAlf3 = "E3";
            handAlf4 = "E4";
            handAlf5 = "E5";
            handAlf6 = "E6";
            promLineA = "A4";
            promLineB = "B4";
            promLineC = "C4";
        }
        // ex. (nextMove = "A1 B2") ===> (srcBoard = "A1", dstBoard = "B2")
        String srcBoard = nextMove.substring(0, 2);
        String dstBoard = nextMove.substring(3, 5);
        // System.out.print(nextMove + " ");

        // When a chick evolves
        if (boardMap.get(srcBoard).equals("c" + new String(turn))) {
            if (dstBoard.equals(promLineA) || dstBoard.equals(promLineB) || dstBoard.equals(promLineC)) {
                boardMap.replace(srcBoard, "h" + new String(turn));
            }
        }
        // if there are no piece to move to
        if (!boardMap.containsKey(dstBoard)) {

            boardMap.put(dstBoard, boardMap.get(srcBoard));
            boardMap.remove(srcBoard);

            // number of piece you pick
            String pickNum = srcBoard.substring(1, 2);
            if (srcBoard.substring(0, 1).equals(handAlf)) {
                if (!boardMap.containsKey(handAlf6)) {
                    if (!boardMap.containsKey(handAlf5)) {
                        if (!boardMap.containsKey(handAlf4)) {
                            if (!boardMap.containsKey(handAlf3)) {
                                // if you have 2 piece
                                if (boardMap.containsKey(handAlf2)) {
                                    boardMap.put(handAlf1, boardMap.get(handAlf2));
                                }
                            } else {
                                // if you have 3 piece
                                if (pickNum.equals("1")) {
                                    boardMap.put(handAlf1, boardMap.get(handAlf2));
                                    boardMap.remove(handAlf2);
                                    boardMap.put(handAlf2, boardMap.get(handAlf3));
                                } else if (pickNum.equals("2")) {
                                    boardMap.put(handAlf2, boardMap.get(handAlf3));
                                }
                                boardMap.remove(handAlf3);
                            }
                        } else {
                            // if you have 4 piece
                            if (pickNum.equals("1")) {
                                boardMap.put(handAlf1, boardMap.get(handAlf2));
                                boardMap.remove(handAlf2);
                                boardMap.put(handAlf2, boardMap.get(handAlf3));
                                boardMap.remove(handAlf3);
                                boardMap.put(handAlf3, boardMap.get(handAlf4));
                            } else if (pickNum.equals("2")) {
                                boardMap.put(handAlf2, boardMap.get(handAlf3));
                                boardMap.remove(handAlf3);
                                boardMap.put(handAlf3, boardMap.get(handAlf4));
                            } else if (pickNum.equals("3")) {
                                boardMap.put(handAlf3, boardMap.get(handAlf4));
                            }
                            boardMap.remove(handAlf4);
                        }
                    } else {
                        // if you have 5 piece
                        if (pickNum.equals("1")) {
                            boardMap.put(handAlf1, boardMap.get(handAlf2));
                            boardMap.remove(handAlf2);
                            boardMap.put(handAlf2, boardMap.get(handAlf3));
                            boardMap.remove(handAlf3);
                            boardMap.put(handAlf3, boardMap.get(handAlf4));
                            boardMap.remove(handAlf4);
                            boardMap.put(handAlf4, boardMap.get(handAlf5));
                        } else if (pickNum.equals("2")) {
                            boardMap.put(handAlf2, boardMap.get(handAlf3));
                            boardMap.remove(handAlf3);
                            boardMap.put(handAlf3, boardMap.get(handAlf4));
                            boardMap.remove(handAlf4);
                            boardMap.put(handAlf4, boardMap.get(handAlf5));
                        } else if (pickNum.equals("3")) {
                            boardMap.put(handAlf3, boardMap.get(handAlf4));
                            boardMap.remove(handAlf4);
                            boardMap.put(handAlf4, boardMap.get(handAlf5));
                        } else if (pickNum.equals("4")) {
                            boardMap.put(handAlf4, boardMap.get(handAlf5));
                        }
                        boardMap.remove(handAlf5);
                    }
                } else {
                    // if you have 6 piece
                    if (pickNum.equals("1")) {
                        boardMap.put(handAlf1, boardMap.get(handAlf2));
                        boardMap.remove(handAlf2);
                        boardMap.put(handAlf2, boardMap.get(handAlf3));
                        boardMap.remove(handAlf3);
                        boardMap.put(handAlf3, boardMap.get(handAlf4));
                        boardMap.remove(handAlf4);
                        boardMap.put(handAlf4, boardMap.get(handAlf5));
                        boardMap.remove(handAlf5);
                        boardMap.put(handAlf5, boardMap.get(handAlf6));
                    } else if (pickNum.equals("2")) {
                        boardMap.put(handAlf2, boardMap.get(handAlf3));
                        boardMap.remove(handAlf3);
                        boardMap.put(handAlf3, boardMap.get(handAlf4));
                        boardMap.remove(handAlf4);
                        boardMap.put(handAlf4, boardMap.get(handAlf5));
                        boardMap.remove(handAlf5);
                        boardMap.put(handAlf5, boardMap.get(handAlf6));
                    } else if (pickNum.equals("3")) {
                        boardMap.put(handAlf3, boardMap.get(handAlf4));
                        boardMap.remove(handAlf4);
                        boardMap.put(handAlf4, boardMap.get(handAlf5));
                        boardMap.remove(handAlf5);
                        boardMap.put(handAlf5, boardMap.get(handAlf6));
                    } else if (pickNum.equals("4")) {
                        boardMap.put(handAlf4, boardMap.get(handAlf5));
                        boardMap.remove(handAlf5);
                        boardMap.put(handAlf5, boardMap.get(handAlf6));
                    } else if (pickNum.equals("5")) {
                        boardMap.put(handAlf5, boardMap.get(handAlf6));
                    }
                    boardMap.remove(handAlf6);
                }
            }
        } else {
            // if you take a piece
            String aniName = boardMap.get(dstBoard).substring(0, 1);
            if (aniName.equals("h")) {
                aniName = "c";
            }
            boardMap.remove(dstBoard);
            boardMap.put(dstBoard, boardMap.get(srcBoard));
            boardMap.remove(srcBoard);

            if (boardMap.containsKey(handAlf1)) {
                if (boardMap.containsKey(handAlf2)) {
                    if (boardMap.containsKey(handAlf3)) {
                        if (boardMap.containsKey(handAlf4)) {
                            if (boardMap.containsKey(handAlf5)) {
                                boardMap.put(handAlf6, aniName + new String(turn));
                            } else {
                                boardMap.put(handAlf5, aniName + new String(turn));
                            }
                        } else {
                            boardMap.put(handAlf4, aniName + new String(turn));
                        }
                    } else {
                        boardMap.put(handAlf3, aniName + new String(turn));
                    }
                } else {
                    boardMap.put(handAlf2, aniName + new String(turn));
                }
            } else {
                boardMap.put(handAlf1, aniName + new String(turn));
            }
        }
        return boardMap;
    }

    private static HashMap<String, String> makeMap(String checkBoard) {
        HashMap<String, String> boardMap = new HashMap<String, String>();
        String[] elem = checkBoard.split(",");
        int i = 0, len = elem.length;
        for (i = 0; i < len; i++) {
            elem[i] = elem[i].strip();
            if (!elem[i].equals("") && !elem[i].split(" ")[1].equals("--")) {
                boardMap.put(elem[i].split(" ")[0], elem[i].split(" ")[1]);
            }
        }
        return boardMap;
    }

    private static ArrayList<String> Nextmv(HashMap<String, String> boardMap, String myTurn,
            Map<String, List<String>> moveList) {
        ArrayList<String> nextMoveList = new ArrayList<String>();
        String OwnPiece = "D";
        if (myTurn.equals("2")) {
            OwnPiece = "E";
        }
        String pieceFlug = "";
        // retrieving the board infomation
        for (Entry<String, String> entry : boardMap.entrySet()) {
            String board = entry.getKey();
            String piece = entry.getValue();
            // when a piece is turn player
            if (piece.substring(1, 2).equals(myTurn)) {
                // when a piece in possession
                if (board.substring(0, 1).equals(OwnPiece)) {
                    if (!pieceFlug.contains(piece.substring(0, 1))) {
                        Set<String> subBoardSet = subBoardSet(boardMap.keySet());
                        for (String b : subBoardSet) {
                            nextMoveList.add(board + " " + b);
                        }
                        pieceFlug += piece.substring(0, 1);
                    }
                } else {
                    // when the board piece
                    List<String> mvList = moveList.get(board + " " + piece);

                    for (String mv : mvList) {
                        if (!boardMap.containsKey(mv) || !boardMap.get(mv).substring(1, 2).equals(myTurn)) {
                            if (!mv.equals("")) {
                                nextMoveList.add(board + " " + mv);
                            }
                        }
                    }
                }
            }
        }
        return nextMoveList;
    }

    private static Set<String> subBoardSet(Set<String> boardKeys) {
        Set<String> allBoard = new HashSet<>();
        allBoard.add("A1");
        allBoard.add("A2");
        allBoard.add("A3");
        allBoard.add("A4");
        allBoard.add("B1");
        allBoard.add("B2");
        allBoard.add("B3");
        allBoard.add("B4");
        allBoard.add("C1");
        allBoard.add("C2");
        allBoard.add("C3");
        allBoard.add("C4");

        for (String b : boardKeys) {
            allBoard.remove(b);
        }

        return allBoard;
    }

}