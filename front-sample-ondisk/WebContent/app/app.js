var sampleApp = angular.module('sampleApp', ['ngOauth2', 'ngResource']);

sampleApp.controller('SampleController', ['$rootScope', '$resource', '$window', 'oauth2Service', function($rootScope, $resource, $window, oauth2Service) {
	var ths = this;
	
	this.alerte = null;
	this.reconnectUrl = null;
	this.userInfo = null;
	this.accessToken = null;
	this.userInfoEndpoint = null;
	
	this.loadUserInfoFromResourceServer = function() {
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
	
	this.loadUserInfoFromUserInfoEndpoint = function() {
		$resource(ths.userInfoEndpoint).get(
				function(result) {
					ths.userInfo = result;
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
	oauth2Service.init('oauth2-client/init', 'oauth2-client/accesstoken', 'oauth2-client/refresh').then(
			function(result) {
				ths.accessToken = result.access_token;
				ths.userInfoEndpoint = result.user_info_endpoint;
			},
			function(reason) {
				if( reason.code == "oauth2.needs_reconnect" )
					$window.location = reason.reconnectUrl;
				else
					ths.alerte = "Erreur à l'initialisation de la danse oauth2";
			}
	);
}]);



