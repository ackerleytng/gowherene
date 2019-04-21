(ns frontend.components.misc
  (:require [reagent.core :as r]
            [re-frame.core :as re-frame]
            [frontend.subs :as subs]
            [frontend.events :as events]

            [reagent.dom.server :as rs]
            [ajax.core :refer [GET]]
            [clojure.string :as str]
            [cemerick.url :as url]))

(defn gowherene []
  [:span {:style {:word-spacing "-0.13em"}} "go where ne"])

(defn header []
  [:div
   {:style {:padding "3rem 1.5rem 0 1.5rem"
            :font-family "'Roboto', sans-serif !important"}
    :on-click #(re-frame/dispatch [::events/set-about-active true])}
   [:div.container
    [:div.columns.is-variable.is-2.is-vcentered.is-mobile
     [:div.column.is-narrow
      [:figure.image.is-64x64
       [:img {:src "img/gowherene.svg"}]]]
     [:div.column
      [:div.columns
       [:div.column.is-narrow
        [:h1.title {:style {:font-weight "bold"}} [gowherene]]]
       [:h2.subtitle.is-hidden-mobile
        {:style {:margin-top "1.3rem"}}
        "map out your recommendations!"]]]]]])

(defn footer []
  [:footer.footer
   [:div.level
    [:div.level-left
     [:div.level-item
      [:a {:on-click #(re-frame/dispatch [::events/set-about-active true])}
       [:span.icon [:i.far.fa-question-circle]] "About " [gowherene]]]
     [:div.level-item
      [:a {:href "https://docs.google.com/forms/d/e/1FAIpQLSeOl80MfgEdHzfptlb44MUakucRBjWwTBzZuVK85Zjtgi9A-Q/viewform"}
       [:span.icon [:i.far.fa-flag]] "Report error"]]]
    [:div.level-right
     [:div.level-item
      [:a {:href "https://www.paypal.me/ackerleytng/1"}
       [:span.icon [:i.fab.fa-paypal]] "Help maintain " [gowherene]]]
     [:div.level-item
      [:a {:href "https://github.com/ackerleytng"}
       [:span.icon [:i.fab.fa-github]] "ackerleytng"]]]]])

(defn about-modal []
  (let [about-modal-class @(re-frame/subscribe [::subs/about-modal-class])]
    [:div#about.modal
     {:class about-modal-class}
     [:div.modal-background
      {:on-click #(re-frame/dispatch [::events/set-about-active false])}]
     [:div.modal-content
      [:div.box
       [:article.media
        [:div.media-left
         [:figure.image.is-64x64
          [:img {:src "img/gowherene.svg"}]]]
        [:div.media-content
         [:div.content
          [:p
           [:strong [gowherene]] [:br]
           "For a special someone, who adorably adds the chinese particle "
           "呢 (" [:em "ne"] ")"
           " when wondering what to do, where to go, deferring the decision..."]
          [:p "Use " [gowherene] " to plot locations from "
           [:a {:href "http://thesmartlocal.com/read/cheap-food-orchard"} "recommendations"] ", "
           [:a {:href "http://www.harveynorman.com.sg/store-finder.html"} "contact-us pages"]
           ", to quickly evaluate your options!"]
          [:p.is-size-7 "A clojure/clojurescript project by "
           [:a {:href "https://github.com/ackerleytng"}
            [:span.icon [:i.fab.fa-github]] "ackerleytng"]]]]]]]
     [:button.modal-close.is-large
      {:aria-label "close"
       :on-click #(re-frame/dispatch [::events/set-about-active false])}]]))

(defn error-modal []
  (let [error-modal-class @(re-frame/subscribe [::subs/error-modal-class])
        error-message @(re-frame/subscribe [::subs/error-message])]
    [:div#error.modal
     {:class error-modal-class}
     [:div.modal-background
      {:on-click #(re-frame/dispatch [::events/set-error-message nil])}]
     [:div.modal-content
      [:div.card
       [:div.card-content error-message]]]
     [:button.modal-close.is-large
      {:aria-label "close"
       :on-click #(re-frame/dispatch [::events/set-error-message nil])}]]))
