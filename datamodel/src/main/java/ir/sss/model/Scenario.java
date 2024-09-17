package ir.sss.model;

import java.util.ArrayList;
import java.util.List;

public class Scenario extends AbstractFact {

    public final String sid;
    public final String name;
    public List<Attack> attackList;

    public Scenario(String sid, String name) {
        super();
        this.sid = sid;
        this.name = name;
        attackList = new ArrayList<>();
    }

    public void addAttack(Attack attack) {
        attackList.add(attack);
    }

    public List<Attack> getAttackList() {
        return attackList;
    }

    public void setAttackList(List<Attack> attackList) {
        this.attackList = attackList;
    }

    public String getSid() {
        return sid;
    }

    public String getName() {
        return name;
    }

    public int getRid() {
        if (attackList.isEmpty()) return 0;
        return attackList.getLast().getRid();
    }

    public Attack getLastAttack() {
        if (attackList.isEmpty()) return null;
        return attackList.getLast();
    }

    public Attack getAttack(int index) {
        if (index >= 0 && index < attackList.size()) {
            return attackList.get(index);
        }
        return null;

    }

    public int getLevel() {
        return attackList.size();
    }
}
