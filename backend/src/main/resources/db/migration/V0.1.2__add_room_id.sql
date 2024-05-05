create table room (
    id   uuid primary key,
    name text not null
);
insert into room
values ('57c91c78-99db-49fe-ae88-f9ef723aca9b', 'リビング'),
    ('074c9520-05de-4ae1-89c7-7530baa85828', 'ESP32 #0');

alter table temperature
drop constraint temperature_pkey;
alter table temperature
add room_id uuid not null default '57c91c78-99db-49fe-ae88-f9ef723aca9b' ::uuid references room(id);
alter table temperature
add primary key (room_id, year, month, day, hour, minute);

alter table pressure
drop constraint pressure_pkey;
alter table pressure
add room_id uuid not null default '57c91c78-99db-49fe-ae88-f9ef723aca9b' ::uuid references room(id);
alter table pressure
add primary key (room_id, year, month, day, hour, minute);

alter table humidity
drop constraint humidity_pkey;
alter table humidity
add room_id uuid not null default '57c91c78-99db-49fe-ae88-f9ef723aca9b' ::uuid references room(id);
alter table humidity
add primary key (room_id, year, month, day, hour, minute);

alter table co2
drop constraint co2_pkey;
alter table co2
add room_id uuid not null default '57c91c78-99db-49fe-ae88-f9ef723aca9b' ::uuid references room(id);
alter table co2
add primary key (room_id, year, month, day, hour, minute);
