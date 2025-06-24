# Auctioneer

[![GitHub Release](https://img.shields.io/github/v/release/Garsooon/Auctioneer?label=release)](https://github.com/Garsooon/Auctioneer/releases/latest)  
[![Downloads](https://img.shields.io/github/downloads/Garsooon/Auctioneer/total.svg?style=flat)](https://github.com/Garsooon/Auctioneer/releases)  
[![GitHub Stars](https://img.shields.io/github/stars/Garsooon/Auctioneer?style=social)](https://github.com/Garsooon/Auctioneer/stargazers)

**Auctioneer** is a simple chat-based auction plugin for Minecraft Beta 1.7.3, built on [Project Poseidon](https://github.com/retromcorg/Project-Poseidon).  
It lets players auction off items they’re holding, with configurable settings and economy support.

---

## Features

- Start auctions with `/auction <price>`
- Live bidding through chat
- Configurable auction time, bid time increases and item blacklist
- Optional minimum bid increase (after first bid)

---

## Requirements

- [Project Poseidon](https://github.com/retromcorg/Project-Poseidon)

---

## Installation

1. Download `Auctioneer.jar` from the [releases](https://github.com/Garsooon/Auctioneer/releases).
2. Drop it into your `plugins` folder.
3. Make sure [Poseidon](https://github.com/retromcorg/Project-Poseidon) is installed.
4. Restart your server.

---

## Usage

- **Start an auction**:  
  `/auction <starting_price> <MinBidIncrease>`  
  Begins an auction for the item you are holding.

- **Place a bid**:  
  Type your bid amount in chat during an active auction.

- **Auction rules**:
  - Only one auction can run at a time.
  - Minimum bid increment applies only after the first bid.
  - Blacklisted items cannot be auctioned.

---

## Configuration

The config file (`plugins/Auctioneer/config.yml`) lets you adjust:

- Auction duration  
- Blacklisted items  
- Minimum bid increase

---

## Development

This plugin is designed for Minecraft Beta 1.7.3 and was designed for Project Poseidon. It may not work with other server types or newer versions.

---

## Building

You can build the plugin using Maven:

```bash
mvn clean package
