(ns frontend.components.controls
  (:require [reagent.core :as r]
            [re-frame.core :as re-frame]
            [frontend.subs :as subs]
            [frontend.events :as events]))

(def url-placeholder "Paste url here (from sethlui, smartlocal...)")

(defn url-input-handle-key-up [visible-append-button key]
  (when (= key "Enter")
    (re-frame/dispatch
     (if visible-append-button
       [::events/add-url-from-input]
       [::events/replace-urls-from-input]))))

(defn url-input []
  (let [value @(re-frame/subscribe [::subs/url-input])
        visible-append-button @(re-frame/subscribe [::subs/show-append-button])]
    [:input#url.input
     {:type "text"
      :value value
      :placeholder url-placeholder
      :on-key-up #(url-input-handle-key-up visible-append-button (.. % -key))
      :on-change #(re-frame/dispatch [::events/set-url-input (.. % -target -value)])}]))

(defn clear-button []
  [:a#clear.button
   {:on-click #(re-frame/dispatch [::events/set-url-input ""])}
   [:span.icon.is-small.has-text-grey-light
    [:i.fas.fa-times]]])

(defn append-button []
  [:a#append.button.is-info
   {:on-click #(re-frame/dispatch [::events/add-url-from-input])}
   [:span.icon
    [:i.fas.fa-angle-double-right]]])

(defn plot-button []
  [:a#plot.button.is-info
   {:on-click #(re-frame/dispatch [::events/replace-urls-from-input])}
   [:span.icon
    [:i.fas.fa-angle-right]]])

(defn clear-button-placeholder []
  (when-let [show-clear-button @(re-frame/subscribe [::subs/show-clear-button])]
    [:div.control
     [clear-button]]))

(defn append-button-placeholder []
  (when-let [show-append-button @(re-frame/subscribe [::subs/show-append-button])]
    [:div.control
     [append-button]]))

(defn controls []
  [:div#controls.space-out-top
   [:div.field.has-addons
    [:div.control.is-expanded
     [url-input]]
    [clear-button-placeholder]
    [:div.control
     [plot-button]]
    [append-button-placeholder]]])
