create table temperature (
    value  text     not null,
    year   smallint not null,
    month  smallint not null,
    day    smallint not null,
    hour   smallint not null,
    minute smallint not null,

    primary key (year, month, day, hour, minute)
);

create table pressure (
    value  text     not null,
    year   smallint not null,
    month  smallint not null,
    day    smallint not null,
    hour   smallint not null,
    minute smallint not null,

    primary key (year, month, day, hour, minute)
);

create table humidity (
    value  text     not null,
    year   smallint not null,
    month  smallint not null,
    day    smallint not null,
    hour   smallint not null,
    minute smallint not null,

    primary key (year, month, day, hour, minute)
);

create table co2 (
    value  text     not null,
    year   smallint not null,
    month  smallint not null,
    day    smallint not null,
    hour   smallint not null,
    minute smallint not null,

    primary key (year, month, day, hour, minute)
);
