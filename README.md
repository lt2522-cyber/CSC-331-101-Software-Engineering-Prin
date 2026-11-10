# Sports Seer (Team G4Y)

Java desktop app (Swing) that predicts NBA matchups and player
performance from past season stats. Built for CSC-331.

## How it is organized (MVC)

| Package | Class | Job |
|---|---|---|
| model | TeamSeason, PlayerSeason | One team or player for one season |
| model | Matchup, Prediction, PlayerProjection | Inputs and outputs of a prediction |
| model | ResultsModel, IResultsListener | Observer pattern: tells the View when results change |
| data | StatsRepository | Singleton. Reads the CSV files once and keeps them in memory |
| engine | IPredictionEngine, StatPredictionEngine | **The prediction engine** (main focus) |
| engine | PredictionCache | Remembers predictions already made (HashMap) |
| controller | PredictionController | Validates input, checks cache, runs engine |
| view | SportsSeerFrame | Swing window with 3 tabs |

No SQL database, no logins, no live API calls. Stats live in
`data/*.csv`, which ship with the app.

## Data

Every finished NBA season, 1946-47 through 2025-26 (regular season),
from the Kaggle "NBA Stats" dataset (built from Basketball-Reference):

- `data/team_seasons.csv`: 1,723 team-seasons (W, L, points scored
  and allowed per game)
- `data/player_seasons.csv`: 25,752 player-seasons (points,
  rebounds, assists, 3PT%). Traded players get one full-season line
  under their last team. Stats the NBA did not track yet (3-pointers
  before 1979-80, early rebounds/assists) show as N/A.

ABA seasons and the season still in progress are left out.

To refresh with a newer Kaggle download, unzip it and run:

```
java -cp bin sportsseer.data.DatasetConverter path/to/kaggle data
```

## Run (VS Code or terminal)

```
javac -d bin $(find src -name "*.java")
java -cp bin sportsseer.SportsSeerApp
```

Run from the project folder so the app can find `data/`.

## Tests (JUnit 5)

Put `junit-platform-console-standalone-1.11.x.jar` in `lib/`, then:

```
javac -d bin -cp lib/* $(find src test -name "*.java")
java -jar lib/junit-platform-console-standalone-*.jar -cp bin --scan-classpath
```

In VS Code, the Java Test Runner extension can run them too.

## Javadoc

```
javadoc -d docs -sourcepath src -subpackages sportsseer
```
