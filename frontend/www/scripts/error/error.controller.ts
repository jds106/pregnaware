/// <reference path="../references.ts" />

module error {
    'use strict';
    import WrappedUser = models.WrappedUser;

    export class ErrorController {
        constructor($scope: ErrorScope, $location: ng.ILocationService, errorService: services.ErrorService) {

            // Clear the error when the user clicks "return to home"
            $scope.clearError = () => errorService.clearError();

            // Set and clear the error message
            errorService.errorEvent(<services.ErrorHandler>{
                onErrorClear: () => {
                    $scope.errorDescription = null;
                    $scope.errorUri = null;
                    $scope.errorMsg = null;
                    $scope.mailStr = null;
                },

                onErrorSet: (url, description, error) => {
                    $scope.errorDescription = description;
                    $scope.errorUri = url;
                    $scope.errorMsg = error;

                    var subject = `[ERROR] Pregnaware UI Failure`;
                    var body =
                        `
Description:
${$scope.errorDescription}

URI:
${$scope.errorUri}

Detail:
${$scope.errorMsg}
            `;

                    $scope.mailStr =
                        "mailto:support@pregnaware.co.uk?subject=" + encodeURI(subject) + "&body=" + encodeURI(body);
                }
            });
        }
    }
}