(ns user
  (:require [mount.core :as mount]
            socialsrv.core))

(defn start []
  (mount/start-without #'socialsrv.core/http-server
                       #'socialsrv.core/repl-server))

(defn stop []
  (mount/stop-except #'socialsrv.core/http-server
                     #'socialsrv.core/repl-server))

(defn restart []
  (stop)
  (start))


