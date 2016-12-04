CREATE TABLE "user_settings" (
  "id"               BIGSERIAL PRIMARY KEY,
  "user_id"          BIGINT,
  "last_fm_username" VARCHAR
);

ALTER TABLE "user_settings"
  ADD CONSTRAINT "LOG_USER_FK" FOREIGN KEY ("user_id") REFERENCES "users" ("id") ON UPDATE RESTRICT ON DELETE CASCADE;