package shray.us.jeopardy;

import org.bukkit.entity.Player;

public class JeopardyContestant {
    private Player player;
    private int money = 0;

    public JeopardyContestant(Player plr) {
        player = plr;
    }
    public Player get_player() {
        return player;
    }
    public void set_player(Player p) {
        player = p;
    }
    public int get_money() {
        return money;
    }
    public void set_money(int amount) {
        money = amount;
    }
    public void add_money(int addend) {
        money += addend;
    }
    public void subtract_money(int subtrahend) {
        money -= subtrahend;
    }
}
