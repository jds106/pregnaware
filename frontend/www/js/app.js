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
        FrontEndUrl.getUrl = function (path, sessionId) {
            if (sessionId === void 0) { sessionId = null; }
            var sessionPart = sessionId ? "sessionId=" + sessionId : "";
            return "/FrontEndSvc/" + path + (sessionId ? "?" + sessionPart : "");
        };
        return FrontEndUrl;
    })();
    // These are the ONLY status codes returned by the app
    var StatusCodeHandler = (function () {
        function StatusCodeHandler() {
            this.OK = 200;
            this.BadRequest = 400;
            this.NotFound = 404;
            this.Conflict = 409;
            this.ServiceUnavailable = 503;
        }
        return StatusCodeHandler;
    })();
    var FrontEndSvc = (function () {
        function FrontEndSvc($http, $cookies) {
            this.$http = $http;
            this.$cookies = $cookies;
        }
        FrontEndSvc.prototype.getSessionId = function () {
            return this.$cookies.get(CookieKeys.SessionIdKey);
        };
        /* ---- Login ---- */
        FrontEndSvc.prototype.login = function (loginRequest) {
            return this.$http.post(FrontEndUrl.getUrl('login'), loginRequest);
        };
        /* ---- User ---- */
        FrontEndSvc.prototype.getUser = function () {
            return this.$http.get(FrontEndUrl.getUrl('user', this.getSessionId()));
        };
        FrontEndSvc.prototype.newUser = function (addUserRequest) {
            return this.$http.post(FrontEndUrl.getUrl('user'), addUserRequest);
        };
        FrontEndSvc.prototype.editUser = function (editUserRequest) {
            return this.$http.put(FrontEndUrl.getUrl('user', this.getSessionId()), editUserRequest);
        };
        FrontEndSvc.prototype.addFriend = function (friendEmail) {
            return this.$http.put(FrontEndUrl.getUrl('user/friend', this.getSessionId()), friendEmail);
        };
        FrontEndSvc.prototype.deleteFriend = function (friendId) {
            return this.$http.delete(FrontEndUrl.getUrl('user/friend/' + friendId, this.getSessionId()));
        };
        /** Creates a link to the session created for the new friend */
        FrontEndSvc.prototype.getCreateFriendLink = function (urlRoot, sessionId) {
            return urlRoot + '/newfriend' + '?sessionId=' + sessionId;
        };
        /* ---- Progress ---- */
        FrontEndSvc.prototype.putDueDate = function (dueDate) {
            return this.$http.put(FrontEndUrl.getUrl('user/duedate', this.getSessionId()), dueDate);
        };
        FrontEndSvc.prototype.deleteDueDate = function () {
            return this.$http.delete(FrontEndUrl.getUrl('user/duedate', this.getSessionId()));
        };
        /* ---- Names ---- */
        FrontEndSvc.prototype.putName = function (name, isBoy, suggestedForUserId) {
            return this.$http.put(FrontEndUrl.getUrl('names/' + suggestedForUserId, this.getSessionId()), { name: name, isBoy: isBoy });
        };
        FrontEndSvc.prototype.deleteName = function (nameId) {
            return this.$http.delete(FrontEndUrl.getUrl('names/' + nameId, this.getSessionId()));
        };
        /* ---- State ---- */
        FrontEndSvc.prototype.getUserState = function () {
            return this.$http.get(FrontEndUrl.getUrl('user/state', this.getSessionId()));
        };
        FrontEndSvc.prototype.putUserState = function (state) {
            return this.$http.put(FrontEndUrl.getUrl('user/state', this.getSessionId()), state);
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
            // The list of user-set handlers
            this.userListeners = [];
            // The list of handlers called when the viewed user is changed
            this.selectedFriendListeners = [];
            this.frontend = frontend;
            this.frontend.getUser()
                .error(function (error) { return console.error('Could not find user name', error); })
                .success(function (response) { return _this.User = response; });
        }
        /** Allow clients to register for the user-set notification (happens once) */
        UserManagementSvc.prototype.userSetEvent = function (handler) {
            this.userListeners.push(handler);
            if (this.user)
                handler(this.user);
        };
        Object.defineProperty(UserManagementSvc.prototype, "User", {
            /** Update the current user (and broadcast to all listeners) */
            set: function (user) {
                if (this.user) {
                    throw new Error("Cannot change user once logged in");
                }
                else {
                    this.user = user;
                    this.userListeners.forEach(function (h) { return h(user); });
                }
            },
            enumerable: true,
            configurable: true
        });
        /** Register a client to subscribe to the viewed user being changed */
        UserManagementSvc.prototype.friendSelectedEvent = function (handler) {
            this.selectedFriendListeners.push(handler);
            if (this.selectedFriend)
                handler(this.selectedFriend);
        };
        Object.defineProperty(UserManagementSvc.prototype, "Friend", {
            /** Update the current user (and broadcast to all listeners) */
            set: function (friend) {
                this.selectedFriend = friend;
                this.selectedFriendListeners.forEach(function (h) { return h(friend); });
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
        function BabyNamesController($scope, frontend, usermgmt) {
            var _this = this;
            this.$scope = $scope;
            this.frontend = frontend;
            this.usermgmt = usermgmt;
            this.usermgmt.userSetEvent(function (user) {
                _this.user = user;
                _this.$scope.viewedUser = _this.user.displayName;
                _this.$scope.canEdit = true;
                _this.$scope.boysNames = _this.user.babyNames.filter(function (n) { return n.isBoy; });
                _this.$scope.girlsNames = _this.user.babyNames.filter(function (n) { return !n.isBoy; });
            });
            this.usermgmt.friendSelectedEvent(function (friend) {
                var babyNames;
                if (friend == null) {
                    _this.$scope.viewedUser = _this.user.displayName;
                    _this.$scope.canEdit = true;
                    babyNames = _this.user.babyNames;
                }
                else {
                    _this.$scope.viewedUser = friend.displayName;
                    _this.$scope.canEdit = false;
                    babyNames = friend.babyNames;
                }
                _this.selectedFriend = friend;
                _this.$scope.boysNames = babyNames.filter(function (n) { return n.isBoy; });
                _this.$scope.girlsNames = babyNames.filter(function (n) { return !n.isBoy; });
            });
        }
        BabyNamesController.prototype.AddCurrentNameGirl = function () {
            var _this = this;
            var suggestedForUserId = this.selectedFriend ? this.selectedFriend.userId : this.user.userId;
            this.frontend.putName(this.$scope.currentNameGirl, false, suggestedForUserId)
                .error(function (error) { return console.error("Failed to add girl's name", error); })
                .success(function (response) {
                _this.$scope.girlsNames.push(response);
                _this.$scope.currentNameGirl = "";
            });
        };
        BabyNamesController.prototype.AddCurrentNameBoy = function () {
            var _this = this;
            var suggestedForUserId = this.selectedFriend ? this.selectedFriend.userId : this.user.userId;
            this.frontend.putName(this.$scope.currentNameBoy, true, suggestedForUserId)
                .error(function (error) { return console.error("Failed to add boy's name", error); })
                .success(function (response) {
                _this.$scope.boysNames.push(response);
                _this.$scope.currentNameBoy = "";
            });
        };
        BabyNamesController.prototype.DeleteName = function (entry) {
            var _this = this;
            this.frontend.deleteName(entry.nameId)
                .error(function (error) { return console.error("Failed to remove name", error); })
                .success(function (response) {
                if (entry.isBoy) {
                    _this.$scope.boysNames = _this.$scope.boysNames.filter(function (e) { return e != entry; });
                }
                else {
                    _this.$scope.girlsNames = _this.$scope.girlsNames.filter(function (e) { return e != entry; });
                }
            });
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
        function EnhancedProgressModel(dueDate) {
            this.gestationPeriod = moment.duration({ days: 280 });
            // Note handling of zero-index months
            this.dueDate = moment().year(dueDate.year).month(dueDate.month - 1).date(dueDate.day);
            var conceptionDate = this.dueDate.clone().subtract(this.gestationPeriod);
            var today = moment();
            this.daysPassed = today.diff(conceptionDate, 'days');
            this.daysRemaining = this.dueDate.diff(today, 'days');
        }
        Object.defineProperty(EnhancedProgressModel.prototype, "weeksPassed", {
            get: function () {
                return Math.floor(this.daysPassed / 7);
            },
            enumerable: true,
            configurable: true
        });
        Object.defineProperty(EnhancedProgressModel.prototype, "weeksRemaining", {
            get: function () {
                return Math.floor(this.daysRemaining / 7);
            },
            enumerable: true,
            configurable: true
        });
        Object.defineProperty(EnhancedProgressModel.prototype, "formattedDueDate", {
            get: function () {
                return this.dueDate.format("LL");
            },
            enumerable: true,
            configurable: true
        });
        return EnhancedProgressModel;
    })();
    var PregnancyProgressController = (function () {
        function PregnancyProgressController($scope, frontend, usermgmt) {
            var _this = this;
            this.$scope = $scope;
            this.frontend = frontend;
            this.usermgmt = usermgmt;
            this.$scope.dueDatePickerOpen = false;
            this.$scope.dueDate = Date.now();
            this.usermgmt.userSetEvent(function (user) {
                _this.user = user;
                _this.$scope.viewedUser = _this.user.displayName;
                _this.$scope.canEdit = true;
                _this.$scope.progress =
                    _this.user.dueDate
                        ? new EnhancedProgressModel(_this.user.dueDate)
                        : null;
            });
            this.usermgmt.friendSelectedEvent(function (friend) {
                if (friend == null) {
                    _this.$scope.viewedUser = _this.user.displayName;
                    _this.$scope.canEdit = true;
                    _this.$scope.progress =
                        _this.user.dueDate
                            ? new EnhancedProgressModel(_this.user.dueDate)
                            : null;
                }
                else {
                    _this.$scope.viewedUser = friend.displayName;
                    _this.$scope.canEdit = false;
                    _this.$scope.progress = friend.dueDate
                        ? new EnhancedProgressModel(friend.dueDate)
                        : null;
                }
                _this.selectedFriend = friend;
            });
        }
        PregnancyProgressController.prototype.UpdateDueDate = function (dueDate) {
            var _this = this;
            var parsedDueDate = moment(dueDate);
            var asLocalDate = {
                year: parsedDueDate.year(),
                month: parsedDueDate.month() + 1,
                day: parsedDueDate.date()
            };
            this.frontend.putDueDate(asLocalDate)
                .error(function (error) { return console.error('Could not put due date', error); })
                .success(function (response) {
                _this.$scope.progress = new EnhancedProgressModel(response);
            });
        };
        PregnancyProgressController.prototype.ChangeDueDate = function () {
            var _this = this;
            this.frontend.deleteDueDate()
                .error(function (error) { return console.error('Could not put due date', error); })
                .success(function (response) {
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
    var CookieKeys = utils.CookieKeys;
    var NavBarController = (function () {
        function NavBarController($cookies, $window, frontend, usermgmt) {
            var _this = this;
            this.$cookies = $cookies;
            this.$window = $window;
            this.frontend = frontend;
            this.usermgmt = usermgmt;
            this.usermgmt.userSetEvent(function (user) { return _this.user = user; });
        }
        Object.defineProperty(NavBarController.prototype, "User", {
            get: function () {
                return this.user;
            },
            enumerable: true,
            configurable: true
        });
        NavBarController.prototype.Logout = function () {
            this.$cookies.remove(CookieKeys.SessionIdKey);
            this.$window.location.pathname = '/login';
        };
        return NavBarController;
    })();
    controller.NavBarController = NavBarController;
})(controller || (controller = {}));
/// <reference path="../references.ts" />
var controller;
(function (controller) {
    'use strict';
    var AccountController = (function () {
        function AccountController($scope, $window, frontend, usermgmt) {
            var _this = this;
            this.$scope = $scope;
            this.$window = $window;
            this.frontend = frontend;
            this.usermgmt = usermgmt;
            this.$scope.password = "";
            this.$scope.confirmPassword = "";
            this.$scope.passwordMatch = false;
            this.$scope.passwordMismatch = false;
            this.$scope.$watch('password', function () { return AccountController.handlePasswordChange($scope); });
            this.$scope.$watch('confirmPassword', function () { return AccountController.handlePasswordChange($scope); });
            this.usermgmt.userSetEvent(function (user) {
                _this.user = user;
                _this.$scope.displayName = _this.user.displayName;
                _this.$scope.email = _this.user.email;
            });
        }
        AccountController.handlePasswordChange = function (scope) {
            if (scope.password != "") {
                scope.passwordMatch = scope.password == scope.confirmPassword;
                scope.passwordMismatch = scope.password != scope.confirmPassword;
            }
            else {
                scope.passwordMatch = false;
                scope.passwordMismatch = false;
            }
        };
        AccountController.prototype.saveChanges = function () {
            var _this = this;
            var editUserRequest = {
                displayName: (this.$scope.displayName != this.user.displayName) ? this.$scope.displayName : null,
                email: (this.$scope.email != this.user.email) ? this.$scope.email : null,
                password: (this.$scope.password != "") ? this.$scope.password : null
            };
            this.frontend.editUser(editUserRequest)
                .error(function (error) { return console.error("Failed to edit user", error); })
                .success(function (updatedUser) {
                _this.usermgmt.User = updatedUser;
                _this.$window.location.pathname = "/main";
            });
        };
        return AccountController;
    })();
    controller.AccountController = AccountController;
})(controller || (controller = {}));
/// <reference path="../references.ts" />
var controller;
(function (controller) {
    'use strict';
    var NewFriendController = (function () {
        function NewFriendController($scope, $window, frontend, usermgmt) {
            var _this = this;
            this.$scope = $scope;
            this.$window = $window;
            this.frontend = frontend;
            this.usermgmt = usermgmt;
            // TODO: Extract session from the URL parameter
            this.$scope.password = "";
            this.$scope.confirmPassword = "";
            this.$scope.passwordMatch = false;
            this.$scope.passwordMismatch = false;
            this.$scope.$watch('password', function () { return NewFriendController.handlePasswordChange($scope); });
            this.$scope.$watch('confirmPassword', function () { return NewFriendController.handlePasswordChange($scope); });
            this.usermgmt.userSetEvent(function (user) {
                _this.user = user;
                _this.$scope.displayName = _this.user.displayName;
                _this.$scope.email = _this.user.email;
            });
        }
        NewFriendController.handlePasswordChange = function (scope) {
            if (scope.password != "") {
                scope.passwordMatch = scope.password == scope.confirmPassword;
                scope.passwordMismatch = scope.password != scope.confirmPassword;
            }
            else {
                scope.passwordMatch = false;
                scope.passwordMismatch = false;
            }
        };
        NewFriendController.prototype.saveChanges = function () {
            var _this = this;
            var editUserRequest = {
                displayName: (this.$scope.displayName != this.user.displayName) ? this.$scope.displayName : null,
                email: (this.$scope.email != this.user.email) ? this.$scope.email : null,
                password: (this.$scope.password != "") ? this.$scope.password : null
            };
            this.frontend.editUser(editUserRequest)
                .error(function (error) { return console.error("Failed to edit user", error); })
                .success(function (updatedUser) {
                _this.usermgmt.User = updatedUser;
                _this.$window.location.pathname = "/main";
            });
        };
        return NewFriendController;
    })();
    controller.NewFriendController = NewFriendController;
})(controller || (controller = {}));
/// <reference path="../references.ts" />
var controller;
(function (controller) {
    'use strict';
    var ShareController = (function () {
        function ShareController($scope, $window, frontend, usermgmt) {
            var _this = this;
            this.$scope = $scope;
            this.$window = $window;
            this.frontend = frontend;
            this.usermgmt = usermgmt;
            this.$scope.showExistingUserSuccess = false;
            this.$scope.showNewUserSuccess = false;
            this.usermgmt.userSetEvent(function (user) {
                _this.user = user;
            });
        }
        return ShareController;
    })();
    controller.ShareController = ShareController;
})(controller || (controller = {}));
/// <reference path="../references.ts" />
var controller;
(function (controller) {
    'use strict';
    var MainController = (function () {
        function MainController() {
        }
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
/// <reference path="controller/navbar.ts" />
/// <reference path="controller/account.ts" />
/// <reference path="controller/newfriend.ts" />
/// <reference path="controller/share.ts" />
/// <reference path="controller/main.ts" />
/// <reference path="app.ts" /> 
/// <reference path="references.ts" />
var App;
(function (App) {
    // Register the app with Angular
    var app = angular.module('graviditate', ['ngCookies', 'ngAnimate', 'ui.bootstrap']);
    // The common nav bar
    app.directive('navBar', function () {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: '/html/snippets/navbar.html',
            controller: controller.NavBarController,
            controllerAs: 'ctrl',
            scope: {
                navBarScope: '=navBar'
            }
        };
    });
    // The common footer
    app.directive('commonFooter', function () {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: '/html/snippets/footer.html'
        };
    });
    // The Pregnancy Progress control
    app.directive('pregnancyProgress', function () {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: '/html/snippets/pregnancyprogress.html',
            controller: controller.PregnancyProgressController,
            controllerAs: 'ctrl',
            scope: {
                pregnancyProgressScope: '=pregnancyProgress'
            }
        };
    });
    // The Baby Names control
    app.directive('babyNames', function () {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: '/html/snippets/babynames.html',
            controller: controller.BabyNamesController,
            controllerAs: 'ctrl',
            scope: {
                babyNamesScope: '=babyNames'
            }
        };
    });
    // Register the services
    app.service('frontend', service.FrontEndSvc);
    app.service('usermgmt', service.UserManagementSvc);
    app.controller('LoginCtrl', controller.LoginController);
    app.controller('MainCtrl', controller.MainController);
    app.controller('AccountCtrl', controller.AccountController);
    app.controller('NewFriendCtrl', controller.NewFriendController);
    app.controller('ShareCtrl', controller.ShareController);
})(App || (App = {}));
//# sourceMappingURL=app.js.map