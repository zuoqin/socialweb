(ns socialsrv.db.zone
  (:require [datomic.api :as d]
            [mount.core :refer [defstate]]
            [socialsrv.db.core :refer [conn] ]
            [socialsrv.config :refer [env]]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.local :as l]
            [clj-time.coerce :as c]
))



(defn get-zones [uid]
  (let [
    zones (d/q '[:find ?z ?n ?c ?d
                 :in $ ?uid
                 :where
                 [?z :zone/user ?uid]
                 [?z :zone/name ?n]
                 [?z :zone/city ?c]
                 [?z :zone/diff ?d]
                ]
                (d/db conn) uid)
    ]
    zones
  )
)


(defn update-zone [id name city diff]
  (let [
        ]

   (d/transact
    conn
    [{:db/id id
      :zone/name name
      :zone/city city
      :zone/diff diff
      }])

  )
)


(defn create-zone [user name city diff]

  (let [
    ;; userid (first (first (d/q '[:find ?u
    ;;                             :in $ ?user
    ;;                             :where
    ;;                             [?u :user/code ?login]
    ;;                             ]
    ;;                           (d/db conn) user)))
    res (d/transact
        conn
        [{:db/id #db/id[:db.part/user -1000001] :zone/user user :zone/name name :zone/city city :zone/diff diff}]
      )
    ] 
    (second (first (:tempids @res)))
  )
)


(defn delete-zone [id]
   (d/transact
   conn
   [[:db.fn/retractEntity id]]
  )
)
