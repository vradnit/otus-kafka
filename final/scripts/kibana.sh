curl -s -X POST http://127.0.0.1:5601/api/saved_objects/index-pattern/index-pattern-id  -H 'kbn-xsrf: true' -H 'Content-Type: application/json' -d '
{
  "attributes": {
    "fieldAttrs":"{}",
    "title": "keycloak-events-*",
    "timeFieldName":"time",
    "fields":"[]"
  }
}'
