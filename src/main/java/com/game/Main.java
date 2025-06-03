package main.java.com.game;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== SURVIVAL GAME ===");
        Scanner scanner = new Scanner(System.in);
        GameState game = new GameState();
        game.startGame(scanner);
        scanner.close();
    }
}