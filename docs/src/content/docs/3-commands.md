---
title: Commands 
slug: commands
---

## Player Commands

- **/bank balance**  
  Displays your current bank balance (e.g, `/bank balance`).
- **/bank balance <player>**  
  Shows the balance of the specified player (e.g., `/bank balance Steve`).
- **/bank deposit <gold>**  
  Deposits the specified amount of gold from your inventory into your bank account (e.g., `/bank deposit 10`). To deposit everything use `/bank deposit` without an amount.
- **/bank withdraw <gold>**  
  Withdraws the specified amount of gold from your bank account into your inventory (e.g., `/bank withdraw 5`). To withdraw everything use `/bank withdraw` without an amount.
- **/bank pay <player> <gold>**  
  Transfers the specified amount of gold to another player (e.g., `/bank pay Alex 20`).

## Admin Commands

- **/bank set <player> <gold>**  
  Sets the balance of the specified player to the specified amount (e.g., `/bank set Steve 100`).
- **/bank add <player> <gold>**
    Adds the specified amount of gold to the balance of the specified player (e.g., `/bank add Steve 50`).
- **/bank remove <player> <gold>**
    Removes the specified amount of gold from the balance of the specified player (e.g., `/bank remove Steve 10`).