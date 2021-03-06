(ns socialsrv.routes.dbservices
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]
            
            [clojure.string :as str]
            [clj-jwt.core  :refer :all]
            [clj-jwt.key   :refer [private-key]]
            [clj-time.core :refer [now plus days]]

            [socialsrv.db.core :as db]
))


(defn claim [user] 
  {:iss user
   :exp (plus (now) (days 1))
   :iat (now)
  }
)

(defn checkSocialUser [id from picture name]
  (let [
       user (first (db/find-social-user id from))
    ]
    (if (nil? user)
      (let []
        (db/create-user id from picture name)
      )
      (first user)
    )
  )
)


(defn checkUser [login password]
  (let [
       user (db/find-user-by-email login)
       locked (nth user 1)
       confirmed (nth user 2)
       id (nth user 3)
    ]
    (if (nil? user)
      {:result 0 :info 1}
      (if (= true locked)
        {:result 1 :info 0}
        (if (= false confirmed)
          {:result 4 :info 1}
          (let [
            user (first (db/find-user login password))
            ]
            (if (nil? user)
              (let [
                  cnt (db/increment-lock login)
                ]
                (if (= cnt 0)
                  {:result 1 :info cnt}
                  {:result 2 :info (- 3 cnt)}
                )
                
              )
              (let []
                (db/unlock-user login)
                {:result 3 :info id}
              )
            )
          )
        )
      )
    )
  )
)

(defn get-usercode-by-token [token]
  (let [
    usercode (:iss (-> token str->jwt :claims)  ) 
    result (first (into [] (db/find-user usercode)   )) 
    ]
    (nth result 0)
  ) 
)


(defn verifyToken [token]
  (try
     (-> token str->jwt :claims)
     (catch Exception e {}))


)

(defn checkToken [token]
  (let [key (nth (str/split token #" ") 1)]

    (if (= (:iss 
      (verifyToken key) ) nil)
      false
      true
    )

  )
)
