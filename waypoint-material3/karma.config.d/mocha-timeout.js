// Mocha fails a test after 2 seconds by default. Compose UI tests with several
// steps take longer than that on a busy CI runner, so they failed at random.
config.set({ client: { mocha: { timeout: 60000 } } });
