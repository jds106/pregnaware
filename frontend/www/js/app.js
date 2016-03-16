var error;
(function (error) {
    'use strict';
})(error || (error = {}));
/// <reference path="../references.ts" />
var error;
(function (error) {
    'use strict';
    var ErrorController = (function () {
        function ErrorController($scope, $location, routeService) {
            var _this = this;
            this.$scope = $scope;
            this.$location = $location;
            this.routeService = routeService;
            this.$scope.loginPage = function () { return _this.routeService.loginPage(); };
            this.$scope.errorDescription = decodeURI($location.search().description);
            this.$scope.errorUri = decodeURI($location.search().uri);
            this.$scope.errorMsg = decodeURI($location.search().msg);
            var subject = "[ERROR] Pregnaware UI Failure";
            var body = "\nDescription:\n" + this.$scope.errorDescription + "\n\nURI:\n" + this.$scope.errorUri + "\n\nDetail:\n" + this.$scope.errorMsg + "\n            ";
            this.$scope.mailStr =
                "mailto:support@pregnaware.co.uk?subject=" + encodeURI(subject) + "&body=" + encodeURI(body);
        }
        return ErrorController;
    })();
    error.ErrorController = ErrorController;
})(error || (error = {}));
/// <reference path="../references.ts" />
var services;
(function (services) {
    'use strict';
    var RouteConfig = (function () {
        function RouteConfig($routeProvider, $locationProvider) {
            $routeProvider.when('/login', {
                templateUrl: function () { return '/scripts/login/login.view.html'; },
                controller: login.LoginController
            });
            $routeProvider.when('/main', {
                templateUrl: '/scripts/main/main.view.html',
                controller: main.MainController
            });
            $routeProvider.when('/error', {
                templateUrl: '/scripts/error/error.view.html',
                controller: error.ErrorController
            });
            $routeProvider.otherwise({
                redirectTo: '/main'
            });
            // The server rewrites all URLs to point at the index.html, so enable HTML5 for pretty URLs
            $locationProvider.html5Mode({ enabled: true });
        }
        return RouteConfig;
    })();
    services.RouteConfig = RouteConfig;
    var RouteService = (function () {
        function RouteService($location) {
            this.$location = $location;
        }
        RouteService.prototype.mainPage = function () {
            this.$location.url('/main');
        };
        RouteService.prototype.loginPage = function () {
            this.$location.url('/login');
        };
        RouteService.prototype.errorPage = function (description, error) {
            var uri = encodeURI(this.$location.absUrl());
            this.$location.url('/error')
                .search('description', encodeURI(description))
                .search('uri', uri)
                .search('msg', encodeURI(error));
        };
        return RouteService;
    })();
    services.RouteService = RouteService;
})(services || (services = {}));
/// <reference path="../references.ts" />
var services;
(function (services) {
    'use strict';
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
    services.StatusCodeHandler = StatusCodeHandler;
    var FrontEndService = (function () {
        function FrontEndService($http, $cookies, routeService, userService) {
            var _this = this;
            this.sessionIdKey = "sessionId";
            this.$http = $http;
            this.$cookies = $cookies;
            this.userService = userService;
            this.routeService = routeService;
            /** Optimistically fetch the user - tests to see if we are logged in */
            var headers = { headers: { "X-SessionId": this.$cookies.get(this.sessionIdKey) } };
            this.$http.get(this.getUrl('user'), headers)
                .error(function (e) {
                _this.sessionId = null;
                _this.$cookies.remove(_this.sessionIdKey);
                _this.userService.User = null;
                _this.routeService.loginPage();
            })
                .success(function (user) {
                _this.sessionId = _this.$cookies.get(_this.sessionIdKey);
                _this.userService.User = user;
                _this.routeService.mainPage();
            });
        }
        FrontEndService.prototype.getHeaders = function () {
            var headers = {};
            if (this.sessionId)
                headers = { "X-SessionId": this.sessionId };
            return { headers: headers };
        };
        FrontEndService.prototype.getUrl = function (path) {
            return "/FrontEndSvc/" + path;
        };
        /* ---- Login / Logout ---- */
        FrontEndService.prototype.login = function (email, password) {
            var _this = this;
            this.$http.post(this.getUrl('login'), { email: email, password: password }, {})
                .error(function (error) { return _this.routeService.errorPage('Login failed', error); })
                .success(function (sessionId) {
                _this.sessionId = sessionId;
                _this.$cookies.put(_this.sessionIdKey, sessionId);
                _this.getUser()
                    .error(function (error) { return _this.routeService.errorPage('Login [fetch user] failed', error); })
                    .success(function (user) {
                    _this.userService.User = user;
                    _this.routeService.mainPage();
                });
            });
        };
        FrontEndService.prototype.logout = function () {
            this.sessionId = null;
            this.$cookies.remove(this.sessionIdKey);
            this.routeService.loginPage();
        };
        /* ---- User ---- */
        FrontEndService.prototype.getUser = function () {
            return this.$http.get(this.getUrl('user'), this.getHeaders());
        };
        FrontEndService.prototype.newUser = function (displayName, email, password) {
            var _this = this;
            this.$http.post(this.getUrl('user'), { displayName: displayName, email: email, password: password }, this.getHeaders())
                .error(function (error) { return _this.routeService.errorPage('New user failed', error); })
                .success(function (sessionId) {
                _this.sessionId = sessionId;
                _this.$cookies.put(_this.sessionIdKey, sessionId);
                _this.getUser()
                    .error(function (error) { return _this.routeService.errorPage('New user [fetch user] failed', error); })
                    .success(function (user) {
                    _this.userService.User = user;
                    _this.routeService.mainPage();
                });
            });
        };
        FrontEndService.prototype.editUser = function (displayName, email, password) {
            return this.$http.put(this.getUrl('user'), { displayName: displayName, email: email, password: password }, this.getHeaders());
        };
        FrontEndService.prototype.addFriend = function (friendEmail) {
            return this.$http.put(this.getUrl('user/friend'), friendEmail, this.getHeaders());
        };
        FrontEndService.prototype.deleteFriend = function (friendId) {
            return this.$http.delete(this.getUrl('user/friend/' + friendId), this.getHeaders());
        };
        /* ---- Progress ---- */
        FrontEndService.prototype.putDueDate = function (dueDate) {
            return this.$http.put(this.getUrl('user/duedate'), dueDate, this.getHeaders());
        };
        FrontEndService.prototype.deleteDueDate = function () {
            return this.$http.delete(this.getUrl('user/duedate'), this.getHeaders());
        };
        /* ---- Names ---- */
        FrontEndService.prototype.putName = function (name, isBoy, suggestedForUserId) {
            return this.$http.put(this.getUrl('names/' + suggestedForUserId), { name: name, isBoy: isBoy }, this.getHeaders());
        };
        FrontEndService.prototype.deleteName = function (nameId) {
            return this.$http.delete(this.getUrl('names/' + nameId), this.getHeaders());
        };
        /* ---- State ---- */
        FrontEndService.prototype.getUserState = function () {
            return this.$http.get(this.getUrl('user/state'), this.getHeaders());
        };
        FrontEndService.prototype.putUserState = function (state) {
            return this.$http.put(this.getUrl('user/state'), state, this.getHeaders());
        };
        return FrontEndService;
    })();
    services.FrontEndService = FrontEndService;
})(services || (services = {}));
/// <reference path="../references.ts" />
var services;
(function (services) {
    'use strict';
    var UserService = (function () {
        function UserService() {
            // The list of user-set handlers
            this.userListeners = [];
            // The list of handlers called when the viewed user is changed
            this.selectedFriendListeners = [];
        }
        /** Allow clients to register for the user-set notification (happens once) */
        UserService.prototype.userSetEvent = function (handler) {
            this.userListeners.push(handler);
            if (this.user)
                handler(this.user);
        };
        Object.defineProperty(UserService.prototype, "User", {
            /** Update the current user (and broadcast to all listeners) */
            set: function (user) {
                this.user = user;
                this.userListeners.forEach(function (h) { return h(user); });
            },
            enumerable: true,
            configurable: true
        });
        /** Register a client to subscribe to the viewed user being changed */
        UserService.prototype.friendSelectedEvent = function (handler) {
            this.selectedFriendListeners.push(handler);
            if (this.selectedFriend)
                handler(this.selectedFriend);
        };
        Object.defineProperty(UserService.prototype, "Friend", {
            /** Update the current user (and broadcast to all listeners) */
            set: function (friend) {
                this.selectedFriend = friend;
                this.selectedFriendListeners.forEach(function (h) { return h(friend); });
            },
            enumerable: true,
            configurable: true
        });
        return UserService;
    })();
    services.UserService = UserService;
})(services || (services = {}));
var login;
(function (login) {
    'use strict';
})(login || (login = {}));
/// <reference path="../references.ts" />
var login;
(function (login) {
    'use strict';
    var LoginController = (function () {
        function LoginController($scope, $location, frontEndService) {
            var _this = this;
            this.$scope = $scope;
            this.$location = $location;
            this.frontEndService = frontEndService;
            $scope.showLogin = function () {
                $scope.isRegisterVisible = false;
                $scope.isLoginVisible = true;
            };
            $scope.showRegister = function () {
                $scope.isLoginVisible = false;
                $scope.isRegisterVisible = true;
            };
            $scope.login = function () {
                _this.frontEndService.login($scope.email, $scope.password);
            };
            $scope.register = function () {
                _this.frontEndService.newUser($scope.displayName, $scope.email, $scope.password);
            };
        }
        return LoginController;
    })();
    login.LoginController = LoginController;
})(login || (login = {}));
/// <reference path="../../references.ts" />
var main;
(function (main) {
    var share;
    (function (share) {
        'use strict';
    })(share = main.share || (main.share = {}));
})(main || (main = {}));
/// <reference path="../../references.ts" />
var main;
(function (main) {
    var share;
    (function (share) {
        'use strict';
        var ShareController = (function () {
            function ShareController($scope, $uibModalInstance, $window, frontEndService, userService) {
                var _this = this;
                this.$scope = $scope;
                this.$uibModalInstance = $uibModalInstance;
                this.$window = $window;
                this.frontEndService = frontEndService;
                this.userService = userService;
                this.$scope.showExistingUserSuccess = false;
                this.$scope.showNewUserSuccess = false;
                this.userService.userSetEvent(function (user) {
                    _this.user = user;
                });
                this.$scope.close = function () { return _this.$uibModalInstance.dismiss(); };
                this.$scope.share = function (email) { return ShareController.share(_this, email); };
            }
            ShareController.share = function (self, email) {
                // Reset previous values
                self.$scope.friendDisplayName = null;
                self.$scope.mailToLink = null;
                self.$scope.showExistingUserSuccess = false;
                self.$scope.showNewUserSuccess = false;
                self.frontEndService.addFriend(email)
                    .error(function (error) {
                    self.$scope.friendEmail = null;
                    self.$scope.showNewUserSuccess = true;
                    var url = self.$window.location.protocol + "//" + self.$window.location.host;
                    self.$scope.mailToLink = ShareController.makeMailTo(self.user, email, url);
                })
                    .success(function (friend) {
                    self.$scope.friendDisplayName = friend.displayName;
                    self.$scope.friendEmail = null;
                    self.$scope.showExistingUserSuccess = true;
                });
            };
            ShareController.makeMailTo = function (user, newUserEmail, url) {
                var subject = "Pregnaware request from your friend " + user.displayName;
                var body = "Hi,\n\n            You have been invited to share in your friend's pregnancy on Pregnaware! Click the link\n            below to set up an account, and add your friend's email address '" + user.email + "':\n\n            " + url + "\n\n            Looking forward to sharing with you!\n\n            " + user.displayName + ".\n            ";
                return "mailto:" + newUserEmail + "?subject=" + encodeURI(subject) + "&body=" + encodeURI(body);
            };
            return ShareController;
        })();
        share.ShareController = ShareController;
    })(share = main.share || (main.share = {}));
})(main || (main = {}));
/// <reference path="../../references.ts" />
var main;
(function (main) {
    var account;
    (function (account) {
        'use strict';
    })(account = main.account || (main.account = {}));
})(main || (main = {}));
/// <reference path="../../references.ts" />
var main;
(function (main) {
    var account;
    (function (account) {
        'use strict';
        var AccountController = (function () {
            function AccountController($scope, $uibModalInstance, routeService, frontEndService, userService) {
                var _this = this;
                this.$scope = $scope;
                this.$uibModalInstance = $uibModalInstance;
                this.routeService = routeService;
                this.frontEndService = frontEndService;
                this.userService = userService;
                this.$scope.newPassword = "";
                this.$scope.confirmPassword = "";
                this.$scope.passwordMatch = false;
                this.$scope.passwordMismatch = false;
                // Handle cancelling the changes
                this.$scope.cancelChanges = function () { return _this.$uibModalInstance.dismiss(); };
                // Handle the persistence of the changes
                this.$scope.saveChanges =
                    function (newDisplayName, originalDisplayName, newEmail, originalEmail, newPassword) {
                        AccountController.saveChanges(_this, newDisplayName, originalDisplayName, newEmail, originalEmail, newPassword);
                    };
                // Warn when the user's passwords do not match
                this.$scope.$watch('password', function () { return AccountController.handlePasswordChange($scope); });
                this.$scope.$watch('confirmPassword', function () { return AccountController.handlePasswordChange($scope); });
                // Detect changes to the user
                this.userService.userSetEvent(function (user) {
                    if (user) {
                        _this.$scope.newDisplayName = user.displayName;
                        _this.$scope.originalDisplayName = user.displayName;
                        _this.$scope.newEmail = user.email;
                        _this.$scope.originalEmail = user.email;
                    }
                    else {
                        _this.$scope.newDisplayName = null;
                        _this.$scope.originalDisplayName = null;
                        _this.$scope.newEmail = null;
                        _this.$scope.originalEmail = null;
                    }
                });
            }
            AccountController.handlePasswordChange = function (scope) {
                if (scope.newPassword != "") {
                    scope.passwordMatch = scope.newPassword == scope.confirmPassword;
                    scope.passwordMismatch = scope.newPassword != scope.confirmPassword;
                }
                else {
                    scope.passwordMatch = false;
                    scope.passwordMismatch = false;
                }
            };
            AccountController.saveChanges = function (self, newDisplayName, originalDisplayName, newEmail, originalEmail, newPassword) {
                var displayName = (newDisplayName != originalDisplayName) ? newDisplayName : null;
                var email = (newEmail != originalEmail) ? newEmail : null;
                var password = newPassword ? newPassword : null;
                self.frontEndService.editUser(displayName, email, password)
                    .error(function (error) { return self.routeService.errorPage("Failed to edit user", error); })
                    .success(function (updatedUser) {
                    self.userService.User = updatedUser;
                    self.$uibModalInstance.close();
                });
            };
            return AccountController;
        })();
        account.AccountController = AccountController;
    })(account = main.account || (main.account = {}));
})(main || (main = {}));
/// <reference path="../../references.ts" />
var main;
(function (main) {
    var progress;
    (function (progress) {
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
            Object.defineProperty(EnhancedProgressModel.prototype, "progress", {
                get: function () {
                    var weeks = Math.floor(this.daysPassed / 7);
                    var days = this.daysPassed % 7;
                    return weeks + "w " + days + "d";
                },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(EnhancedProgressModel.prototype, "remaining", {
                get: function () {
                    var weeks = Math.floor(this.daysRemaining / 7);
                    var days = (this.daysRemaining + 1) % 7;
                    return weeks + "w " + days + "d";
                },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(EnhancedProgressModel.prototype, "dueDateVal", {
                get: function () {
                    return this.dueDate.valueOf();
                },
                enumerable: true,
                configurable: true
            });
            return EnhancedProgressModel;
        })();
        progress.EnhancedProgressModel = EnhancedProgressModel;
    })(progress = main.progress || (main.progress = {}));
})(main || (main = {}));
/// <reference path="../../references.ts" />
var main;
(function (main) {
    var progress;
    (function (progress) {
        'use strict';
        var ProgressController = (function () {
            function ProgressController($scope, routeService, frontEndService, userService) {
                var _this = this;
                this.$scope = $scope;
                this.routeService = routeService;
                this.frontEndService = frontEndService;
                this.userService = userService;
                this.$scope.dueDatePickerOpen = false;
                this.$scope.dueDate = Date.now();
                this.$scope.updateDueDate = function (dueDate) { return ProgressController.updateDueDate(_this, dueDate); };
                this.$scope.changeDueDate = function () { return ProgressController.changeDueDate(_this); };
                this.userService.userSetEvent(function (user) {
                    _this.user = user;
                    if (user) {
                        _this.$scope.viewedUser = _this.user.displayName;
                        _this.$scope.canEdit = true;
                        _this.$scope.progress =
                            _this.user.dueDate
                                ? new progress.EnhancedProgressModel(_this.user.dueDate)
                                : null;
                    }
                    else {
                        _this.$scope.viewedUser = null;
                        _this.$scope.canEdit = false;
                        _this.$scope.progress = null;
                    }
                });
                this.userService.friendSelectedEvent(function (friend) {
                    if (friend) {
                        _this.$scope.viewedUser = friend.displayName;
                        _this.$scope.canEdit = false;
                        _this.$scope.progress = friend.dueDate
                            ? new progress.EnhancedProgressModel(friend.dueDate)
                            : null;
                    }
                    else if (_this.user) {
                        _this.$scope.viewedUser = _this.user.displayName;
                        _this.$scope.canEdit = true;
                        _this.$scope.progress =
                            _this.user.dueDate
                                ? new progress.EnhancedProgressModel(_this.user.dueDate)
                                : null;
                    }
                    else {
                        _this.$scope.viewedUser = null;
                        _this.$scope.canEdit = false;
                        _this.$scope.progress = null;
                    }
                });
            }
            ProgressController.updateDueDate = function (self, dueDate) {
                var parsedDueDate = moment(dueDate);
                var asLocalDate = {
                    year: parsedDueDate.year(),
                    month: parsedDueDate.month() + 1,
                    day: parsedDueDate.date()
                };
                self.frontEndService.putDueDate(asLocalDate)
                    .error(function (error) { return self.routeService.errorPage('Could not put due date', error); })
                    .success(function (response) {
                    self.$scope.progress = new progress.EnhancedProgressModel(response);
                });
            };
            ProgressController.changeDueDate = function (self) {
                self.frontEndService.deleteDueDate()
                    .error(function (error) { return self.routeService.errorPage('Could not put due date', error); })
                    .success(function (response) {
                    self.$scope.progress = null;
                });
            };
            return ProgressController;
        })();
        progress.ProgressController = ProgressController;
    })(progress = main.progress || (main.progress = {}));
})(main || (main = {}));
/// <reference path="../../references.ts" />
var main;
(function (main) {
    var names;
    (function (names) {
        'use strict';
    })(names = main.names || (main.names = {}));
})(main || (main = {}));
/// <reference path="../../references.ts" />
var main;
(function (main) {
    var names;
    (function (names) {
        'use strict';
        var NamesController = (function () {
            function NamesController($scope, routeService, frontEndService, userService) {
                var _this = this;
                this.$scope = $scope;
                this.routeService = routeService;
                this.frontEndService = frontEndService;
                this.userService = userService;
                this.$scope.addCurrentNameGirl = function (name) { return NamesController.addCurrentNameGirl(_this, name); };
                this.$scope.addCurrentNameBoy = function (name) { return NamesController.addCurrentNameBoy(_this, name); };
                this.$scope.deleteName = function (entry) { return NamesController.deleteName(_this, entry); };
                this.$scope.isNameInvalid = function (name) { return _this.isNameInvalid(name); };
                this.userService.userSetEvent(function (user) {
                    _this.user = user;
                    if (user) {
                        _this.$scope.viewedUser = _this.user.displayName;
                        _this.$scope.canEdit = true;
                        _this.$scope.boysNames = _this.user.babyNames.filter(function (n) { return n.isBoy; });
                        _this.$scope.girlsNames = _this.user.babyNames.filter(function (n) { return !n.isBoy; });
                    }
                    else {
                        _this.$scope.viewedUser = null;
                        _this.$scope.canEdit = false;
                        _this.$scope.boysNames = [];
                        _this.$scope.girlsNames = [];
                    }
                });
                this.userService.friendSelectedEvent(function (friend) {
                    var babyNames;
                    if (friend) {
                        _this.$scope.viewedUser = friend.displayName;
                        _this.$scope.canEdit = false;
                        babyNames = friend.babyNames;
                    }
                    else if (_this.user) {
                        _this.$scope.viewedUser = _this.user.displayName;
                        _this.$scope.canEdit = true;
                        babyNames = _this.user.babyNames;
                    }
                    else {
                        _this.$scope.viewedUser = null;
                        _this.$scope.canEdit = false;
                        babyNames = [];
                    }
                    _this.selectedFriend = friend;
                    _this.$scope.boysNames = babyNames.filter(function (n) { return n.isBoy; });
                    _this.$scope.girlsNames = babyNames.filter(function (n) { return !n.isBoy; });
                });
            }
            /** Basic name validation logic */
            NamesController.prototype.isNameInvalid = function (name) {
                if (!this.user || !name)
                    return true;
                name = name.trim();
                if (name.length == 0) {
                    return true;
                }
                else {
                    return this.user.babyNames.filter(function (existingName) { return existingName.name == name; }).length > 0;
                }
            };
            NamesController.addCurrentNameGirl = function (self, name) {
                name = name.trim();
                var suggestedForUserId = self.selectedFriend ? self.selectedFriend.userId : self.user.userId;
                self.frontEndService.putName(name, false, suggestedForUserId)
                    .error(function (error) { return self.routeService.errorPage("Failed to add girl's name", error); })
                    .success(function (response) {
                    self.$scope.girlsNames.push(response);
                    self.user.babyNames.push(response);
                    self.$scope.currentNameGirl = "";
                });
            };
            NamesController.addCurrentNameBoy = function (self, name) {
                name = name.trim();
                var suggestedForUserId = self.selectedFriend ? self.selectedFriend.userId : self.user.userId;
                self.frontEndService.putName(name, true, suggestedForUserId)
                    .error(function (error) { return self.routeService.errorPage("Failed to add boy's name", error); })
                    .success(function (response) {
                    self.$scope.boysNames.push(response);
                    self.user.babyNames.push(response);
                    self.$scope.currentNameBoy = "";
                });
            };
            NamesController.deleteName = function (self, entry) {
                self.frontEndService.deleteName(entry.nameId)
                    .error(function (error) { return self.routeService.errorPage("Failed to remove name", error); })
                    .success(function (response) {
                    if (entry.isBoy) {
                        self.$scope.boysNames = self.$scope.boysNames.filter(function (e) { return e != entry; });
                    }
                    else {
                        self.$scope.girlsNames = self.$scope.girlsNames.filter(function (e) { return e != entry; });
                    }
                });
            };
            return NamesController;
        })();
        names.NamesController = NamesController;
    })(names = main.names || (main.names = {}));
})(main || (main = {}));
/// <reference path="../../references.ts" />
var main;
(function (main) {
    var nav;
    (function (nav) {
        'use strict';
    })(nav = main.nav || (main.nav = {}));
})(main || (main = {}));
/// <reference path="../../references.ts" />
var main;
(function (main) {
    var nav;
    (function (nav) {
        'use strict';
        var NavController = (function () {
            function NavController($scope, $uibModal, $locale, routeService, frontEndService, userService) {
                var _this = this;
                this.$scope = $scope;
                this.$uibModal = $uibModal;
                this.$locale = $locale;
                this.routeService = routeService;
                this.frontEndService = frontEndService;
                this.userService = userService;
                this.userService.userSetEvent(function (user) { return _this.$scope.user = user; });
                this.$scope.locale = this.$locale.id;
                this.$scope.confirmFriendRequest = function (friend) { return _this.confirmFriendRequest(friend); };
                this.$scope.ignoreFriendRequest = function (friend) { return _this.ignoreFriendRequest(friend); };
                this.$scope.logout = function () {
                    _this.userService.User = null;
                    _this.userService.Friend = null;
                    _this.frontEndService.logout();
                };
                this.$scope.viewFriend = function (friend) {
                    _this.userService.Friend = friend;
                };
                this.$scope.viewUser = function () {
                    _this.userService.Friend = null;
                };
                this.$scope.addFriend = function () {
                    _this.$uibModal.open({
                        animation: true,
                        templateUrl: '/scripts/main/share/share.view.html',
                        controller: main.share.ShareController,
                        controllerAs: 'vm',
                        size: 'lg',
                    });
                };
                // Pop-up the account settings screen
                this.$scope.updateAccountSettings = function () {
                    _this.$uibModal.open({
                        animation: true,
                        templateUrl: '/scripts/main/account/account.view.html',
                        controller: main.account.AccountController,
                        controllerAs: 'vm',
                        size: 'lg',
                    });
                };
            }
            NavController.prototype.confirmFriendRequest = function (friend) {
                var _this = this;
                this.frontEndService.addFriend(friend.email)
                    .error(function (e) { return _this.routeService.errorPage('Failed to confirm friend', e); })
                    .success(function () {
                    _this.frontEndService.getUser()
                        .error(function (e) { return _this.routeService.errorPage('Failed to fetch user after friend confirmation', e); })
                        .success(function (user) { return _this.userService.User = user; });
                });
            };
            NavController.prototype.ignoreFriendRequest = function (friend) {
                var _this = this;
                this.frontEndService.deleteFriend(friend.userId)
                    .error(function (e) { return _this.routeService.errorPage('Failed to ignore friend', e); })
                    .success(function () {
                    _this.frontEndService.getUser()
                        .error(function (e) { return _this.routeService.errorPage('Failed to fetch user after ignoring friend', e); })
                        .success(function (user) { return _this.userService.User = user; });
                });
            };
            return NavController;
        })();
        nav.NavController = NavController;
    })(nav = main.nav || (main.nav = {}));
})(main || (main = {}));
/// <reference path="../references.ts" />
var main;
(function (main) {
    'use strict';
    var MainController = (function () {
        function MainController() {
        }
        MainController.navDirective = {
            name: 'navBar',
            controller: main.nav.NavController,
            controllerAs: 'vm',
            scope: {},
            restrict: 'E',
            replace: true,
            templateUrl: '/scripts/main/nav/nav.view.html'
        };
        MainController.progressDirective = {
            name: 'pregnancyProgress',
            controller: main.progress.ProgressController,
            controllerAs: 'vm',
            scope: {},
            restrict: 'E',
            replace: true,
            templateUrl: '/scripts/main/progress/progress.view.html'
        };
        MainController.namesDirective = {
            name: 'names',
            controller: main.names.NamesController,
            controllerAs: 'vm',
            scope: {},
            restrict: 'E',
            replace: true,
            templateUrl: '/scripts/main/names/names.view.html'
        };
        MainController.directives = [
            MainController.navDirective,
            MainController.progressDirective,
            MainController.namesDirective,
        ];
        return MainController;
    })();
    main.MainController = MainController;
})(main || (main = {}));
/// <reference path="typings/tsd.d.ts" />
/// <reference path="models/local-date.ts" />
/// <reference path="models/wrapped-baby-name.ts" />
/// <reference path="models/wrapped-friend.ts" />
/// <reference path="models/wrapped-user.ts" />
/// <reference path="error/error.model.ts" />
/// <reference path="error/error.controller.ts" />
/// <reference path="services/route-service.ts" />
/// <reference path="services/front-end-service.ts" />
/// <reference path="services/user-service.ts" />
/// <reference path="login/login.model.ts" />
/// <reference path="login/login.controller.ts" />
/// <reference path="main/share/share.model.ts" />
/// <reference path="main/share/share.controller.ts" />
/// <reference path="main/account/account.model.ts" />
/// <reference path="main/account/account.controller.ts" />
/// <reference path="main/progress/progress.model.ts" />
/// <reference path="main/progress/progress.controller.ts" />
/// <reference path="main/names/names.model.ts" />
/// <reference path="main/names/names.controller.ts" />
/// <reference path="main/nav/nav.model.ts" />
/// <reference path="main/nav/nav.controller.ts" />
/// <reference path="main/main.controller.ts" />
/// <reference path="app.ts" /> 
/// <reference path="references.ts" />
var App;
(function (App) {
    var app = angular.module('pregnaware', ['ngRoute', 'ngCookies', 'ngAnimate', 'ui.bootstrap']);
    app.service('frontEndService', services.FrontEndService);
    app.service('routeService', services.RouteService);
    app.service('userService', services.UserService);
    // Configure the application routes
    app.config(services.RouteConfig);
    // Add the directives required by the main view
    main.MainController.directives.forEach(function (d) { return app.directive(d.name, function () { return d; }); });
})(App || (App = {}));
//# sourceMappingURL=app.js.map