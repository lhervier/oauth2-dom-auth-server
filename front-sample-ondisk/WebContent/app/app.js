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
	
	// Abonnement aux évenements
	$rootScope.$on("alerte", function(event, level, message) {
		ths.alerte = {
				level: level,
				message: message
		}
	});
	
	$rootScope.$on("reconnect", function(event, url) {
		ths.reconnectUrl = url;
	});
	
	// Charge le access_token au démarrage de la page
	// On force la reconnexion
	tokenService.getToken(true).then(
			function(token) {
				ths.accessToken = token.access_token;
			},
			function() {
				alerteService.error("Erreur à la récupération du access_token");
			}
	);
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
		_getToken: function(force, url) {
			var result = $resource(url).get();
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
					reconnectService.reconnect(force);
				}
			);
		},
		getToken: function(force) {
			// On a le token => On le retourne
			if( svc.token ) {
				var defer = $q.defer();
				defer.resolve({access_token: svc.token});
				return defer.promise;
			}
			
			// On ne l'a pas, on va le chercher
			return this._getToken(force, 'accessToken.xsp');
		},
		refreshToken: function() {
			return this._getToken(false, 'refresh.xsp');
		}
	};
	return svc;
}]);

sampleApp.config(['$httpProvider', function($httpProvider) {
	$httpProvider.interceptors.push(['$injector', '$location', '$q', function($injector, $location, $q) {
		var external = RegExp('^((f|ht)tps?:)?//(?!' + $location.host() + ')');
		var shouldProcess = function(url) {
			if( external.test(url) )
				return true;
			if( url.endsWith('accessToken.xsp') )
				return false;
			if( url.endsWith('refresh.xsp') )
				return false;
			return true;
		}
		return {
			request: function(config) {
				if( !shouldProcess(config.url) )
					return config;
				
				var tokenService = $injector.get('tokenService');
				return tokenService.getToken(false).then(
						function(token) {
							config.headers.authorization = "Bearer " + token.access_token;
							return config;
						},
						function() {
							// Bidouille pour annuler la requête courante.
							var canceller = $q.defer();
							canceller.resolve();
							config.timeout = canceller.promise;
							return config;
						}
				);
			},
			
			responseError: function(rejection) {
				if( rejection.status != 403 || !shouldProcess(rejection.config.url) )
					return $q.reject(rejection);;
				
				var tokenService = $injector.get('tokenService');
				var $http = $injector.get('$http');
				return tokenService.refreshToken().then(
						function(token) {
							return $http(rejection.config);
						},
						function(error) {
							console.log('XXX');
						}
				);
			}
		};
	}]);
}]);

