(defproject gowherene "0.1.0-SNAPSHOT"
  :description "gowherene plots addresses from recommendation pages in Singapore on a map!"
  :url "https://gowherene.ackerleytng.com"
  :min-lein-version "2.5.3"
  :dependencies [[org.clojure/clojure "1.10.0"]

                 ;; For backend
                 [compojure "1.6.1"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-json "0.5.0"]
                 [ring/ring-jetty-adapter "1.8.0"]

                 [environ "1.1.0"]
                 [clj-http "3.9.1"]
                 [xtreak/clj-http-ssrf "0.2.2"]
                 [hickory "0.7.1"]

                 ;; For frontend
                 [org.clojure/clojurescript "1.10.520"]
                 [reagent "0.8.1"]
                 [re-frame "0.10.6"]
                 [day8.re-frame/http-fx "0.1.6"]
                 [cljsjs/google-maps "3.18-1"]
                 [com.cemerick/url "0.1.1"]]
  :source-paths ["src/clj" "src/cljs"]
  :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}
  :profiles
  {:dev {:source-paths ["dev"]
         :dependencies [;; For backend
                        [mount "0.1.16"]
                        [ring/ring-mock "0.3.2"]
                        [org.clojure/tools.namespace "0.2.11"]

                        ;; For frontend
                        [com.bhauman/figwheel-main "0.2.3"]
                        [day8.re-frame/re-frame-10x "0.4.0"]
                        [day8.re-frame/tracing "0.5.1"]
                        [cider/piggieback "0.4.2"]]}
   :uberjar {;; Don't need the frontend files in here
             :uberjar-exclusions [#"public/.*"]
             :source-paths ["src/clj"]
             :omit-source  true
             :main         gowherene.core
             :aot          :all}}
  :aliases {"fig"      ["trampoline" "run" "-m" "figwheel.main"]
            "fig:dev"  ["trampoline" "run" "-m" "figwheel.main" "--build" "dev" "--repl"]
            "fig:prod" ["trampoline" "run" "-m" "figwheel.main" "--build-once" "prod"]})
