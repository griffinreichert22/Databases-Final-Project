create table customer (
	id int,
	name varchar(32),
	birthdate date,
	address varchar(32),
	city varchar(32),
	state varchar(2),
	zip int,
	DL_number int,
	DL_expiration date,
	primary key (id)
);

create table depot (
	id int,
	city varchar(32),
	type varchar(32), 
	check(type in ('city center', 'train station', 'airport', 'junkyard')),
	primary key (id)
);

create table vehicle (
	VIN int,
	odometer int,
	location int,
	make varchar(32),
	model varchar(32),
	type varchar(32),
	primary key (VIN),
	foreign key (location) references depot(id) on delete cascade
);

create table rents (
	id int,
	VIN int,
	start_date date,
	end_date date,
	pickup_depot int,
	dropoff_depot int,
    mileage int,
	primary key (id, VIN),
	foreign key (id) references customer(id) on delete cascade,
	foreign key (VIN) references vehicle(VIN) on delete cascade,
	foreign key (pickup_depot) references depot(id) on delete cascade,
	foreign key (dropoff_depot) references depot(id) on delete cascade,
	check (mileage > 0)
);

create table organization (
	verification int,
	title varchar(32),
	discount int,
	primary key (verification)
);

create table member (
	id int,
	verification int,
	primary key (id, verification),
	foreign key (id) references customer(id) on delete cascade, 
	foreign key (verification) references organization(verification) on delete cascade
);

create table fees (
	fee varchar(32),
	price number(8,2),
	primary key (fee),
	check (price > 0)
);

create table charges (
	id int,
	VIN int,
	fee varchar(32),
	primary key (id, vin, fee),
	foreign key (id) references customer(id) on delete cascade,
	foreign key (VIN) references vehicle(VIN) on delete cascade,
	foreign key (fee) references fees(fee) on delete cascade	
);


