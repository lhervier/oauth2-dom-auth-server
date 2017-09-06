var sampleApp = angular.module('sampleApp', ['ngResource']);

sampleApp.controller('SampleController', ['$rootScope', '$resource', '$window', 'tokenService', function($rootScope, $resource, $window, tokenService) {
	
	var ths = this;
	this._reconnectUrl = "init.xsp?redirect_url=" + encodeURIComponent($window.location);
	
	this.alerte = null;
	this.reconnectUrl = null;
	this.userInfo = null;
	this.accessToken = null;		// Juste pour info...
	
	this.loadUserInfo = function() {
		ths.userInfo = $resource(ths.param.restServer + '/userInfo').get(
				function() {},
				function(reason) {
					if( "oauth2.needs_reconnect" == reason )
						ths.reconnectUrl = ths._reconnectUrl;
					else
						ths.alerte = "Erreur à la récupération des infos utilisateur : " + reason;
				});
	};
	
	// Abonnement aux évenements
	$rootScope.$on("alerte", function(event, level, message) {
		ths.alerte = {
				level: level,
				message: message
		}
	});
	
	$rootScope.$on("oauth2.reconnect", function(event, url) {
		ths.reconnectUrl = ths._reconnectUrl;
	});
	
	// Charge le paramétrage
	$resource('param.xsp').get(function(param) {
		ths.param = param;;
	});
	
	// Charge le token. On force la reconnexion s'il n'est pas en session.
	tokenService.getToken().then(
			function(token) {
				ths.accessToken = token.access_token;
			},
			function() {
				$window.location = "init.xsp?redirect_url=" + encodeURIComponent($window.location);
			}
	);
}]);

sampleApp.factory('tokenService', ['$rootScope', '$q', '$resource', '$window', function($rootScope, $q, $resource, $window) {
	var svc = {
		token: null,		// The token
		iss: null,			// Token issued date
		_getToken: function(url) {
			var ths = this;
			
			var deferred = $q.defer();
			$resource(url).get().$promise.then(deferred.resolve, deferred.reject);
			
			return deferred.promise.then(
				function(result) {
					var def = $q.defer();
					if( !result.access_token ) {	// On n'a pas le token, même si le serveur a repondu avec un code 200
						def.reject();
					} else {
						svc.token = result.access_token;
						svc.iss = new Date().getTime();
						def.resolve(result);
					}
					return def.promise;
				},
				function() {						// Le serveur a répondu autre chose que 200
					var def = $q.defer();
					def.reject();
					return def.promise;
				}
			);
		},
		getToken: function() {
			// On a le token => On le retourne
			if( svc.token ) {
				var defer = $q.defer();
				defer.resolve({access_token: svc.token});
				return defer.promise;
			}
			
			// On ne l'a pas, on va le chercher
			return this._getToken('accessToken.xsp');
		},
		refreshToken: function() {
			// On ne rafraîchit que si le token n'a pas été mise à jour il y a peu de temps (10s)
			if( this.iss != null && new Date().getTime() - this.iss < 10000 ) {
				var deferred = $q.defer();
				deferred.reject();
				return deferred.promise;
			} else
				return this._getToken('refresh.xsp');
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
			if( url.endsWith('param.xsp') )
				return false;
			return true;
		}
		return {
			request: function(config) {
				if( !shouldProcess(config.url) )
					return config;
				
				var tokenService = $injector.get('tokenService');
				return tokenService.getToken().then(
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
			
			responseError: function(response) {
				if( response.status == 403 && shouldProcess(response.config.url) ) {
					var tokenService = $injector.get('tokenService');
					var $http = $injector.get('$http');
					var deferred = $q.defer();
					tokenService.refreshToken().then(deferred.resolve, deferred.reject);
					return deferred.promise.then(
							function(token) {
								return $http(response.config);
							},
							function(error) {
								var deferred = $q.defer();
								deferred.reject("oauth2.needs_reconnect");
								return deferred.promise;
							}
					);
				}
				return $q.reject(response);
			}
		};
	}]);
}]);

