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