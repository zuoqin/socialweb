(ns socialsrv.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[socialsrv started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[socialsrv has shut down successfully]=-"))
   :middleware identity})
