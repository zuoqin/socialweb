(ns socialweb.core-test
  (:require-macros [cljs.test :refer (is deftest testing)])
  (:require 
     [cljs.test]
     [cljs-time.format :as tf]
     [cljs-time.coerce :as te]
     [cljs-time.core :as tc]
  )
)

(deftest example-passing-test
  (is (= 1 1)))


(deftest ^:async timeout
  (let [now #(.getTime (js/Date.))
        t (now)]
    (js/setTimeout
	  (fn []
	    (is (>= (now) (+ t 2000)))
		(done))
      2000)))




;(deftest test-async-awesome
;  (testing "the API is awesome"
;    (let [url "http://foo.com/api.edn"
;          res (http/get url)]
;      (async done
;        (go
;          (is (= (<! res) :awesome))
;          (done))))))
