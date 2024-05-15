create table smell (
    value   text     not null,
    year    smallint not null,
    month   smallint not null,
    day     smallint not null,
    hour    smallint not null,
    minute  smallint not null,
    room_id uuid     not null references room(id),

    primary key (room_id, year, month, day, hour, minute)
);
