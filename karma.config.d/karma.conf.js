config.set({
    // https://karma-runner.github.io/6.4/config/configuration-file.html#capturetimeout
    // default timeout 60_000, but looks like it is not enough for macOS on GitHub runner
    // increasing the timeout should help to reduce number of failures
    captureTimeout: 120_000,
    // the default value is 20_000
    // according to the doc, the socket timeout can cause similar error as captureTimeout
    // https://karma-runner.github.io/6.4/config/configuration-file.html#browsersockettimeout
    browserSocketTimeout: 120_000,
    // the default value is 5_000
    // https://karma-runner.github.io/6.4/config/configuration-file.html#pingtimeout
    pingTimeout: 60_000,
});

// A workaround from https://android-review.googlesource.com/c/platform/frameworks/support/+/3413540
(function() {
    const originalExit = process.exit;
    process.exit = function(code) {
        console.log('Delaying exit for logs...');
        // This extra time allows any pending I/O operations (such as printing logs) to complete,
        // preventing flakiness when Kotlin marks a test as complete.
        setTimeout(() => {
            originalExit(code);
        }, 5000);
    };
})();
