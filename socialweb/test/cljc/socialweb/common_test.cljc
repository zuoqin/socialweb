(ns socialweb.common-test
  #? (:cljs

        (:require-macros [cljs.test :refer (is deftest testing)])
  		(:require [org.httpkit.client :as http])
  	 )
  (:require [socialweb.common :as sut]
            ;[org.httpkit.client :as http]
            ;[ajax.core :refer [GET POST PUT]]
            #?(:clj [clojure.test :refer :all]
               :cljs [cljs.test])))



;(deftest timeout
;  (let [now #(.getTime (js/Date.))
;        t (now)]
;    (js/setTimeout
;	  (fn [] (is (>= (now) (+ t 2000))))
;     2000)))



;(deftest test-login
;	(let [
;	options {:form-params {:grant_type "password" :username "zuoqin@mail.ru" :password "Qwerty123"}}
;	{:keys [status headers body error] :as resp} @(http/post "http://devstat.aytm.com:3000/token" options)]
;	  (if error
;	    (println "Failed, exception: " error)
;	    (is (> (count (get body "access_token")) 0))))
;)


(deftest example-passing-test-cljc
  (is (= 1 1)))
