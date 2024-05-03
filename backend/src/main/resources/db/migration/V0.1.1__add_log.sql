create table log (
    device_id text      not null,
    level     text      not null,
    value     text      not null,
    timestamp timestamp not null
);

create index idx_log on log(device_id, level);
