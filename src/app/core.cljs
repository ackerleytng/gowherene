(ns app.core
  (:require [reagent.core :as r]
            [ajax.core :refer [GET]]
            [clojure.string :as str]))

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

(defn plot-url [url add-to-current-plot]
  (.log js/console (clj->js [url add-to-current-plot])))

(defn controls []
  (let [url-placeholder "Enter your url here (from sethlui, smartlocal...)"
        url (r/atom url-placeholder)
        add-to-current-plot (r/atom false)]
    (fn []
      [:div#controls
       [:div.field.has-addons
        [:div.control.is-expanded
         [:input#url.input {:type "text"
                            :value @url
                            :on-key-press #(when (= "Enter" (.-key %))
                                             (plot-url @url @add-to-current-plot))
                            :on-focus #(when (= @url url-placeholder) (reset! url ""))
                            :on-blur #(when (str/blank? @url) (reset! url url-placeholder))
                            :on-change #(reset! url (.. % -target -value))}]]
        [:div.control
         [:a#plot.button.is-info {:on-click #(plot-url @url @add-to-current-plot)} "Plot!"]]]
       [:div.field.is-grouped.is-grouped-centered
        [:div.control
         [:label.checkbox
          [:input {:type "checkbox"
                   :checked @add-to-current-plot
                   :on-change #(swap! add-to-current-plot not)}]
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
