var sampleApp = angular.module('sampleApp', ['ngOauth2', 'ngResource']);

sampleApp.controller('SampleController', ['$rootScope', '$resource', '$window', 'oauth2Service', function($rootScope, $resource, $window, oauth2Service) {
	var ths = this;
	
	this.alerte = null;
	this.reconnectUrl = null;
	this.userInfo = null;
	this.accessToken = null;		// Juste pour info...
	
	this.loadUserInfo = function() {
		$resource(ths.param.restServer + '/userInfo').get(
				function(userInfo) {
					ths.userInfo = userInfo;
				},
				function(reason) {
					if( reason.code == "oauth2.needs_reconnect" )
						ths.reconnectUrl = reason.reconnectUrl;
					else
						ths.alerte = "Erreur à la récupération des infos utilisateur : " + reason;
				});
	};
	
	// Charge le paramétrage
	$resource('param.xsp').get(
			function(param) {
				ths.param = param;;
			},
			function(reason) {
				ths.alerte = "Erreur au chargement des paramètres de l'application : " + reason;
			}
	);
	
	// Initialise la danse oauth2. On force la reconnexion s'il n'est pas en session.
	oauth2Service.init('init.xsp', 'accessToken.xsp', 'refreshToken.xsp').then(
			function(token) {
				ths.accessToken = token;
			},
			function(reason) {
				if( reason.code == "oauth2.needs_reconnect" )
					$window.location = reason.reconnectUrl;
				else
					ths.alerte = "Erreur à l'initialisation de la danse oauth2";
			}
	);
}]);



