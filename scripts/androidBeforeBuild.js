let fs = require('fs');

fs.readdir('platforms/android/app/src/main/java/com/bf', (error, folders) => {
	if (error) return console.log('processFiles err: ' + error);

	const bundleId = folders[0];
	const file = `platforms/android/app/src/main/java/com/bf/${bundleId}/MainActivity.java`;

	fs.readFile(file, 'utf-8', (err, data) => {
		if (err) return console.log(err);

		data = data.replace('import org.apache.cordova.*;', 'import com.ibeaconbg.www.MainCordovaActivity;');

		data = data.replace('extends CordovaActivity', 'extends MainCordovaActivity');

		data = data.substring(0, data.indexOf('{')) + '{}';

		fs.writeFile(file, data, 'utf-8', console.log);
	});
});