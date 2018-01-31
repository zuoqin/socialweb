(ns socialsrv.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [socialsrv.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[socialsrv started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[socialsrv has shut down successfully]=-"))
   :middleware wrap-dev})
