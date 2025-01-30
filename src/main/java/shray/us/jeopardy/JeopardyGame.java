package shray.us.jeopardy;

import java.util.Map;

public class JeopardyGame {
    private Map<String, JeopardyCategory> single_jeopardy;
    private Map<String, JeopardyCategory> double_jeopardy;
    private JeopardyCategory final_jeopardy;
    private JeopardyCategory tie_breaker;

    /*
     * Returns the categories for the given round.
     * 
     * @param round the round to get the categories for.
     * @return a list of categories for the given round.
     */
    public Map<String, JeopardyCategory> get_categories(String round) {
        if (round.equalsIgnoreCase("single")) {
            return single_jeopardy;
        } else if (round.equalsIgnoreCase("double")) {
            return double_jeopardy;
        } else {
            return null;
        }
    }

    /*
     * Special overloaded case: returns the category for Final Jeopardy or the Tiebreaker.
     * 
     * @param round the round to get the category for.
     * @return the category for the given round.
     */
    public JeopardyCategory get_category(String round) {
        if (round.equalsIgnoreCase("final")) {
            return final_jeopardy;
        } else if (round.equalsIgnoreCase("tiebreaker")) {
            return tie_breaker;
        } else {
            return null;
        }
    }

    /*
     * Returns the clue for the given round, category, and clue index.
     * 
     * @param round the round to get the clue for.
     * @param cat_idx the category index to get the clue for.
     * @param clue_idx the clue index to get the clue for.
     * @return the clue for the given round, category, and clue index.
     */
    public JeopardyClue get_clue(String round, String cat_idx, String clue_idx) {
        if (round.equalsIgnoreCase("single")) {
            return single_jeopardy.get(cat_idx).get_clue(clue_idx);
        } else if (round.equalsIgnoreCase("double")) {
            return double_jeopardy.get(cat_idx).get_clue(clue_idx);
        } else if (round.equalsIgnoreCase("final")) {
            return final_jeopardy.get_clue(clue_idx);
        } else if (round.equalsIgnoreCase("tiebreaker")) {
            return tie_breaker.get_clue(clue_idx);
        }
        return null;
    }
}
