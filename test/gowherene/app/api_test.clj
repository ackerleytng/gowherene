(ns gowherene.app.api-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [gowherene.app.api :refer [app-routes]]))

(deftest test-app
  (testing "not-found route"
    (let [response (app-routes (mock/request :get "/invalid"))]
      (is (= (:status response) 404)))))
