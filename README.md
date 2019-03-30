# RISK Team 3

A recreation of the strategy board game RISK.

## Building

There is a two step compilation process--one for the frontend UI that transpiles 
and bundles React, and another for the Scala backend and its templating framework.

1. Verify that Node.js is installed. If not, visit [this site](https://nodejs.org/en/download/) and install the version for your platform
2. Install node modules: `$ npm install`
3. Bundle using Webpack from the project directory root: `$ npm run build`

## Testing
1. Start the Scala server by typing: `$ sbt run`
2. Visit [http://localhost:9000](http://localhost:9000)

## Playing
To play the game, you need to open 3 different localhost tabs and input at least
3 different player names. After refreshing the page, the game can then be started. 

## Developing
1. Start Webpack bundling (in development mode) by typing: `$ npm run watch`

## Authors
Ishan Arya\
Justin Prindle\
Rahul Bhethanabotla\
Pranav Kommabathula\
Jeffrey Luo