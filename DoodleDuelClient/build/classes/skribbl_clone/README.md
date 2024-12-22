# DoodleDuel
A Local Multiplayer _Skribble_ inspired game using Java

## TOOLS I USED:
* Java GUI Swing
* Java Sockets

## HOW TO INSTALL?
if you want to test it out with your friends to play with them you must consider the followings: 

* Download Java. I recommended to download Java version 8

* One must manage the Server, download the following files _GameServer.java_, _ClientHandlerGame.java_   _WordDictionary.java_ and the _word_list.txt_ <br>
* if you want to be both to manage the Game server and be able to play the game, download the required files and the _DoodleDuel.zip_ file
  
* for your friends or classmates to play the game they must download _DoodDuel.zip_ file

## HOW TO RUN THE GAME?
Server responsibility:  to compile the  _GameServer.java_, _ClientHandlerGame.java_ , _WordDictionary.java_   files you must:
  1. Go to your Operating System command line or terminal
  2. Change the directory folder where you downloaded the  _GameServer.java_ , _ClientHandlerGame.java_ , _WordDictionary.java_
  3. To compile type:
     
     ```
     javac  GameServer.java  ClientHandlerGame.java  WordDictionary.java
     ```
     
  5. run the main java file:
     
     ```
      java GameServer.java
     ```

* To run the following _DoodleDuel.zip_ file you must:
  1. Extract the zip file
  2. Go to your Operating system command line or terminal
  3. Change the directory where you downloaded the zip file
  4. Type:
     
     ```
      java -jar "DoddleDuel.jar"
     ```

## HOW TO PLAY THE GAME?
for the game to be played, players must be in the _same network connection_

* Server Responsibility: The players must know your IP and Port. To know your IP 
  1. Go to your Operating system command line or terminal
  2. Type:
     
    ```
      ipconfig
    ```

  3. once you know your IP address as the Server, the players are required to know it
  4. Port default number is _123_
  
> [!NOTE]
> Ensure that firewalls on both the server and client (players) devices allow traffic on the port.
> On the server side, make sure port 123 (or whatever port you're using) is open for incoming connections

> [!TIP]
> I recommend to use WI-FI hotspot as a network to share and connect to. this will prevent the hassle to troubleshoot connectivity due to network restrictions provided by the network administrators

### 📜 Copyright Notice
This project is created for educational and study purposes only.
All assets used in this project are either:

- Free resources available for public use, or
- Created by me, the author of this project.

ENJOYYYYY!!! 
