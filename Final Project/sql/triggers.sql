
-- CUSTOMER

CREATE SEQUENCE customerID
	START WITH 0
	INCREMENT BY 1
	MINVALUE 0
	MAXVALUE 999999
	NOCYCLE;

create trigger nextID before insert on customer
    for each row
    begin
    select customerID.nextval into :new.id from dual;
    end;

-- DEPOT

CREATE SEQUENCE depotID
	START WITH 0
	INCREMENT BY 1
	MINVALUE 0
	MAXVALUE 999999
	NOCYCLE;

create trigger nextDepotID before insert on depot
    for each row
    begin
    select depotID.nextval into :new.id from dual;
    end;

-- VEHICLE

CREATE SEQUENCE getVin
	START WITH 0
	INCREMENT BY 1
	MINVALUE 0
	MAXVALUE 9999999
	NOCYCLE;

create trigger nextVin before insert on vehicle
    for each row
    begin
    select getVin.nextval into :new.vin from dual;
    end;

-- Organization
create sequence getOrg
	start with 0
	increment by 1
	MINVALUE 0 
	MAXVALUE 9999999
	nocycle;

create trigger nextOrg before insert on organization
	for each row
	begin
	select getOrg.nextval into :new.verification from dual;
	end;