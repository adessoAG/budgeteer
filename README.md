## What is Budgeteer?
Budgeteer is a budget monitoring tool that allows you to track
project budgets. A project in the sense of Budgeteer contains a set of
budgets each defined in a currency. For each person that contributes to the
project, you set up a daily rate in that currency. Budgeteer allows you to
import working hours and does the math for you, so that you always know where
your budgets are heading.

## Screenshots
### Start Page
![Start Page](https://raw.githubusercontent.com/adessoAG/budgeteer/master/screenshots/start-page.png)

### Budget Overview
![Budget Overview](https://raw.githubusercontent.com/adessoAG/budgeteer/master/screenshots/budget-overview.png)

### Month Report
![Month Report](https://raw.githubusercontent.com/adessoAG/budgeteer/master/screenshots/month-report.png)

## Configuration with docker-compose
### Running
To start the application with the default configuration simply run `docker-compose up`.
The server should then start running on localhost with port 8080.

### Spring config
To get access to config files on the host the docker-compose creates a bind mount to the `./config` directory on the host.
So put any spring configurations into that folder.

### Database
Since the provided `docker-compose.yaml` uses Postgres as a database you can configure it using the available
[environment variables](https://github.com/docker-library/docs/blob/master/postgres/README.md#environment-variables).
If you change the username, password, or database, don't forget to make the necessary changes to the spring config.
