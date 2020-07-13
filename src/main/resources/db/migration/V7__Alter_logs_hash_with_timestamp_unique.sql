DELETE
  FROM "logs" a
  USING "logs" b
WHERE a.id < b.id
  AND a.created_at_client = b.created_at_client
  AND a.data = b.data;

ALTER TABLE "logs"
 DROP COLUMN "hash";

ALTER TABLE "logs"
  ADD CONSTRAINT "LOG_UNIQUE" UNIQUE ("data", "created_at_client");