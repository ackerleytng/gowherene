(ns gowherene.reader.geocodables-test
  (:require [clojure.test :refer :all]
            [clojure.zip :as zip]
            [hickory.zip :refer [hickory-zip]]
            [hickory.convert :refer [hiccup-to-hickory]]
            [gowherene.reader.geocodables :refer :all]
            [gowherene.reader.utils :refer :all]))

(deftest test-nearest-boundary-sibling
  (testing "nearest-boundary-sibling"
    (let [subtree (hickory-zip
                   (hiccup-to-hickory
                    [[:p
                      "Thomson Branch"
                      [:br]
                      [:strong {:style "line-height: 1.3em;"}
                       [:strong {:style "color: #d47978;"} "Address:"]
                       "&nbsp;"]
                      "187 Upper Thomson Road"
                      [:br]]]))
          thomson-branch-loc (->> subtree
                                  (iterate zip/next)
                                  (take 6)
                                  last)
          first-br-loc (zip/next thomson-branch-loc)
          second-br-loc (zip/rightmost thomson-branch-loc)
          address-loc (->> subtree
                           (iterate zip/next)
                           (take 10)
                           last)]
      (is (= first-br-loc (nearest-boundary-sibling thomson-branch-loc)))
      (is (= second-br-loc (nearest-boundary-sibling first-br-loc)))
      (is (nil? (nearest-boundary-sibling second-br-loc)))
      (is (nil? (nearest-boundary-sibling address-loc))))))

(deftest test-prune-out-loc-to-boundary
  (testing "prune-out-loc-to-boundary"
    (let [subtree (hickory-zip
                   (hiccup-to-hickory
                    [[:p
                      "Thomson Branch"
                      [:br]
                      [:strong {:style "line-height: 1.3em;"}
                       [:strong {:style "color: #d47978;"} "Address:"]
                       "&nbsp;"]
                      "187 Upper Thomson Road"
                      [:br]
                      [:strong {:style "color: #d47978;"} "Opening Hours:"]
                      [:strong "&nbsp;"]
                      "Mon to Thurs: 4pm - 12am |&nbsp;Fri to Sun: 12pm - 12am"
                      [:br]
                      [:br]
                      "Katong Branch"
                      [:br]
                      [:span {:style "color: #d47978;"}
                       [:strong "Address:"]]
                      " 465 Joo Chiat Road"
                      [:br]
                      [:strong {:style "color: #d47978;"} "Opening Hours:"]
                      [:strong "&nbsp;"]
                      "Mon to Thurs: 4pm - 12am |&nbsp;Fri to Sun: 12pm - 12am"]]))
          address-loc (->> subtree
                           (iterate zip/next)
                           (take 10)
                           last)
          ]
      (is (= "Address: 187 Upper Thomson Road Opening Hours: Mon to Thurs: 4pm - 12am | Fri to Sun: 12pm - 12am Katong Branch Address: 465 Joo Chiat Road Opening Hours: Mon to Thurs: 4pm - 12am | Fri to Sun: 12pm - 12am"
             (content (hickory-zip (zip/root (prune-out-loc-to-boundary address-loc)))))
          "Should have no change, since :br boundary is not a right of address-loc")
      (is (= "Address: 187 Upper Thomson Road"
             (content (hickory-zip (zip/root (->> address-loc
                                                  prune-out-loc-to-boundary
                                                  zip/up
                                                  prune-out-loc-to-boundary
                                                  zip/up
                                                  prune-out-loc-to-boundary
                                                  zip/up)))))))))
