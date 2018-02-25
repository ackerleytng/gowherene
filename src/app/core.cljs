(ns app.core
  (:require [reagent.core :as r]
            [reagent.dom.server :as rs]
            [ajax.core :refer [GET]]
            [clojure.string :as str]
            [cemerick.url :as url]))

(enable-console-print!)

(def url-placeholder "Paste url here (from sethlui, smartlocal...)")

(def app-state (r/atom {:add-to-plot false
                        :loading false
                        :error-message nil
                        :about-active false
                        :url ""
                        :data []}))

;; ------------------------
;; Helper functions

(defn by-id [id]
  (.getElementById js/document id))

(defn window-url []
  (.. js/window -location -href))

(defn window-no-query []
  (str js/window.location.origin js/window.location.pathname))

(defn window-search []
  (str/replace js/window.location.search #"^\?" ""))

(defn history-push-state [s]
  (.pushState js/history {} "go where ne" s))

;; ------------------------
;; Url management

(defn url->query [url]
  (str "q=" (url/url-encode url)))

(defn urls->search [urls]
  (->> urls
       (map url->query)
       (str/join "&")))

(defn query->url [url]
  (url/url-decode (str/replace url #"^q=" "")))

(defn search->urls [search]
  (->> (str/split search #"&")
       (map query->url)))

(def queried-urls (atom []))

(add-watch queried-urls :queried-urls-watcher
           (fn [_ _ _ urls]
             (history-push-state (str (window-no-query) "?" (urls->search urls)))))

;; ------------------------
;; Scrolling stuff
;;   From https://gist.github.com/jasich/21ab25db923e85e1252bed13cf65f0d8

(defn cur-doc-top []
  (+ (.. js/document -body -scrollTop) (.. js/document -documentElement -scrollTop)))

(defn element-top [elem top]
  (if (.-offsetParent elem)
    (let [client-top (or (.-clientTop elem) 0)
          offset-top (.-offsetTop elem)]
      (+ top client-top offset-top (element-top (.-offsetParent elem) top)))
    top))

(defn scroll-to-id
  [elem-id]
  (let [speed 400
        moving-frequency 10
        elem (by-id elem-id)
        hop-count (/ speed moving-frequency)
        doc-top (cur-doc-top)
        gap (/ (- (element-top elem 0) doc-top) hop-count)]
    (doseq [i (range 1 (inc hop-count))]
      (let [hop-top-pos (* gap i)
            move-to (+ hop-top-pos doc-top)
            timeout (* moving-frequency i)]
        (.setTimeout js/window (fn []
                                 (.scrollTo js/window 0 move-to))
                     timeout)))))

;; ------------------------
;; Google Maps stuff

(defn gmap-latlng
  [{:keys [lat lng]}]
  (js/google.maps.LatLng. lat lng))

(defn gmap-latlng-bounds [latlngs]
  (when (seq latlngs)
    (reduce #(.extend %1 %2)
            (js/google.maps.LatLngBounds.)
            latlngs)))

(def singapore-bounds
  (gmap-latlng-bounds (map gmap-latlng [{:lat 1.365035 :lng 103.644231}
                                        {:lat 1.355701 :lng 104.036779}
                                        {:lat 1.471183 :lng 103.810720}
                                        {:lat 1.236344 :lng 103.832829}])))

(defn compute-and-fit-bounds
  [map-object data]
  (let [new-bounds (->> data
                        (map :latlng)
                        (map gmap-latlng)
                        gmap-latlng-bounds)]
    (.fitBounds map-object (or new-bounds singapore-bounds) 0)))

(defn infowindow-content
  [header content]
  [:div
   [:b header] [:br]
   content])

(defn gmap-info-window
  [header content]
  (js/google.maps.InfoWindow.
   (clj->js {:content (rs/render-to-static-markup [infowindow-content header content])})))

(defn gmap-marker
  [map-object latlng label title content]
  (let [marker (js/google.maps.Marker.
                (clj->js {:position latlng
                          :map map-object
                          :title title
                          :label label}))]
    (.addListener marker
                  "click"
                  (fn [] (.open (gmap-info-window title content)
                                map-object
                                marker)))
    marker))

(defn gmap-marker-remove
  [marker]
  (.setMap marker nil))

(defn do-build-marker
  [map-object {:keys [place address latlng]}]
  (when place
    (let [label (get (re-find #"^\s*(\d+)" place) 1)]
      (gmap-marker map-object (gmap-latlng latlng) label place address))))

(defn do-plot!
  [map-object markers-atom data]
  ;; Need to use mapv because map is lazy
  (let [markers (mapv (partial do-build-marker map-object) data)]
    ;; Need to use doseq because map is lazy
    (doseq [m @markers-atom]
      (gmap-marker-remove m))
    ;; Not sure why the use of markers here does not force execution above
    (reset! markers-atom markers)))

(defn recommendation-map []
  (let [map-atom (r/atom nil)
        markers (r/atom [])]
    (r/create-class
     {:display-name "recommendation-map"
      :component-did-mount
      (fn [this]
        (let [map-canvas (r/dom-node this)
              map-options (clj->js {:disableDefaultUI true
                                    :zoomControl true})
              map-object (js/google.maps.Map. map-canvas map-options)
              data (-> this r/props :data)]
          (reset! map-atom map-object)
          (compute-and-fit-bounds map-object data)))
      :component-did-update
      (fn [this]
        (let [data (-> this r/props :data)]
          (do-plot! @map-atom markers data)
          (compute-and-fit-bounds @map-atom data)
          (scroll-to-id "recommendation-map")
          ))
      :reagent-render
      (fn []
        [:div#recommendation-map {:style {:height "100vh"}}])})))

(defn handle-data
  [response]
  (.log js/console (clj->js [:response response]))
  (swap! app-state assoc :loading false)
  (if (zero? (count response))
    (swap! app-state assoc :error-message "Couldn't find any addresses! :(")
    (if (@app-state :add-to-plot)
      (swap! app-state update :data into response)
      (swap! app-state assoc :data response))))

(defn parse-url-error [response]
  (.log js/console (clj->js [:error-response response]))
  (swap! app-state assoc :loading false)
  (swap! app-state assoc :error-message "Couldn't read your URL :("))

(defn parse-url
  [{:keys [url add-to-plot]}]
  (.log js/console (clj->js [:url url
                             :add-to-plot add-to-plot]))
  ;; Keep track of queried urls
  (if add-to-plot
    (swap! queried-urls conj url)
    (reset! queried-urls [url]))
  (swap! app-state assoc :loading true)
  (GET "/parse" {:params {:url url}
                 :handler handle-data
                 :error-handler parse-url-error
                 :response-format :json
                 :keywords? true}))

(defn setup-queried-urls! []
  (let [search (window-search)]
    (when-not (str/blank? search)
      (let [urls (search->urls search)
            num-urls (count urls)]
        (.log js/console (clj->js [:setup-queried-urls!-urls urls num-urls]))
        (if (pos? num-urls)
          (do (swap! app-state assoc :add-to-plot (< 1 num-urls))
              (mapv #(parse-url {:url % :add-to-plot true}) urls)
              (swap! app-state assoc :url (last urls))))))))

(setup-queried-urls!)

;; ------------------------
;; Controls components

(defn url-input-handle-key-press [e]
  (when (= "Enter" (.-key e))
    (parse-url @app-state)))

(defn url-input-handle-change [e]
  (swap! app-state assoc :url (.. e -target -value)))

(defn url-input []
  [:input#url.input
   {:type "text"
    :value (@app-state :url)
    :placeholder url-placeholder
    :on-key-press url-input-handle-key-press
    :on-change url-input-handle-change}])

(defn plot-button []
  [:a#plot.button.is-info
   {:class (when (@app-state :loading) "is-loading")
    :on-click #(parse-url @app-state)}
   [:span.icon
    [:i.fas.fa-chevron-right]]])

(defn clear-button []
  [:a#clear.button
   [:span.icon.is-small.has-text-grey-light
    {:on-click #(swap! app-state assoc :url "")}
    [:i.fas.fa-times]]])

(defn controls []
  [:div#controls
   [:div.field.has-addons
    [:div.control.is-expanded
     [url-input]]
    (when (not (str/blank? (@app-state :url)))
      [:div.control
       [clear-button]])
    [:div.control
     [plot-button]]]
   [:div.field.is-grouped.is-grouped-centered
    [:div.control
     [:label.checkbox
      [:input {:type "checkbox"
               :checked (@app-state :add-to-plot)
               :on-change #(swap! app-state update :add-to-plot not)}]
      " Add to current plot"]]]])

;; ------------------------
;; Error modal

(defn clear-error []
  (swap! app-state assoc :error-message nil))

(defn error-modal []
  [:div#error.modal
   {:class (when (@app-state :error-message) "is-active")}
   [:div.modal-background
    {:on-click clear-error}]
   [:div.modal-content
    [:div.card
     [:div.card-content (@app-state :error-message)]]]
   [:button.modal-close.is-large {:aria-label "close"
                                  :on-click clear-error}]])

;; ------------------------
;; About modal

(defn gowherene []
  [:span {:style {:word-spacing "-0.13em"}} "go where ne"])

(defn clear-about []
  (swap! app-state assoc :about-active false))

(defn about-modal []
  [:div#about.modal
   {:class (when (@app-state :about-active) "is-active")}
   [:div.modal-background
    {:on-click clear-about}]
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
   [:button.modal-close.is-large {:aria-label "close"
                                  :on-click clear-about}]])

;; ------------------------
;; Header

(defn header []
  [:div.level.is-mobile
   {:on-click #(swap! app-state assoc :about-active true)}
   [:div.level-left
    [:div.level-item
     [:figure.image.is-64x64
      [:img {:src "img/gowherene.svg"}]]]
    [:div.level-item
     [:h1.title [gowherene]]]
    [:div.level-item
     [:h2.subtitle.is-hidden-mobile {:style {:margin-left "1em"}}
      "map out your recommendations!"]]]])

;; ------------------------
;; Footer

(defn footer []
  [:footer.footer
   [:div.level
    [:div.level-left
     [:div.level-item
      [:a {:on-click #(swap! app-state assoc :about-active true)}
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

;; ------------------------
;; Main app

(defn app []
  [:div
   [error-modal]
   [about-modal]
   [:section.section
    [:div.container.is-fluid
     [header]
     [controls]]]
   [:div.container.is-widescreen
    ;; props passed to a reagent component must be a map
    [recommendation-map {:data (@app-state :data)}]]
   [footer]])

(defn ^:export run []
  (r/render [app]
            (by-id "app")))

(run)
