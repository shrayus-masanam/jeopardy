# Jeopardy in Minecraft
<p>A fan-made recreation of the game show "Jeopardy!" for Minecraft 1.20 servers (work in progress)</p>

<details>
<summary><h2>Clips 📸</h2></summary>
This game is best played in a call with all players. The clips below don't necessarily showcase ideal gameplay, just some of the features of this plugin.

### Board Filling (loading questions/answers from JSON file)
https://github.com/user-attachments/assets/6655ff44-c0ec-4fcf-bf7a-1c68107e96d7

<br>

### Revealing clues
https://github.com/user-attachments/assets/23ce9fa7-fa16-4465-b9d0-b309371fcbc9

<br>

### Final Jeopardy
https://github.com/user-attachments/assets/bcf2ed4d-6b2a-44b3-b65d-d50a5eb1a7e2

</details>

## Setup
This plugin is intended for use with <a href="https://github.com/shrayus-masanam/jeopardy/releases/tag/resources">this world & resource pack</a> on a Spigot/Paper 1.20.4 server. DecentHolograms is the only requirement for the plugin to work, but the rest of the plugins are recommended for nice cosmetic effects. If you use the provided world file, you can use the included config files to easily set up all the plugins.

### Required Dependencies
- DecentHolograms (2.8.6+)
### Optional (Recommended) Dependencies
- ForcePack (1.3.3+)
  - Used to force the required resource pack. Pre-made config file <a href="">here</a>.
- WorldEdit (7.2.15+)
  - Used to change the color of the walls based on the game round. Config not needed.
- WorldGuard (7.0.9+)
  - Used to protect the world from modification. Pre-made config file <a href="">here</a>.
- CinematicStudio (1.4.6+)
  - Used to play a custom intro cutscene. Pre-made cutscene file <a href="">here</a>.
  - This is a paid plugin.

## Usage
⚠️ Disclaimer: I made this plugin in a few days to use with my friends. It hasn't been thoroughly tested and was not made for production use. This documentation is mostly for my own personal reference, but in the future I may update this to become more useable.

Create a game by having a server operator run this command:
```
/jeopardy create [game file name] [host] [contestant 1] [contestant 2] [contestant 3]
```
The game file refers to the JSON file containing clues for the game. It should be placed in the plugin's data folder under `games/`. Don't include the file extension in this command.

The host can now run: 
```
/jeopardy start (intro)
```
Including "intro" at the end will play the cutscene made with CinematicStudio.

The host can load a round by running:
```
/jeopardy load [single|double|final]
```

Right-clicking the host menu allows the host to reveal clues and perform other actions. Left-clicking the menu allows the host to toggle response acceptance. In Jeopardy, contestants are only allowed to respond after the host has fully finished reading the clue.

The player heads in the hosts inventory allow the host to add or subtract money from the contestant that is currently responding to a clue. Left-clicking will add, and right-clicking will subtract the dollar amount of the clue from the contestant. For Daily Doubles or Final Jeopardy wagers, the host must manually enter the dollar amount that is at stake (command will be shown to the host when this happens).

Contestants have a buzzer which they can right-click to attempt to buzz-in to respond to a clue. Only one person can respond at a time, and nobody can respond twice. There is a cooldown between clicks.

During Final Jeopardy, contestants **must** press the "Done" button on their book GUI before the host takes away their books. This is because the Minecraft client only updates the book's contents after the user cloeses the book. When I hosted a game, I gave my contestants an extra 5 seconds in Final Jeopardy to close their books.

## Acknowledgements
<a href="https://www.jeopardy.com/">Jeopardy Productions, Inc.</a> - Jeopardy! music and soundbites in resource pack<br>
<a href="https://www.planetminecraft.com/member/shark_cool/">Shark_cool</a> - Darker blackstone textures in the resource pack<br>
<a href="https://www.planetminecraft.com/member/disco_/">disco_</a> - The Temple of Notch map used in the intro cutscene

<hr>
<p>"Jeopardy!" is a trademark registered by Jeopardy Productions, Inc., which is not affiliated with, and does not endorse, this repository. All rights reserved by them.</p>
