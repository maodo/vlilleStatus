/**
 * AngularJs - app.js
 */
angular.module('SharedServices', [])
    .config(function ($httpProvider) {
        $httpProvider.responseInterceptors.push('myHttpInterceptor');
        var spinnerFunction = function (data, headersGetter) {
            // todo start the spinner here
            return data;
        };
        $httpProvider.defaults.transformRequest.push(spinnerFunction);
    })
    // register the interceptor as a service, intercepts ALL angular ajax http calls
    .factory('myHttpInterceptor', function ($q, $window) {
        return function (promise) {
            return promise.then(function (response) {
                // do something on success
                // todo hide the spinner
                return response;

            }, function (response) {
                // do something on error
                // todo hide the spinner
                return $q.reject(response);
            });
        };
    });

angular.module('VlilleStatus', ['SharedServices'])
    .config(['$routeProvider','$locationProvider', function ($routeProvider, $locationProvider) {
        $routeProvider.
            when('/', { templateUrl: "assets/partials/stations.html", controller: StationsController}).
            otherwise({redirectTo: '/'});
    }]);

