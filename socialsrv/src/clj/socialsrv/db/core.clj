(ns socialsrv.db.core
  (:require [datomic.api :as d]
            [mount.core :refer [defstate]]
            [socialsrv.config :refer [env]]))

(defstate conn
          :start (-> env :database-url d/connect)
          :stop (-> conn .release))

(defn create-schema []
  (let [schema [{:db/ident              :user/id
                 :db/valueType          :db.type/string
                 :db/cardinality        :db.cardinality/one
                 :db.install/_attribute :db.part/db}
                {:db/ident              :user/first-name
                 :db/valueType          :db.type/string
                 :db/cardinality        :db.cardinality/one
                 :db.install/_attribute :db.part/db}
                {:db/ident              :user/last-name
                 :db/valueType          :db.type/string
                 :db/cardinality        :db.cardinality/one
                 :db.install/_attribute :db.part/db}
                {:db/ident              :user/email
                 :db/valueType          :db.type/string
                 :db/cardinality        :db.cardinality/one
                 :db.install/_attribute :db.part/db}]]
    @(d/transact conn schema)))

(defn entity [conn id]
  (d/entity (d/db conn) id))

(defn touch [conn results]
  "takes 'entity ids' results from a query
    e.g. '#{[272678883689461] [272678883689462] [272678883689459] [272678883689457]}'"
  (let [e (partial entity conn)]
    (map #(-> % first e d/touch) results)))

(defn create-user [id from picture name]
  (let [res (d/transact
   conn
   [{:db/id #db/id[:db.part/user -1000001] :user/email id :user/password id :user/locked false :user/confirmed true :user/role "user" :user/source from :user/name name :user/picture (str "data:image/jpeg;base64," picture)}]
  )]
   (second (first (:tempids @res)))
  )
)

;; (defn add-user [conn {:keys [i email]}]
;;   @(d/transact conn [{:db/id           id
;;                       :user/first-name first-name
;;                       :user/last-name  last-name
;;                       :user/email      email}]))

(defn find-user [conn id]
  (let [user (d/q '[:find ?e :in $ ?id
                      :where [?e :user/id ?id]]
                    (d/db conn) id)]
    (touch conn user)))


(defn increment-lock [email]
  (let [
    users (d/q '[:find ?u ?locked ?cnt
                      :in $ ?email
                      :where
                      [?u :user/email ?email]
                      [?u :user/locked ?locked]
                      [?u :user/logcnt ?cnt]
                     ]
                     (d/db conn) email)
    cnt (+ (nth (first users) 2) 1)
    locked (if (> cnt 2) true false)
    cnt (if locked 0 cnt)
    ]
    (d/transact
      conn
      [{:user/email email ;; this finds the existing entity
        :db/id #db/id [:db.part/user]  ;; will be replaced by exiting id
        :user/locked locked
        :user/logcnt cnt
       }
      ]
    )
    cnt
  )
)

(defn unlock-user [email]
  (let [
    userid (first (first (d/q '[:find ?u
                          :in $ ?email
                          :where
                          [?u :user/email ?email]
                          ]
                        (d/db conn) email)))

    ]
    (println (str "userid=" userid) )
    (d/transact
      conn
      [{
        :db/id userid ;; will be replaced by exiting id
        :user/logcnt 0
       }
      ]
    )
  )
)


(defn find-user-by-email [email]
  (let [users (d/q '[:find ?email ?l ?c ?u
                      :in $ ?email
                      :where
                      [?u :user/email ?email]
                      [(get-else $ ?u :user/locked false) ?l]
                      [(get-else $ ?u :user/confirmed false) ?c]
                     ]
                     (d/db conn) email)
    ]
    (first users)
  )
)

(defn find-social-user [id from]
  (let [users (d/q '[:find ?u ?r ?l
                      :in $ ?id ?from
                      :where
                      [?u :user/source ?from]
                      [?u :user/email ?id]
                      [?u :user/role ?r]
                      [?u user/locked ?l]
                     ]
                     (d/db conn) id from)
    ]
    users
  )
)


(defn find-user [email password]
  (let [users (d/q '[:find ?email ?r ?l
                      :in $ ?email ?password
                      :where
                      [?u :user/email ?email]
                      [?u :user/password ?password]
                      [?u :user/role ?r]
                      [?u user/locked ?l]
                     ]
                     (d/db conn) email password)
    ]
    users
  )
)

