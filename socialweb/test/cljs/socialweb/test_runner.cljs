(ns socialweb.test-runner
  (:require
   [doo.runner :refer-macros [doo-tests]]
   [socialweb.core-test]
   ;[org.httpkit.client :as http]
   [socialweb.common-test]))

(enable-console-print!)

(doo-tests 'socialweb.core-test
           'socialweb.common-test)
