import java.util.ArrayList;
public class Board extends ArrayList<Triad>{
    boolean isComposite, compTested;
    
    public Sequencer s;

    //assigned the amount *down* to transpose the set to match the mode
    //which is the amount *up* to transpose the mode to match the set
    //assigned when fitsmode() returns true
    public int modeTrans;

    Board(Sequencer seq){ s = seq;}

    Board(Board b){
        for(Triad t: b)
            add(new Triad(t));
            s = b.s;
    }

    public boolean contains(Triad t){
        for(Triad triad: this)
            if(triad.equals(t))
                return true;
        return false;
    }

    public boolean fitsMode(){

        ArrayList<Integer> superSet = new ArrayList<Integer>();
        for(Triad t: this)
            superSet.addAll(t.notes());
        for(int[] mode:s.modes)
            for(int i = 0; i < superSet.size(); i++){
                int propRoot = superSet.get(i);
                boolean isC = true;
                for(int m: superSet){
                    boolean isCont = false;
                    for(int c: mode){
                        if(c == (m + (s.TET - propRoot)) % s.TET){
                            isCont = true;
                            break;
                        }
                    }
                    if(!isCont){
                        isC = false;
                        break;
                    }
                }
                if(isC){
                    modeTrans = propRoot;
                    return true;
                }
            }
        return false;
    }

    public boolean isComposite(){
        if(compTested)
            return isComposite;
        compTested = true;
        isComposite = false;
        ArrayList<Integer> superSet = new ArrayList<Integer>();
        for(Triad t: this)
            superSet.addAll(t.notes());
        for(int i = 0; i < superSet.size(); i++){
            int propRoot = superSet.get(i);
            boolean isC = true;
            for(int m: superSet){
                boolean isCont = false;
                for(int c: Triad.COMPOSITE){
                    if(c == (m + (s.TET - propRoot)) % s.TET){
                        isCont = true;
                        break;
                    }
                }
                if(!isCont){
                    isC = false;
                    break;
                }
            }
            if(isC){
                isComposite = true;
                break;
            }
        }
        return isComposite;
    }

    int getMinSyntacticDistance(){
        int min = Integer.MAX_VALUE;
        for(int i = 1; i < size(); i++)
            min = Math.min(min, get(i-1).findShortestPath(get(i)));
        return min;
    }

    double getAveSyntacticDistance(){
        double average = 0;
        for(int i = 1; i < size(); i++)
            average += get(i-1).findShortestPath(get(i));
        average /= (double)(size() - 1);
        return average;
    }

    double getAveParadigmaticDistance(){
        double average = 0;
        double numberOfPairs = 0;
        for(int i = 0; i < size() - 1; i++)
            for(int n = i; n < size(); n++){
                average += get(i).findShortestPath(get(n));
                numberOfPairs++;
            }
        average /= numberOfPairs;
        return average;
    }

    //@Precondition size() > 0
    int getDistanceTo(Triad prospectiveFinalTriad){
        int d = get(size() - 1).findShortestPath(prospectiveFinalTriad);
        return d;
    }

    @Override
    public String toString(){
        String name = "";
        for(Triad t: this)
            name += t + " => ";
        name += "(" + getMinSyntacticDistance() + ") " + (get(0).root - get(1).root);
        if(isComposite())
            name += "*";

        return name;
    }

    public void print(){
        String name = "";
        for(Triad t: this)
            name += t.print() + " => ";
        System.out.println(name);
    }

    public void printPairs(){
        for(int i = 0; i< size(); i++){
            for(int n = i + 1; n < size(); n++){
                System.out.println(get(i) + "|" + get(n) + ":" + get(i).findShortestPath(get(n)));
            }
        }
    }
}