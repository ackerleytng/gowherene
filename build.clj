(ns build
  (:require [clojure.tools.build.api :as b]))

(def output-dir "target")
(def class-dir (str output-dir "/classes"))
(def basis (b/create-basis {:project "deps.edn"}))
(def uber-file (str output-dir "/gowherene.jar"))

(defn clean [_]
  (b/delete {:path uber-file}))

(defn uber [_]
  (clean nil)
  (b/copy-dir {:src-dirs ["src/clj"]
               :target-dir class-dir})
  (b/compile-clj {:basis basis
                  :src-dirs ["src/clj"]
                  :class-dir class-dir})
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis basis
           :main 'gowherene.core}))
