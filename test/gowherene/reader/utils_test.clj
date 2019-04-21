(ns gowherene.reader.utils-test
  (:require [clojure.test :refer :all]
            [hickory.zip :refer [hickory-zip]]
            [hickory.convert :refer [hiccup-to-hickory]]
            [clojure.zip :as zip]
            [gowherene.reader.utils :refer :all]))

(deftest test-word-count
  (testing "word-count"
    (is (= 0 (word-count "")))
    (is (= 0 (word-count "   ")))
    (is (= 0 (word-count "\u00a0")))
    (is (= 1 (word-count "testing")))
    (is (= 2 (word-count "testing     one")))
    (is (= 3 (word-count "one two three")))))

(deftest test-content
  (testing "content"
    (is (= "header hello"
           (->> (hickory-zip (hiccup-to-hickory [[:h1 "header"] [:p "hello"]]))
                content))))
  (testing "content strips extra spaces"
    (is (= "header hello"
           (->> (hickory-zip (hiccup-to-hickory [[:h1 "header"] "        " [:p "hello"]]))
                content))))
  (testing "content works recursively"
    (is (= "header hello world"
           (->> (hickory-zip (hiccup-to-hickory [[:h1 "header"] [:p [:b "hello"] [:i "world"]]]))
                content)))))

(deftest test-prune-before
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
        thomson-branch-loc (->> subtree
                                (iterate zip/next)
                                (take 6)
                                last)]
    (testing "prune-before"
      (is (= "Address: 187 Upper Thomson Road Opening Hours: Mon to Thurs: 4pm - 12am | Fri to Sun: 12pm - 12am Katong Branch Address: 465 Joo Chiat Road Opening Hours: Mon to Thurs: 4pm - 12am | Fri to Sun: 12pm - 12am"
             (content (hickory-zip (zip/root (prune-before address-loc)))))))
    (testing "prune-before at top"
      (is (= subtree (prune-before subtree))))))

(deftest test-prune-after
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
                    "Mon to Thurs: 4pm - 12am |&nbsp;Fri to Sun: 5am - 5pm"]]))
        second-br-loc (->> subtree
                           (iterate zip/next)
                           (take 13)
                           last)
        end (->> subtree
                 (iterate zip/next)
                 (take-while (complement zip/end?))
                 last)]
    (testing "prune-after"
      (is (= "Thomson Branch Address: 187 Upper Thomson Road"
             (content (hickory-zip (zip/root (prune-after second-br-loc)))))))
    (testing "prune-after-last"
      (is (= subtree
             (hickory-zip (zip/root (prune-after end))))))))
