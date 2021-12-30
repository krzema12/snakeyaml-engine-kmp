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

### Use the data 

The easy way, but it does not work because of the hierarchy in the data.

It does not work because of the hierarchy on the test data.

- download [the latest data](https://github.com/yaml/yaml-test-suite/archive/refs/tags/data-2021-10-09.tar.gz) 
from [tags](https://github.com/yaml/yaml-test-suite/tags). It must begin with 'data' in the name.
- unzip it
- remove 3 folders ***name, meta, tags***
- move the rest to `src/test/resources/comprehensive-test-suite-data`

### Build the data

- clone [YAML Test Suite](https://github.com/yaml/yaml-test-suite)
- take tag
```
    git tag | grep data
    git checkout <TAG>
```
- build with flat data

```shell
make clean data && mv data orig && mkdir data; find orig -name === | sed 's/===//; s/orig\///' | while read d; do (set -x; cp -r orig/$d data/${d/\/0/-0}); done; rm -fr orig
```
- copy *data* folder to `src/test/resources/comprehensive-test-suite-data`

### Check the import

Run the tests and fix the errors:

    ./docker-run-jdk8.sh