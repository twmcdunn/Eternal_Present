package src.sound;

public class PrintChords {
    static String[] noteNames = new String[] {
            " C", "#C",
            " D", "#D", "SD",
            " E",
            " F", "#F",
            " G", "SG", "#G",
            " A", "#A",
            " B", "SB"
    };

    public static void printTs(int[][] chords) {
        for (int t = 0; t < 15; t += 3) {
            String a = "T" + t;
            if(t < 10)
            a += " ";
            for (int[] c : chords) {
                a += "[";
                for (int n = 0; n < c.length; n++) {
                    a += noteNames[c[n] % noteNames.length];
                    if (n < c.length - 1) {
                        a += ",";
                    }
                }
                a += "]";
            }
            System.out.println(a);
        }
    }

    public static void printProgressions() {
        System.out.println("---------------KEYBOARD---------------");
        int[][] chords = new int[][] { { 11, 1, 5, 8, 13 }, { 2, 7, 10, 14, 4 } };
        System.out.println("DEC 1,2");
        printTs(chords);

        chords = new int[][] { { 13, 9, 1, 3, 6 }, { 10, 0, 4, 7, 12 } };
        System.out.println("DEC 0,1");
        printTs(chords);
    }

    public static void main(String[] args) {
        printProgressions();
    }
}
