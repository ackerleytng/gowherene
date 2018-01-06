(ns app.core)

(defn set-html! [el content]
  (set! (.-innerHTML el) content))

(defn main
  []
  (let [content "Hello World from ClojureScript"
        element (js/document.getElementsByTagName "app")]
    (set-html! element content)))

(.clear js/console)

(enable-console-print!)

(main)
