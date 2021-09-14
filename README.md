# Daifugo

Daifugo is the japanese name of the card game also known as *President*, and this is our incarnation of it as a digital game. Featuring a client-server architecture with each client having access to a common lobby to the same server, with the possibility to change to a server of choice. Created as part of a university course, this is our implementation of the card game, since we couldn't find any digital way of playing it in the ongoing pandemic, and it was the breaktime diversion of choice for us when the student group had worked on other projects together.


![image](https://user-images.githubusercontent.com/6575679/113013757-4389ab00-917c-11eb-8f64-0a1bd9e7d61c.png)

The application and server is both written in Java, and even has support for profanity filtering and running several games in parallell, and even have them be password protected. The main challenge we had in this project was using Swing to create the UI (which was a requirement), a tool not well suited for this specific task at all.

![image](https://user-images.githubusercontent.com/6575679/113013788-4ab0b900-917c-11eb-8121-1704b0921e80.png)

## Running the game

You'll need Java version 15 or later installed, and then you'll simply have to run the `Daifugo.jar` file found under releases. A default server is online and should be able to serve your games, but If you want to host your own server you can also download the server executable `Daifugo-server.jar` also found under releases. However this is a game that needs at least three participants to be able to start it and begin playing.
