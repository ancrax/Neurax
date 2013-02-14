package neurax;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helpers class contain helpers and parsers code. Methods are called only
 * staticaly. @todo refactor to Helpers and Parsers classes
 *
 * @author ancrax
 */
public class Helpers {

    /**
     * return index from disared column by Go Text Protocol specification
     *
     * @param column
     * @return int
     * @throws Exception
     */
    public static int getIndexByColumnByGTP(char column) throws Exception {
        //alphabet without 'I' character @see GTP v2 protocol - section 2.11
        int index = GameInterface.COLUMN_CHARS_GTP.indexOf(
                Character.toUpperCase(column));
        if (index == -1) {
            //@todo define own Exceptions
            throw new Exception("Cannot parse Column");
        }
        return index;
    }

    /**
     * return index from disared column by Smart Game Format specification
     *
     * @param column
     * @return int
     */
    public static int getIndexByColumnBySGF(char column) {
        //use all alphabet
        int index = GameInterface.COLUMN_CHARS_SGF.indexOf(
                Character.toUpperCase(column));
        return index;
    }

    /**
     * return label of column by index
     *
     * @param column
     * @return char
     */
    public static char getColumnByIndex(int column) {
        return GameInterface.COLUMN_CHARS_GTP.charAt(column);
    }

    /**
     * parse sample form file
     *
     * @param sgfFile
     * @return array of Samples []
     */
    public static Sample[] parseSamples(FileInputStream sgfFile) {
        //@todo need refactor to more separated methods - too looong method
        ArrayList<Sample> samples = new ArrayList<Sample>();
        ArrayList<String> moves = new ArrayList<String>();
        ArrayList<String> handicaps = new ArrayList<String>();
        char winner = 0;
        int boardSize = 0;

        DataInputStream input = new DataInputStream(sgfFile);
        BufferedReader buffered = new BufferedReader(new InputStreamReader(input));
        String str;

        //regex def
        Pattern patternWinner = Pattern.compile("(?i)RE\\[(.*?)\\]");
        Pattern patternBoardSize = Pattern.compile("(?i)SZ\\[(\\d+)\\]");
        Pattern patternHandicapNewLine = Pattern.compile("^\\[(\\w{2})]");

        try {
            while ((str = buffered.readLine()) != null) {
                //parse winner / match result
                if (winner == 0) {
                    Matcher matcherWinner = patternWinner.matcher(str);
                    if (matcherWinner.find()) {
                        winner = matcherWinner.group(1).charAt(0);
                    }
                }

                //parse boardSize
                if (boardSize == 0) {
                    Matcher matcherBoardSize = patternBoardSize.matcher(str);
                    if (matcherBoardSize.find()) {
                        boardSize = Integer.parseInt(matcherBoardSize.group(1));
                    }
                }

                //if not contain handicap of game sequence
                if (str.indexOf("AB[") != -1) {
                    try {
                        handicaps.add(str.substring(str.indexOf("AB[") + 3, str.
                                indexOf("AB[") + 5));
                        int searchIndex = str.indexOf("AB[") + 6;
                        while (true) {
                            //fetch handicaps
                            if (str.charAt(searchIndex) == '[') {
                                handicaps.add(str.substring(searchIndex + 1,
                                        searchIndex + 3));
                                searchIndex += 4;
                                continue;
                            }
                            if (str.charAt(searchIndex) == ' ') {
                                searchIndex++;
                                continue;
                            }
                            break;
                        }
                    }
                    catch (StringIndexOutOfBoundsException e) {
                        //nop
                    }
                }
                //find all handicap
                Matcher matcherHandicapNewLine = patternHandicapNewLine.matcher(str);
                while (matcherHandicapNewLine.find()) {
                    for (int i = 0; i < matcherHandicapNewLine.groupCount(); i++) {
                        handicaps.add(matcherHandicapNewLine.group(i).
                                replaceAll("\\W", ""));
                    }
                }

                //end of files head
                if (str.indexOf(";") == 0) {
                    break;
                }
                if (str.indexOf(";") != -1
                        && str.charAt(str.indexOf(";") - 1) != '(') {

                    str = str.substring(str.indexOf(";") + 1);
                    break;
                }
            }

            //add parsed moves
            String[] splitedMoves;
            while (str != null) {
                splitedMoves = str.split(";");
                moves.addAll(Arrays.asList(splitedMoves));

                str = buffered.readLine();
            }
            input.close();
        }
        catch (IOException ex) {
            if (GameInterface.DEBUG) {
                System.err.println("parse err in head of sgf file");
            }
            return null;
        }

        //Start working with Move abstraction
        Move playersMove;
        BoardState currentState = new BoardState(new int[boardSize][boardSize]);
        currentState.setBoardSize(boardSize);
        //insert handicap stones
        for (String handicapCoord : handicaps) {
            try {
                currentState.actionSetStone(new Move(
                        Helpers.getIndexByColumnBySGF(handicapCoord.charAt(0)),
                        Helpers.getIndexByColumnBySGF(handicapCoord.charAt(1)),
                        GameInterface.PLAYER_BLACK_ID));
            }
            catch (Exception ex) {
                if (GameInterface.DEBUG) {
                    System.err.println("Err while inserting handicap stone");
                }
                return null;
            }
        }

        BoardState nextState = (BoardState) currentState.clone();

        //@todo refactor - too long method
        for (String actmove : moves) {
            if (actmove.length() < 2) {
                continue;
            }

            if (actmove.indexOf("[]") == -1) {
                try {
                    if (boardSize < 20 && actmove.contains("[tt]")) {
                        playersMove = new Move(true, Helpers.parseColor(
                                Character.toString(actmove.charAt(0))));
                    }
                    else {
                        //fetch palyers moves
                        playersMove = new Move(
                                Helpers.getIndexByColumnBySGF(actmove.charAt(2)),
                                Helpers.getIndexByColumnBySGF(actmove.charAt(3)),
                                Helpers.parseColor(Character.toString(
                                actmove.charAt(0))));
                    }

                    nextState.actionSetStone(playersMove);

                }
                catch (Exception ex) {
                    System.out.println("err at coords:" + actmove);
                    System.out.println("at move" + Arrays.binarySearch(moves.
                            toArray(), actmove));

                    //file cant be parsed, some error occured
                    return null;
                }
            }
            else {
                /*
                 * next code is ready to serve when will be pass move in learn
                 * implemented try { playersMove = new Move(true,
                 * Helpers.parseColor( Character.toString(actmove.charAt(0))));
                 * } catch (Exception ex) { //file cant be parsed, some error
                 * occured return null; }
                 */
                continue;
            }

            //can be set to learn only from winner
            samples.add(new Sample(
                    currentState.getBoardState(),
                    nextState.getBoardState(), playersMove));

            currentState = (BoardState) nextState.clone();
        }

        return samples.toArray(new Sample[samples.size()]);
    }

    /**
     * Create Move instance from serialized String
     *
     * @param move
     * @return new instance of Object
     * @throws Exception
     */
    static Move getParsedMove(String move) throws Exception {
        String[] arguments = move.split(" ");
        int vertex[] = Helpers.parseVertex(arguments[1]);

        if (vertex[0] == -1) {
            return new Move(true, Helpers.parseColor(arguments[0]));
        }

        return new Move(vertex[0], vertex[1], Helpers.parseColor(arguments[0]));
    }

    /**
     *
     * @param vertex
     * @return int [] array of position, if was passed return {-1, -1}
     * @throws Exception
     */
    static int[] parseVertex(String vertex) throws Exception {
        vertex = vertex.toUpperCase();
        if (vertex.trim().equals("PASS")) {
            return new int[]{-1, -1};
        }
        char column = vertex.charAt(0);
        int row = Integer.parseInt(vertex.substring(1));
        return new int[]{getIndexByColumnByGTP(column), row - 1};
    }

    /**
     * parse color from String
     *
     * @param color
     * @return int as player index
     * @throws Exception
     */
    static int parseColor(String color) throws Exception {
        String lower = color.toLowerCase();
        if (lower.equals("w") || lower.equals("white")) {
            return GameInterface.PLAYER_WHITE_ID;
        }
        if (lower.equals("b") || lower.equals("black")) {
            return GameInterface.PLAYER_BLACK_ID;
        }
        throw new Exception("Cannot parse Color");
    }

    /**
     * return column axis for showed board
     *
     * @param boardSize
     * @return String axis (one row)
     */
    public static String getColumnAxis(int boardSize) {
        StringBuilder board = new StringBuilder();
        board.append("   ");
        for (int i = 1; i <= boardSize; i++) {
            board.append(" " + i);
        }
        return board.toString();
    }

    /**
     * get row index on desired line of showed board output
     *
     * @param index
     * @return String
     */
    public static String getRowIndex(int index) {
        StringBuilder board = new StringBuilder();
        if (index < 10) {
            board.append(' ');
        }
        board.append(GameInterface.COLUMN_CHARS_GTP.charAt(index) + " ");
        return board.toString();
    }

    /**
     * return vertex (from two positions, not pass) specified by GTP
     *
     * @param x
     * @param y
     * @return String
     */
    public static String getVertexFromCoord(int x, int y) {
        return Helpers.getColumnByIndex(x) + "" + (y + 1);
    }

    /**
     * invert players index
     *
     * @param color
     * @return if is inserted black, return white and analogous
     */
    public static int getInvertedColor(int color) {
        if (color == GameInterface.PLAYER_BLACK_ID) {
            return GameInterface.PLAYER_WHITE_ID;
        }
        if (color == GameInterface.PLAYER_WHITE_ID) {
            return GameInterface.PLAYER_BLACK_ID;
        }
        return GameInterface.FREE_NODE_ID;
    }

    /**
     * can parse score from array of int
     *
     * @param scores - scores [1] = score black, scores [2] = score white
     * @return String
     */
    public static String parseScoreFromArray(int[] scores) {
        if (scores[GameInterface.PLAYER_BLACK_ID]
                > scores[GameInterface.PLAYER_WHITE_ID]) {
            return "B+" + (scores[GameInterface.PLAYER_BLACK_ID]
                    - scores[GameInterface.PLAYER_WHITE_ID]);
        }
        if (scores[GameInterface.PLAYER_BLACK_ID]
                < scores[GameInterface.PLAYER_WHITE_ID]) {
            return "W+" + (scores[GameInterface.PLAYER_WHITE_ID]
                    - scores[GameInterface.PLAYER_BLACK_ID]);
        }
        else {
            return "0";
        }
    }

    /**
     * make one dimensional array from two dimensional
     *
     * @param twoDArray
     * @return Integer [] array
     */
    public static Integer[] get1DArrayFrom2D(int[][] twoDArray) {
        List<Integer> oneD = new ArrayList<Integer>();

        for (int[] row : twoDArray) {
            for (int c : row) {
                oneD.add(c);
            }
        }
        return oneD.toArray(new Integer[oneD.size()]);
    }
}
