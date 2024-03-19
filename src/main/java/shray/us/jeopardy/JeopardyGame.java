package shray.us.jeopardy;

import java.util.Map;

public class JeopardyGame {
    private Map<String, JeopardyCategory> single_jeopardy;
    private Map<String, JeopardyCategory> double_jeopardy;
    private JeopardyCategory final_jeopardy;
    private JeopardyCategory tie_breaker;

    public Map<String, JeopardyCategory> get_categories(String round) {
        if (round.equalsIgnoreCase("single")) {
            return single_jeopardy;
        } else if (round.equalsIgnoreCase("double")) {
            return double_jeopardy;
        } else {
            return null;
        }
    }

    public JeopardyClue get_clue(String round, String cat_idx, String clue_idx) {
        if (round.equalsIgnoreCase("single")) {
            return single_jeopardy.get(cat_idx).get_clue(clue_idx);
        } else if (round.equalsIgnoreCase("double")) {
            return double_jeopardy.get(cat_idx).get_clue(clue_idx);
        } else {
            return null;
        }
    }
}
