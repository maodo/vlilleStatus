VlilleStatus
============

App to log downtime stations from vlille.fr

Mongo Install
-------------

Create indexes:
db.stations.ensureIndex({ name: 1 })
db.stations_items.ensureIndex({ stationId: 1, down: 1})
db.stations_items.ensureIndex({ startAt: 1 })
db.stations_items.ensureIndex({ endAt: 1 })

