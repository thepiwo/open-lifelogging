ALTER TABLE "logs"
 DROP COLUMN "hash";

ALTER TABLE "logs"
  ADD CONSTRAINT "LOG_UNIQUE" UNIQUE ("data", "created_at_client");