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
            return "/FrontEndSvc/" + path + (sessionId ? "?" + sessionPart : "")
                + (userId ? "&" + userIdPart : "");
        };
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
        /* ---- Login / logout ---- */
        FrontEndSvc.prototype.login = function (loginRequest) {
            return this.$http.post(FrontEndUrl.getUrl('login'), loginRequest);
        };
        FrontEndSvc.prototype.logout = function () {
            return this.$http.post(FrontEndUrl.getUrl('logout', this.getSessionId()), {});
        };
        /* ---- User ---- */
        FrontEndSvc.prototype.newUser = function (newUserRequest) {
            return this.$http.put(FrontEndUrl.getUrl('user'), newUserRequest);
        };
        FrontEndSvc.prototype.getUser = function (userId) {
            if (userId === void 0) { userId = null; }
            return this.$http.get(FrontEndUrl.getUrl('user', this.getSessionId(), userId));
        };
        FrontEndSvc.prototype.editUser = function (editUserRequest) {
            return this.$http.put(FrontEndUrl.getUrl('editUser', this.getSessionId()), editUserRequest);
        };
        FrontEndSvc.prototype.findUser = function (email) {
            return this.$http.get(FrontEndUrl.getUrl('UserSvc/findUser/' + email, this.getSessionId()));
        };
        FrontEndSvc.prototype.addFriend = function (friend) {
            return this.$http.put(FrontEndUrl.getUrl('friend', this.getSessionId()), friend);
        };
        FrontEndSvc.prototype.createFriend = function (friendEmail) {
            return this.$http.put(FrontEndUrl.getUrl('createFriend', this.getSessionId()), { email: friendEmail });
        };
        /** Creates a link to the session created for the new friend */
        FrontEndSvc.prototype.getCreateFriendLink = function (urlRoot, sessionId) {
            return urlRoot + '/newfriend' + '?sessionId=' + sessionId;
        };
        /* ---- Progress ---- */
        FrontEndSvc.prototype.getDueDate = function (userId) {
            return this.$http.get(FrontEndUrl.getUrl('ProgressSvc/progress', this.getSessionId(), userId));
        };
        FrontEndSvc.prototype.putDueDate = function (dueDate, userId) {
            return this.$http.put(FrontEndUrl.getUrl('ProgressSvc/progress', this.getSessionId(), userId), dueDate);
        };
        FrontEndSvc.prototype.deleteDueDate = function (userId) {
            return this.$http.delete(FrontEndUrl.getUrl('ProgressSvc/progress', this.getSessionId(), userId));
        };
        /* ---- Names ---- */
        FrontEndSvc.prototype.getNames = function (userId) {
            return this.$http.get(FrontEndUrl.getUrl('NamingSvc/names', this.getSessionId(), userId));
        };
        FrontEndSvc.prototype.putName = function (name, gender, userId) {
            return this.$http.put(FrontEndUrl.getUrl('NamingSvc/name', this.getSessionId(), userId), { nameId: -1, name: name, gender: gender, suggestedByUserId: userId });
        };
        FrontEndSvc.prototype.deleteName = function (nameId, userId) {
            return this.$http.delete(FrontEndUrl.getUrl('NamingSvc/name/' + nameId, this.getSessionId(), userId));
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
            this.viewedUserListeners = [];
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
                    // Also update the viewed user to be the same as the current user
                    this.ViewedUser = user;
                }
            },
            enumerable: true,
            configurable: true
        });
        /** Register a client to subscribe to the viewed user being changed */
        UserManagementSvc.prototype.viewedUserChangedEvent = function (handler) {
            this.viewedUserListeners.push(handler);
            if (this.viewedUser)
                handler(this.viewedUser);
        };
        Object.defineProperty(UserManagementSvc.prototype, "ViewedUser", {
            /** Update the current user (and broadcast to all listeners) */
            set: function (viewedUser) {
                this.viewedUser = viewedUser;
                this.viewedUserListeners.forEach(function (h) { return h(viewedUser); });
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
    var EnhancedNamingEntry = (function () {
        function EnhancedNamingEntry(entry, user) {
            this.nameId = entry.nameId;
            this.name = entry.name;
            this.gender = entry.gender;
            this.suggestedByUserId = entry.suggestedByUserId;
            if (entry.suggestedByUserId == user.userId) {
                this.suggestedBy = user.displayName;
            }
            else {
                this.suggestedBy = "UNKNOWN";
                for (var friendIndex = 0; friendIndex < user.friends.length; friendIndex++) {
                    var friend = user.friends[friendIndex];
                    if (friend.userId == entry.suggestedByUserId) {
                        this.suggestedBy = friend.displayName;
                        break;
                    }
                }
            }
        }
        return EnhancedNamingEntry;
    })();
    var BabyNamesController = (function () {
        function BabyNamesController($scope, frontend, usermgmt) {
            var _this = this;
            this.$scope = $scope;
            this.frontend = frontend;
            this.usermgmt = usermgmt;
            this.usermgmt.userSetEvent(function (user) {
                _this.user = user;
            });
            this.usermgmt.viewedUserChangedEvent(function (user) {
                _this.$scope.viewedUser = user;
                // Can only edit when the logged-in user is the same as the viewed user
                _this.$scope.canEdit = _this.user.userId == user.userId;
                _this.frontend.getNames(user.userId)
                    .error(function (error) { return console.error("Failed to get baby names", error); })
                    .success(function (response) {
                    var enhancedEntries = response.entries.map(function (e) { return new EnhancedNamingEntry(e, user); });
                    _this.$scope.boysNames = enhancedEntries.filter(function (n) { return n.gender.toLowerCase() == "boy"; });
                    _this.$scope.girlsNames = enhancedEntries.filter(function (n) { return n.gender.toLowerCase() == "girl"; });
                });
            });
        }
        BabyNamesController.prototype.AddCurrentNameGirl = function () {
            var _this = this;
            this.frontend.putName(this.$scope.currentNameGirl, "girl", this.user.userId)
                .error(function (error) { return console.error("Failed to add girl's name", error); })
                .success(function (response) {
                _this.$scope.girlsNames.push(new EnhancedNamingEntry(response, _this.user));
                _this.$scope.currentNameGirl = "";
            });
        };
        BabyNamesController.prototype.AddCurrentNameBoy = function () {
            var _this = this;
            this.frontend.putName(this.$scope.currentNameBoy, "boy", this.user.userId)
                .error(function (error) { return console.error("Failed to add boy's name", error); })
                .success(function (response) {
                _this.$scope.boysNames.push(new EnhancedNamingEntry(response, _this.user));
                _this.$scope.currentNameBoy = "";
            });
        };
        BabyNamesController.prototype.DeleteName = function (entry) {
            var _this = this;
            this.frontend.deleteName(entry.nameId, this.user.userId)
                .error(function (error) { return console.error("Failed to remove name", error); })
                .success(function (response) {
                if (entry.gender == "boy") {
                    _this.$scope.boysNames = _this.$scope.boysNames.filter(function (e) { return e != entry; });
                }
                else if (entry.gender == "girl") {
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
        function EnhancedProgressModel(model) {
            this.daysPassed = model.daysPassed;
            this.daysRemaining = model.daysRemaining;
            this.dueDate = model.dueDate;
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
            this.$scope = $scope;
            this.frontend = frontend;
            this.usermgmt = usermgmt;
            this.$scope.dueDatePickerOpen = false;
            this.$scope.dueDate = Date.now();
            this.usermgmt.userSetEvent(function (user) {
                _this.user = user;
            });
            this.usermgmt.viewedUserChangedEvent(function (user) {
                _this.$scope.viewedUser = user;
                // Can only edit when the logged-in user is the same as the viewed user
                _this.$scope.canEdit = _this.user.userId == user.userId;
                _this.frontend.getDueDate(user.userId)
                    .error(function (error) { return _this.$scope.progress = null; })
                    .success(function (response) {
                    _this.$scope.progress = new EnhancedProgressModel(response);
                });
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
            this.frontend.putDueDate(asLocalDate, this.user.userId)
                .error(function (error) { return console.error('Could not put due date', error); })
                .success(function (response) {
                _this.$scope.progress = new EnhancedProgressModel(response);
            });
        };
        PregnancyProgressController.prototype.ChangeDueDate = function () {
            var _this = this;
            this.frontend.deleteDueDate(this.user.userId)
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
            var _this = this;
            this.frontend.logout()
                .error(function (error) {
                console.error("Unable to log out: " + error);
            })
                .success(function (response) {
                _this.$cookies.remove(CookieKeys.SessionIdKey);
                _this.$window.location.pathname = '/login';
            });
        };
        NavBarController.prototype.ViewUser = function (userId) {
            var _this = this;
            if (userId === void 0) { userId = null; }
            this.frontend.getUser(userId)
                .error(function (error) { return console.error("Could not find user: " + userId); })
                .success(function (user) { return _this.usermgmt.ViewedUser = user; });
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
        ShareController.prototype.Share = function (email) {
            var _this = this;
            // Reset previous values
            this.$scope.friendDisplayName = null;
            this.$scope.mailToLink = null;
            this.$scope.showExistingUserSuccess = false;
            this.$scope.showNewUserSuccess = false;
            this.frontend.findUser(email)
                .error(function (error) { return _this.shareNew(email); })
                .success(function (response) { return _this.shareExisting(response); });
        };
        ShareController.prototype.shareExisting = function (existingUser) {
            var _this = this;
            this.$scope.friendDisplayName = existingUser.displayName;
            this.frontend.addFriend(existingUser)
                .error(function (error) { return console.error("Could not add new user as a friend", error); })
                .success(function (response) {
                _this.$scope.friendEmail = null;
                _this.$scope.showExistingUserSuccess = true;
            });
        };
        ShareController.prototype.shareNew = function (newUserEmail) {
            var _this = this;
            this.frontend.createFriend(newUserEmail)
                .error(function (error) { return console.error("Could not add new user as a friend", newUserEmail); })
                .success(function (sessionId) {
                _this.$scope.friendEmail = null;
                _this.$scope.showNewUserSuccess = true;
                var urlRoot = _this.$window.location.protocol + "//" + _this.$window.location.host;
                _this.$scope.mailToLink =
                    ShareController.makeMailTo(_this.user, newUserEmail, _this.frontend.getCreateFriendLink(urlRoot, sessionId));
            });
        };
        ShareController.makeMailTo = function (user, newUserEmail, sessionLink) {
            var subject = "Your friend " + user.displayName + " would like you to share her pregnancy progress!";
            var body = "Hi,\n\n            You have been invited to share in your friend's pregnancy on Pregnaware!\n\n            Click this link to register and start sharing in her growing anticipation:\n                " + sessionLink + "\n\n            Thanks,\n            " + user.displayName + "\n            ";
            return "mailto:" + newUserEmail + "?subject=" + encodeURI(subject) + "&body=" + encodeURI(body);
        };
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