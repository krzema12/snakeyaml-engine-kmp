# Contribute to SnakeYAML Engine

## Architecture

Tons of useful information can be found on [the official site](https://yaml.org/spec/1.2.2/).

General step are defined [in the spec](https://yaml.org/spec/1.2.2/#31-processes):

![](https://yaml.org/spec/1.2.2/img/overview2.svg)

Loading has the following explicit steps

![](doc/YAML-streams.drawio.png)


## Testing

### Import test date

Engine uses [Comprehensive Test Suite for YAML](https://github.com/yaml/yaml-test-suite) for the tests.

The import:

- download [the latest data](https://github.com/yaml/yaml-test-suite/archive/refs/tags/data-2021-10-09.tar.gz) 
from [tags](https://github.com/yaml/yaml-test-suite/tags). It must begin with 'data' in the name.
- unzip it
- remove 3 folders ***name, meta, tags***
- move the rest to `src/test/resources/comprehensive-test-suite-data`

Run the tests:

    ./mvnw clean install