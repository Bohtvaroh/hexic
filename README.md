hexic
=====

Hexic game simulation demo

<pre>
      _       _       _       _       _
  _ / 3 \ _ / 0 \ _ / 1 \ _ / 4 \ _ / 1 \
/ 1 \ _ / 0 \ _ / 2 \ _ / 3 \ _ / 4 \ _ /
\ _ / 1 \ _ / 3 \ _ / 0 \ _ / 1 \ _ / 0 \
/ 2 \ _ / 1 \ _ / 1 \ _ / 4 \ _ / 2 \ _ /
\ _ / 2 \ _ / 3 \ _ / 3 \ _ / 1 \ _ / 0 \
/ 1 \ _ / 2 \ _ / 0 \ _ / 3 \ _ / 1 \ _ /
\ _ / 0 \ _ / 0 \ _ / 2 \ _ / 2 \ _ / 3 \
/ 0 \ _ / 1 \ _ / 3 \ _ / 0 \ _ / 3 \ _ /
\ _ / 2 \ _ / 3 \ _ / 0 \ _ / 0 \ _ / 3 \
/ 1 \ _ / 3 \ _ / 1 \ _ / 4 \ _ / 0 \ _ /
\ _ / 0 \ _ / 2 \ _ / 4 \ _ / 2 \ _ / 2 \
/ 4 \ _ / 0 \ _ / 2 \ _ / 2 \ _ / 2 \ _ /
\ _ / 3 \ _ / 1 \ _ / 3 \ _ / 2 \ _ / 2 \
/ 4 \ _ / 0 \ _ / 0 \ _ / 4 \ _ / 3 \ _ /
\ _ / 3 \ _ / 2 \ _ / 1 \ _ / 4 \ _ / 0 \
/ 4 \ _ / 4 \ _ / 3 \ _ / 1 \ _ / 3 \ _ /
\ _ / 2 \ _ / 3 \ _ / 4 \ _ / 3 \ _ / 4 \
    \ _ /   \ _ /   \ _ /   \ _ /   \ _ /
</pre>

## Compilation and running

[Leiningen 2](https://github.com/technomancy/leiningen) is required to build.

<pre>
$ cd hexic
$ lein uberjar
$ java -jar target/hexic-0.1.0-SNAPSHOT-standalone.jar
</pre>

## Command-line options

<pre>
$ java -jar target/hexic-0.1.0-SNAPSHOT-standalone.jar usage

Options allowed:

 improved-ui - start in improved UI mode
 fallback-ui - start in fallback UI mode (default on Windows)
 usage - print this information and exit
</pre>

## UI modes

Two UI modes are supported: improved and fallback. Fallback mode is just an ASCII console output whether improved mode updates its console output in place and uses some colors and font styling. Improved mode is used by default on Max OS X and Linux. Fallback mode is default on Windows. You can force improved mode on Windows by supplying 'improved-ui' command-line argument.

![Improved UI screenshot](http://img547.imageshack.us/img547/2528/screenshot20121215at720.png)
![Improved UI screenshot](http://img837.imageshack.us/img837/4167/screenshot20121215at726.png)
