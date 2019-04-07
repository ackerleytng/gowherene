(ns gowherene.reader.utils-test
  (:require [clojure.test :refer :all]
            [gowherene.reader.utils :refer :all]))

(deftest test-word-count
  (testing "word-count"
    (is (= 0 (word-count "")))
    (is (= 0 (word-count "   ")))
    (is (= 0 (word-count "\u00a0")))
    (is (= 1 (word-count "testing")))
    (is (= 2 (word-count "testing     one")))
    (is (= 3 (word-count "one two three")))))
