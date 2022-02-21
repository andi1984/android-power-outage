# Power Outage Android App

## Motivation

We want to get notified when the device gets removed from power supply aka.
when a permanently connected device affects some power outage.

## Ideas

1. Listen for `ACTION_POWER_CONNECTED` or `ACTION_POWER_DISCONNECTED` intents.
   Unfortunately that is [no longer simply possible from Android 8
   on](https://developer.android.com/about/versions/oreo/background?authuser=1#broadcasts).
2. Usage of [`BatteryManager`](https://stackoverflow.com/a/45459483/778340),
   but then the question is how to watch for changes? No answer to this yet.
3. Use some more complex structure [using a system service together with a job
   scheduler](https://stackoverflow.com/a/56556138/778340).

Probably we will go with solution 3), currently implemented in a simple way is
2) without consistent watch.
