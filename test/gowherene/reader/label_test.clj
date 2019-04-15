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

(deftest test-earlier-x-large-font-size
  (testing "earlier-x-large positive case"
    (is (= :span (->> (hickory-zip (hiccup-to-hickory
                                    [[:body [:p {:style "text-align: justify;"}
                                             [:span {:style "font-size: x-large;"} "126 WEN DAO SHI"]]
                                      [:p "hello"]]]))
                      (iterate zip/next)
                      (take 9)
                      last  ;; This is the loc of "hello"
                      earlier-x-large
                      zip/node
                      :tag))))
  (testing "earlier-x-large when there isn't an earlier header"
    (is (nil? (->> (hickory-zip (hiccup-to-hickory [[:p "hello"]]))
                   (iterate zip/next)
                   (take 6)
                   last  ;; This is the loc of "hello"
                   earlier-x-large))))
  (testing "earlier-x-large when input is nil"
    (is (nil? (earlier-x-large nil)))))

(deftest test-earlier-strong
  (testing "earlier-strong positive case"
    (is (= :strong (->> (hickory-zip (hiccup-to-hickory
                                      [[:body [:p {:style "text-align: justify;"}
                                               [:strong "Edge"]]
                                        [:p "hello"]]]))
                        (iterate zip/next)
                        (take 9)
                        last  ;; This is the loc of "hello"
                        earlier-strong
                        zip/node
                        :tag))))
  (testing "earlier-strong when there isn't an earlier header"
    (is (nil? (->> (hickory-zip (hiccup-to-hickory [[:p "hello"]]))
                   (iterate zip/next)
                   (take 6)
                   last  ;; This is the loc of "hello"
                   earlier-strong))))
  (testing "earlier-strong when input is nil"
    (is (nil? (earlier-strong nil)))))

(deftest test-label
  (testing "label finds header"
    (is (= "header" (->> (hickory-zip (hiccup-to-hickory [[:body [:h1 "header"] [:p "hello"]]]))
                         (iterate zip/next)
                         (take 8)
                         last  ;; This is the loc of "hello"
                         label)))
    (is (= "126 WEN DAO SHI" (->> (hickory-zip (hiccup-to-hickory
                                                [[:body [:p {:style "text-align: justify;"}
                                                         [:span {:style "font-size: x-large;"} "126 WEN DAO SHI"]]
                                                  [:p "hello"]]]))
                                  (iterate zip/next)
                                  (take 9)
                                  last  ;; This is the loc of "hello"
                                  label)))
    (is (= "Edge" (->> (hickory-zip (hiccup-to-hickory
                                     [[:body [:p {:style "text-align: justify;"}
                                              [:strong "Edge \n"]]
                                       [:p "hello"]]]))
                       (iterate zip/next)
                       (take 9)
                       last  ;; This is the loc of "hello"
                       label))))
  (testing "label when there isn't an earlier header"
    (is (nil? (->> (hickory-zip (hiccup-to-hickory [[:p "hello"]]))
                   (iterate zip/next)
                   (take 6)
                   last  ;; This is the loc of "hello"
                   label))))
  (testing "label when input is nil"
    (is (nil? (label nil)))))
