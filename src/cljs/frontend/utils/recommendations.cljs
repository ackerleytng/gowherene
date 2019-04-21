(ns frontend.utils.recommendations)

(defn handle-recommendations
  [existing action url data]
  (.log js/console (clj->js {:existing existing
                             :action action
                             :data data}))
  (let [new (map #(assoc % :url url) data)]
    (case action
      :append (concat existing new)
      :plot new
      existing)))
