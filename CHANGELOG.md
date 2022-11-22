0.4.2: (November 22nd 2022)
- Recompiled app with new Android libraries, no changes.

0.4.1: (July 4th 2018)
- Fixed timer formatting for runs longer than 1 hour 

0.4.0: (August 16th 2017)
- Included the GitHub guide
- Updated icon

0.3.8: (May 23rd 2017)
- Fixed possible crash when in.readLine() responded null to a network request
- Enabled ProGuard to optimize and shrink the .apk
- Added setting for vibration
- Added some contact links to info dialog
- Some refactoring to update the code

0.3.7: (November 17th 2016)
- App is now out of beta (no real code changes here)

0.3.6: (October 9th 2016)
- Added timer format setting

0.3.5: (October 1st 2016)
- Exceptions aren't caught anymore, now the proper Android dialog can be used upon crash
- .apk are now signed beginning with this version 0.3.5 

0.3.4: (October 1st 2016)
- Fixed potential crash

0.3.3: (September 30th 2016)
- Added dark theme
- Improved changelog file

0.3.2: (September 25th 2016)
- Added vibration

0.3.1: (September 23rd 2016)
- Network socket is now reused instead of creating a new one for each request, allowing more frequent and stable polling (maybe)
- Added progress wheel as network indicator in top right corner, replacing the twitching info text
- Forgot to update intenal version number for 0.3.0, skipped it to 0.3.1 now

0.3.0: (September 22nd 2016)
- Added settings
- Added info dialog

0.2.0: (September 19th 2016)
- App is now dependent on the new server version to get timer phase
- Improved dynamic timer phase behavior and polling
- Improved error handling
- Some refactoring

0.1.5: (September 16th 2016)
- Added workaround and info toast for the 0.00 game time bug

0.1.4: (September 16th 2016)
- Fixed LiveSplit milliseconds being parsed wrong on some devices
- Screen should now stay awake
- Fixed timer stopping when closing and reopening app when server is not reachable

0.1.3: (September 16th 2016)
- Fixed a crash when onResume was called before onCreate
- Adjusted timer placeholder to 2 decimal digits

0.1.2: (September 16th 2016)
- Timer now has 2 decimal digits
- Timer now updates every frame when running
- Timer synchronization rate increased: 10s -> 3s
- Improved thread protocol execution for older android versions

0.1.1: (September 15th 2016)
- Fixed minor text bug

0.1.0: (September 15th 2016)
- Added a timer which should behave like the server timer (except formatting maybe)
- Changed layout to have "undo", "skip" and "pause" buttons
- Implemented support for getcurrenttimerphase server command, making the app more dynamic
- Updated android stuff
- Reduced network command timeout to 3 seconds
- Added LiveSplit logo in toolbar
- A lot of small improvements

0.0.3: (July 8th 2016)
- Uncaught exceptions should be printed to a toast

0.0.2: (July 7th 2016)
- Lowered minimum required android from 4.0 to 2.3

0.0.1: (July 7th 2016)
- Initial commit, IP can be set, basic timer commands for tests