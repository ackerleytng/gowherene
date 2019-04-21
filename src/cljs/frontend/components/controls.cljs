(ns frontend.components.controls
  (:require [reagent.core :as r]
            [re-frame.core :as re-frame]
            [frontend.subs :as subs]
            [frontend.events :as events]))

(def url-placeholder "Paste url here (from sethlui, smartlocal...)")

(defn url-input []
  (let [value @(re-frame/subscribe [::subs/url-input])]
    [:input#url.input
     {:type "text"
      :value value
      :placeholder url-placeholder
      ;; :on-key-press url-input-handle-key-press
      :on-change #(re-frame/dispatch [::events/set-url-input (.. % -target -value)])}]))

(defn clear-button []
  [:a#clear.button
   [:span.icon.is-small.has-text-grey-light
    {:on-click #(re-frame/dispatch [::events/set-url-input ""])}
    [:i.fas.fa-times]]])

(defn append-button []
  (let [class @(re-frame/subscribe [::subs/append-loading])]
    [:a#append.button.is-info
     {:class class}
     [:span.icon
      {:on-click #(re-frame/dispatch [::events/parse-url :append])}
      [:i.fas.fa-angle-double-right]]]))

(defn plot-button []
  (let [class @(re-frame/subscribe [::subs/plot-loading])]
    [:a#plot.button.is-info
     {:class class}
     [:span.icon
      {:on-click #(re-frame/dispatch [::events/parse-url :plot])}
      [:i.fas.fa-angle-right]]]))

(defn clear-button-placeholder []
  (when-let [show-clear-button @(re-frame/subscribe [::subs/show-clear-button])]
    [:div.control
     [clear-button]]))

(defn append-button-placeholder []
  (when-let [show-append-button @(re-frame/subscribe [::subs/show-append-button])]
    [:div.control
     [append-button]]))

(defn controls []
  [:div#controls
   [:div.field.has-addons
    [:div.control.is-expanded
     [url-input]]
    [clear-button-placeholder]
    [:div.control
     [plot-button]]
    [append-button-placeholder]]])
