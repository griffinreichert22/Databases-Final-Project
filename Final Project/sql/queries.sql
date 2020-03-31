select * from (rents natural join vehicle), depot where rents.pickup_depot = depot.id and rents.id=25


select * 
from rents natural join vehicle, depot 
where rents.id = 25 and vehicle.location = depot.id

select id, vin, pickup_depot, dropoff_depot, type from rents natural join vehicle where pickup_depot <> dropoff_depot order by id
