# Revolut Moneys

Service for managing wallets and money transfers

## Specs

- More or less simple implementation with little amount of functionality
- Used akka-http, macwire, logback and scalatest
- Custom simple in-memory database without much internal logic
- Can be executed with `sbt run`
- Full specs for REST service, not much coverege otherwise

## Missing

- Better error handling, right now it's litteraly just error or not outside and simple text messages inside.
- Full test coverege
- Probably some revision of the style and some relocation of logic
