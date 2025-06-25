import java.util.ArrayList;
import java.util.Random;

/**
 * Write a description of class Game here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Sequencer {
    public final int GAME_LENGTH = 45;// 24;//24;//192;//174;//198;//29;//max possible = size of triad space * 12 /
                                      // size of syntagm
    public Board sourceSyntagm;
    public ArrayList<Game> gameTree;
    public int globalMax;
    public double globalMaxAverage, localMaxAverage, tollerableAverage, tollerableMin, minAveRep;
    public Game myGame;
    public ArrayList<Triad> telos;
    public int maxGameLengthSoFar, minRepNotes, maxAllowedRep;
    private Random rand;
    public int TET;// 21;
    public ArrayList<int[][]> transformationMatrix;
    public int[][] triadDictionary;
    public int[][] modes;
    public int[][] alternateChords;
    public double[] probDistOfAltChords;
    public static ArrayList<ChordRecord> chordRecords;
    public double dir; // used to control direction of gestures
    public int modeTrans;// amount upward to transpose mode to match last chords
    // this is updated when getChords() is called
    // no logic is currently implemented to identify *WHICH* mode is used, in cases
    // where multiple modes are allowed (not using this feature for 20-TET EFSP
    // project)
    // to implement this, start with Board.fitsMode()

    public Sequencer(int type) {
        dir = 0.5;
        switch (type) {
            case 0:
                TET = 15;
                triadDictionary = new int[][] {
                        { 0, 4, 7, 10, 12 }, { 0, 2, 5, 8, 12 },
                };
                modes = new int[][] { { 0, 1, 3, 4, 6, 7, 9, 10, 12, 13 } };
                sourceSyntagm = new Board(this);
                // type, root
                initializeHardCodedSource(new int[][] { { 0, 1 }, { 1, 2 } });
                break;

        }

        initializeVariables();
        rand = new Random(123);
        if (chordRecords == null)
            chordRecords = new ArrayList<ChordRecord>();
        Game sourceGame = new Game(sourceSyntagm, this);
        myGame = sourceGame;
    }

    // only implementing for 33TET dictionary
    public int[][] getCurrentTriadicSubsets() {
        switch (TET) {
            case 33:
                int[][] triads = new int[][] { { 0, 11, 19 }, { 0, 8, 19 } };
                int[][] subsets = new int[myGame.getLastBoard().size()][3];
                for (int i = 0; i < myGame.getLastBoard().size(); i++) {
                    Triad t = myGame.getLastBoard().get(i);
                    int[] triad = triads[t.type % 2];
                    for (int n = 0; n < triad.length; n++) {
                        subsets[i][n] = (triad[n] + t.root) % TET;
                    }
                }
                return subsets;
        }
        return null;
    }

    public void initializeVariables() {
        globalMax = 3; // max of minsyntacticdistance in a game
        globalMaxAverage = 0.0; // max of average distance in a game
        localMaxAverage = 0.0; // max of minLocalAverage in a game
        tollerableAverage = 2.4; // findAGoodGame continues to play
        tollerableMin = 2; // if useMin in findAGoodGame, then only continue if above tollerable min
        maxGameLengthSoFar = 0;// used only to monitor progress, doesn't factor into search calculations
        minRepNotes = 30;// Integer.MAX_VALUE;//smallest num of repeated notes used in a complete game
        minAveRep = 2.584;// average rep notes per board in best game found
        maxAllowedRep = 1;// limits num of rep notes allowed in any board

        Triad.ascending = true;

    }

    public void addTriadToSource(int type, int root) {
        sourceSyntagm.add(new Triad(type, root, this));
    }

    public void initializeHardCodedSource(int[][] hardCodedSource) {
        // int[][] hardCodedSource = new int[][]{{0,0},{1,2},{3,1}};

        for (int i = 0; i < hardCodedSource.length; i++)
            sourceSyntagm.add(new Triad(hardCodedSource[i][0], hardCodedSource[i][1], this));

        int[][] hardCodedTelos = new int[][] { { 0, 10 }, { 1, 8 } };
        // int[][] hardCodedTelos = new int[][]{{1,8}, {3,10}};
        telos = new ArrayList<Triad>();
        for (int i = 0; i < hardCodedTelos.length; i++)
            telos.add(new Triad(hardCodedTelos[i][0], hardCodedTelos[i][1], this));

        telos = null;
        // System.out.println("Source: " + sourceSyntagm);
        // System.out.println("Telos: " + telos);
        // System.out.println("minDist = " + sourceSyntagm.getMinSyntacticDistance());
    }

    public static void testTraids(Sequencer s) {
        int mdist = 0;
        for (int a = 0; a < s.triadDictionary.length; a++)
            for (int b = 0; b < s.triadDictionary.length; b++)
                for (int d = 0; d < s.TET; d++) {

                    Board brd = new Board(s);
                    brd.add(new Triad(a, 0, s));
                    brd.add(new Triad(b, d, s));
                    if (true && s.triadDictionary[a].length != s.triadDictionary[b].length)
                        continue;
                    if (brd.getMinSyntacticDistance() >= mdist) {
                        mdist = brd.getMinSyntacticDistance();
                        // System.out.println(brd);
                    }

                }

        Board brd = new Board(s);
        brd.add(new Triad(3, 0, s));
        brd.add(new Triad(0, 6, s));
        // System.out.println(brd);

    }

    private ArrayList<Board> getAllPossibleMoves(Game currentGame) {
        ArrayList<Board> incompleteBoards = new ArrayList<Board>();
        incompleteBoards.add(new Board(this));
        ArrayList<Board> completeBoards = new ArrayList<Board>();
        Board lastBoard = currentGame.getLastBoard();
        for (Triad t : lastBoard) {
            completeBoards = new ArrayList<Board>();
            for (int i = 0; i < t.transformationGroup.length; i++) {// directedTransformationGroup
                Triad transformed = t.generateTransformed(i);// generateDirectedTransformed
                if (
                // currentGame.notUsed(transformed) &&
                !currentGame.getLastBoard().contains(transformed) &&
                        ((notTelos(transformed) && currentGame.size() < GAME_LENGTH - 1) ||
                                (telos == null || !notTelos(transformed) && currentGame.size() == GAME_LENGTH - 1))

                ) {
                    for (Board b : incompleteBoards) {
                        if (b.contains(transformed))
                            continue;
                        b = new Board(b);
                        b.add(transformed);
                        // check if board fits in scale
                        if (b.fitsMode())
                            completeBoards.add(b);
                    }
                }
            }
            if (completeBoards.size() == 0)// no possible transformation
                return null;
            incompleteBoards = completeBoards;
        }
        // Collections.shuffle(completeBoards);
        return completeBoards;
    }

    class ChordRecord {
        double myTime;
        int myTet;
        int[][] myChords;

        public ChordRecord(double t, int tet, int[][] chords) {
            myTime = t;
            myTet = tet;
            myChords = chords;
        }
    }

    public static ChordRecord getChordRecord(double time) {
        ChordRecord rec = null;
        for (int i = 0; i < chordRecords.size(); i++) {
            if (chordRecords.get(i).myTime > time) {
                break;
            }
            rec = chordRecords.get(i);
        }
        return rec;
    }

    public static double[] getChordsTimeFrame(double time) {
        ChordRecord rec = null;
        double endTime = -1;
        for (int i = 0; i < chordRecords.size(); i++) {
            if (chordRecords.get(i).myTime > time) {
                break;
            }
            rec = chordRecords.get(i);
            if (i < chordRecords.size() - 1) {
                endTime = chordRecords.get(i + 1).myTime;
            } else {
                endTime = 11 * 60 + 45;
            }
        }

        return new double[] { rec.myTime, endTime };
    }

    

    public int[][] getChords(int[][] lastChords) {
        int minAllowedSynt = 3;
        minAllowedSynt = 0;

        ArrayList<Board> allPossibleMoves = getAllPossibleMoves(myGame);
        ArrayList<Game> allPossibleGames = new ArrayList<Game>();

        // chordPopulation (number of times chords in new boards have already occured in
        // the game
        int bestChordPop = Integer.MAX_VALUE;
        for (Board move : allPossibleMoves) {
            if (move.getMinSyntacticDistance() < minAllowedSynt ||
                    myGame.get(myGame.size() - 1).get(move.size() - 1).findShortestPath(move.get(0)) < minAllowedSynt)
                continue;
            Game game = new Game(myGame, this);
            game.makeMove(move);
            allPossibleGames.add(game);

            int chordPop = 0;
            for (Triad t : game.getLastBoard())
                chordPop += game.grid[t.type][t.root];
            bestChordPop = Math.min(bestChordPop, chordPop);

        }

        // of the best chord populations, find the best
        // synatic distances
        int opts = 0;
        int best = 0;
        for (Game game : allPossibleGames) {
            int chordPop = 0;
            for (Triad t : game.getLastBoard())
                chordPop += game.grid[t.type][t.root];
            if (chordPop == bestChordPop) {
                opts++;
                best = Math.max(best, game.minSyntacticDistance);
            }
        }

        // System.out.println("BEST DISTANCE: " + best + " BEST CHORD POP: " +
        // bestChordPop);

        // build list of games filtered based on the above
        // (first by chord pop, then by syntactic distance)
        ArrayList<Game> bestGames = new ArrayList<Game>();
        for (Game game : allPossibleGames) {
            int chordPop = 0;
            for (Triad t : game.getLastBoard())
                chordPop += game.grid[t.type][t.root];
            if (game.minSyntacticDistance == best && chordPop == bestChordPop)
                bestGames.add(game);
        }

        // pick randomly from the filtered game options
        System.out.println("NUM OF OPTIONS: " + bestGames.size() + "  |  SYNTACTIC DISTANCE: " + best
                + "   |  CHORD POP: " + bestChordPop + "  |  FILTERED OPTIONS: " + bestGames.size()
                + "  |  w/o syntactic dist filter: " + opts);
        myGame = bestGames.get((int) (rand.nextDouble() * bestGames.size()));

        Board lastBoard = myGame.getLastBoard();
        int[][] chords = new int[lastBoard.size()][];
        for (int i = 0; i < lastBoard.size(); i++) {
            ArrayList<Integer> notes = lastBoard.get(i).notes();
            chords[i] = new int[notes.size()];
            for (int n = 0; n < notes.size(); n++)
                chords[i][n] = notes.get(n);
        }

        modeTrans = lastBoard.modeTrans;
        String cstr = "";
        for (int[] c : chords) {
            cstr += "[";
            for (int n : c)
                cstr += n + ",";
            cstr += "]";
        }

        // System.out.println(cstr);

        return chords;
    }

    public static double aveRepNotes(Game g) {
        return repeatedNotes(g) / (double) g.size();
    }

    public static int repeatedNotes(Game g) {
        int reps = 0;
        for (Board b : g) {
            reps += numOfRepNotes(b);
        }
        return reps;
    }

    public static boolean lacksRepNotes(Board b) {
        ArrayList<Integer> notes = new ArrayList<Integer>();
        for (Triad chord : b) {
            notes.addAll(chord.notes());
        }
        for (int n1 = 0; n1 < notes.size(); n1++)
            for (int n2 = n1 + 1; n2 < notes.size(); n2++)
                if (notes.get(n1).equals(notes.get(n2)))
                    return false;
        return true;
    }

    public static int numOfRepNotes(Board b) {
        int reps = 0;
        ArrayList<Integer> notes = new ArrayList<Integer>();
        for (Triad chord : b) {
            notes.addAll(chord.notes());
        }
        for (int n1 = 0; n1 < notes.size(); n1++)
            for (int n2 = n1 + 1; n2 < notes.size(); n2++)
                if (notes.get(n1).equals(notes.get(n2)))
                    reps++;
        return reps;
    }

    public static void test() {
        System.out.println((new Integer(1)).equals(new Integer(1)));
    }

    public boolean hasCommonTone(Board b1, Board b2) {
        ArrayList<Triad> chords = new ArrayList<Triad>();
        chords.add(b1.get(b1.size() - 1));
        chords.addAll(b2);
        for (int i = 0; i < chords.size() - 1; i++) {
            Triad t1 = chords.get(i);
            Triad t2 = chords.get(i + 1);
            for (int m1 : triadDictionary[t1.type]) {
                for (int m2 : triadDictionary[t2.type]) {
                    if ((m1 + t1.root) % TET == (m2 + t2.root) % TET)
                        return true;
                }
            }
        }
        return false;
    }

    public boolean notTelos(Triad t) {
        if (telos == null)
            return true;
        for (Triad aT : telos)
            if (t.equals(aT))
                return false;
        return true;
    }

}
