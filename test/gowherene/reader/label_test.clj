(ns gowherene.reader.label-test
  (:require [clojure.test :refer :all]
            [hickory.zip :refer [hickory-zip]]
            [hickory.convert :refer [hiccup-to-hickory]]
            [clojure.zip :as zip]
            [gowherene.reader.label :refer :all]))

(deftest test-earlier-header
  (testing "earlier-header positive case"
    (is (= :h1 (->> (hickory-zip (hiccup-to-hickory [[:body [:h1 "header"] [:p "hello"]]]))
                    (iterate zip/next)
                    (take 8)
                    last  ;; This is the loc of "hello"
                    earlier-header
                    zip/node
                    :tag))))
  (testing "earlier-header when there isn't an earlier header"
    (is (nil? (->> (hickory-zip (hiccup-to-hickory [[:p "hello"]]))
                   (iterate zip/next)
                   (take 6)
                   last  ;; This is the loc of "hello"
                   earlier-header))))
  (testing "earlier-header when input is nil"
    (is (nil? (earlier-header nil)))))
