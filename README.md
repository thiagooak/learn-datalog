# learn-datalog

## Quick setup
```shell
npx create-uix-app@latest my-app # bare-bones project
npx create-uix-app@latest my-app --re-frame # adds re-frame setup
npx create-uix-app@latest my-app --fly-io # creates full stack app with Fly.io
npx create-uix-app@latest MyApp --react-native # setup cljs project in existing React Native project
npx create-uix-app@latest MyApp --expo # create a new React Native project using Expo
```

## Development
```shell
npm i # install NPM deps
npm run dev # run front-end dev build in watch mode with CLJS REPL
clojure -M -m app.core # or run the server from REPL
```

## Deployment
```shell
fly app create uix-starter # create a new Fly.io app, run once
fly deploy
```
