---
title: Configuration
slug: configuration
---

TheGoldEconomy plugin provides several configuration options to customize its behavior. Below is a detailed explanation of each configuration option available in the `config.yml` file.

## Options

### `removeGoldDrop`
- **Description**: Determines whether gold drops from mobs like Piglins should be removed.
- **Default**: `true`
- **Options**: `true`, `false`

### `updateCheck`
- **Description**: Specifies whether the plugin should check for updates.
- **Default**: `true`
- **Options**: `true`, `false`

### `language`
- **Description**: Sets the language for the plugin messages.
- **Default**: `"en_US"`
- **Options**:
  - `de_DE` (German)
  - `en_US` (English)
  - `es_ES` (Spanish)
  - `zh_CN` (Simplified Chinese)
  - `tr_TR` (Turkish)
  - `pt_BR` (Brazilian Portuguese)
  - `nb_NO` (Norwegian)
  - `uk` (Ukrainian)

### `restrictToBankPlot`
- **Description**: Restricts bank commands to bank plots (requires Towny).
- **Default**: `false`
- **Options**: `true`, `false`

### `prefix`
- **Description**: Sets the prefix for plugin messages.
- **Default**: `"TheGoldEconomy"`
- **Options**: Any string value.

### `base`
- **Description**: Sets the base denomination of the economy.
- **Default**: `"nuggets"`
- **Options**:
  - `nuggets`: 1 nugget is 1 currency, 1 ingot is 9, 1 block is 81
  - `ingots`: 1 ingot is 1 currency, 1 block is 9
  - `raw`: 1 raw gold is 1 currency, 1 block is 9

## Example Configuration

Here is an example of a `config.yml` file with the default settings:

```yaml
removeGoldDrop: true
updateCheck: true
language: "en_US"
restrictToBankPlot: false
prefix: "TheGoldEconomy"
base: "nuggets"