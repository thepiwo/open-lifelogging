CREATE TABLE "log_coords" (
  "id"            BIGSERIAL PRIMARY KEY,
  "log_entity_id" BIGINT,
  "latitude"      DOUBLE PRECISION NOT NULL,
  "longitude"     DOUBLE PRECISION NOT NULL
);

ALTER TABLE "log_coords"
  ADD CONSTRAINT "LOG_ENTITY_FK" FOREIGN KEY ("log_entity_id") REFERENCES "logs" ("id") ON UPDATE RESTRICT ON DELETE CASCADE;