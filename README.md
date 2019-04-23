# Log Analysis Tool
This project attempts to implement a solution to Signafire's take-home exercise.

Given a collection of time-stamps and a reference to a log-file (in csv format), this command-line application outputs answers to a set of predetermined queries.

## Description

## Use
This application is run from the command-line, either from source or compiled artifact (i.e., java jar).

### Requirements
To run the application from source, compile, or test, `leiningen` is required. Please see [installation instructions](https://leiningen.org/#install) if necessary.

Under all circumstances, java (1.8 or greater) is required. The application is tested in development with java 1.8.

### Command-Line Interface
Given a list of time-stamps (comma-separated, adhering to format) and a log csv file (defaults to `resources/log.csv` if no argument is provided), the tool outputs a report:

```
lein run -t <list-of-timestamps> --log-file <path-to-log-file>
```

For example:
```
lein run -t 2017-10-23T12:00:00.000,2017-10-23T11:00:00.000
Analyzing: file: resources/log.csv
Time: 2017-10-23T12:00:00Z
	Active IP addresses: 1.2.3.15
	Count: 1
Time: 2017-10-23T11:00:00Z
	Active IP addresses:
	Count: 0
Statistics:
	Total time elapsed: 3000300 milliseconds
	Total uptime: 4740 milliseconds
	Average connections: 0.002 connections
	Fewest connections (0) at 2017-10-23T11:00:00Z
	Most connections (1) at 2017-10-23T12:00:00Z
	Earliest connection: 2017-10-23T11:59:59.700Z
	Latest disconnection: 2017-10-23T12:50:00.000Z
```

Overview of flags:
| short flag | long flag  | description                                           | default           | validation rules                                          |
|------------|------------|-------------------------------------------------------|-------------------|-----------------------------------------------------------|
| -t         | --times    | Comma-separated list of times upon which to run query | None              | Must be comma separated list of strings parsable as dates |
| None       | --log-file | Path to log csv file                                  | resources/log.csv | File must exist                                           |

### Interpreting the Response
The program output contains several indentation-denoted blocks of query results.

Firstly, each time-stamp included as a command-line input (`--times` argument) results in a block of information. An example is annotated below:

```
Time: 2017-10-23T12:00:00Z          # time-stamp from input
	Active IP addresses: 1.2.3.15   # list of active ip addresses during that time
	Count: 1                        # total number of active ip addresses at the time
```

Next, a small set of statistics is included. As an example:
```
Statistics:
	Total time elapsed: 3000300 milliseconds           # total time from beginning to final connection in log
	Total uptime: 4740 milliseconds                    # time for which ips were active (ignoring overlap)
	Average connections: 0.002 connections             # total uptime / total time
	Fewest connections (0) at 2017-10-23T11:00:00Z     # time from input with fewest active connections
	Most connections (1) at 2017-10-23T12:00:00Z       # time from input with most active connections
	Earliest connection: 2017-10-23T11:59:59.700Z      # time at which connections start
	Latest disconnection: 2017-10-23T12:50:00.000Z     # time of final disconnection
```

Please note that the average connection calculation assumes the following is a valid means of calculation: The sum of time spent connected (taking into account all rows in the log csv) is divided by the span of time over which connections existed (the range of times included in the log csv).

### Compilation
A compiled artifact may be produced as follows:

```
lein uberjar
```

This compiled artifact behaves identically to running from source:

```
java -jar <path-to-jar-file> -t <list-of-times> --log-file <path-to-log-file>
```

### Testing
Packaged automated tests may be run from source:
```
lein test
```

## Directions for Improvement
- Handling larger inputs
- Ties are not addressed
