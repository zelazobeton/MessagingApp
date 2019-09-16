package com.Client.src;

import java.util.Scanner;

public class ConsoleInOutHandler implements IUserInOutHandler {
    private Scanner scanner;

    public ConsoleInOutHandler() {
        scanner = new Scanner(System.in);
    }

    @Override
    public void displayString(String toDisplay) {
        System.out.println(toDisplay);
    }

    @Override
    public String readString() {
        return scanner.nextLine();
    }
}
