/// <reference path="../references.ts" />
var utils;
(function (utils) {
    var CookieKeys = (function () {
        function CookieKeys() {
        }
        CookieKeys.EmailKey = "email";
        CookieKeys.SessionIdKey = "sessionId";
        return CookieKeys;
    })();
    utils.CookieKeys = CookieKeys;
})(utils || (utils = {}));
/// <reference path="../references.ts" />
var entities;
(function (entities) {
    'use strict';
})(entities || (entities = {}));
/// <reference path="../references.ts" />
var service;
(function (service) {
    'use strict';
    var CookieKeys = utils.CookieKeys;
    var FrontEndUrl = (function () {
        function FrontEndUrl() {
        }
        FrontEndUrl.getUrl = function (path, sessionId, userId) {
            if (sessionId === void 0) { sessionId = null; }
            if (userId === void 0) { userId = null; }
            var sessionPart = sessionId ? "sessionId=" + sessionId : "";
            var userIdPart = userId ? "userId=" + userId : "";
            return FrontEndUrl.devUrlRoot
                + path + (sessionId ? "?" + sessionPart : "")
                + (userId ? "&" + userIdPart : "");
        };
        FrontEndUrl.devUrlRoot = 'http://localhost:8601';
        FrontEndUrl.prodUrlRoot = '';
        return FrontEndUrl;
    })();
    var FrontEndSvc = (function () {
        function FrontEndSvc($http, $cookies) {
            this.$http = $http;
            this.$cookies = $cookies;
        }
        FrontEndSvc.prototype.getSessionId = function () {
            return this.$cookies.get(CookieKeys.SessionIdKey);
        };
        FrontEndSvc.prototype.login = function (loginRequest) {
            return this.$http.post(FrontEndUrl.getUrl('/FrontEndSvc/login'), loginRequest);
        };
        FrontEndSvc.prototype.logout = function () {
            return this.$http.post(FrontEndUrl.getUrl('/FrontEndSvc/logout', this.getSessionId()), {});
        };
        FrontEndSvc.prototype.newUser = function (newUserRequest) {
            return this.$http.put(FrontEndUrl.getUrl('/FrontEndSvc/user'), newUserRequest);
        };
        FrontEndSvc.prototype.getUser = function (userId) {
            if (userId === void 0) { userId = null; }
            return this.$http.get(FrontEndUrl.getUrl('/FrontEndSvc/user', this.getSessionId(), userId));
        };
        FrontEndSvc.prototype.getNames = function (userId) {
            if (userId === void 0) { userId = null; }
            return this.$http.get(FrontEndUrl.getUrl('/FrontEndSvc/NamingSvc/names', this.getSessionId(), userId));
        };
        FrontEndSvc.prototype.getDueDate = function (userId) {
            if (userId === void 0) { userId = null; }
            return this.$http.get(FrontEndUrl.getUrl('/FrontEndSvc/ProgressSvc/progress', this.getSessionId(), userId));
        };
        FrontEndSvc.prototype.putDueDate = function (dueDate, userId) {
            if (userId === void 0) { userId = null; }
            return this.$http.put(FrontEndUrl.getUrl('/FrontEndSvc/ProgressSvc/progress', this.getSessionId(), userId), dueDate);
        };
        FrontEndSvc.prototype.deleteDueDate = function (userId) {
            if (userId === void 0) { userId = null; }
            return this.$http.delete(FrontEndUrl.getUrl('/FrontEndSvc/ProgressSvc/progress', this.getSessionId(), userId));
        };
        return FrontEndSvc;
    })();
    service.FrontEndSvc = FrontEndSvc;
})(service || (service = {}));
/// <reference path="../references.ts" />
var service;
(function (service) {
    'use strict';
    var UserManagementSvc = (function () {
        function UserManagementSvc(frontend) {
            var _this = this;
            // The list of user-update handlers
            this.userListeners = [];
            this.frontend = frontend;
            this.frontend.getUser()
                .error(function (error) {
                console.error('Could not find user name', error);
            })
                .success(function (response) {
                _this.user = response;
                _this.userListeners.forEach(function (h) { return h(_this.user); });
            });
        }
        /** Allow clients to register for user-changed events */
        UserManagementSvc.prototype.userChangedEvent = function (handler) {
            this.userListeners.push(handler);
            if (this.user)
                handler(this.user);
        };
        Object.defineProperty(UserManagementSvc.prototype, "User", {
            /** Get the current user */
            get: function () {
                return this.user;
            },
            /** Update the current user (and broadcast to all listeners) */
            set: function (user) {
                this.user = user;
                this.userListeners.forEach(function (h) { return h(user); });
            },
            enumerable: true,
            configurable: true
        });
        return UserManagementSvc;
    })();
    service.UserManagementSvc = UserManagementSvc;
})(service || (service = {}));
/// <reference path="../references.ts" />
var controller;
(function (controller) {
    var CookieKeys = utils.CookieKeys;
    'use strict';
    var LoginController = (function () {
        function LoginController($cookies, $window, frontend) {
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
            this.$cookies = $cookies;
            this.$window = $window;
            this.frontend = frontend;
            var email = $cookies.get(CookieKeys.EmailKey);
            if (email) {
                this.loginRequest.email = email;
            }
        }
        LoginController.prototype.loginAction = function () {
            var _this = this;
            this.frontend.login(this.loginRequest)
                .error(function (error) {
                console.error('Login Error', error);
            })
                .success(function (sessionId) {
                console.error('Login sucess', sessionId);
                _this.$cookies.put(CookieKeys.EmailKey, _this.loginRequest.email);
                _this.$cookies.put(CookieKeys.SessionIdKey, sessionId);
                _this.$window.location.pathname = '/main';
            });
        };
        LoginController.prototype.newUserAction = function () {
            var _this = this;
            this.frontend.newUser(this.newUserRequest)
                .error(function (error) {
                console.error('New User Error', error);
            })
                .success(function (sessionId) {
                _this.$cookies.put(CookieKeys.EmailKey, _this.newUserRequest.email);
                _this.$cookies.put(CookieKeys.SessionIdKey, sessionId);
                _this.$window.location.pathname = '/main';
            });
        };
        return LoginController;
    })();
    controller.LoginController = LoginController;
})(controller || (controller = {}));
/// <reference path="../references.ts" />
var controller;
(function (controller) {
    'use strict';
    var BabyNamesController = (function () {
        function BabyNamesController(frontend, usermgmt) {
            var _this = this;
            this.frontend = frontend;
            this.usermgmt = usermgmt;
            this.usermgmt.userChangedEvent(function (user) {
                _this.user = user;
                _this.frontend.getNames(user.userId)
                    .error(function (error) { return console.error("Failed to get baby names", error); })
                    .success(function (response) { return _this.names = response; });
            });
        }
        Object.defineProperty(BabyNamesController.prototype, "NamesForBoys", {
            get: function () {
                return (this.names)
                    ? this.names.entries.filter(function (n) { return n.gender.toLowerCase() == "boy"; })
                    : null;
            },
            enumerable: true,
            configurable: true
        });
        Object.defineProperty(BabyNamesController.prototype, "NamesForGirls", {
            get: function () {
                return (this.names)
                    ? this.names.entries.filter(function (n) { return n.gender.toLowerCase() == "girl"; })
                    : null;
            },
            enumerable: true,
            configurable: true
        });
        Object.defineProperty(BabyNamesController.prototype, "CurrentNameGirl", {
            get: function () { return this.currentNameGirl; },
            set: function (newName) { this.currentNameGirl = newName; },
            enumerable: true,
            configurable: true
        });
        Object.defineProperty(BabyNamesController.prototype, "CurrentNameBoy", {
            get: function () { return this.currentNameBoy; },
            set: function (newName) { this.currentNameBoy = newName; },
            enumerable: true,
            configurable: true
        });
        BabyNamesController.prototype.AddCurrentNameGirl = function () { this.addCurrentName("girl", this.currentNameGirl); };
        BabyNamesController.prototype.AddCurrentNameBoy = function () { this.addCurrentName("boy", this.currentNameBoy); };
        BabyNamesController.prototype.addCurrentName = function (gender, name) {
        };
        return BabyNamesController;
    })();
    controller.BabyNamesController = BabyNamesController;
})(controller || (controller = {}));
/// <reference path="../references.ts" />
var controller;
(function (controller) {
    'use strict';
    var EnhancedProgressModel = (function () {
        function EnhancedProgressModel(model) {
            this.daysPassed = model.daysPassed;
            this.daysRemaining = model.daysRemaining;
            this.dueDate = model.dueDate;
        }
        Object.defineProperty(EnhancedProgressModel.prototype, "WeeksPassed", {
            get: function () {
                return Math.floor(this.daysPassed / 7);
            },
            enumerable: true,
            configurable: true
        });
        Object.defineProperty(EnhancedProgressModel.prototype, "WeeksRemaining", {
            get: function () {
                return Math.floor(this.daysRemaining / 7);
            },
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
    var PregnancyProgressController = (function () {
        function PregnancyProgressController($scope, frontend, usermgmt) {
            var _this = this;
            this.dueDatePickerOpen = false;
            this.dueDate = Date.now();
            this.$scope = $scope;
            this.frontend = frontend;
            this.usermgmt = usermgmt;
            this.usermgmt.userChangedEvent(function (user) {
                _this.setDueDate(user.userId);
            });
        }
        PregnancyProgressController.prototype.setDueDate = function (userId) {
            var _this = this;
            this.frontend.getDueDate(userId)
                .error(function (error) { return console.error('Progress Error', error); })
                .success(function (response) {
                _this.$scope.progress = new EnhancedProgressModel(response);
            });
        };
        Object.defineProperty(PregnancyProgressController.prototype, "DueDatePickerOpen", {
            get: function () {
                return this.dueDatePickerOpen;
            },
            set: function (isOpen) {
                this.dueDatePickerOpen = isOpen;
            },
            enumerable: true,
            configurable: true
        });
        Object.defineProperty(PregnancyProgressController.prototype, "DueDate", {
            get: function () {
                return this.dueDate;
            },
            set: function (newDate) {
                this.dueDate = newDate;
            },
            enumerable: true,
            configurable: true
        });
        PregnancyProgressController.prototype.UpdateDueDate = function () {
            var _this = this;
            var parsedDueDate = moment(this.dueDate);
            var asLocalDate = {
                year: parsedDueDate.year(),
                month: parsedDueDate.month() + 1,
                day: parsedDueDate.date()
            };
            this.frontend.putDueDate(asLocalDate)
                .error(function (error) { return console.error('Could not put due date', error); })
                .success(function (response) {
                console.log('Successfully set due date', response);
                _this.$scope.progress = new EnhancedProgressModel(response);
            });
        };
        PregnancyProgressController.prototype.ChangeDueDate = function () {
            var _this = this;
            this.frontend.deleteDueDate()
                .error(function (error) { return console.error('Could not put due date', error); })
                .success(function (response) {
                console.log('Successfully reset due date', response);
                _this.$scope.progress = null;
            });
        };
        return PregnancyProgressController;
    })();
    controller.PregnancyProgressController = PregnancyProgressController;
})(controller || (controller = {}));
/// <reference path="../references.ts" />
var controller;
(function (controller) {
    'use strict';
    var MainController = (function () {
        function MainController($window, frontend, usermgmt) {
            var _this = this;
            this.$window = $window;
            this.frontend = frontend;
            this.usermgmt = usermgmt;
            this.usermgmt.userChangedEvent(function (user) {
                _this.user = user;
            });
        }
        Object.defineProperty(MainController.prototype, "User", {
            get: function () {
                return this.user;
            },
            enumerable: true,
            configurable: true
        });
        MainController.prototype.LogoutAction = function () {
            var _this = this;
            this.frontend.logout()
                .error(function (error) {
                console.error("Unable to log out: " + error);
            })
                .success(function (response) {
                _this.$window.location.pathname = '/login';
            });
        };
        return MainController;
    })();
    controller.MainController = MainController;
})(controller || (controller = {}));
/// <reference path="typings/tsd.d.ts" />
/// <reference path="utils/cookiekeys.ts" />
/// <reference path="entities/userentities.ts" />
/// <reference path="service/frontendsvc.ts" />
/// <reference path="service/usermanagementsvc.ts" />
/// <reference path="controller/login.ts" />
/// <reference path="controller/babynames.ts" />
/// <reference path="controller/pregnancyprogress.ts" />
/// <reference path="controller/main.ts" />
/// <reference path="app.ts" /> 
/// <reference path="references.ts" />
var App;
(function (App) {
    // Register the app with Angular
    var app = angular.module('graviditate', ['ngCookies', 'ngAnimate', 'ui.bootstrap']);
    // The Pregnancy Progress control
    app.directive('pregnancyProgress', function () {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'html/snippets/pregnancyprogress.html',
            controller: controller.PregnancyProgressController,
            controllerAs: 'ctrl'
        };
    });
    // The Baby Names control
    app.directive('babyNames', function () {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'html/snippets/babynames.html',
            controller: controller.BabyNamesController,
            controllerAs: 'ctrl'
        };
    });
    // Register the services
    app.service('frontend', service.FrontEndSvc);
    app.service('usermgmt', service.UserManagementSvc);
    app.controller('LoginCtrl', controller.LoginController);
    app.controller('MainCtrl', controller.MainController);
})(App || (App = {}));
//# sourceMappingURL=app.js.map