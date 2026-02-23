package sim.util;

public class PinID {

    PinID() {
        throw new IllegalStateException("Utility class");
    }

    private static int pinCounter = 1;

    public static int getNextPinID() {
        return pinCounter++;
    }

    public static void setNextPinID(int id) {
        if (id > pinCounter) {
            pinCounter = id;
        }
    }
}
