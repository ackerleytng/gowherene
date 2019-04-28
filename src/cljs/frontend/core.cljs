(ns frontend.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [day8.re-frame.http-fx]
            [frontend.events :as events]
            [frontend.views :as views]
            [frontend.config :as config]
            [goog.events :as gevents])
  (:import [goog History]
           [goog.history Html5History EventType]
           [goog.history.Html5History TokenTransformer]))


(defn init-history
  "Creates a new history object that correctly interprets urls https://gist.github.com/pleasetrythisathome/d1d9b1d74705b6771c20#file-browser-cljs-L11"
  []
  (let [transformer (TokenTransformer.)]
    (set! (.. transformer -retrieveToken)
          (fn [path-prefix location]
            (str (.-pathname location) (.-search location))))
    (set! (.. transformer -createUrl)
          (fn [token path-prefix location]
            (str path-prefix token)))
    (doto (Html5History. js/window transformer)
      (.setUseFragment false)
      (.setPathPrefix "")
      (.setEnabled true))))

(defn hook-browser-navigation! []
  (gevents/listen
   (init-history)
   EventType.NAVIGATE
   #(re-frame/dispatch [::events/replace-from-addr-bar])))

(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (re-frame/dispatch [::events/replace-from-addr-bar])
  (reagent/render [views/app]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (re-frame/dispatch-sync [::events/initialize-db])
  (hook-browser-navigation!)
  (dev-setup)
  (mount-root))
