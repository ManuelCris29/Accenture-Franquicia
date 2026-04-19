#!/bin/bash
echo "=== Inicializando AWS local ==="

awslocal secretsmanager create-secret \
    --name ${AWS_SECRET_NAME:-/accenture/dev/db-credentials} \
    --secret-string "{\"username\":\"${MYSQL_USER:-admin}\",\"password\":\"${MYSQL_PASSWORD:-password}\",\"host\":\"mysql\",\"port\":\"3306\",\"dbname\":\"${DB_NAME:-franquicia_db}\"}"

echo "✓ Secret creado"
echo "=== LocalStack listo ==="