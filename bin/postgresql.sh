#!/bin/bash
docker run --name cna-postgres -e POSTGRES_PASSWORD=password   -p 5432:5432 postgres
