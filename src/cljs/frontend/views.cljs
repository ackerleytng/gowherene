(ns frontend.views
  (:require
   [frontend.components.misc :refer [header spinner-modal about-modal error-modal footer]]
   [frontend.components.controls :refer [controls]
    :rename {controls c-controls}]
   [frontend.components.url-list :refer [url-list]
    :rename {url-list c-url-list}]
   [frontend.components.recommendation-map :refer [recommendation-map]
    :rename {recommendation-map c-recommendation-map}]))

(defn app []
  [:<>
   [spinner-modal]
   [about-modal]
   [error-modal]
   [header]
   [:div.container
    [c-controls]
    [c-url-list]]
   [c-recommendation-map]
   [footer]])
