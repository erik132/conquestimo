If you are setting this project up do not forget to create application-local.properties in conquestimo\\conquestimo-backend\\src\\main\\resources.

This file is git ignored and is where you store local configuration that should not be committed. It should contain at minimum:

```
spring.datasource.username=<your_db_username>
spring.datasource.password=<your_db_password>

app.delete-old-games=true
```

Setting `app.delete-old-games=true` will delete all existing games from the database on every backend startup. This is useful during development. Set it to `false` (or omit it) in any environment where game data should be preserved.

