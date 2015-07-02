// Enter all relative paths to your app's files / directories here

// TODO andrew - init app name from file

var appname = 'simple';

var Config = {
 index: '../../samples/' + appname + '/src/main/resources/WEB-INF/jsp/main.jsp',
 basepath:  '../../web/src/main/resources/',  // common web bundle assets
 apppath: '../../samples/' + appname + '/src/main/resources/', // app bundle assets
 appdir_to_remove: appname + '/web',
}

module.exports = Config;
