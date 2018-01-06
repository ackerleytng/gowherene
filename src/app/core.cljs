(ns app.core
  (:require [reagent.core :as reagent :refer [atom]]))

(defn plot-url []
  (println "clicked"))

(defn process-key-press [e]
  (when (= "Enter" (.-key e))
    (plot-url)))

(defn controls []
  [:div.field.is-grouped
   [:p.control.is-expanded
    [:input#url.input {:type "text"
                       :placeholder "Enter your url here"
                       :on-key-press process-key-press}]]
   [:p.control
    [:a#plot.button.is-info {:on-click plot-url} "Plot!"]]])

(defn todos-list []
  [:section.section
   [:div.container
    [:h1.title "Playout: Layout your recommendations!"]
    [controls]]])

(defn page-component []
  (reagent/create-class {:render todos-list}))

;; initialize app
(reagent/render-component [page-component]
                          (.getElementById js/document "app"))

(enable-console-print!)
