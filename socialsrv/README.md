[:find ?u
 :where
 [?u :user/code "zuoqin"]
]


17592186045418



[:find ?t
 :where
 [?t :trip/user ?u]
 [?u :user/code "zuoqin"]
]
