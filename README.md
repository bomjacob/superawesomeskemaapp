# @Skema
Aarhus Tech Timetable App  
Informationtechnologi exam project (danish)

# Overview

## Horario
This folder contains the android app source code. The most interesting files (the java source code) is in Horario\app\src\main\java\dk\aarhustech\edu\rainbow\horario. Android Studio is required to build the files.
## server
This folder contains the server that talks with the app and selvbetjening.aarhustech.dk. It is written in javascript using node.js and loopback. Execute the following commands to start server (replacing YOUR_USERNAME_HERE and YOUR_PASSWORD_HERE with base64 encoded versions of username and password for selvbetjening respectively) (assuming you are in root of repository and node.js is installed).
### linux/mac
``` bash
cd server
npm install
export UVMUSERNAME=YOUR_USERNAME_HERE
export UVMPASSWORD=YOUR_PASSWORD_HERE
node .
```
### windows
```batch
cd server
npm install
set UVMUSERNAME="YOUR_USERNAME_HERE"
set UVMPASSWORD="YOUR_PASSWORD_HERE"
node .
```
## itexam
This folder contains python files used to generate png files from svg and to train the machine learning models used in the app.
Ipython and various other scientific python libaries are required to open and run the ipynb notebook files. We recommend installing an python Anaconda installation (py3k).
