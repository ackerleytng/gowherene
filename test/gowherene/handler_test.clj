(ns gowherene.handler-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [gowherene.handler :refer :all]))

(deftest test-app
  (testing "main route"
    (let [response (app (mock/request :get "/"))]
      (is (= (:status response) 200))
      (is (= (:body response) "Hello World"))))

  (testing "not-found route"
    (let [response (app (mock/request :get "/invalid"))]
      (is (= (:status response) 404)))))

(deftest test-handle
  (testing "handle missing url"
    (let [response (handle nil)]
      (is (nil? (:data response)))
      (is (= "Missing url!" (:error response)))))

  (testing "handle missing addresses"
    (let [response (handle "http://httpstat.us/200")]
      (is (nil? (:data response)))
      (is (= "Couldn't find any addresses! :(" (:error response)))))

  (testing "handle error on retrieving"
    (let [response (handle "http://httpstat.us/404")]
      (is (nil? (:data response)))
      (is (= "Couldn't retrieve url! (404)" (:error response))))))
