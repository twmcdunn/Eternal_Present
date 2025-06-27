package src.sequencer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class SmoothVoiceLeading {

    public static ArrayList<int[]> getAllOrdersArr(int[] notesToOrder) {
        ArrayList<int[]> orders = new ArrayList<int[]>();
        if (notesToOrder.length > 1) {
            for (int i = 0; i < notesToOrder.length; i++) {
                ArrayList<Integer> remaining = new ArrayList<Integer>();
                for (int n = 0; n < notesToOrder.length; n++) {
                    if (n != i)
                        remaining.add(notesToOrder[n]);
                }
                int firstNote = notesToOrder[i];

                ArrayList<int[]> ordersOfRemaining = getAllOrdersArr(
                        remaining.stream().mapToInt(a -> a.intValue()).toArray());
                for (int[] order : ordersOfRemaining) {
                    ArrayList<Integer> completeOrder = new ArrayList<Integer>();
                    completeOrder.add(firstNote);
                    for (int elem : order) {
                        completeOrder.add(elem);
                    }
                    orders.add(completeOrder.stream().mapToInt(a -> a.intValue()).toArray());
                }
            }
        } else {
            orders.add(notesToOrder.clone());
        }
        return orders;
    }

    public static ArrayList<ArrayList<Integer>> getAllOrders(ArrayList<Integer> notesToOrder) {
        ArrayList<ArrayList<Integer>> orders = new ArrayList<ArrayList<Integer>>();

        if (notesToOrder.size() > 1) {
            for (int i = 0; i < notesToOrder.size(); i++) {
                ArrayList<Integer> remaining = new ArrayList<Integer>(notesToOrder);
                remaining.remove(i);
                int firstNote = notesToOrder.get(i);

                ArrayList<ArrayList<Integer>> ordersOfRemaining = getAllOrders(remaining);
                for (ArrayList<Integer> order : ordersOfRemaining) {
                    ArrayList<Integer> completeOrder = new ArrayList<Integer>();
                    completeOrder.add(firstNote);
                    completeOrder.addAll(order);
                    orders.add(completeOrder);
                }
            }
        } else {
            ArrayList<Integer> singleOrder = new ArrayList<Integer>(notesToOrder);
            orders.add(singleOrder);
        }
        return orders;
    }

    // @Precondition: firstChord and secondChord have the same number of notes
    public static int[] smoothVoiceLeading(int[] firstChord, int[] secondChord, int tet) {
        ArrayList<Integer> secondChordAsArrayList = arrToArrList(secondChord);
        ArrayList<ArrayList<Integer>> allOrders = getAllOrders(secondChordAsArrayList);

        int[] bestOrder = null;// dummy values
        int smallestTotalMovement = Integer.MAX_VALUE;
        for (ArrayList<Integer> order : allOrders) {
            int totalMovement = 0;
            int[] orderInBestOctaves = new int[secondChord.length];
            for (int i = 0; i < firstChord.length; i++) {
                orderInBestOctaves[i] = closestOct(firstChord[i], order.get(i), tet);
                totalMovement += Math.abs(orderInBestOctaves[i] - firstChord[i]);
            }
            if (totalMovement < smallestTotalMovement) {
                smallestTotalMovement = totalMovement;
                bestOrder = orderInBestOctaves;
            }
        }
        return bestOrder;
    }

    // @Precondition: firstChord and secondChord have the same number of notes
    public static int[] smoothVoiceLeadingToTarget(int[] firstChord, int[] secondChord, double target, int tet) {
        ArrayList<Integer> secondChordAsArrayList = arrToArrList(secondChord);
        ArrayList<ArrayList<Integer>> allOrders = getAllOrders(secondChordAsArrayList);

        int[] bestOrder = null;// dummy values
        double smallestDistanceFromTarget = Double.MAX_VALUE;
        for (ArrayList<Integer> order : allOrders) {
            double averagePitch = 0;
            int[] orderInBestOctaves = new int[secondChord.length];
            for (int i = 0; i < firstChord.length; i++) {
                orderInBestOctaves[i] = closestOct(firstChord[i], order.get(i), tet);
                averagePitch += orderInBestOctaves[i];
            }
            averagePitch /= (double) orderInBestOctaves.length;

            if (Math.abs(averagePitch - target) < smallestDistanceFromTarget) {
                smallestDistanceFromTarget = Math.abs(averagePitch - target);
                bestOrder = orderInBestOctaves;
            }
        }
        return bestOrder;
    }

    public static ArrayList<Integer> arrToArrList(int[] arr) {
        return new ArrayList<Integer>(Arrays.stream(arr).boxed().collect(Collectors.toList()));
    }

    public static int closestOct(int target, int pc, int tet) {
        int oct = (int) Math.rint((target - pc) / (double) tet);
        return oct * tet + pc;
    }
}
