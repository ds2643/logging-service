# Log Analysis Tool
This project attempts to implement a solution to Signafire's take-home exercise.

Given a collection of timestamps and a reference to a log-file (in csv format), this command-line application outputs answers to a set of predetermined queries (e.g., the number of connections open at a given set of times).

## Description

## Problem Interpretation and Assumptions
A log file of arbitrary length contains information describing connection events to a hypothetical service. Each of these entries contains an IP address, the time the connection as terminated, and the length of connection in milliseconds (units clarified by Gurgen).

This application serves the purpose of surveying the amount and identity of connections from the log file active at a particular set of times. Additionally, some descriptive statistics are produced, including the average uptime.

The following assumptions are implicit in this implementation:
1. The predicate that tests to see if some timestamp falls within a log entry is *non-inclusive*. That is, timestamps falling at the starting or ending instants bounding an entry are not considered to be included in a range. For example, if a connection entry ends at `2017-10-23T12:00:00.000`, the timestamp `2017-10-23T12:00:00.000` does not fall into that range.

2. Average uptime is calculated as follows (discussed with Gurgen over email): The sum of time intervals described in the log file is divided over the time from the earliest connection to latest disconnection. Overlap between intervals is not considered, as each connection is regarded independently.

3. The log file data is assumed to be valid. Missing values are not checked for in processing the data. However, later iterations of such a program could implement such validation using something like schema validation. Moreover, duplicates in the csv data are not accounted for. For example, if the csv lists the same IP address connecting in overlapping intervals, each interval will be regarded independently. In other words, the IP is not used yet as a unique identifier.

4. The command-line interface loosely obeys the problem specification. For example, rather than outputting a literal map, the results are pretty-printed to console. This change is intended to make the program easier to use.

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

## Limitations and Directions for improvement
1. Accomodating larger data sets:
- The problem specification does not place bounds upon the size of the log file. In the suggested case in which this log file grows several orders of magnitude to GB or TB size, several scaling constraints will come into play. Firstly, the amount of runtime memory would be exhausted, since the entire dataset is currently read into memory as a clojure map (see references to `log-entries` in `challenge.analysis`). To address this problem, we could possibly expore the possibility of using streams to process the data. Such a design choice would limit the amount of data held in memory at any particular instant. As a conseqence, the apparatus used to analyze the data would need to be updated to accomodate streaming data-structures. 
- Secondly, an additional constraint in *time* is introduced: The program would take much longer to produce results. Depending on the setting in which the program is used, this may or may not be a problem. However, if the results are required quickly, exploring some method of parallel or concurrent analysis might be in order. Such changes would require significant refactoring of `analysis`. The author's opinion is to avoid to such changes unless absolutely necessary, given that such complexity might introduce complexity and make the code harder to maintain.

2. Testing story leaves much to be desired
- Automated testing is currently limited to modest property and schema level testing (e.g., testing for a key expected to be present in a return value from a function). An approach lending further confidence to the implementation might be to include more example based tests.

3. Ties in the minimum and maximum connections are currently ignored. For example, if two times share 3 connections and 3 connections is the minimum, both should be outputted. This behavior should be regarded as a bug. Updating the implementation to output a collection would be more appropriate.
