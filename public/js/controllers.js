/**
 * AngularJS - controllers.
 */

function StationsController($scope, $http) {

    $scope.stations = null;

    /**
     * On load...
     */
    $http.get("api/stations").success(function(data) {
        $scope.stations = data;
    });
}
