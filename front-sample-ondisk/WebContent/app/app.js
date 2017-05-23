var sampleApp = angular.module('sampleApp', ['ngResource']);

sampleApp.controller('SampleController', ['$rootScope', '$resource', 'tokenService', function($rootScope, $resource, tokenService) {
	var ths = this;
	
	this.hello = "Hello World !";
	this.alerte = null;
	this.userInfo = null;
	
	this.loadUserInfo = function() {
		$resource('http://apis.privatenetwork.net:8080/rest-sample/userInfo').query(function(userInfo) {
			ths.userInfo = userInfo;
		});
	};
	
	this.test = function() {
		var promise = tokenService.getToken(false).then(
				function(token) {
					alert(token.access_token);
				},
				function(error) {
					console.log(error);
				}
		)
	}
	
	$rootScope.$on("alerte", function(event, level, message, url) {
		ths.alerte = {
			message: message,
			level: level,
			url: url
		};
	});
}]);

sampleApp.factory('alertService', ['$rootScope', function($rootScope) {
	return {
		info: function(message, url) {
			$rootScope.$emit("alerte", "info", message, url);
		},
		warning: function(message, url) {
			$rootScope.$emit("alerte", "warning", message, url);
		},
		error: function(message, url) {
			$rootScope.$emit("alerte", "error", message, url);
		}
	}
}]);

sampleApp.factory('tokenService', ['$q', '$resource', '$window', 'alertService', function($q, $resource, $window, alertService) {
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
			var result = $resource('accessToken.xsp').get(
					function(response) {
						if( !result.access_token )
							svc.reconnect(force);
						else
							svc.token = result.access_token;
					},
					function(error) {
						result.token = null;
						svc.reconnect(force);
					}
			);
			
			return result.$promise;
		},
		reconnect: function(force) {
			if( force )
				$window.location = "init.xsp?redirect_url=" + encodeURIComponent($window.location);
			else
				alertService.error("Reconnectez vous...", "init.xsp?redirect_url=" + encodeURIComponent($window.location));
		}
	};
	return svc;
}]);

sampleApp.config(['$httpProvider', '$injector', function($httpProvider, $injector) {
	$httpProvider.interceptors.push(function() {
		return {
			request: function(config) {
				var external = RegExp('^((f|ht)tps?:)?//(?!' + location.host + ')');
				if( !external.test(config.url) )
					return config;
				
				var promise = $injector.get('tokenService').getToken();
				return promise.then(
						function(token) {
							config.headers.authorization = "Bearer " + token;
							return config;
						}
				);
			}
		};
	});
}]);

