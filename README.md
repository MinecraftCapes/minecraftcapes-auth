# 🔐 MinecraftCapes Auth Server
This is the auth plugin for registration, forgot password and account deletion. We are now using Velocity for speed of development and ease of updating.

## Authors
*  **James Harrison** - *Initial work* - [james090500](https://github.com/james090500)

## Dependencies
-  [Velocity](https://velocitypowered.com/)
- Java 25

## Configuration and operation
Settings are copied to the plugin data directory on first startup. Set `AUTH_ENDPOINT`,
`USERS_ENDPOINT`, and `API_KEY` in `settings.toml` before use.

`/minecraftcapesauth` (alias `/mcauth`) reloads settings and requires
`minecraftcapesauth.reload`. Invalid reloads keep the previous configuration.

Authentication uses a shared asynchronous HTTP client with a 5-second connection
timeout and a 10-second request timeout. Skin URLs come directly from the player's
existing profile; no additional skin lookup is made. Failed requests, missing codes,
and banned accounts are denied with the configured message.

User counts refresh every 60 seconds with at most one refresh in flight. The MOTD
is cached between count changes and reloads, and a failed refresh retains the last
successful count. Before the first successful refresh, the proxy's MOTD is used.

## Build and checks
Run `./gradlew build` (`gradlew.bat build` on Windows). The build includes standalone
regression checks for malformed skin data, authentication messages, configuration
reloads, and HTTP payloads and failures using a local test server.
