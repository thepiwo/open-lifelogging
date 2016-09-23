CREATE TABLE "log_wifi" (
  "id"            BIGSERIAL PRIMARY KEY,
  "log_entity_id" BIGINT,
  "ssid"          VARCHAR(255) NOT NULL,
  "speed"         INTEGER      NOT NULL,
  "status"        VARCHAR(255) NOT NULL
);

ALTER TABLE "log_wifi"
  ADD CONSTRAINT "LOG_ENTITY_FK" FOREIGN KEY ("log_entity_id") REFERENCES "logs" ("id") ON UPDATE RESTRICT ON DELETE CASCADE;