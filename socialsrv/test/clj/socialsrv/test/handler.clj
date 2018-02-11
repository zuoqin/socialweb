(ns socialsrv.test.handler
  (:require [clojure.test :refer :all]
            [ring.mock.request :refer :all]
            [socialsrv.handler :refer :all]
            [socialsrv.routes.dbservices :as dbsrvc]
            [user :as u]
            [cheshire.core :refer :all]
  )
)

(deftest test-app
  (testing "main route"
    (let [response ((app) (request :get "/"))]
      (is (= 200 (:status response)))))

  (testing "not-found route"
    (let [response ((app) (request :get "/invalid"))]
      (is (= 404 (:status response))))
  )

  (testing "login"
    (let [
    	tr1 (u/start)
    	response ((app) (-> 
    		(request :post "http://localhost:3000/token") 
    		(body "grant_type=password&username=zuoqin%40mail.ru&password=Qwerty123")
    		(header :accept "application/json")
    	    (content-type "application/x-www-form-urlencoded")
    		))
    	body (cheshire.core/parse-stream (-> response :body clojure.java.io/reader) keyword)
    	]
      (is (= (:id body) 17592186045418))
    )
  )

  (testing "getting users"
    (let [
    	tr1 (u/start)
    	response ((app) (-> 
    		(request :post "http://localhost:3000/token") 
    		(body "grant_type=password&username=zuoqin%40mail.ru&password=Qwerty123")
    		(header :accept "application/json")
    	    (content-type "application/x-www-form-urlencoded")
    		))
    	body (cheshire.core/parse-stream (-> response :body clojure.java.io/reader) keyword)



    	requsers ((app) (-> 
    		(request :get "/api/users?page=1")
    		(header :authorization (str "Bearer " (:access_token body)))
    		(header :accept "application/json")
    		))

    	users (cheshire.core/parse-stream (-> requsers :body clojure.java.io/reader) keyword)

    	firstuser (first users)

    	]
      (is (= firstuser 
		  {
		    :email "user3albert@gmail.com",
		    :role "user",
		    :locked false,
		    :id 17592186046298,
		    :password "kjhkjhkjhkjhkjhjk",
		    :source "site",
		    :confirmed false,
		    :name "Albert Einstein"
		  }
      	)
      )
    )
  )


  (testing "get user"
    (let [
    	tr1 (u/start)
    	response ((app) (-> 
    		(request :post "http://localhost:3000/token") 
    		(body "grant_type=password&username=zuoqin%40mail.ru&password=Qwerty123")
    		(header :accept "application/json")
    	    (content-type "application/x-www-form-urlencoded")
    		))
    	body (cheshire.core/parse-stream (-> response :body clojure.java.io/reader) keyword)

    	response ((app) (header (request :get "/api/user?id=17592186046282")
    		:authorization (str "Bearer " (:access_token body))
    		))

    	body (cheshire.core/parse-stream (-> response :body clojure.java.io/reader) keyword)
    	]
      (is (= (:id body) 17592186046282))
    )
  )


  (testing "get user's zones"
    (let [
    	tr1 (u/start)
    	response ((app) (-> 
    		(request :post "http://localhost:3000/token") 
    		(body "grant_type=password&username=zuoqin%40mail.ru&password=Qwerty123")
    		(header :accept "application/json")
    	    (content-type "application/x-www-form-urlencoded")
    		))
    	body (cheshire.core/parse-stream (-> response :body clojure.java.io/reader) keyword)

    	reqzones ((app) (-> 
    		(request :get (str "/api/zone?id=" (:id body)))
    		(header :authorization (str "Bearer " (:access_token body)))
    		(header :accept "application/json")
    		))

    	zones (cheshire.core/parse-stream (-> reqzones :body clojure.java.io/reader) keyword)
    	]
      (is (= (and (>= (count zones) 9) 
      	(= (first zones)   {
    	:id 17592186046338
    	:name "India"
    	:city "New Delhi"
    	:diff 5.5
  		}
  	  ))  true))
    )
  )


  (testing "update user's zones"
    (let [
    	tr1 (u/start)
    	response ((app) (-> 
    		(request :post "http://localhost:3000/token") 
    		(body "grant_type=password&username=zuoqin%40mail.ru&password=Qwerty123")
    		(header :accept "application/json")
    	    (content-type "application/x-www-form-urlencoded")
    		))
    	body (cheshire.core/parse-stream (-> response :body clojure.java.io/reader) keyword)

    	reqzones ((app) (-> 
    		(request :get (str "/api/zone?id=" (:id body)))
    		(header :authorization (str "Bearer " (:access_token body)))
    		(header :accept "application/json")
    		))

    	zones (cheshire.core/parse-stream (-> reqzones :body clojure.java.io/reader) keyword)


    	updzones ((app) (-> 
    		(request :put (str "/api/zone?id=" (:id body)))
    		(header :authorization (str "Bearer " (:access_token body)))
    		(header :accept "application/json")
    		(content-type "application/json")
    		(json-body {:id (:id (first zones)) :name (:name (first zones)) :city (:city (first zones)) :diff (:diff (first zones))})
    		))

    	res (cheshire.core/parse-stream (-> updzones :body clojure.java.io/reader) keyword)
    	]
    	(is (= (:res res) "Success"))

    )
  )


  (testing "insert user's zone"
    (let [
    	tr1 (u/start)
    	response ((app) (-> 
    		(request :post "http://localhost:3000/token") 
    		(body "grant_type=password&username=zuoqin%40mail.ru&password=Qwerty123")
    		(header :accept "application/json")
    	    (content-type "application/x-www-form-urlencoded")
    		))
    	body (cheshire.core/parse-stream (-> response :body clojure.java.io/reader) keyword)

    	userid (:id body)

    	inszones ((app) (-> 
    		(request :post (str "/api/zone" ))
    		(header :authorization (str "Bearer " (:access_token body)))
    		(header :accept "application/json")
    		(content-type "application/json")
    		(json-body {:user userid :name "My New Zone" :city "My New City" :diff -8})
    		))

    	res (cheshire.core/parse-stream (-> inszones :body clojure.java.io/reader) keyword)
    	zoneid (:id res)

    	reqzones ((app) (-> 
    		(request :get (str "/api/zone?id=" userid))
    		(header :authorization (str "Bearer " (:access_token body)))
    		(header :accept "application/json")
    		))

    	zones (cheshire.core/parse-stream (-> reqzones :body clojure.java.io/reader) keyword)
    	cnt (count (filter (fn[x] (if (= (:id x) zoneid) true false)) zones) )

    	delzone ((app) (-> 
    		(request :delete (str "/api/zone?id=" zoneid))
    		(header :authorization (str "Bearer " (:access_token body)))
    		(header :accept "application/json")
    		))

    	res (cheshire.core/parse-stream (-> delzone :body clojure.java.io/reader) keyword)
    	]
    	;(is (= true (and (> cnt 0))))
    	(is (= true (and (= (:res res) "Success") (> cnt 0))))
    )
  )


  (testing "insert existing user"
    (let [
    	tr1 (u/start)
    	response ((app) (-> 
    		(request :post "http://localhost:3000/token") 
    		(body "grant_type=password&username=zuoqin%40mail.ru&password=Qwerty123")
    		(header :accept "application/json")
    	    (content-type "application/x-www-form-urlencoded")
    		))
    	body (cheshire.core/parse-stream (-> response :body clojure.java.io/reader) keyword)


    	requsers ((app) (-> 
    		(request :get "/api/users?page=1")
    		(header :authorization (str "Bearer " (:access_token body)))
    		(header :accept "application/json")
    		))

    	users (cheshire.core/parse-stream (-> requsers :body clojure.java.io/reader) keyword)

    	firstuser (first users)

    	insuser ((app) (-> 
    		(request :post "/api/user")
    		(header :authorization (str "Bearer " (:access_token body)))
    		(header :accept "application/json")
    		(content-type "application/json")
    		(json-body {:role (:role (first users)) :email (:email (first users))
    			:password "12345678" :name (:name (first users)) :confirmed true :source "site" :locked false :pic ""})
    		))

    	res (cheshire.core/parse-stream (-> insuser :body clojure.java.io/reader) keyword)

    	]

      (is (= (:error res) (str "User " (:email (first users)) " already exists")))
    )
  )



  (testing "invite existing user"
    (let [
    	tr1 (u/start)
    	response ((app) (-> 
    		(request :post "http://localhost:3000/token") 
    		(body "grant_type=password&username=zuoqin%40mail.ru&password=Qwerty123")
    		(header :accept "application/json")
    	    (content-type "application/x-www-form-urlencoded")
    		))
    	body (cheshire.core/parse-stream (-> response :body clojure.java.io/reader) keyword)


    	requsers ((app) (-> 
    		(request :get "/api/users?page=1")
    		(header :authorization (str "Bearer " (:access_token body)))
    		(header :accept "application/json")
    		))

    	users (cheshire.core/parse-stream (-> requsers :body clojure.java.io/reader) keyword)

    	firstuser (first users)

    	invuser ((app) (-> 
    		(request :post "/api/invite")
    		(header :authorization (str "Bearer " (:access_token body)))
    		(header :accept "application/json")
    		(content-type "application/json")
    		(json-body {:email (:email (first users))})
    		))

    	res (cheshire.core/parse-stream (-> invuser :body clojure.java.io/reader) keyword)
    	]

      (is (= (:result res) (str (:email (first users)) " already exists")))
    )
  )


  (testing "invite non existing user"
    (let [
    	tr1 (u/start)
    	response ((app) (-> 
    		(request :post "http://localhost:3000/token") 
    		(body "grant_type=password&username=zuoqin%40mail.ru&password=Qwerty123")
    		(header :accept "application/json")
    	    (content-type "application/x-www-form-urlencoded")
    		))
    	body (cheshire.core/parse-stream (-> response :body clojure.java.io/reader) keyword)


    	requsers ((app) (-> 
    		(request :get "/api/users?page=1")
    		(header :authorization (str "Bearer " (:access_token body)))
    		(header :accept "application/json")
    		))

    	users (cheshire.core/parse-stream (-> requsers :body clojure.java.io/reader) keyword)

    	firstuser (first users)

    	invuser ((app) (-> 
    		(request :post "/api/invite")
    		(header :authorization (str "Bearer " (:access_token body)))
    		(header :accept "application/json")
    		(content-type "application/json")
    		(json-body {:email (str "ab" (:email (first users)))})
    		))

    	res (cheshire.core/parse-stream (-> invuser :body clojure.java.io/reader) keyword)
    	]

      (is (= (:result res) (str "messages sent")))
    )
  )
)