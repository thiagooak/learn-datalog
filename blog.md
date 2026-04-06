# How I Built an Interactive Datalog Learning Website

I wanted to build a website where people could learn Datalog by actually running queries in their browser — no setup, no installs, just write a query and see the results. Here's how I put it together.

## The Stack

The whole backend is Clojure. The key libraries are:

- **[http-kit](https://github.com/http-kit/http-kit)** — the HTTP server
- **[Compojure](https://github.com/weavejester/compojure)** — routing
- **[Hiccup](https://github.com/weavejester/hiccup)** — HTML generation from Clojure data structures
- **[Datomic Peer](https://www.datomic.com/)** — the database that speaks Datalog natively
- **[Datastar](https://data-star.dev/)** — a tiny JS framework for reactive UI without writing JavaScript

There is no frontend build step on the Clojure side. HTML is rendered server-side with Hiccup, and Datastar handles the interactive bits via a CDN script tag.

## The Idea: Run Queries in the Browser

The core interaction is simple: the user sees a Datalog query in an editable code block, clicks "Run", and sees the result appear below it.

Each runnable example on the page is a self-contained component. In `ui.clj`, the `runnable` function generates the HTML for one of these widgets. It creates two named signals — an input signal holding the current query text and an output signal holding the result — and wires them up with Datastar attributes:

```clojure
(defn runnable [input]
  (let [random-name (swap! runnable-counter inc)
        input-name  (str "in" random-name)
        output-name (str "out" random-name)]
    [:div {(str "data-signals:" input-name) (str "'" input "'")
           (str "data-signals:" output-name) "',,,'"}
     ...]))
```

The "Run" button fires a POST to `/api/q`, sending only the two relevant signals:

```html
data-on:click__prevent="@post('/api/q', {filterSignals: {include: /^in1|out1$/}})"
```

The server receives the query, runs it against a fresh in-memory Datomic database, and returns the result. The output signal updates, and Datastar patches the DOM.

## The Backend: Fresh Database Per Request

Every query runs against a brand new in-memory Datomic database. `db.clj` creates an anonymous connection on each request using `datomic.api/squuid` to generate a unique URI:

```clojure
(defn scratch-conn []
  (let [uri (str db-uri-base (d/squuid))]
    (d/delete-database uri)
    (d/create-database uri)
    (d/connect uri)))
```

After the connection is created, the Pokemon dataset is loaded from `pokemon.edn` via `transact-all`. Then the user's query is evaluated with `d/q`.

This approach keeps things simple and stateless: there is no session, no shared mutable state between users, and no cleanup needed. The tradeoff is the cost of re-loading the data on every request, but for a learning tool with a small dataset it's perfectly fine.

## The Data

The dataset is Pokemon — a good choice for a learning tool because the schema is intuitive and the data is rich enough to write interesting queries. Each Pokemon is an entity with attributes like `:pokemon/name`, `:pokemon/type`, and stats like `:stat/speed`, `:stat/attack`, and so on.

The schema and data are stored as EDN in `resources/pokemon.edn` and loaded into Datomic at startup.

## Syntax Highlighting

Code blocks use the [`syntax-highlight-element`](https://github.com/andreruffert/syntax-highlight-element) web component, loaded from a CDN. It handles both the static display blocks and the editable input blocks. A small config file in `public/syntax-highlight-config.js` tells it to load only the Clojure grammar.

The input blocks are `contenteditable`, so the user can edit the query directly in the highlighted code view. When the content changes, Datastar syncs the inner text back into the signal, keeping the state up to date without any custom JavaScript.

## HTML Generation with Hiccup

There is no template engine. The entire page — layout, content, and interactive components — is Clojure data. Hiccup converts nested vectors into HTML:

```clojure
(defn page [title children]
  [:html
   [:head
    [:title title]
    ...]
   [:body children
    [:script {:type "module" :src "...datastar.js"}]]])
```

Content lives in `content.clj`, which assembles the page by composing static prose with calls to `(app.ui/runnable ...)`.

## Deployment

The app is deployed to [Fly.io](https://fly.io) using a two-stage Dockerfile. The builder stage compiles the Clojure code into an uberjar using `tools.build`. The runtime stage is a lean Alpine JVM image that just runs the jar:

```dockerfile
FROM clojure:tools-deps-bookworm-slim AS builder
...
RUN clojure -T:build uber

FROM eclipse-temurin:21-alpine AS runtime
COPY --from=builder /opt/target/app.jar /app.jar
ENTRYPOINT ["java", "-cp", "app.jar", "clojure.main", "-m", "app.core"]
```

The Fly configuration (`fly.toml`) deploys to the `gru` region (São Paulo), sets up HTTPS, and configures the machine to stop when idle and start on the next request — keeping costs near zero for a low-traffic educational site.

## What I Learned

The most interesting design decision was running a fresh Datomic database per request. It feels wasteful at first glance, but it's exactly the right tradeoff for this use case: no auth, no state management, no security concerns about one user's queries affecting another's, and the code stays simple.

Datastar is a genuinely small dependency. The interactive behavior — editable inputs, button actions, reactive output display — required zero custom JavaScript. All the interactivity is expressed as HTML attributes generated by the Clojure code.

The result is a website with no frontend build pipeline, no JavaScript framework, and no database server to manage, that still feels interactive and responsive.
