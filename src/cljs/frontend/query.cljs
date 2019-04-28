(ns frontend.query
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [cemerick.url :refer [url-encode url-decode] :as zzz]
            [frontend.subs :as subs]))

;; Writing to address bar

(def query-marker "q=")

(defn string->query [string]
  (str query-marker (url-encode string)))

(defn url-root []
  (str js/window.location.origin js/window.location.pathname))

(defn history-push-state! [s]
  (.pushState js/history {} "go where ne" s))

(defn set-addr-bar-urls! [urls]
  (let [query-params (->> urls
                          (map string->query)
                          (string/join "&"))
        prefix (when (not (string/blank? query-params)) "?")
        address (str (url-root) prefix query-params)]
    (history-push-state! address)))

;; Reading from address bar

(defn query->string [query]
  (-> query
      (string/replace (re-pattern (str "^" query-marker)) "")
      url-decode))

(defn addr-bar-urls []
  (let [query-params (string/replace (.. js/window -location -search) #"^\?" "")]
    (if (string/blank? query-params) []
        (mapv query->string (string/split query-params #"&")))))
