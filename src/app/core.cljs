(ns app.core
  (:require [reagent.core :as r]
            [ajax.core :refer [GET]]))

(def singapore-bounds
  (reduce #(.extend %1 (apply (fn [x y] (js/google.maps.LatLng. x y)) %2))
          (js/google.maps.LatLngBounds.)
          [[1.365035 103.644231]
           [1.355701 104.036779]
           [1.471183 103.810720]
           [1.236344 103.832829]]))

(defn recommendation-map-render []
  [:div {:style {:height "100vh"}}])

(defn recommendation-map-did-mount [this]
  (let [map-canvas (r/dom-node this)
        map-options (clj->js {:disableDefaultUI true
                              :zoomControl true})]
    (-> (js/google.maps.Map. map-canvas map-options)
        (.fitBounds singapore-bounds 0))))

(defn recommendation-map []
  (r/create-class {:reagent-render recommendation-map-render
                   :component-did-mount recommendation-map-did-mount}))

(defn plot-url [url]
  (println url))

(defn controls []
  (let [url (r/atom "Enter your url here (from sethlui, smartlocal...)")]
    (fn []
      [:div#controls
       [:div.field.has-addons
        [:div.control.is-expanded
         [:input#url.input {:type "text"
                            :value @url
                            :on-key-press #(when (= "Enter" (.-key %))
                                             (plot-url @url))
                            :on-change #(reset! url (.. % -target -value))}]]
        [:div.control
         [:a#plot.button.is-info {:on-click #(plot-url @url)} "Plot!"]]]
       [:div.field.is-grouped.is-grouped-centered
        [:div.control
         [:label.checkbox
          [:input {:type "checkbox"}]
          " Add to current plot"]]]])))

(defn app []
  [:div
   [:section.section
    [:div.container.is-fluid
     [:h1.title "Mappout your recommendations!"]
     [controls]]]
   [:div.container.is-widescreen
    [recommendation-map]]])

(defn ^:export run []
  (r/render [app]
            (.getElementById js/document "app"))
  (enable-console-print!))

(run)
