ALTER TABLE "logs"
  ADD COLUMN "created_at_client" TIMESTAMP NULL;

UPDATE "logs"
SET "created_at_client" = "created_at";

ALTER TABLE "logs"
  ALTER COLUMN "created_at_client" SET NOT NULL;
