(ns gowherene.app.handler-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [gowherene.app.handler :refer [handle]]))

(deftest test-handle
  (testing "handle missing url"
    (let [response (handle nil)]
      (is (nil? (:data response)))
      (is (= "Null url!" (:error response)))))

  (testing "handle empty url"
    (let [response (handle "")]
      (is (nil? (:data response)))
      (is (= "Empty url!" (:error response)))))

  (testing "handle url empty but with spaces"
    (let [response (handle "    ")]
      (is (nil? (:data response)))
      (is (= "Empty url!" (:error response)))))

  (testing "handle missing addresses"
    (let [response (handle "http://httpstat.us/200")]
      (is (nil? (:data response)))
      (is (= "Couldn't find any addresses! :(" (:error response)))))

  (testing "handle error on retrieving"
    (let [response (handle "http://httpstat.us/404")]
      (is (nil? (:data response)))
      (is (= "Couldn't retrieve url! (404)" (:error response)))))

  (testing "handle domains that cannot be resolved"
    (let [response (handle "something")]
      (is (nil? (:data response)))
      (is (= "Couldn't connect to https://something" (:error response)))))

  (testing "handle https"
    (let [response (handle "https://www.google.com")]
      (is (nil? (:data response)))
      (is (= "Couldn't find any addresses! :(" (:error response))))))
