(ns frontend.components
  (:require [reagent.core :as r]
            [reagent.dom.server :as rs]
            [ajax.core :refer [GET]]
            [clojure.string :as str]
            [cemerick.url :as url]))

;; TODO reimplement address manipulation

(defn by-id [id]
  (.getElementById js/document id))

(defn window-url []
  (.. js/window -location -href))

(defn window-no-query []
  (str js/window.location.origin js/window.location.pathname))

(defn window-search []
  (str/replace js/window.location.search #"^\?" ""))

(defn history-push-state [s]
  (.pushState js/history {} "go where ne" s))

;; ------------------------
;; Url management

(defn url->query [url]
  (str "q=" (url/url-encode url)))

(defn urls->search [urls]
  (->> urls
       (map url->query)
       (str/join "&")))

(defn query->url [url]
  (url/url-decode (str/replace url #"^q=" "")))

(defn search->urls [search]
  (->> (str/split search #"&")
       (map query->url)))

(def queried-urls (atom []))

(add-watch queried-urls :queried-urls-watcher
           (fn [_ _ _ urls]
             (history-push-state (str (window-no-query) "?" (urls->search urls)))))
