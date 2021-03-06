CREATE TABLE "logs" (
  "id"         BIGSERIAL PRIMARY KEY,
  "user_id"    BIGINT,
  "key"        VARCHAR   NOT NULL,
  "data"       JSONB     NOT NULL,
  "created_at" TIMESTAMP NOT NULL
);

ALTER TABLE "logs"
  ADD CONSTRAINT "LOG_USER_FK" FOREIGN KEY ("user_id") REFERENCES "users" ("id") ON UPDATE RESTRICT ON DELETE CASCADE;