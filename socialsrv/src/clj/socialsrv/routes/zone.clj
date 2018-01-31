(ns socialsrv.routes.zone
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]
            

            [clj-jwt.core  :refer :all]
            [clj-jwt.key   :refer [private-key]]
            [clj-time.core :refer [now plus days]]

            [socialsrv.db.zone :as db]

            [clojure.string :as str]
  )
)

(defn getZones [token id]
  (let [
    email (:iss (-> token str->jwt :claims))
    result (into [] (db/get-zones id))
    ]
    result
  )
)


(defn createZone [token user name city diff]
  (let [
    usercode (:iss (-> token str->jwt :claims))
    ;;; TO-DO: add check authorization to add 

    result {:res "Success"}
    ]
    
    {:id (db/create-zone user name city diff)}
    ;; TO-DO Add check successfull
    ;;result
  )
)


(defn updateZone [token id name city diff]
  (let [
    usercode (:iss (-> token str->jwt :claims))
    ;;; TO-DO: add check authorization to add

    result {:res "Success"}
    ]
    
    (db/update-zone id name city diff)
    ;; TO-DO Add check successfull
    result
  )
)


(defn deleteZone [token id]
  (let [
    usercode (:iss (-> token str->jwt :claims)  ) 
    ;;; TO-DO: add check authorization to add 

    result {:res "Success"}
    ]
    
    (db/delete-zone id)
    ;; TO-DO Add check successfull
    result
  )
 
)

