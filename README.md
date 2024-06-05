# CURRENCY EXCHANGE RATE PORTAL 

![alt text](https://github.com/andrey-makeyev/CurrencyExchangeRatePortal/blob/master/src/main/resources/img/1.png?raw=true)

![alt text](https://github.com/andrey-makeyev/CurrencyExchangeRatePortal/blob/master/src/main/resources/img/2.png?raw=true)

### Features

* Central bank exchange rates page.
* After selecting a specific currency, its exchange rate history is displayed (chart and table).
* Currency calculator. The amount is entered, the currency is selected, the program displays the amount in foreign
  currency and the rate at which it was calculated.
* Exchange rates are automatically obtained every day (using job scheduling Quartz).
* Data storage saves exchange rates from the Bank of Lithuania.

### Used technology stack

* Java 17
* Spring Boot
* Maven
* H2
* Angular

### Run with tests

Run `mvn clean verify -Prun-with-tests` and navigate to `http://localhost:8080/`.

### Run without tests

Run `mvn clean compile spring-boot:run` and navigate to `http://localhost:8080/`.

### Angular live reload

In a new terminal window go to frontend folder, run `npm start` and navigate to `http://localhost:4200/`. The
frontend will automatically reload if you change any of the source files.
