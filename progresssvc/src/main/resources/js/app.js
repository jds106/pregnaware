/// <reference path="../references.ts" />
var Utils;
(function (Utils) {
    var WebRoot = (function () {
        function WebRoot() {
        }
        WebRoot.Url = function (path, sessionId) {
            if (sessionId === void 0) { sessionId = null; }
            return this.devUrlRoot + path + (sessionId ? "?" + WebRoot.SessionIdKey + "=" + sessionId : "");
        };
        /** The cookie key used to store the current session */
        WebRoot.SessionIdKey = "sessionId";
        WebRoot.devUrlRoot = 'http://localhost:8601';
        WebRoot.prodUrlRoot = '';
        return WebRoot;
    })();
    Utils.WebRoot = WebRoot;
})(Utils || (Utils = {}));
/// <reference path="../references.ts" />
var Controller;
(function (Controller) {
    'use strict';
})(Controller || (Controller = {}));
/// <reference path="../references.ts" />
var Controller;
(function (Controller) {
    'use strict';
    var WebRoot = Utils.WebRoot;
    var LoginController = (function () {
        function LoginController($http, $cookies, $window) {
            // Current user information populated in login.html
            this.loginRequest = {
                email: "",
                password: "",
                rememberUser: false
            };
            this.newUserRequest = {
                displayName: "",
                email: "",
                password: ""
            };
            this.$http = $http;
            this.$cookies = $cookies;
            this.$window = $window;
            var email = $cookies.get(LoginController.EmailKey);
            if (email) {
                this.loginRequest.email = email;
            }
        }
        LoginController.prototype.loginAction = function () {
            var _this = this;
            this.$http.post(WebRoot.Url('/FrontEndSvc/login'), this.loginRequest)
                .error(function (error) {
                console.error('Login Error', error);
            })
                .success(function (sessionId) {
                console.error('Login sucess', sessionId);
                _this.$cookies.put(LoginController.EmailKey, _this.loginRequest.email);
                _this.$cookies.put(WebRoot.SessionIdKey, sessionId);
                _this.$window.location.pathname = '/main';
            });
        };
        LoginController.prototype.newUserAction = function () {
            var _this = this;
            this.$http.put(WebRoot.Url('/FrontEndSvc/newUser'), this.newUserRequest)
                .error(function (error) {
                console.error('New User Error', error);
            })
                .success(function (sessionId) {
                _this.$cookies.put(LoginController.EmailKey, _this.newUserRequest.email);
                _this.$cookies.put(WebRoot.SessionIdKey, sessionId);
                _this.$window.location.pathname = '/main';
            });
        };
        LoginController.EmailKey = "email";
        return LoginController;
    })();
    Controller.LoginController = LoginController;
})(Controller || (Controller = {}));
/// <reference path="../references.ts" />
var Controller;
(function (Controller) {
    'use strict';
    var WebRoot = Utils.WebRoot;
    var EnhancedProgressModel = (function () {
        function EnhancedProgressModel(model) {
            this.daysPassed = model.daysPassed;
            this.daysRemaining = model.daysRemaining;
            this.dueDate = model.dueDate;
        }
        Object.defineProperty(EnhancedProgressModel.prototype, "WeeksPassed", {
            get: function () { return Math.floor(this.daysPassed / 7); },
            enumerable: true,
            configurable: true
        });
        Object.defineProperty(EnhancedProgressModel.prototype, "WeeksRemaining", {
            get: function () { return Math.floor(this.daysRemaining / 7); },
            enumerable: true,
            configurable: true
        });
        Object.defineProperty(EnhancedProgressModel.prototype, "FormattedDueDate", {
            get: function () {
                // Format the due date - handling the zero-index months
                var asMomentDate = moment()
                    .year(this.dueDate.year).month(this.dueDate.month - 1).date(this.dueDate.day);
                return asMomentDate.format("LL");
            },
            enumerable: true,
            configurable: true
        });
        return EnhancedProgressModel;
    })();
    var MainController = (function () {
        function MainController($http, $cookies, $window, $scope) {
            // Due date selection logic
            this.dueDatePickerOpen = false;
            this.dueDate = Date.now();
            this.$http = $http;
            this.$cookies = $cookies;
            this.$window = $window;
            this.$scope = $scope;
            this.NamesAction();
            this.DueDateAction();
        }
        Object.defineProperty(MainController.prototype, "SessionId", {
            get: function () {
                return this.$cookies.get(WebRoot.SessionIdKey);
            },
            enumerable: true,
            configurable: true
        });
        MainController.prototype.NamesAction = function () {
            var _this = this;
            this.$http.get(WebRoot.Url('/FrontEndSvc/NamingSvc/names', this.SessionId))
                .error(function (error) {
                console.error('Names Error', error);
            })
                .success(function (result) {
                _this.names = result;
            });
        };
        MainController.prototype.DueDateAction = function () {
            var _this = this;
            this.$http.get(WebRoot.Url('/FrontEndSvc/ProgressSvc/progress', this.SessionId))
                .error(function (error) {
                console.error('Progress Error', error);
            })
                .success(function (response) {
                _this.progressModel = new EnhancedProgressModel(JSON.parse(response));
            });
        };
        Object.defineProperty(MainController.prototype, "Names", {
            get: function () {
                return this.names;
            },
            enumerable: true,
            configurable: true
        });
        Object.defineProperty(MainController.prototype, "Progress", {
            get: function () {
                return this.progressModel;
            },
            enumerable: true,
            configurable: true
        });
        Object.defineProperty(MainController.prototype, "DueDatePickerOpen", {
            get: function () { return this.dueDatePickerOpen; },
            set: function (isOpen) { this.dueDatePickerOpen = isOpen; },
            enumerable: true,
            configurable: true
        });
        Object.defineProperty(MainController.prototype, "DueDate", {
            get: function () { return this.dueDate; },
            set: function (newDate) { this.dueDate = newDate; },
            enumerable: true,
            configurable: true
        });
        MainController.prototype.UpdateDueDate = function () {
            var _this = this;
            var parsedDueDate = moment(this.dueDate);
            var asLocalDate = {
                year: parsedDueDate.year(),
                month: parsedDueDate.month() + 1,
                day: parsedDueDate.date()
            };
            console.log("Setting due date to: " + asLocalDate);
            this.$http.put(WebRoot.Url('/FrontEndSvc/ProgressSvc/progress', this.SessionId), asLocalDate)
                .error(function (error) {
                console.error('Could not put due date', error);
            })
                .success(function (response) {
                console.log('Successfully set due date', response);
                _this.progressModel = new EnhancedProgressModel(JSON.parse(response));
            });
        };
        MainController.prototype.ChangeDueDate = function () {
            var _this = this;
            this.$http.delete(WebRoot.Url('/FrontEndSvc/ProgressSvc/progress', this.SessionId))
                .error(function (error) {
                console.error('Could not put due date', error);
            })
                .success(function (response) {
                console.log('Successfully reset due date', response);
                _this.progressModel = null;
            });
        };
        return MainController;
    })();
    Controller.MainController = MainController;
})(Controller || (Controller = {}));
/// <reference path="typings/tsd.d.ts" />
/// <reference path="utils/webroot.ts" />
/// <reference path="entities/userentities.ts" />
/// <reference path="controller/login.ts" />
/// <reference path="controller/main.ts" />
/// <reference path="app.ts" /> 
/// <reference path="references.ts" />
var App;
(function (App) {
    // Register the app with Angular
    var app = angular.module('graviditate', ['ngCookies', 'ngAnimate', 'ui.bootstrap']);
    // Render if due date available
    app.directive('dueDateSet', function () {
        return { restrict: 'E', replace: true, templateUrl: 'html/snippets/DueDateSet.html' };
    });
    // Render if due date missing
    app.directive('dueDateMissing', function () {
        return { restrict: 'E', replace: true, templateUrl: 'html/snippets/DueDateMissing.html' };
    });
    app.controller('LoginCtrl', Controller.LoginController);
    app.controller('MainCtrl', Controller.MainController);
})(App || (App = {}));
//# sourceMappingURL=app.js.map