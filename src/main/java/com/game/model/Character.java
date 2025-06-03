package main.java.com.game.model;

public class Character {
    private String name;
    private int health = 100;
    private boolean isHungry = false;

    // Constructor
    public Character(String name) {
        this.name = name;
    }

    // Methods
    public void applyHungerPenalty() {
        if(isHungry) health -= 20;
    }

    public void feed() {
        isHungry = false;
    }

    public void applyDailyEffects() {
        if(isHungry) {
            health -= 20;
            System.out.println(name + " traci 20 zdrowia z głodu!");
        }
    }

    // Getter
    public String getName() { return name; }
    public int getHealth() { return health; }
    public boolean isHungry() { return isHungry; }

    // Setter
    public void setHungry(boolean hungry) { isHungry = hungry; }
}
