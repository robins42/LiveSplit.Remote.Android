<h1> <img src="https://raw.githubusercontent.com/LiveSplit/LiveSplit/master/LiveSplit/Resources/Icon.png" alt="LiveSplit Android Remote" height="42" width="45" align="top"/> LiveSplit Android Remote</h1>

## Overview ##

LiveSplit Android Remote is an app to control [LiveSplit](https://github.com/LiveSplit/LiveSplit) from your Android device. (Android 2.3 or above)

<p align="center">
	<a href='https://play.google.com/store/apps/details?id=de.ekelbatzen.livesplit.remote'>
		<img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' width=200/>
	</a>
</p>

<p align="center">
	<img src="https://raw.githubusercontent.com/Ekelbatzen/LiveSplit.Remote.Android/master/pictures/screenshot.png" alt="LiveSplit Android Remote"/>
</p>

## Features ##
- A running timer that synchronizes with LiveSplit
- Start
- Pause
- Resume
- Reset
- Split
- Undo split
- Skip split
- Keeps screen on

## Setup ##

1. Download and install the current app version from Google Play (Click on the Google Play Button above to get there)<br/><br/>or<br/><br/>download any current/previous .apk from the apk folder here on GitHub. (You may have to allow apps from unknown sources in your phone security/developer settings. Older versions may not work as well for obvious reasons.)
2. Download and extract LiveSplit 1.7 or newer: http://livesplit.org/downloads/
	<p align="center">
		<img src="https://raw.githubusercontent.com/Ekelbatzen/LiveSplit.Remote.Android/master/app/src/main/res/drawable/guide_1.png" alt="Setup step 2"/>
    </p>
    
3. Download and extract the LiveSplit server component (LiveSplit.Server_1.7.x.zip or newer) into the LiveSplit "components" sub-directory: https://github.com/LiveSplit/LiveSplit.Server/releases
	<p align="center">
		<img src="https://raw.githubusercontent.com/Ekelbatzen/LiveSplit.Remote.Android/master/app/src/main/res/drawable/guide_2.png" alt="Setup step 3"/>
    </p>
    
4. Start LiveSplit, open your layout
    <p align="center">
		<img src="https://raw.githubusercontent.com/Ekelbatzen/LiveSplit.Remote.Android/master/app/src/main/res/drawable/guide_3.png" alt="Setup step 4"/>
	</p>
5. Add the LiveSplit Server component to your layout
	<p align="center">
		<img src="https://raw.githubusercontent.com/Ekelbatzen/LiveSplit.Remote.Android/master/app/src/main/res/drawable/guide_4.png" alt="Setup step 5"/>
	</p>
6. Note your IP, you will need it for the app (You may also change port if necessary). (This IP is needed so the app knows how to connect to your LiveSplit computer.)
	<p align="center">
		<img src="https://raw.githubusercontent.com/Ekelbatzen/LiveSplit.Remote.Android/master/app/src/main/res/drawable/guide_5.png" alt="Setup step 6"/>
	</p>
7. Start the server (Windows may ask for firewall permissions, you have to allow it)
	<p align="center">
		<img src="https://raw.githubusercontent.com/Ekelbatzen/LiveSplit.Remote.Android/master/app/src/main/res/drawable/guide_6.png" alt="Setup step 7"/>
	</p>

8. Go to the app settings and enter your IP (and port if you changed it)
	<p align="center">
		<img src="https://raw.githubusercontent.com/Ekelbatzen/LiveSplit.Remote.Android/master/app/src/main/res/drawable/guide_7.png" alt="Setup step 8"/>
	</p>

(You can also use your public IP to access the server via internet, but you will have to forward that port in your router)

## Using the code ##

If you want to compile the .apk yourself (although they are all in the apk folder) or edit the code (although you can make requests to me what you want as a new feature or fix), you should be able to pretty much just put the whole project into Android Studio. You will need the SDK files specified in app/build.gradle or change it to yours.