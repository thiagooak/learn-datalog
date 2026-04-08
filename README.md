# Learn Datalog

An interactive web app for learning [Datomic Datalog](https://docs.datomic.com/whatis/supported-ops.html#datalog).

The app serves a series of exercises. Each exercise includes an editable Datalog query that runs against a Datomic database populated with Pokemon data.

## Stack

- [http-kit](https://github.com/http-kit/http-kit) - HTTP server
- [Compojure](https://github.com/weavejester/compojure) - Routing
- [Hiccup](https://github.com/weavejester/hiccup) - HTML rendering from Clojure data structures
- [Datomic](https://docs.datomic.com/) - Database
- [Datastar](https://github.com/starfederation/datastar) - Frontend reactivity
- [Code Highlighter](https://github.com/thiagooak/code-highlighter) - Editable code blocks with syntax highlighting

## Development

Start the server:

```shell
clojure -X:run
```

Run tests:

```shell
clojure -X:test
```

Build an uberjar:

```shell
clojure -T:build uber
```

## Deployment

```shell
fly deploy
```
