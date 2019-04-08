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
