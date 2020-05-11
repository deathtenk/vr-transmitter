# vr-transmitter

FIXME: description

## Env Vars

BOOTSTRAP_SERVER - URL for the kafka brokers
INPUT_TOPIC - the input topic where data gets dumped (default: vrdata)

## Usage

Run the project directly:

    $ clj -m deathtenk.vr-transmitter

Run the project's tests (they'll fail until you edit them):

    $ clj -A:test:runner

Run the project as a headless application
    
    $ make run-dev

Run the nrepl (requires nrepl profile in .clojure profile)

    $ make nrepl

## Options

FIXME: listing of options this app accepts.

## Endpoints

get "/" - Health Check
post "/vr" - Takes a JSON body thats a valid ::openvr object and posts it to INPUT_TOPIC on BOOTSTRAP_SERVER

### Bugs

...

### Any Other Sections
### That You Think
### Might be Useful

## License

Copyright © 2020 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
