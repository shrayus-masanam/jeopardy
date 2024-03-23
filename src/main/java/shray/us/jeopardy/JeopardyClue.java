package shray.us.jeopardy;

public class JeopardyClue {
    private String clue;
    private String acceptable_responses;

    boolean daily_double;
    boolean revealed = false;
    int value = 0; // filled in later

    public String toString() {
        return clue;
    }
    public String get_acceptable_responses() { return acceptable_responses; }
    public void set_value(int val) { value = val; }
    public int get_value() { return value; }

    public void set_revealed(boolean b) {
        revealed = b;
    }
}
