(defproject gowherene "0.1.0-SNAPSHOT"
  :description "gowherene plots addresses from recommendation pages in Singapore on a map!"
  :url "https://gowherene.herokuapp.com"
  :min-lein-version "2.5.3"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [compojure "1.6.1"]
                 [ring/ring-jetty-adapter "1.7.1"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-json "0.4.0" :exclusions [cheshire
                                                      com.fasterxml.jackson.core/jackson-core]]
                 [environ "1.1.0"]
                 [mount "0.1.16"]

                 ;; Logging
                 [org.clojure/core.async "0.4.490"]
                 [com.novemberain/monger "3.5.0" :exclusions [com.google.guava/guava]]

                 ;; For reader
                 [clj-http "3.9.1"]
                 [xtreak/clj-http-ssrf "0.2.2"]
                 [hickory "0.7.1"]

                 ;; For client-side
                 [org.clojure/clojurescript "1.10.520"]
                 [reagent "0.8.1"]
                 [re-frame "0.10.6"]
                 [day8.re-frame/http-fx "0.1.6"]
                 [cljsjs/google-maps "3.18-1"]
                 [com.cemerick/url "0.1.1"]]
  :plugins [[lein-cljsbuild "1.1.7" :exclusions [[org.clojure/clojure]]]]
  :source-paths ["src/clj" "src/cljs"]
  :figwheel {:css-dirs ["resources/public/css"]}
  :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}
  :cljsbuild
  {:builds
   [{:id           "dev"
     :source-paths ["src/cljs"]
     :figwheel     {:on-jsload "frontend.core/mount-root"}
     :compiler     {:main                 frontend.core
                    :output-to            "resources/public/js/compiled/main.js"
                    :output-dir           "resources/public/js/compiled/out"
                    :asset-path           "js/compiled/out"
                    :source-map           "target/cljs-output/public/js/compiled/main.js.map"
                    :source-map-timestamp true
                    :preloads             [devtools.preload
                                           day8.re-frame-10x.preload]
                    :closure-defines      {"re_frame.trace.trace_enabled_QMARK_"        true
                                           "day8.re_frame.tracing.trace_enabled_QMARK_" true}
                    :external-config      {:devtools/config {:features-to-install :all}}}}
    ;; For production: lein cljsbuild once min
    {:id           "min"
     :source-paths ["src/cljs"]
     :jar          true
     ;; This output directory is part of a hack
     :compiler     {:main            frontend.core
                    :output-to       "target/cljs-output/public/js/compiled/main.js"
                    :output-dir      "target/cljs-output/public/js/compiled"
                    ;; Uncomment to debug compilation
                    ;; :source-map "target/cljs-output/public/js/compiled/main.js.map"
                    :optimizations   :advanced
                    :closure-defines {goog.DEBUG false}
                    :pretty-print    false}}]}
  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]
  :aliases {"requests-cleanup" ["run" "-m" "gowherene.logging.requests/cleanup-requests"]
            "requests-show"    ["run" "-m" "gowherene.logging.requests/show-requests"]}
  :profiles
  {:dev {:dependencies [;; Backend
                        [javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.2"]
                        [org.clojure/tools.namespace "0.2.11"]

                        ;; Frontend
                        [binaryage/devtools "0.9.10"]
                        [day8.re-frame/re-frame-10x "0.4.0"]
                        [day8.re-frame/tracing "0.5.1"]
                        [figwheel-sidecar "0.5.18"]
                        [cider/piggieback "0.4.0"]]
         :plugins      [[lein-figwheel "0.5.18"]]
         :source-paths ["dev"]}
   :prod    {:dependencies [[day8.re-frame/tracing-stubs "0.5.1"]]}
   :uberjar {:source-paths   ["src/clj"]
             :dependencies   [[day8.re-frame/tracing-stubs "0.5.1"]]
             :omit-source    true
             :main           gowherene.core
             :aot            :all
             ;; hack: By including this, lein will copy the production main.js output
             ;;   over the one from figwheel. The js/out directory still gets copied in,
             ;;   so that's not that great. The client won't load it though, so that's
             ;;   not so bad.
             :resource-paths ["target/cljs-output"]
             :prep-tasks     ["compile" ["cljsbuild" "once" "min"]]}})
