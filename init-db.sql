SELECT 'CREATE DATABASE keycloak_db OWNER social_user'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'keycloak_db')\gexec
