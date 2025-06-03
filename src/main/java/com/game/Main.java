package main.java.com.game;

import main.java.com.game.core.GameState;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.print("\033[H\033[2J");
        System.out.flush();
        System.out.println("=== SURVIVAL GAME ===");
        Scanner scanner = new Scanner(System.in);
        GameState game = new GameState();
        game.startGame(scanner);
        scanner.close();
    }
}