var app = angular.module('NotebookBrowser', []);

function ListController($scope, $http) {

        $scope.getDataFromServer = function() {
                $http({
                        method : 'GET',
                        url : 'do/listFiles'
                }).success(function(data, status, headers, config) {
                        $scope.listFile = data;
                }).error(function(data, status, headers, config) {
                        // called asynchronously if an error occurs
                        // or server returns response with an error status.
                });
        };
};
