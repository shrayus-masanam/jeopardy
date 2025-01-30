package shray.us.jeopardy;

public class JeopardyClue {
    private String clue;
    private String acceptable_responses;

    boolean daily_double;
    boolean cat_revealed = false; // for when you need to reveal the category first
    boolean revealed = false;
    int value = 0; // filled in later

    public String toString() {
        return clue;
    }
    /*
     * Returns a string containing responses that are acceptable for this clue.
     * 
     * @return a string containing responses that are acceptable for this clue.
     */
    public String get_acceptable_responses() { return acceptable_responses; }

    public void set_value(int val) { value = val; }
    public int get_value() { return value; }

    /*
     * Sets whether or not this clue has already been revealed.
     * 
     * @param b whether or not this clue has already been revealed.
     */
    public void set_revealed(boolean b) {
        revealed = b;
    }
}
