(ns frontend.views
  (:require
   [re-frame.core :as re-frame]
   [frontend.subs :as subs]
   [frontend.components.misc :refer [header about-modal error-modal footer]]
   [frontend.components.controls :refer [controls]
    :rename {controls c-controls}]
   [frontend.components.url-list :refer [url-list]
    :rename {url-list c-url-list}]
   [frontend.components.recommendation-map :refer [recommendation-map]
    :rename {recommendation-map c-recommendation-map}]))

;; home

(defn home-panel []
  [:div
   [:div.container
    [c-controls]
    [c-url-list]]
   [c-recommendation-map]])

;; not-found

(defn not-found-panel []
  [:div
   [:h1 "Not found! What panel are you looking for?"]])


;; panels

(defn- panels [panel-name]
  (case panel-name
    :home-panel [home-panel]
    [not-found-panel]))

(defn show-panel [panel-name]
  [panels panel-name])

(defn main-panel []
  (let [active-panel @(re-frame/subscribe [::subs/active-panel])]
    [show-panel active-panel]))

(defn app []
  [:<>
   [about-modal]
   [error-modal]
   [header]
   [main-panel]
   [footer]])
