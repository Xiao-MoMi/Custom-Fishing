package net.momirealms.customfishing.object.loot;

import net.momirealms.customfishing.object.Difficulty;
import net.momirealms.customfishing.object.action.ActionB;
import net.momirealms.customfishing.requirements.Requirement;

import java.util.List;

public class Loot {

    String key;
    String nick;
    Difficulty difficulty;
    String group;
    boolean showInFinder;
    List<String> layout;
    List<ActionB> successActions;
    List<ActionB> failureActions;
    List<ActionB> hookActions;
    int weight;
    int time;
    List<Requirement> requirements;
    double score;

    public Loot(String key) {
        this.key = key;
    }

    public List<ActionB> getHookActions() {
        return hookActions;
    }

    public void setHookActions(List<ActionB> hookActions) {
        this.hookActions = hookActions;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<ActionB> getSuccessActions() {
        return successActions;
    }

    public void setSuccessActions(List<ActionB> successActions) {
        this.successActions = successActions;
    }

    public List<ActionB> getFailureActions() {
        return failureActions;
    }

    public void setFailureActions(List<ActionB> failureActions) {
        this.failureActions = failureActions;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public List<String> getLayout() {
        return layout;
    }

    public void setLayout(List<String> layout) {
        this.layout = layout;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public boolean isShowInFinder() {
        return showInFinder;
    }

    public void setShowInFinder(boolean showInFinder) {
        this.showInFinder = showInFinder;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public List<Requirement> getRequirements() {
        return requirements;
    }

    public void setRequirements(List<Requirement> requirements) {
        this.requirements = requirements;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
