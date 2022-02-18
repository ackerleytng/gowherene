(ns frontend.components.url-list
  (:require [re-frame.core :as re-frame]
            [frontend.subs :as subs]
            [frontend.events :as events]
            [frontend.utils.color :refer [build-color]]))

(defn color-box [color]
  [:svg {:width "12" :height "12" :style {:margin "3px"}}
   [:rect {:width "12" :height "12"
           :style {:fill color :fill-opacity 0.5
                   :stroke-width 2 :stroke "#000" :stroke-opacity 0.5}}]])

(defn url-row [url]
  [:div.panel-block {:style {:font-size "0.9em"}}
   [:span.panel-icon
    {:on-click #(re-frame/dispatch [::events/remove-url url])}
    [:i.fas.fa-times-circle]]
   [:a {:style {:overflow "hidden"} :href url} url]
   [color-box (build-color url)]])

(defn url-list []
  (let [url-list @(re-frame/subscribe [::subs/url-list])]
    (when (> (count url-list) 0)
      [:div#url-list.panel.space-out-top
       [:p.panel-heading {:style {:font-size "inherit"}} "Recommendations"]
       (for [url url-list]
         ^{:key url} [url-row url])])))
