import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * bodogeAI
 */
public class bodogeAI {
    private Map<String, List<String>> moveList;

    public bodogeAI(Map<String, List<String>> moveList) {
        this.moveList = moveList;
    }

    public static void main(String[] args) {
        Map<String, List<String>> moveList = makemoveList();
        new bodogeAI(moveList).execute();
    }

    public void execute() {
        String sevName = "localHost";
        int sevPort = 4444;

        try {
            Socket socket = new Socket(sevName, sevPort);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            String firstSend = reader.readLine();
            // System.out.println(firstSend);
            String myTurn = firstSend.substring(14, 15);
            String yourTurn = "2";
            if (myTurn.equals("2")) {
                yourTurn = "1";
            } else if (myTurn.equals("1")) {
            } else {
                System.out.println("Player is full");
                System.exit(0);
            }
            // System.out.println(myTurn);

            String checkTurn, checkBoard, checkMove;
            HashMap<String, String> boardMap = new HashMap<String, String>();
            while (true) {
                writer.println("turn");
                checkTurn = reader.readLine();
                System.out.println(checkTurn);
                if (checkTurn.substring(6, 7).equals(myTurn)) {
                    writer.println("board");
                    checkBoard = reader.readLine();
                    if (!checkBoard.equals("error")) {
                        System.out.println(checkBoard);
                        boardMap = makeMap(checkBoard);
                    }
                    System.out.println(boardMap);

                    abResults nextMove = alphabeta1(boardMap, myTurn, yourTurn, 9, -500000, 500000);

                    String WorL = winOrLose(boardMap, myTurn);

                    System.out.println("mv  " + nextMove.getBestMove());
                    writer.println("mv " + nextMove.getBestMove());
                    checkMove = reader.readLine();
                    System.out.println(checkMove);
                    if (WorL.equals("win")) {
                        System.out.println("you win!");
                        break;
                    } else if (WorL.equals("lose")) {
                        System.out.println("you lose!");
                        break;
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

    // alphabeta method
    private abResults alphabeta1(HashMap<String, String> boardMap, String myTurn, String yourTurn, int depth, int alpha,
            int beta) {
        // static evaluation if the edge
        if (depth == 0) {
            return new abResults(judge(boardMap, myTurn, depth + 1), "");
        }

        // dynamic evaluation if not the edge
        String bestMove = "";
        String WorL = winOrLose(boardMap, myTurn);
        if (WorL.equals("win")) {
            return new abResults(10001 + depth, "");
        } else if (WorL.equals("lose")) {
            return new abResults(-10001 + depth, "");
        }
        // explore all possible moves
        ArrayList<String> nextMoveList = new ArrayList<String>();
        nextMoveList = Nextmv(boardMap, myTurn, moveList);
        for (String nextMove : nextMoveList) {
            HashMap<String, String> nextBoard = makeNextBoard(boardMap, nextMove, myTurn);
            abResults tempResults = alphabeta2(nextBoard, myTurn, yourTurn, depth - 1, beta, alpha);
            // if evaluated point > alpha, uprade alpha
            if (tempResults.getPoint() > alpha) {
                alpha = tempResults.getPoint();
                bestMove = nextMove;
            }
            // if alpha >= beta, no more looking into possible moves
            if (alpha >= beta) {
                break;
            }
        }
        return new abResults(alpha, bestMove);
    }

    // alphabeta method
    private abResults alphabeta2(HashMap<String, String> boardMap, String myTurn, String yourTurn, int depth, int alpha,
            int beta) {
        // static evaluation if the edge
        if (depth == 0) {
            return new abResults(judge(boardMap, myTurn, depth + 1), "");
        }

        // dynamic evaluation if not the edge
        String bestMove = "";
        String WorL = winOrLose(boardMap, myTurn);
        if (WorL.equals("win")) {
            return new abResults(10001 + depth, "");
        } else if (WorL.equals("lose")) {
            return new abResults(-10001 + depth, "");
        }
        // explore all possible moves
        ArrayList<String> nextMoveList = new ArrayList<String>();
        nextMoveList = Nextmv(boardMap, yourTurn, moveList);
        for (String nextMove : nextMoveList) {
            HashMap<String, String> nextBoard = makeNextBoard(boardMap, nextMove, yourTurn);
            abResults tempResults = alphabeta1(nextBoard, myTurn, yourTurn, depth - 1, beta, alpha);
            // if evaluated point < alpha, upgrade alpha
            if (tempResults.getPoint() < alpha) {
                alpha = tempResults.getPoint();
                bestMove = nextMove;
            }
            // if alpha <= beta, no more looking into possible moves
            if (alpha <= beta) {
                break;
            }
        }
        return new abResults(alpha, bestMove);
    }

    private int judge(HashMap<String, String> boardMap, String myTurn, int depth) {
        int point = 0;
        String myHandAlf = "D";
        String yourHandAlf = "E";
        if (myTurn.equals("2")) {
            myHandAlf = "E";
            yourHandAlf = "D";
        }

        for (Entry<String, String> entry : boardMap.entrySet()) {
            String board = entry.getKey();
            String piece = entry.getValue();
            String WorL = winOrLose(boardMap, myTurn);
            if (WorL.equals("win")) {
                return 100000 + depth;
            } else if (WorL.equals("lose")) {
                return -100000 + depth;
            }

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
            }
            if (pieceAlf.equals("h")) {
                if (pieceNum.equals(myTurn)) {
                    point += 10;
                } else {
                    point -= 10;
                }
            }
            if (pieceAlf.equals("e")) {
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
            }
            if (pieceAlf.equals("g")) {
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
        return point + depth * 100;
    }

    private String winOrLose(HashMap<String, String> boardMap, String myTurn) {
        String yourTurn = "2";
        String winLine = "1";
        String loseLine = "4";
        if (myTurn.equals("2")) {
            yourTurn = "1";
            winLine = "4";
            loseLine = "1";
        }

        // if the lion does not exist
        if (!boardMap.values().contains("l" + yourTurn)) {
            return "win";
        } else if (!boardMap.values().contains("l" + myTurn)) {
            return "lose";
        }
        // our try decision
        if ((boardMap.containsKey("A" + winLine) && boardMap.get("A" + winLine).equals("l" + myTurn))
                || (boardMap.containsKey("B" + winLine) && boardMap.get("B" + winLine).equals("l" + myTurn))
                || (boardMap.containsKey("C" + winLine) && boardMap.get("C" + winLine).equals("l" + myTurn))) {
            ArrayList<String> nextMoveList = new ArrayList<String>();
            nextMoveList = Nextmv(boardMap, yourTurn, moveList);
            for (String nextMove : nextMoveList) {
                HashMap<String, String> nextBoard = makeNextBoard(boardMap, nextMove, yourTurn);
                if (!nextBoard.containsValue("l" + myTurn)) {
                    return "lose";
                }
            }
            return "win";
        }
        // enemy try decision
        if ((boardMap.containsKey("A" + loseLine) && boardMap.get("A" + loseLine).equals("l" + yourTurn))
                || (boardMap.containsKey("B" + loseLine) && boardMap.get("B" + loseLine).equals("l" + yourTurn))
                || (boardMap.containsKey("C" + loseLine) && boardMap.get("C" + loseLine).equals("l" + yourTurn))) {
            ArrayList<String> nextMoveList = new ArrayList<String>();
            nextMoveList = Nextmv(boardMap, myTurn, moveList);
            for (String nextMove : nextMoveList) {
                HashMap<String, String> nextBoard = makeNextBoard(boardMap, nextMove, myTurn);
                if (!nextBoard.containsValue("l" + yourTurn)) {
                    return "win";
                }
            }
            return "lose";
        }
        return "";
    }

    private HashMap<String, String> makeNextBoard(HashMap<String, String> boardMap0, String nextMove, String turn) {
        HashMap<String, String> boardMap = new HashMap<String, String>(boardMap0);
        String handAlf = "D";
        String promLine = "1";
        if (turn.equals("2")) {
            handAlf = "E";
            promLine = "4";
        }
        // ex. nextMove = "A1 B2" ===> srcBoard = "A1", dstBoard = "B2"
        String srcBoard = nextMove.substring(0, 2);
        String dstBoard = nextMove.substring(3, 5);

        // System.out.print(nextMove + " ");
        // When a chick evolves
        if (boardMap.get(srcBoard).equals("c" + new String(turn))) {
            if (dstBoard.equals("A" + promLine) || dstBoard.equals("B" + promLine) || dstBoard.equals("C" + promLine)) {
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
                if (!boardMap.containsKey(handAlf + "6")) {
                    if (!boardMap.containsKey(handAlf + "5")) {
                        if (!boardMap.containsKey(handAlf + "4")) {
                            if (!boardMap.containsKey(handAlf + "3")) {
                                // if you have 2 piece
                                if (boardMap.containsKey(handAlf + "2")) {
                                    boardMap.put(handAlf + "1", boardMap.get(handAlf + "2"));
                                }
                            } else {
                                // if you have 3 piece
                                if (pickNum.equals("1")) {
                                    boardMap.put(handAlf + "1", boardMap.get(handAlf + "2"));
                                    boardMap.remove(handAlf + "2");
                                    boardMap.put(handAlf + "2", boardMap.get(handAlf + "3"));
                                } else if (pickNum.equals("2")) {
                                    boardMap.put(handAlf + "2", boardMap.get(handAlf + "3"));
                                }
                                boardMap.remove(handAlf + "3");
                            }
                        } else {
                            // if you have 4 piece
                            if (pickNum.equals("1")) {
                                boardMap.put(handAlf + "1", boardMap.get(handAlf + "2"));
                                boardMap.remove(handAlf + "2");
                                boardMap.put(handAlf + "2", boardMap.get(handAlf + "3"));
                                boardMap.remove(handAlf + "3");
                                boardMap.put(handAlf + "3", boardMap.get(handAlf + "4"));
                            } else if (pickNum.equals("2")) {
                                boardMap.put(handAlf + "2", boardMap.get(handAlf + "3"));
                                boardMap.remove(handAlf + "3");
                                boardMap.put(handAlf + "3", boardMap.get(handAlf + "4"));
                            } else if (pickNum.equals("3")) {
                                boardMap.put(handAlf + "3", boardMap.get(handAlf + "4"));
                            }
                            boardMap.remove(handAlf + "4");
                        }
                    } else {
                        // if you have 5 piece
                        if (pickNum.equals("1")) {
                            boardMap.put(handAlf + "1", boardMap.get(handAlf + "2"));
                            boardMap.remove(handAlf + "2");
                            boardMap.put(handAlf + "2", boardMap.get(handAlf + "3"));
                            boardMap.remove(handAlf + "3");
                            boardMap.put(handAlf + "3", boardMap.get(handAlf + "4"));
                            boardMap.remove(handAlf + "4");
                            boardMap.put(handAlf + "4", boardMap.get(handAlf + "5"));
                        } else if (pickNum.equals("2")) {
                            boardMap.put(handAlf + "2", boardMap.get(handAlf + "3"));
                            boardMap.remove(handAlf + "3");
                            boardMap.put(handAlf + "3", boardMap.get(handAlf + "4"));
                            boardMap.remove(handAlf + "4");
                            boardMap.put(handAlf + "4", boardMap.get(handAlf + "5"));
                        } else if (pickNum.equals("3")) {
                            boardMap.put(handAlf + "3", boardMap.get(handAlf + "4"));
                            boardMap.remove(handAlf + "4");
                            boardMap.put(handAlf + "4", boardMap.get(handAlf + "5"));
                        } else if (pickNum.equals("4")) {
                            boardMap.put(handAlf + "4", boardMap.get(handAlf + "5"));
                        }
                        boardMap.remove(handAlf + "5");
                    }
                } else {
                    // if you have 6 piece
                    if (pickNum.equals("1")) {
                        boardMap.put(handAlf + "1", boardMap.get(handAlf + "2"));
                        boardMap.remove(handAlf + "2");
                        boardMap.put(handAlf + "2", boardMap.get(handAlf + "3"));
                        boardMap.remove(handAlf + "3");
                        boardMap.put(handAlf + "3", boardMap.get(handAlf + "4"));
                        boardMap.remove(handAlf + "4");
                        boardMap.put(handAlf + "4", boardMap.get(handAlf + "5"));
                        boardMap.remove(handAlf + "5");
                        boardMap.put(handAlf + "5", boardMap.get(handAlf + "6"));
                    } else if (pickNum.equals("2")) {
                        boardMap.put(handAlf + "2", boardMap.get(handAlf + "3"));
                        boardMap.remove(handAlf + "3");
                        boardMap.put(handAlf + "3", boardMap.get(handAlf + "4"));
                        boardMap.remove(handAlf + "4");
                        boardMap.put(handAlf + "4", boardMap.get(handAlf + "5"));
                        boardMap.remove(handAlf + "5");
                        boardMap.put(handAlf + "5", boardMap.get(handAlf + "6"));
                    } else if (pickNum.equals("3")) {
                        boardMap.put(handAlf + "3", boardMap.get(handAlf + "4"));
                        boardMap.remove(handAlf + "4");
                        boardMap.put(handAlf + "4", boardMap.get(handAlf + "5"));
                        boardMap.remove(handAlf + "5");
                        boardMap.put(handAlf + "5", boardMap.get(handAlf + "6"));
                    } else if (pickNum.equals("4")) {
                        boardMap.put(handAlf + "4", boardMap.get(handAlf + "5"));
                        boardMap.remove(handAlf + "5");
                        boardMap.put(handAlf + "5", boardMap.get(handAlf + "6"));
                    } else if (pickNum.equals("5")) {
                        boardMap.put(handAlf + "5", boardMap.get(handAlf + "6"));
                    }
                    boardMap.remove(handAlf + "6");
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

            if (boardMap.containsKey(handAlf + "1")) {
                if (boardMap.containsKey(handAlf + "2")) {
                    if (boardMap.containsKey(handAlf + "3")) {
                        if (boardMap.containsKey(handAlf + "4")) {
                            if (boardMap.containsKey(handAlf + "5")) {
                                boardMap.put(handAlf + "6", aniName + new String(turn));
                            } else {
                                boardMap.put(handAlf + "5", aniName + new String(turn));
                            }
                        } else {
                            boardMap.put(handAlf + "4", aniName + new String(turn));
                        }
                    } else {
                        boardMap.put(handAlf + "3", aniName + new String(turn));
                    }
                } else {
                    boardMap.put(handAlf + "2", aniName + new String(turn));
                }
            } else {
                boardMap.put(handAlf + "1", aniName + new String(turn));
            }
        }
        return boardMap;
    }

    private static HashMap<String, String> makeMap(String checkBoard) {
        HashMap<String, String> boardMap = new HashMap<String, String>();
        String[] elem = checkBoard.split(",");
        for (int i = 0; i < elem.length; i++) {
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

    private static Map<String, List<String>> makemoveList() {
        HashMap<String, List<String>> result = new HashMap<>();
        result.put("A1 c1", new ArrayList<String>(Arrays.asList("")));
        result.put("A2 c1", new ArrayList<String>(Arrays.asList("A1")));
        result.put("A3 c1", new ArrayList<String>(Arrays.asList("A2")));
        result.put("A4 c1", new ArrayList<String>(Arrays.asList("A3")));
        result.put("B1 c1", new ArrayList<String>(Arrays.asList("")));
        result.put("B2 c1", new ArrayList<String>(Arrays.asList("B1")));
        result.put("B3 c1", new ArrayList<String>(Arrays.asList("B2")));
        result.put("B4 c1", new ArrayList<String>(Arrays.asList("B3")));
        result.put("C1 c1", new ArrayList<String>(Arrays.asList("")));
        result.put("C2 c1", new ArrayList<String>(Arrays.asList("C1")));
        result.put("C3 c1", new ArrayList<String>(Arrays.asList("C2")));
        result.put("C4 c1", new ArrayList<String>(Arrays.asList("C3")));
        result.put("A1 c2", new ArrayList<String>(Arrays.asList("A2")));
        result.put("A2 c2", new ArrayList<String>(Arrays.asList("A3")));
        result.put("A3 c2", new ArrayList<String>(Arrays.asList("A4")));
        result.put("A4 c2", new ArrayList<String>(Arrays.asList("")));
        result.put("B1 c2", new ArrayList<String>(Arrays.asList("B2")));
        result.put("B2 c2", new ArrayList<String>(Arrays.asList("B3")));
        result.put("B3 c2", new ArrayList<String>(Arrays.asList("B4")));
        result.put("B4 c2", new ArrayList<String>(Arrays.asList("")));
        result.put("C1 c2", new ArrayList<String>(Arrays.asList("C2")));
        result.put("C2 c2", new ArrayList<String>(Arrays.asList("C3")));
        result.put("C3 c2", new ArrayList<String>(Arrays.asList("C4")));
        result.put("C4 c2", new ArrayList<String>(Arrays.asList("")));
        result.put("A1 g1", new ArrayList<String>(Arrays.asList("A2", "B1")));
        result.put("A2 g1", new ArrayList<String>(Arrays.asList("A3", "B2", "A1")));
        result.put("A3 g1", new ArrayList<String>(Arrays.asList("A4", "B3", "A2")));
        result.put("A4 g1", new ArrayList<String>(Arrays.asList("B4", "A3")));
        result.put("B1 g1", new ArrayList<String>(Arrays.asList("B2", "C1", "A1")));
        result.put("B2 g1", new ArrayList<String>(Arrays.asList("B3", "C2", "A2", "B1")));
        result.put("B3 g1", new ArrayList<String>(Arrays.asList("B4", "C3", "A3", "B2")));
        result.put("B4 g1", new ArrayList<String>(Arrays.asList("C4", "A4", "B3")));
        result.put("C1 g1", new ArrayList<String>(Arrays.asList("C2", "B1")));
        result.put("C2 g1", new ArrayList<String>(Arrays.asList("C3", "B2", "C1")));
        result.put("C3 g1", new ArrayList<String>(Arrays.asList("C4", "B3", "C2")));
        result.put("C4 g1", new ArrayList<String>(Arrays.asList("B4", "C3")));
        result.put("A1 g2", new ArrayList<String>(Arrays.asList("A2", "B1")));
        result.put("A2 g2", new ArrayList<String>(Arrays.asList("A3", "B2", "A1")));
        result.put("A3 g2", new ArrayList<String>(Arrays.asList("A4", "B3", "A2")));
        result.put("A4 g2", new ArrayList<String>(Arrays.asList("B4", "A3")));
        result.put("B1 g2", new ArrayList<String>(Arrays.asList("B2", "C1", "A1")));
        result.put("B2 g2", new ArrayList<String>(Arrays.asList("B3", "C2", "A2", "B1")));
        result.put("B3 g2", new ArrayList<String>(Arrays.asList("B4", "C3", "A3", "B2")));
        result.put("B4 g2", new ArrayList<String>(Arrays.asList("C4", "A4", "B3")));
        result.put("C1 g2", new ArrayList<String>(Arrays.asList("C2", "B1")));
        result.put("C2 g2", new ArrayList<String>(Arrays.asList("C3", "B2", "C1")));
        result.put("C3 g2", new ArrayList<String>(Arrays.asList("C4", "B3", "C2")));
        result.put("C4 g2", new ArrayList<String>(Arrays.asList("B4", "C3")));
        result.put("A1 e1", new ArrayList<String>(Arrays.asList("B2")));
        result.put("A2 e1", new ArrayList<String>(Arrays.asList("B3", "B1")));
        result.put("A3 e1", new ArrayList<String>(Arrays.asList("B4", "B2")));
        result.put("A4 e1", new ArrayList<String>(Arrays.asList("B3")));
        result.put("B1 e1", new ArrayList<String>(Arrays.asList("C2", "A2")));
        result.put("B2 e1", new ArrayList<String>(Arrays.asList("C3", "A3", "A1", "C1")));
        result.put("B3 e1", new ArrayList<String>(Arrays.asList("C4", "A4", "A2", "C2")));
        result.put("B4 e1", new ArrayList<String>(Arrays.asList("A3", "C3")));
        result.put("C1 e1", new ArrayList<String>(Arrays.asList("B2")));
        result.put("C2 e1", new ArrayList<String>(Arrays.asList("B3", "B1")));
        result.put("C3 e1", new ArrayList<String>(Arrays.asList("B4", "B2")));
        result.put("C4 e1", new ArrayList<String>(Arrays.asList("B3")));
        result.put("A1 e2", new ArrayList<String>(Arrays.asList("B2")));
        result.put("A2 e2", new ArrayList<String>(Arrays.asList("B3", "B1")));
        result.put("A3 e2", new ArrayList<String>(Arrays.asList("B4", "B2")));
        result.put("A4 e2", new ArrayList<String>(Arrays.asList("B3")));
        result.put("B1 e2", new ArrayList<String>(Arrays.asList("C2", "A2")));
        result.put("B2 e2", new ArrayList<String>(Arrays.asList("C3", "A3", "A1", "C1")));
        result.put("B3 e2", new ArrayList<String>(Arrays.asList("C4", "A4", "A2", "C2")));
        result.put("B4 e2", new ArrayList<String>(Arrays.asList("A3", "C3")));
        result.put("C1 e2", new ArrayList<String>(Arrays.asList("B2")));
        result.put("C2 e2", new ArrayList<String>(Arrays.asList("B3", "B1")));
        result.put("C3 e2", new ArrayList<String>(Arrays.asList("B4", "B2")));
        result.put("C4 e2", new ArrayList<String>(Arrays.asList("B3")));
        result.put("A1 h1", new ArrayList<String>(Arrays.asList("A2", "B1")));
        result.put("A2 h1", new ArrayList<String>(Arrays.asList("A3", "B1", "B2", "A1")));
        result.put("A3 h1", new ArrayList<String>(Arrays.asList("A4", "B2", "B3", "A2")));
        result.put("A4 h1", new ArrayList<String>(Arrays.asList("B3", "B4", "A3")));
        result.put("B1 h1", new ArrayList<String>(Arrays.asList("B2", "C1", "A1")));
        result.put("B2 h1", new ArrayList<String>(Arrays.asList("B3", "C1", "C2", "A1", "A2", "B1")));
        result.put("B3 h1", new ArrayList<String>(Arrays.asList("B4", "C2", "C3", "A2", "A3", "B2")));
        result.put("B4 h1", new ArrayList<String>(Arrays.asList("C3", "C4", "A3", "A4", "B3")));
        result.put("C1 h1", new ArrayList<String>(Arrays.asList("C2", "B1")));
        result.put("C2 h1", new ArrayList<String>(Arrays.asList("C3", "B1", "B2", "C1")));
        result.put("C3 h1", new ArrayList<String>(Arrays.asList("C4", "B2", "B3", "C2")));
        result.put("C4 h1", new ArrayList<String>(Arrays.asList("B3", "B4", "C3")));
        result.put("A1 h2", new ArrayList<String>(Arrays.asList("B1", "B2", "A2")));
        result.put("A2 h2", new ArrayList<String>(Arrays.asList("B2", "B3", "A1", "A3")));
        result.put("A3 h2", new ArrayList<String>(Arrays.asList("B3", "B4", "A2", "A4")));
        result.put("A4 h2", new ArrayList<String>(Arrays.asList("B4", "A3")));
        result.put("B1 h2", new ArrayList<String>(Arrays.asList("C1", "C2", "A2", "A1", "B2")));
        result.put("B2 h2", new ArrayList<String>(Arrays.asList("C2", "C3", "A3", "A2", "B1", "B3")));
        result.put("B3 h2", new ArrayList<String>(Arrays.asList("C3", "C4", "A4", "A3", "B2", "B4")));
        result.put("B4 h2", new ArrayList<String>(Arrays.asList("C4", "A4", "B3")));
        result.put("C1 h2", new ArrayList<String>(Arrays.asList("B2", "B1", "C2")));
        result.put("C2 h2", new ArrayList<String>(Arrays.asList("B3", "B2", "C1", "C3")));
        result.put("C3 h2", new ArrayList<String>(Arrays.asList("B4", "B3", "C2", "C4")));
        result.put("C4 h2", new ArrayList<String>(Arrays.asList("B4", "C3")));
        result.put("A1 l1", new ArrayList<String>(Arrays.asList("B1", "B2", "A2")));
        result.put("A2 l1", new ArrayList<String>(Arrays.asList("A1", "B1", "B2", "B3", "A3")));
        result.put("A3 l1", new ArrayList<String>(Arrays.asList("A2", "B2", "B3", "B4", "A4")));
        result.put("A4 l1", new ArrayList<String>(Arrays.asList("A3", "B3", "B4")));
        result.put("B1 l1", new ArrayList<String>(Arrays.asList("C1", "A1", "C2", "A2", "B2")));
        result.put("B2 l1", new ArrayList<String>(Arrays.asList("B1", "C1", "A1", "C2", "A2", "C3", "A3", "B3")));
        result.put("B3 l1", new ArrayList<String>(Arrays.asList("B2", "C2", "A2", "C3", "A3", "C4", "A4", "B4")));
        result.put("B4 l1", new ArrayList<String>(Arrays.asList("B3", "C3", "A3", "C4", "A4")));
        result.put("C1 l1", new ArrayList<String>(Arrays.asList("B1", "B2", "C2")));
        result.put("C2 l1", new ArrayList<String>(Arrays.asList("C1", "B1", "B2", "B3", "C3")));
        result.put("C3 l1", new ArrayList<String>(Arrays.asList("C2", "B2", "B3", "B4", "C4")));
        result.put("C4 l1", new ArrayList<String>(Arrays.asList("C3", "B3", "B4")));
        result.put("A1 l2", new ArrayList<String>(Arrays.asList("B1", "B2", "A2")));
        result.put("A2 l2", new ArrayList<String>(Arrays.asList("A1", "B1", "B2", "B3", "A3")));
        result.put("A3 l2", new ArrayList<String>(Arrays.asList("A2", "B2", "B3", "B4", "A4")));
        result.put("A4 l2", new ArrayList<String>(Arrays.asList("A3", "B3", "B4")));
        result.put("B1 l2", new ArrayList<String>(Arrays.asList("C1", "A1", "C2", "A2", "B2")));
        result.put("B2 l2", new ArrayList<String>(Arrays.asList("B1", "C1", "A1", "C2", "A2", "C3", "A3", "B3")));
        result.put("B3 l2", new ArrayList<String>(Arrays.asList("B2", "C2", "A2", "C3", "A3", "C4", "A4", "B4")));
        result.put("B4 l2", new ArrayList<String>(Arrays.asList("B3", "C3", "A3", "C4", "A4")));
        result.put("C1 l2", new ArrayList<String>(Arrays.asList("B1", "B2", "C2")));
        result.put("C2 l2", new ArrayList<String>(Arrays.asList("C1", "B1", "B2", "B3", "C3")));
        result.put("C3 l2", new ArrayList<String>(Arrays.asList("C2", "B2", "B3", "B4", "C4")));
        result.put("C4 l2", new ArrayList<String>(Arrays.asList("C3", "B3", "B4")));

        return Collections.unmodifiableMap(result);
    }
}