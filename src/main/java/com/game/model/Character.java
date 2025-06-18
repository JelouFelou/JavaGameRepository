package main.java.com.game.model;

public class Character {
    private String name;
    private int health = 100;
    private boolean isHungry = false;
    private boolean isThirsty = false;
    private boolean isSick = false;
    private boolean isDead = false;

    // Constructor
    public Character(String name) {
        this.name = name;
    }

    // Methods
    public void feed() {
        isHungry = false;
    }
    public void applyDailyEffects() {
        if(isHungry) {
            health -= 10;
            System.out.println(name + " traci 10 zdrowia z głodu!");
        }
        if(isThirsty) {
            health -= 10;
            System.out.println(name + " traci 10 zdrowia z pragnienia!");
        }
        if(isSick) {
            health -= 5;
            System.out.println(name + " traci 5 zdrowia z powodu choroby!");
        }
    }
    public void copyStateFrom(Character source) {
        this.health = source.health;
        this.isHungry = source.isHungry;
        this.isThirsty = source.isThirsty;
        this.isSick = source.isSick;
    }
    public void setHealth(int health) {
        this.health = Math.max(0, Math.min(100, health));
        if (this.health <= 0) {
            this.isDead = true;
        }
    }
    public void setDead(boolean isDead) {
        this.isDead = isDead;
        if (isDead) {
            this.health = 0;
            this.isHungry = false;
            this.isThirsty = false;
            this.isSick = false;
        }
    }


    // Getter
    public String getName() { return name; }
    public int getHealth() { return health; }
    public boolean isHungry() { return isHungry; }
    public boolean isThirsty() { return isThirsty; }
    public boolean isSick() { return isSick; }
    public boolean isDead() {
        return isDead;
    }

    // Setter
    public void setHungry(boolean hungry) { isHungry = hungry; }
    public void setThirsty(boolean thirsty) { isThirsty = thirsty; }
    public void setSick(boolean sick) { isSick = sick; }
}
