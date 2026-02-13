![Modrinth Downloads](https://img.shields.io/modrinth/dt/thegoldeconomy)
![Spiget Rating](https://img.shields.io/spiget/rating/102242)
![Spiget tested server versions](https://img.shields.io/spiget/tested-versions/102242)
![Weblate project translated](https://img.shields.io/weblate/progress/thegoldeconomy)

![TheGoldEconomy Logo](https://github.com/user-attachments/assets/dd12ed0c-3058-4b1f-b12f-478c780730f4)

## Introduction

TheGoldEconomy is a powerful economy plugin that allows servers to manage their gold-based currency through a bank system. Players can deposit and withdraw gold and send money to each other. With support for both gold nuggets, ingots and raw gold this plugin is designed to be lightweight, easy to configure, and fully compatible with popular plugins like Vault and Towny.

## Table of Contents

<!--ts-->

- [Features](#features)
- [How to Install](#how-to-install)
- [Commands](#commands)
- [Permissions](#permissions)
- [Placeholders](#placeholders)
- [Config](#config-file)
<!--te-->

## Features

- Easy to use.
- Use nuggets or ingots as your base!
- PlaceholderAPI Integration!
- Towny and Vault support.
- TownyAPI Integration!
- Optional removing of Gold Drops by Mobs to reduce inflation.
- Multiple Language support!

## How to Install

1. **Install Vault**: Ensure you have [Vault](https://www.spigotmc.org/resources/vault.34315/) installed on your server. Vault is compatible with Minecraft versions newer than 1.17.
2. **Download TheGoldEconomy**: Get the latest version of TheGoldEconomy [here](https://modrinth.com/plugin/thegoldeconomy).
3. **Place the Plugin**: Move the downloaded `.jar` file into your server's `plugins` folder.
4. **Enjoy playing!**

## Commands

### Player

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

## Permissions

### Default Permissions

- `thegoldeconomy.balance`  
  /bank balance
- `thegoldeconomy.balance.others`  
  /bank balance <player>
- `thegoldeconomy.deposit`  
  /bank deposit
- `thegoldeconomy.withdraw`  
  /bank withdraw
- `thegoldeconomy.pay`  
  /bank pay

### Non-default Permissions

- `thegoldeconomy.set`  
  /bank set
- `thegoldeconomy.add`  
  /bank add
- `thegoldeconomy.remove`  
  /bank remove

## Placeholders

The following placeholders are available if using PlaceholderAPI

- `thegoldeconomy_inventoryBalance`
- `thegoldeconomy_bankbalance`
- `thegoldeconomy_totalBalance`

## Config File

```yaml
# Remove Gold Drops from Mobs like Piglins? (default: true)
removeGoldDrop: true
# Should the plugin check for updates? (default: true)
updateCheck: true
# Valid language are:
# Brazilian Portuguese: pt_BR
# Bulgarian: bg_BG
# English: en_US
# German: de_DE
# Japanese: jp_JP
# Norwegian: nb_NO
# Polish: pl_PL
# Simplified Chinese: zh_CN
# Spanish: es_ES
# Tamil: ta
# Turkish: tr_TR
# Ukrainian: uk
language: "en_US"
# Do you want to restrict bank commands to bank plots (requires Towny)
restrictToBankPlot: false
# Prefix
prefix: "TheGoldEconomy"
# This value sets the base domination of the economy
# 'nuggets' = 1 nugget is 1 currency, 1 ingot is 9, 1 block is 81
# 'ingots'  = 1 ingots is 1 currency, 1 block is 9
# 'raw' = 1 raw gold is 1 currency, 1 block is 9
base: "nuggets"
```
