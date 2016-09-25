0.0.1:
- Initial commit, IP can be set, basic timer commands for tests

0.0.2:
- Lowered minimum required android from 4.0 to 2.3

0.0.3:
- Uncaught exceptions should be printed to a toast

0.1.0:
- Added a timer which should behave like the server timer (except formatting maybe)
- Changed layout to have "undo", "skip" and "pause" buttons
- Implemented support for getcurrenttimerphase server command, making the app more dynamic
- Updated android stuff
- Reduced network command timeout to 3 seconds
- Added LiveSplit logo in toolbar
- A lot of small improvements

0.1.1:
- Fixed minor text bug

0.1.2:
- Timer now has 2 decimal digits
- Timer now updates every frame when running
- Timer synchronization rate increased: 10s -> 3s
- Improved thread protocol execution for older android versions

0.1.3:
- Fixed a crash when onResume was called before onCreate
- Adjusted timer placeholder to 2 decimal digits

0.1.4:
- Fixed LiveSplit milliseconds being parsed wrong on some devices
- Screen should now stay awake
- Fixed timer stopping when closing and reopening app when server is not reachable

0.1.5:
- Added workaround and info toast for the 0.00 game time bug

0.2.0:
- App is now dependent on the new server version to get timer phase
- Improved dynamic timer phase behavior and polling
- Improved error handling
- Some refactoring

0.3.0:
- Added settings
- Added info dialog

0.3.1:
- Network socket is now reused instead of creating a new one for each request, allowing more frequent and stable polling (maybe)
- Added progress wheel as network indicator in top right corner, replacing the twitching info text
- Forgot to update intenal version number for 0.3.0, skipped it to 0.3.1 now

0.3.2:
- Added vibration
