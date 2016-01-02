
# Github Repo Stats

Prints out stats for each non-forked, private repo owned by a GitHub organization for the given date range in org-mode format.

## Why use it?
- Outputs stats for every repo an organization owns into org-mode format
- Custom date ranges for which all stats are calculated
- Total number of commits per repo for the team
- Additions and deletions per team member per repo
- Hour of commit in UTC to see when the team is pushing changes
- Hour of commit per team member

## Usage

To save a report of repo stats for an organization for a given date range to an org mode file, run the following command.

```
lein run <username> <password> <github-org-name> <start yyyy-mm-dd> <end yyyy-mm-dd> > report.org
```

**NOTE**: GitHub's rate limits are stringent so for a repo with many commits it can take a very long time to complete since it spends time waiting for rate limits to clear. This is due to the need to look up each commit to get accurate data for the stats being produced.

## Example

```org-mode
Repo stats for my-org from 2015-01-01 to 2015-12-31

my-repo

Total Commits 419

Commit Count by User

|        :name | :commits |
|--------------+----------|
|      user123 |       34 |
|      user456 |      386 |

Additions vs Deletions

|        :name | :additions | :deletions | :total |
|--------------+------------+------------+--------|
|      user123 |       1993 |        691 |   2684 |
|      user456 |      75823 |      31980 | 107803 |

Commits by Hour Overall

|  0 |  1 |  2 |  3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 12 | 13 |  14 |  15 |  16 |  17 |  18 |  19 |  20 |  21 |  22 |  23 |
|----+----+----+----+---+---+---+---+---+---+----+----+----+----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----|
| 87 | 27 | 21 | 10 | 5 | 3 |   |   |   | 3 |  1 |  3 |  5 | 31 | 205 | 343 | 342 | 244 | 363 | 361 | 410 | 429 | 323 | 163 |

Commits by Hour by User

|        :name |  0 |  1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 12 | 13 | 14 | 15 | 16 | 17 | 18 |  19 | 20 | 21 | 22 | 23 |
|--------------+----+----+---+---+---+---+---+---+---+---+----+----+----+----+----+----+----+----+----+-----+----+----+----+----|
|      user123 |  3 |  5 |   | 1 |   |   |   |   |   |   |    |    |    |    |    |  2 |    |  3 |  5 |   3 |  4 |  5 |  1 |  2 |
|      user456 |  8 |    | 3 | 1 | 4 | 3 |   |   |   |   |    |    |    |  2 | 21 | 42 | 43 | 22 | 40 |  35 | 44 | 50 | 50 | 18 |

```

## License

Copyright © 2015 Alex Kehayias

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
