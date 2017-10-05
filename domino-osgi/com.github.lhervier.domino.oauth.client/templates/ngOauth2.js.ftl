var ngOauth2 = angular.module('ngOauth2', ['ngResource']);

ngOauth2.factory('oauth2Service', ['$rootScope', '$q', '$resource', '$window', function($rootScope, $q, $resource, $window) {
	var svc = {
		token: null,					// Cache for the token
		iss: null,						// Token issued date
		initEndPoint: null,				// Initialization end point
		tokenEndPoint: null,			// Token end point
		refreshEndPoint: null,			// Refresh end point
		_getToken: function(url) {
			var ths = this;
			
			var deferred = $q.defer();
			$resource(url).get().$promise.then(deferred.resolve, deferred.reject);
			
			return deferred.promise.then(
				function(result) {
					var def = $q.defer();
					if( !result.access_token ) {	// On n'a pas le token, même si le serveur a repondu avec un code 200
						def.reject({
							code: "oauth2.needs_reconnect",
							reconnectUrl: ths.initEndPoint + "?redirect_url=" + encodeURIComponent($window.location)
						});
					} else {
						svc.token = result.access_token;
						svc.iss = new Date().getTime();
						def.resolve(result);
					}
					return def.promise;
				},
				function() {						// Le serveur a répondu autre chose que 200
					var def = $q.defer();
					def.reject({
						code: "oauth2.error_getting_token"
					});
					return def.promise;
				}
			);
		},
		init: function(initEndPoint, tokenEndPoint, refreshEndPoint) {
			this.initEndPoint = initEndPoint;
			this.tokenEndPoint = tokenEndPoint;
			this.refreshEndPoint = refreshEndPoint;
			return this._getToken(this.tokenEndPoint);
		},
		getAccessToken: function() {
			// On a le token => On le retourne
			if( svc.token ) {
				var defer = $q.defer();
				defer.resolve(svc.token);
				return defer.promise;
			}
			
			// On ne l'a pas, on va le chercher
			return this._getToken(this.tokenEndPoint);
		},
		refreshToken: function() {
			// On ne rafraîchit que si le token n'a pas été mise à jour il y a peu de temps (10s)
			if( this.iss != null && new Date().getTime() - this.iss < 10000 ) {
				var deferred = $q.defer();
				deferred.reject({
					code: "oauth2.error.unable_to_refresh_token"
				});
				return deferred.promise;
			} else
				return this._getToken(this.refreshEndPoint);
		}
	};
	return svc;
}]);

ngOauth2.config(['$httpProvider', function($httpProvider) {
	$httpProvider.interceptors.push(['$injector', '$location', '$q', function($injector, $location, $q) {
		var external = RegExp('^((f|ht)tps?:)?//(?!' + $location.host() + ')');
		var shouldProcess = function(url) {
			return external.test(url);
		}
		return {
			request: function(config) {
				if( !shouldProcess(config.url) )
					return config;
				
				var oauth2Service = $injector.get('oauth2Service');
				return oauth2Service.getAccessToken().then(
						function(token) {
							config.headers.authorization = "Bearer " + token;
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
					var oauth2Service = $injector.get('oauth2Service');
					var $http = $injector.get('$http');
					var $window = $injector.get('$window');
					var deferred = $q.defer();
					oauth2Service.refreshToken().then(deferred.resolve, deferred.reject);
					return deferred.promise.then(
							function(token) {
								return $http(response.config);
							},
							function(error) {
								var deferred = $q.defer();
								deferred.reject({
									code: "oauth2.needs_reconnect",
									reconnectUrl: oauth2Service.initEndPoint + "?redirect_url=" + encodeURIComponent($window.location)
								});
								return deferred.promise;
							}
					);
				}
				return $q.reject(response);
			}
		};
	}]);
}]);