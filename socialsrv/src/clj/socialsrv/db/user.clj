(ns socialsrv.db.user
  (:require [datomic.api :as d]
            [mount.core :refer [defstate]]
            [socialsrv.db.core :refer [conn] ]
            [socialsrv.config :refer [env]]))


(defn find-user [email]
  (let [users (d/q '[:find ?email ?r
                      :in $ ?email
                      :where
                      [?u :user/email ?email]
                      [?u :user/role ?r]
                     ]
                     (d/db conn) email)
    ]
    (if (not= users #{} ) (first users)  ["" ""]) 
  )
)

(defn get-users [email]
  (let [
        user (find-user email)

        users (if ( = (nth user 1) "user" ) 
                (d/q '[:find ?email ?role ?locked ?u ?p ?pwd ?s ?c ?n
                               :in $ ?email
                               :where
                               [?u :user/email ?email]
                               [?u :user/role ?role]
                               [(get-else $ ?u :user/locked false) ?locked]
                               [(get-else $ ?u :user/picture "") ?p]
                               [(get-else $ ?u :user/password "") ?pwd]
                               [(get-else $ ?u :user/confirmed false) ?c]
                               [(get-else $ ?u :user/source false) ?s]
                               [(get-else $ ?u :user/name false) ?n]
                               ]
                        (d/db conn) email) 
                (if (or ( = (nth user 1)  "admin" )  
                        ( = (nth user 1)  "manager" )
                        )
                  (d/q '[:find ?email ?role ?locked ?u ?p ?pwd ?s ?c ?n
                               :where
                               [?u :user/email ?email]
                               [?u :user/role ?role]
                               [(get-else $ ?u :user/locked false) ?locked]
                               [(get-else $ ?u :user/picture "") ?p]
                               [(get-else $ ?u :user/password "") ?pwd]
                               [(get-else $ ?u :user/confirmed false) ?c]
                               [(get-else $ ?u :user/source "site") ?s]
                               [(get-else $ ?u :user/name false) ?n]
                               ]
                        (d/db conn)) #{})
                  )
    ]
    users
  )
)

(defn update-user [id name email password role locked picture]
  (d/transact
   conn
   [{ ;; this finds the existing entity
     :db/id id ;;#db/id [:db.part/user]  ;; will be replaced by exiting id
     :user/email email
     :user/name name
     :user/password password
     :user/role role
     :user/locked locked
     :user/picture picture
     }])
)


(defn create-user [name email password role locked picture]
  (d/transact
   conn
   [{:db/id #db/id[:db.part/user -1000001] :user/name name :user/email email :user/password password :user/role role :user/locked false :user/logcnt 0 :user/source ""}]
  )
)

(defn delete-user [id]
   
  (let [
        ;; id (first (first (d/q '[:find ?u
        ;;                     :in $ ?email
        ;;                     :where
        ;;                     [?u :user/email ?email]
        ;;                     ]
        ;;                   (d/db conn) email) ))  

        ]
       

  (d/transact
     conn
     [[:db.fn/retractEntity id]]
    )
  )
)
