(ns playout.reader.core-test
  (:require [clojure.test :refer :all]
            [clojure.pprint :refer [pprint]]
            [hickory.core :refer [as-hickory parse]]
            [playout.reader.core :as r]))

(defn get-index [header]
  (if-let [num (re-find #"(\d+)\." header)]
    (Integer/parseInt (get num 1))
    nil))

(deftest test-reader
  (testing "cheap-food-orchard"
    (let [hickory (->> (slurp "test/playout/reader/fixtures/cheap-food-orchard")
                       parse
                       as-hickory)
          response (r/process hickory)]

      ;; There are 23 items parsed
      (is (= (count response) 23))
      
      ;; All the latlngs after processing were geocoded
      (is (= (count (filter (comp nil? :latlng) response)) 0))

      ;; All headers were parsed, beginning from 1. to 22.
      ;;   (The 23rd one is a "Bonus")
      ;;   (Ensures no missing headers)
      (is (= (sort (->> response
                        (map :place)
                        (map get-index) 
                        (filter identity))) 
             (range 1 23)))
      
      ;; All addresses contain postal codes (true for this site)
      (is (= 23 (->> response
                     (map :address)
                     (map #(re-find r/re-postal-code %))
                     count)))))
  
  (testing "no-gst-restaurants"
    (let [hickory (->> (slurp "test/playout/reader/fixtures/no-gst-restaurants")
                       parse
                       as-hickory)
          response (r/process hickory)
          
          tags-removed (r/remove-tags r/uninteresting-tags hickory)
          postal-code-locs (r/get-postal-code-locs tags-removed)
          
          data (r/hickory->data hickory)
          data-cleaned (r/cleanup-addresses data)]
      
      ;; There shouldn't be any blank addresses
      (is (= (count (filter (comp clojure.string/blank? :address) data-cleaned)) 0))
      
      ;; Check that remove-tags has some effect
      ;;   More tags appear here than in r/uninteresting-tags because
      ;;     child tags of r/uninteresting-tags are also removed
      (is (= #{:hr :script :iframe :ins :footer :header :title :style :head :link
               :noscript :base :nav :img}
             (clojure.set/difference (r/get-all-tags hickory)
                                     (r/get-all-tags tags-removed))))
      
      (is (= (count response) 18))

      (is (= (count (filter (comp nil? :latlng) response)) 0))
      
      (is (= (sort (->> response
                        (map :place)
                        (map get-index) 
                        (filter identity))) 
             (range 1 19)))))
  
  (testing "no-gst-cafes"
    (let [hickory (->> (slurp "test/playout/reader/fixtures/no-gst-cafes")
                       parse
                       as-hickory)
          response (r/process hickory)
          
          tags-removed (r/remove-tags r/uninteresting-tags hickory)
          postal-code-locs (r/get-postal-code-locs tags-removed)
          
          data (r/hickory->data hickory)
          data-cleaned (r/cleanup-addresses data)]
      
      ;; There shouldn't be any blank addresses
      (is (= (count (filter (comp clojure.string/blank? :address) data-cleaned)) 0))
      
      ;; Check that remove-tags has some effect
      ;;   More tags appear here than in r/uninteresting-tags because
      ;;     child tags of r/uninteresting-tags are also removed
      (is (= #{:hr :script :iframe :ins :footer :header :title :style :head :link
               :noscript :base :nav :img}
             (clojure.set/difference (r/get-all-tags hickory)
                                     (r/get-all-tags tags-removed))))
      
      (is (= (count response) 15))

      (is (= (count (filter (comp nil? :latlng) response)) 0))
      
      (is (= (sort (->> response
                        (map :place)
                        (map get-index) 
                        (filter identity))) 
             (range 1 16))))))
