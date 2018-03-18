SELECT
  floor(max(a.id))     AS maxId,
  floor(min(a.id))     AS minId,
  floor(max(a.number)) AS modSelector
FROM (SELECT
        l.id AS      id,
        row_number() OVER () AS number
      FROM logs l
      WHERE user_id = 1) a;


SELECT *
FROM logs
WHERE id IN (SELECT id
             FROM (SELECT
                     l.id AS      id,
                     row_number() OVER () AS number
                   FROM logs l
                   WHERE user_id = 1) a
             WHERE a.id = maxId OR a.id = minId OR a.number % ceil(modSelector / LIMIT)::BIGINT = 0 ORDER BY a.id DESC);