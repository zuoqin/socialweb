(ns socialsrv.routes.user
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]
            

            [clj-jwt.core  :refer :all]
            [clj-jwt.key   :refer [private-key]]
            [clj-time.core :refer [now plus days]]

            [socialsrv.db.user :as db]

            [clojure.string :as str]
))

(defn getUsers [token]
  (let [
    usercode (:iss (-> token str->jwt :claims)  ) 
    result (into [] (db/get-users usercode)   )         
    ]
    (println (str "usercode=" usercode) )
    result
  )
)


(defn createUser [token name email password role locked pic]
  (let [
    usercode (:iss (-> token str->jwt :claims)  ) 
    ;;; TO-DO: add check authorization to add 

    result {:res "Success"}
    ]
    
    {:id (db/create-user name email password role locked pic)}
    ;; TO-DO Add check successfull
;    result
  )
)


(defn updateUser [token id name email password role locked picture]
  (let [
    usercode (:iss (-> token str->jwt :claims)  ) 
    ;;; TO-DO: add check authorization to add 

    result {:res "Success"}
    ]
    
    (db/update-user id name email password role locked picture)
    ;; TO-DO Add check successfull
    result
  )
)


(defn deleteUser [token id]
  (let [
    usercode (:iss (-> token str->jwt :claims)  ) 
    ;;; TO-DO: add check authorization to add 

    result {:res "Success"}
    ]
    
    (db/delete-user id)
    ;; TO-DO Add check successfull
    result
  )
)
