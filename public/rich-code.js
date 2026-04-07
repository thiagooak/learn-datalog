class RichCode extends HTMLElement {
  #highlights = new Set();

  constructor() {
    super();
    this.highlight();
  }

  highlight() {
    const language = this.getAttribute("language");
    if (!language || !Prism.languages[language]) {
      console.error(`Language '${language}' not available. Run console.log(Prism.languages) to list the available languages.`)
      return
    }

    this.clearHighlights();
    this.normalize();

    let tokens = Prism.tokenize(this.innerText, Prism.languages[language]);
    let pos = 0;
    for (const token of tokens) {
      if (token.type) {
        const range = new Range();

        range.setStart(this.firstChild, pos);
        range.setEnd(this.firstChild, pos + token.length);

        if (!CSS.highlights.has(token.type)) {
          CSS.highlights.set(token.type, new Highlight());
        }
        CSS.highlights.get(token.type)?.add(range);
        this.#highlights.add({ tokenType:token.type, range });
      }

      pos += token.length;
    }
  }

  clearHighlights() {
    for (const highlight of this.#highlights) {
      CSS.highlights.get(highlight.tokenType)?.delete(highlight.range);
      this.#highlights.delete(highlight);
    }
  }
}

customElements.define("rich-code", RichCode);
