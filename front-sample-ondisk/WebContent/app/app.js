var sampleApp = angular.module('sampleApp', ['ngResource']);

sampleApp.controller('SampleController', ['$rootScope', '$resource', 'tokenService', 'alerteService', function($rootScope, $resource, tokenService, alerteService) {
	
	var ths = this;
	
	this.alerte = null;
	this.reconnectUrl = null;
	this.userInfo = null;
	this.accessToken = null;
	
	this.loadUserInfo = function() {
		ths.userInfo = $resource('http://apis.privatenetwork.net:8080/rest-sample/userInfo').get(
				function() {},
				function(error) {
					alerteService.error("Erreur à la récupération des infos utilisateur...");
				});
	};
	
	this.recupererAccessToken = function() {
		tokenService.getToken(false).then(
				function(token) {
					ths.accessToken = token.access_token;
				},
				function() {
					ths.accessToken = null;
				}
		)
	}
	
	$rootScope.$on("alerte", function(event, level, message) {
		ths.alerte = {
				level: level,
				message: message
		}
	});
	
	$rootScope.$on("reconnect", function(event, url) {
		ths.reconnectUrl = url;
	});
}]);

sampleApp.factory('reconnectService', ['$rootScope', '$window', function($rootScope, $window) {
	return {
		reconnect: function(force) {
			var connectUrl = "init.xsp?redirect_url=" + encodeURIComponent($window.location);
			if( force )
				$window.location = connectUrl;
			else
				$rootScope.$emit("reconnect", connectUrl);
		}
	};
}]);

sampleApp.factory('alerteService', ['$rootScope', function($rootScope) {
	return {
		info: function(message) {
			$rootScope.$emit("alerte", "info", message);
		},
		warning: function(message, url) {
			$rootScope.$emit("alerte", "warning", message);
		},
		error: function(message, url) {
			$rootScope.$emit("alerte", "error", message);
		}
	}
}]);

sampleApp.factory('tokenService', ['$q', '$resource', '$window', 'alerteService', 'reconnectService', function($q, $resource, $window, alerteService, reconnectService) {
	var svc = {
		token: null,
		getToken: function(force) {
			// On a le token => On le retourne
			if( svc.token ) {
				var defer = $q.defer();
				defer.resolve({access_token: svc.token});
				return defer.promise;
			}
			
			// On n'a pas le token, on va le chercher
			var result = $resource('accessToken.xsp').get();
			return result.$promise.then(
					function() {
						var deferred = $q.defer();
						if( !result.access_token ) {	// On n'a pas le token, même si le serveur a repondu avec un code 200
							reconnectService.reconnect(force);
							deferred.reject();
						} else {
							svc.token = result.access_token;
							deferred.resolve(result);
						}
						return deferred.promise;
					},
					function() {						// Le serveur a répondu autre chose que 200
						svc.reconnect(force);
					}
			);
		}
	};
	return svc;
}]);

sampleApp.config(['$httpProvider', function($httpProvider) {
	$httpProvider.interceptors.push(['$injector', '$location', function($injector, $location) {
		return {
			request: function(config) {
				var tokenService = $injector.get('tokenService');
				
				var external = RegExp('^((f|ht)tps?:)?//(?!' + $location.host() + ')');
				if( !external.test(config.url) )
					return config;
				
				return tokenService.getToken(false).then(
						function(token) {
							config.headers.authorization = "Bearer " + token.access_token;
							return config;
						},
						function() {
							return config;
						}
				);
			}
		};
	}]);
}]);

