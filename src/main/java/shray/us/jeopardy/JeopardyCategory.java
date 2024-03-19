package shray.us.jeopardy;

import java.util.Map;

public class JeopardyCategory {
    private String category_name;
    private Map<String, JeopardyClue> clues;

    public String get_name() {
        return category_name;
    }

    public JeopardyClue get_clue(String idx) { return clues.get(idx); }
}
