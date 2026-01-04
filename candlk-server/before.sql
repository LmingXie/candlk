
-- 更新token
UPDATE gs_meta
SET value = JSON_SET(value, '$.token', '3386f6083b9aab21bb490373ccc770734958665b'),
    update_time = NOW(3)
WHERE type = 1 AND name = 'KY';

-- 更新代理
UPDATE gs_meta
SET value = JSON_SET(value, '$.proxy', 'proxy://user-sp5licnop4-country-hk:HgjlQkK3Os61akh6i_@isp.decodo.com:10001'),
    update_time = NOW(3)
WHERE type = 1
  AND JSON_VALID(value);