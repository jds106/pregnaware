/// <reference path="../references.ts" />
var services;
(function (services) {
    'use strict';
    var ErrorService = /** @class */ (function () {
        function ErrorService($location) {
            // The list of error handlers
            this.errorListeners = [];
            this.$location = $location;
        }
        /** Allow clients to register for the error notification */
        ErrorService.prototype.errorEvent = function (handler) {
            this.errorListeners.push(handler);
        };
        /** Broadcast the error to all listeners */
        ErrorService.prototype.raiseError = function (description, error) {
            var _this = this;
            this.errorListeners.forEach(function (h) { return h.onErrorSet(_this.$location.absUrl(), description, error); });
        };
        /** Clear the error event */
        ErrorService.prototype.clearError = function () {
            this.errorListeners.forEach(function (h) { return h.onErrorClear(); });
        };
        return ErrorService;
    }());
    services.ErrorService = ErrorService;
})(services || (services = {}));
/// <reference path="../references.ts" />
var services;
(function (services) {
    'use strict';
    // These are the ONLY status codes returned by the app
    var StatusCodeHandler = /** @class */ (function () {
        function StatusCodeHandler() {
            this.OK = 200;
            this.BadRequest = 400;
            this.NotFound = 404;
            this.Conflict = 409;
            this.ServiceUnavailable = 503;
        }
        return StatusCodeHandler;
    }());
    services.StatusCodeHandler = StatusCodeHandler;
    var FrontEndService = /** @class */ (function () {
        function FrontEndService($http, $cookies, userService, errorService) {
            var _this = this;
            this.sessionIdKey = "sessionId";
            this.$http = $http;
            this.$cookies = $cookies;
            this.userService = userService;
            this.errorService = errorService;
            /** Optimistically fetch the user - tests to see if we are logged in */
            var headers = { headers: { "X-SessionId": this.$cookies.get(this.sessionIdKey) } };
            this.$http.get(this.getUrl('user'), headers)
                .error(function () {
                _this.sessionId = null;
                _this.$cookies.remove(_this.sessionIdKey);
                _this.userService.User = null;
            })
                .success(function (user) {
                _this.sessionId = _this.$cookies.get(_this.sessionIdKey);
                _this.userService.User = user;
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
                .error(function (error) { return _this.errorService.raiseError('Login failed', error); })
                .success(function (sessionId) {
                _this.sessionId = sessionId;
                _this.$cookies.put(_this.sessionIdKey, sessionId);
                _this.getUser()
                    .error(function (error) { return _this.errorService.raiseError('Login [fetch user] failed', error); })
                    .success(function (user) {
                    _this.userService.User = user;
                });
            });
        };
        FrontEndService.prototype.logout = function () {
            this.sessionId = null;
            this.$cookies.remove(this.sessionIdKey);
            this.userService.User = null;
            this.userService.Friend = null;
        };
        /* ---- User ---- */
        FrontEndService.prototype.getUser = function () {
            return this.$http.get(this.getUrl('user'), this.getHeaders());
        };
        FrontEndService.prototype.newUser = function (displayName, email, password) {
            var _this = this;
            this.$http.post(this.getUrl('user'), { displayName: displayName, email: email, password: password }, this.getHeaders())
                .error(function (error) { return _this.errorService.raiseError('New user failed', error); })
                .success(function (sessionId) {
                _this.sessionId = sessionId;
                _this.$cookies.put(_this.sessionIdKey, sessionId);
                _this.getUser()
                    .error(function (error) { return _this.errorService.raiseError('New user [fetch user] failed', error); })
                    .success(function (user) {
                    _this.userService.User = user;
                    //TODO this.routeService.mainPage();
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
        /* --- Name stats --- */
        FrontEndService.prototype.toGender = function (isBoy) { return isBoy ? 'boy' : 'girl'; };
        FrontEndService.prototype.getNameStatsCount = function () {
            return this.$http.get(this.getUrl('namestats/meta/count'), this.getHeaders());
        };
        FrontEndService.prototype.getNameStatsCompleteForName = function (name, isBoy) {
            var gender = this.toGender(isBoy);
            return this.$http.get(this.getUrl("namestats/data/" + gender + "/complete/name/" + name), this.getHeaders());
        };
        FrontEndService.prototype.getNameStatsCompleteForYear = function (year, isBoy) {
            var gender = this.toGender(isBoy);
            return this.$http.get(this.getUrl("namestats/data/" + gender + "/complete/summary/" + year), this.getHeaders());
        };
        FrontEndService.prototype.getNameStatsByCountryForName = function (name, isBoy) {
            var gender = this.toGender(isBoy);
            return this.$http.get(this.getUrl("namestats/data/" + gender + "/country/name/" + name), this.getHeaders());
        };
        FrontEndService.prototype.getNameStatsByCountryForYear = function (year, isBoy) {
            var gender = this.toGender(isBoy);
            return this.$http.get(this.getUrl("namestats/data/" + gender + "/country/summary/" + year), this.getHeaders());
        };
        FrontEndService.prototype.getNameStatsByMonthForName = function (name, isBoy) {
            var gender = this.toGender(isBoy);
            return this.$http.get(this.getUrl("namestats/data/" + gender + "/month/name/" + name), this.getHeaders());
        };
        FrontEndService.prototype.getNameStatsByMonthForYear = function (year, isBoy) {
            var gender = this.toGender(isBoy);
            return this.$http.get(this.getUrl("namestats/data/" + gender + "/month/summary/" + year), this.getHeaders());
        };
        FrontEndService.prototype.getNameStatsByRegionForName = function (name, isBoy) {
            var gender = this.toGender(isBoy);
            return this.$http.get(this.getUrl("namestats/data/" + gender + "/region/name/" + name), this.getHeaders());
        };
        FrontEndService.prototype.getNameStatsByRegionForYear = function (year, isBoy) {
            var gender = this.toGender(isBoy);
            return this.$http.get(this.getUrl("namestats/data/" + gender + "/region/summary/" + year), this.getHeaders());
        };
        return FrontEndService;
    }());
    services.FrontEndService = FrontEndService;
})(services || (services = {}));
/// <reference path="../references.ts" />
var services;
(function (services) {
    'use strict';
    var UserService = /** @class */ (function () {
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
            /** Returns the current user */
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
        /** Register a client to subscribe to the viewed user being changed */
        UserService.prototype.friendSelectedEvent = function (handler) {
            this.selectedFriendListeners.push(handler);
            if (this.selectedFriend)
                handler(this.selectedFriend);
        };
        Object.defineProperty(UserService.prototype, "Friend", {
            /** Returns the currently selected friend */
            get: function () {
                return this.selectedFriend;
            },
            /** Update the current user (and broadcast to all listeners) */
            set: function (friend) {
                this.selectedFriend = friend;
                this.selectedFriendListeners.forEach(function (h) { return h(friend); });
            },
            enumerable: true,
            configurable: true
        });
        return UserService;
    }());
    services.UserService = UserService;
})(services || (services = {}));
/// <reference path="../references.ts" />
var services;
(function (services) {
    'use strict';
    var StateService = /** @class */ (function () {
        function StateService(frontEndService, userService, errorService) {
            var _this = this;
            this.stateListeners = [];
            this.currentState = {};
            this.frontEndService = frontEndService;
            userService.userSetEvent(function (user) {
                _this.currentState = {};
                if (user) {
                    _this.frontEndService.getUserState()
                        .error(function (error) { return errorService.raiseError('Could not fetch user state', error); })
                        .success(function (response) {
                        _this.currentState = JSON.parse(response);
                        _this.stateListeners.forEach(function (h) { return h(_this.currentState); });
                    });
                }
            });
        }
        /** Allow clients to register for the state-change notification */
        StateService.prototype.stateChangedEvent = function (handler) {
            this.stateListeners.push(handler);
            if (this.currentState)
                handler(this.currentState);
        };
        /** Broadcast the state change to all listeners, and persist */
        StateService.prototype.changeState = function (stateChanger) {
            var _this = this;
            // Get the state changer to change the state
            this.currentState = stateChanger(this.currentState);
            // Publish the change
            this.stateListeners.forEach(function (h) { return h(_this.currentState); });
            // Store the state on the server
            this.frontEndService.putUserState(this.currentState);
        };
        return StateService;
    }());
    services.StateService = StateService;
})(services || (services = {}));
var error;
(function (error) {
    'use strict';
})(error || (error = {}));
/// <reference path="../references.ts" />
var error;
(function (error_1) {
    'use strict';
    var ErrorController = /** @class */ (function () {
        function ErrorController($scope, $location, errorService) {
            // Clear the error when the user clicks "return to home"
            $scope.clearError = function () { return errorService.clearError(); };
            // Set and clear the error message
            errorService.errorEvent({
                onErrorClear: function () {
                    $scope.errorDescription = null;
                    $scope.errorUri = null;
                    $scope.errorMsg = null;
                    $scope.mailStr = null;
                },
                onErrorSet: function (url, description, error) {
                    $scope.errorDescription = description;
                    $scope.errorUri = url;
                    $scope.errorMsg = error;
                    var subject = "[ERROR] Pregnaware UI Failure";
                    var body = "\nDescription:\n" + $scope.errorDescription + "\n\nURI:\n" + $scope.errorUri + "\n\nDetail:\n" + $scope.errorMsg + "\n            ";
                    $scope.mailStr =
                        "mailto:support@pregnaware.co.uk?subject=" + encodeURI(subject) + "&body=" + encodeURI(body);
                }
            });
        }
        return ErrorController;
    }());
    error_1.ErrorController = ErrorController;
})(error || (error = {}));
var login;
(function (login) {
    'use strict';
})(login || (login = {}));
/// <reference path="../references.ts" />
var login;
(function (login) {
    'use strict';
    var LoginController = /** @class */ (function () {
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
    }());
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
        var ShareController = /** @class */ (function () {
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
        }());
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
        var AccountController = /** @class */ (function () {
            function AccountController($scope, $uibModalInstance, frontEndService, userService, errorService) {
                var _this = this;
                this.$scope = $scope;
                this.$uibModalInstance = $uibModalInstance;
                this.frontEndService = frontEndService;
                this.userService = userService;
                this.errorService = errorService;
                this.$scope.newPassword = "";
                this.$scope.confirmPassword = "";
                this.$scope.passwordMatch = false;
                this.$scope.passwordMismatch = false;
                // Handle cancelling the changes
                this.$scope.cancelChanges = function () { return _this.$uibModalInstance.dismiss(); };
                // Handle the persistence of the changes
                this.$scope.saveChanges =
                    function (newDisplayName, originalDisplayName, newEmail, originalEmail, newPassword) {
                        return _this.saveChanges(newDisplayName, originalDisplayName, newEmail, originalEmail, newPassword);
                    };
                // Warn when the user's passwords do not match
                this.$scope.$watch('password', function () { return _this.handlePasswordChange(); });
                this.$scope.$watch('confirmPassword', function () { return _this.handlePasswordChange(); });
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
            AccountController.prototype.handlePasswordChange = function () {
                if (this.$scope.newPassword != "") {
                    this.$scope.passwordMatch = this.$scope.newPassword == this.$scope.confirmPassword;
                    this.$scope.passwordMismatch = this.$scope.newPassword != this.$scope.confirmPassword;
                }
                else {
                    this.$scope.passwordMatch = false;
                    this.$scope.passwordMismatch = false;
                }
            };
            AccountController.prototype.saveChanges = function (newDisplayName, originalDisplayName, newEmail, originalEmail, newPassword) {
                var _this = this;
                var displayName = (newDisplayName != originalDisplayName) ? newDisplayName : null;
                var email = (newEmail != originalEmail) ? newEmail : null;
                var password = newPassword ? newPassword : null;
                this.frontEndService.editUser(displayName, email, password)
                    .error(function (error) { return _this.errorService.raiseError("Failed to edit user", error); })
                    .success(function (updatedUser) {
                    _this.userService.User = updatedUser;
                    _this.$uibModalInstance.close();
                });
            };
            return AccountController;
        }());
        account.AccountController = AccountController;
    })(account = main.account || (main.account = {}));
})(main || (main = {}));
/// <reference path="../../references.ts" />
var main;
(function (main) {
    var progress;
    (function (progress) {
        'use strict';
        var EnhancedProgressModel = /** @class */ (function () {
            function EnhancedProgressModel(dueDate) {
                this.gestationPeriod = moment.duration({ days: 280 });
                this.daysInTrimester1 = 14 * 7;
                this.daysInTrimester2 = 14 * 7;
                this.daysInTrimester3 = 12 * 7;
                // Note handling of zero-index months
                this.dueDate = moment().startOf('day').year(dueDate.year).month(dueDate.month - 1).date(dueDate.day);
                var conceptionDate = this.dueDate.clone().subtract(this.gestationPeriod);
                var today = moment().startOf('day');
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
                    var days = (this.daysRemaining) % 7;
                    return weeks + "w " + days + "d";
                },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(EnhancedProgressModel.prototype, "daysPassedTrimester1", {
                get: function () {
                    return Math.max(0, Math.min(this.daysInTrimester1, this.daysPassed));
                },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(EnhancedProgressModel.prototype, "daysPassedTrimester2", {
                get: function () {
                    return Math.max(0, Math.min(this.daysInTrimester2, this.daysPassed - this.daysInTrimester1));
                },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(EnhancedProgressModel.prototype, "daysPassedTrimester3", {
                get: function () {
                    return Math.max(0, Math.min(this.daysInTrimester3, this.daysPassed - this.daysInTrimester1 - this.daysInTrimester2));
                },
                enumerable: true,
                configurable: true
            });
            Object.defineProperty(EnhancedProgressModel.prototype, "babySize", {
                get: function () {
                    var sizes = [
                        // First trimester
                        "Microscopic",
                        "Microscopic",
                        "Microscopic",
                        "Microscopic",
                        "Poppy seed (2mm)",
                        "Sesame seed (3mm)",
                        "Lentil (5mm)",
                        "Blueberry (1.2cm)",
                        "Kidney bean (1.6cm / 1g)",
                        "Grape (2.3cm / 2g)",
                        "Green olive (3.1cm / 4g)",
                        "Fig (4.1cm / 7g)",
                        "Lime (5.4cm / 14g)",
                        "Pea pod (7.4cm / 23g)",
                        // Second trimester
                        "As big as a lemon (8.7cm / 43g)",
                        "As big as an apple (10.1cm / 70g)",
                        "As big as an avocado (11.6cm / 100g)",
                        "As heavy as a turnip (13cm / 140g)",
                        "As big as a bell pepper (14.2cm / 190g)",
                        "As big as an heirloom tomato (15.3cm / 240g)",
                        "As long as a small banana (16.4cm head-to-bottom, 25.6cm head-to-heel / 300g)",
                        "As long as a carrot (26.7cm head-to-heel / 360g)",
                        "As big as a spaghetti squash (27.8cm head-to-heel / 430g)",
                        "As heavy as a large mango (28.9cm head-to-heel / 500g)",
                        // Third trimester
                        "As long as an ear of corn (30cm head-to-heel / 600g)",
                        "As heavy as a swede (34.6cm head-to-heel / 660g)",
                        "As heavy as a red cabbage (35.6cm head-to-heel / 760g)",
                        "As heavy as a head of cauliflower (36.6cm head-to-heel / 875g)",
                        "As heavy as an aubergine (37.6cm head-to-heel / 1kg)",
                        "As big Butternut squash (38.6cm head-to-heel / 1.2kg)",
                        "As big as a good-sized cabbage (39.9cm head-to-heel / 1.3kg)",
                        "As heavy as a coconut (41.1cm head-to-heel / 1.5kg)",
                        "As long as a kale (42.4cm head-to-heel / 1.7kg)",
                        "As heavy as a pineapple (43.7cm head-to-heel / 1.9kg)",
                        "As big as a cantaloupe melon (45cm head-to-heel / 2.1kg)",
                        "As heavy as a honeydew melon (46.2cm head-to-heel / 2.4kg)",
                        "As big as a romaine lettuce (47.4cm head-to-heel / 2.6kg)",
                        "As long as a stalk of Swiss chard (48.6cm head-to-heel / 2.9kg)",
                        "As long as a leek (49.8cm head-to-heel / 3kg)",
                        "As heavy as a mini-watermelon (50.7cm head-to-heel / 3.3kg)",
                        "As big as a small pumpkin (51.2cm head-to-heel / 3.5kg)",
                    ];
                    var weeks = Math.min(40, Math.floor(this.daysPassed / 7));
                    return sizes[weeks];
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
        }());
        progress.EnhancedProgressModel = EnhancedProgressModel;
    })(progress = main.progress || (main.progress = {}));
})(main || (main = {}));
/// <reference path="../../references.ts" />
var main;
(function (main) {
    var progress;
    (function (progress) {
        'use strict';
        var ProgressController = /** @class */ (function () {
            function ProgressController($scope, frontEndService, userService, errorService) {
                var _this = this;
                this.$scope = $scope;
                this.frontEndService = frontEndService;
                this.userService = userService;
                this.errorService = errorService;
                this.$scope.dueDatePickerOpen = false;
                this.$scope.dueDate = Date.now();
                this.$scope.updateDueDate = function (dueDate) { return _this.updateDueDate(dueDate); };
                this.$scope.changeDueDate = function () { return _this.changeDueDate(); };
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
            ProgressController.prototype.updateDueDate = function (dueDate) {
                var _this = this;
                var parsedDueDate = moment(dueDate);
                var asLocalDate = {
                    year: parsedDueDate.year(),
                    month: parsedDueDate.month() + 1,
                    day: parsedDueDate.date()
                };
                this.frontEndService.putDueDate(asLocalDate)
                    .error(function (error) { return _this.errorService.raiseError('Could not put due date', error); })
                    .success(function (response) { return _this.$scope.progress = new progress.EnhancedProgressModel(response); });
            };
            ProgressController.prototype.changeDueDate = function () {
                var _this = this;
                this.frontEndService.deleteDueDate()
                    .error(function (error) { return _this.errorService.raiseError('Could not put due date', error); })
                    .success(function () { return _this.$scope.progress = null; });
            };
            return ProgressController;
        }());
        progress.ProgressController = ProgressController;
    })(progress = main.progress || (main.progress = {}));
})(main || (main = {}));
/// <reference path="../../../../references.ts" />
var main;
(function (main) {
    var names;
    (function (names) {
        var stats;
        (function (stats) {
            var general;
            (function (general) {
                'use strict';
            })(general = stats.general || (stats.general = {}));
        })(stats = names.stats || (names.stats = {}));
    })(names = main.names || (main.names = {}));
})(main || (main = {}));
/// <reference path="../../../../references.ts" />
var main;
(function (main) {
    var names;
    (function (names) {
        var stats;
        (function (stats_1) {
            var general;
            (function (general) {
                'use strict';
                var GeneralStatsController = /** @class */ (function () {
                    function GeneralStatsController($scope, isBoy, $uibModalInstance, frontEndService) {
                        var _this = this;
                        this.$scope = $scope;
                        this.$uibModalInstance = $uibModalInstance;
                        this.frontEndService = frontEndService;
                        this.$scope.isBoy = isBoy;
                        this.frontEndService.getNameStatsCount()
                            .success(function (stats) {
                            _this.nameSummary = stats;
                            var years = [];
                            stats.forEach(function (stat) {
                                if (years.indexOf(stat.year) == -1)
                                    years.push(stat.year);
                            });
                            _this.$scope.availableYears = years.sort(function (l, r) { return r - l; });
                            _this.$scope.selectedYear = _this.$scope.availableYears[0];
                            _this.yearSelected(_this.$scope.selectedYear, isBoy);
                        });
                        this.$scope.selectYear = function (year) { return _this.yearSelected(year, isBoy); };
                        this.$scope.babiesBornInYear = function (year) { return _this.babiesBornInYear(year, isBoy); };
                        this.$scope.close = function () { return _this.$uibModalInstance.dismiss(); };
                    }
                    GeneralStatsController.prototype.yearSelected = function (year, isBoy) {
                        var _this = this;
                        this.$scope.nameStatsByCountry = [];
                        this.$scope.nameStatsByMonth = [];
                        this.$scope.nameStatsByRegion = [];
                        this.$scope.nameStats = [];
                        var forCountry = this.frontEndService.getNameStatsByCountryForYear(year, isBoy);
                        var forMonth = this.frontEndService.getNameStatsByMonthForYear(year, isBoy);
                        var forRegion = this.frontEndService.getNameStatsByRegionForYear(year, isBoy);
                        var forAll = this.frontEndService.getNameStatsCompleteForYear(year, isBoy);
                        forCountry.success(function (results) {
                            _this.addPercent(results, year, isBoy);
                            _this.$scope.nameStatsByCountry = results;
                        });
                        forMonth.success(function (results) {
                            _this.addPercent(results, year, isBoy);
                            _this.$scope.nameStatsByMonth = results;
                        });
                        forRegion.success(function (results) {
                            _this.addPercent(results, year, isBoy);
                            _this.$scope.nameStatsByRegion = results;
                        });
                        forAll.success(function (results) {
                            _this.addPercent(results, year, isBoy);
                            _this.$scope.nameStats = results;
                        });
                    };
                    GeneralStatsController.prototype.addPercent = function (stats, year, isBoy) {
                        if (!this.nameSummary)
                            return;
                        var summary = this.nameSummary.filter(function (s) { return s.year == year && s.isBoy == isBoy; });
                        if (summary.length == 0)
                            return;
                        stats.forEach(function (s) { return s.percent = 100 * s.count / summary[0].count; });
                    };
                    GeneralStatsController.prototype.babiesBornInYear = function (year, isBoy) {
                        if (!this.nameSummary)
                            return 0;
                        var summary = this.nameSummary.filter(function (s) { return s.year == year && s.isBoy == isBoy; });
                        return summary.length == 0 ? 0 : summary[0].count;
                    };
                    return GeneralStatsController;
                }());
                general.GeneralStatsController = GeneralStatsController;
            })(general = stats_1.general || (stats_1.general = {}));
        })(stats = names.stats || (names.stats = {}));
    })(names = main.names || (main.names = {}));
})(main || (main = {}));
/// <reference path="../../../../references.ts" />
var main;
(function (main) {
    var names;
    (function (names) {
        var stats;
        (function (stats) {
            var specific;
            (function (specific) {
                'use strict';
            })(specific = stats.specific || (stats.specific = {}));
        })(stats = names.stats || (names.stats = {}));
    })(names = main.names || (main.names = {}));
})(main || (main = {}));
/// <reference path="../../../../references.ts" />
var main;
(function (main) {
    var names;
    (function (names) {
        var stats;
        (function (stats_2) {
            var specific;
            (function (specific) {
                'use strict';
                var SpecificStatsController = /** @class */ (function () {
                    function SpecificStatsController($scope, isBoy, name, $uibModalInstance, frontEndService) {
                        var _this = this;
                        this.$scope = $scope;
                        this.$uibModalInstance = $uibModalInstance;
                        this.frontEndService = frontEndService;
                        this.$scope.isBoy = isBoy;
                        this.$scope.name = name;
                        this.$scope.orderedMonths = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
                        this.$scope.orderedRegions = [
                            'North West', 'North East', 'South East', 'South West', 'East Midlands', 'West Midlands',
                            'East', 'Wales', 'London', 'Yorkshire and The Humber'
                        ];
                        this.$scope.orderedCountries = ['England', 'Wales'];
                        this.frontEndService.getNameStatsCount()
                            .success(function (stats) {
                            _this.nameSummary = stats;
                        });
                        this.frontEndService.getNameStatsCompleteForName(name, isBoy)
                            .success(function (stats) {
                            _this.addPercent(stats);
                            _this.$scope.nameStats = stats;
                            var years = [];
                            stats.forEach(function (stat) {
                                if (years.indexOf(stat.year) == -1)
                                    years.push(stat.year);
                            });
                            _this.$scope.availableYears = years.sort(function (l, r) { return r - l; });
                        });
                        this.frontEndService.getNameStatsByCountryForName(name, isBoy)
                            .success(function (stats) { return _this.$scope.nameStatsByCountry = stats; });
                        this.frontEndService.getNameStatsByMonthForName(name, isBoy)
                            .success(function (stats) { return _this.$scope.nameStatsByMonth = stats; });
                        this.frontEndService.getNameStatsByRegionForName(name, isBoy)
                            .success(function (stats) { return _this.$scope.nameStatsByRegion = stats; });
                        this.$scope.getNameCountForYearMonth = function (year, month) { return _this.getNameCountForYearMonth(year, month); };
                        this.$scope.getNameCountForYearRegion = function (year, region) { return _this.getNameCountForYearRegion(year, region); };
                        this.$scope.getNameCountForYearCountry = function (year, country) { return _this.getNameCountForYearCountry(year, country); };
                        this.$scope.floor = function (x) { return Math.floor(x); };
                        this.$scope.close = function () { return _this.$uibModalInstance.dismiss(); };
                    }
                    SpecificStatsController.prototype.addPercent = function (stats) {
                        var _this = this;
                        if (!this.nameSummary)
                            return;
                        stats.forEach(function (stat) {
                            var summary = _this.nameSummary.filter(function (s) { return s.year == stat.year && s.isBoy == stat.isBoy; });
                            if (summary.length == 0)
                                stat.percent = 100;
                            else
                                stat.percent = 100 * stat.count / summary[0].count;
                        });
                    };
                    SpecificStatsController.prototype.getNameCountForYearMonth = function (year, month) {
                        if (!this.$scope.nameStatsByMonth)
                            return NaN;
                        var stat = this.$scope.nameStatsByMonth.filter(function (s) { return s.year == year && s.month == month; });
                        return stat.length > 0 ? stat[0].count : NaN;
                    };
                    SpecificStatsController.prototype.getNameCountForYearRegion = function (year, region) {
                        if (!this.$scope.nameStatsByRegion)
                            return NaN;
                        var stat = this.$scope.nameStatsByRegion.filter(function (s) { return s.year == year && s.region == region; });
                        return stat.length > 0 ? stat[0].count : null;
                    };
                    SpecificStatsController.prototype.getNameCountForYearCountry = function (year, country) {
                        if (!this.$scope.nameStatsByCountry)
                            return NaN;
                        var stat = this.$scope.nameStatsByCountry.filter(function (s) { return s.year == year && s.country == country; });
                        return stat.length > 0 ? stat[0].count : NaN;
                    };
                    return SpecificStatsController;
                }());
                specific.SpecificStatsController = SpecificStatsController;
            })(specific = stats_2.specific || (stats_2.specific = {}));
        })(stats = names.stats || (names.stats = {}));
    })(names = main.names || (main.names = {}));
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
        var NamesController = /** @class */ (function () {
            function NamesController($scope, $uibModal, frontEndService, userService, errorService) {
                var _this = this;
                this.$scope = $scope;
                this.$uibModal = $uibModal;
                this.frontEndService = frontEndService;
                this.userService = userService;
                this.errorService = errorService;
                this.$scope.addCurrentNameGirl = function (name) { return _this.addCurrentNameGirl(name); };
                this.$scope.addCurrentNameBoy = function (name) { return _this.addCurrentNameBoy(name); };
                this.$scope.deleteName = function (entry) { return _this.deleteName(entry); };
                this.$scope.isNameInvalid = function (name) { return _this.isNameInvalid(name); };
                // A name is "new" if it was suggested in the last 3 days
                this.$scope.isNew = function (entry) {
                    var suggestedDate = moment({
                        year: entry.suggestedDate.year,
                        month: (entry.suggestedDate.month - 1),
                        date: entry.suggestedDate.day,
                    });
                    return moment.utc().valueOf() - suggestedDate.valueOf() < 1 * 24 * 60 * 60 * 1000;
                };
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
                // Pop-up the general name stats page
                this.$scope.showGeneralNameStats = function (isBoy) {
                    _this.$uibModal.open({
                        animation: true,
                        templateUrl: '/scripts/main/names/stats/general/generalstats.view.html',
                        controller: main.names.stats.general.GeneralStatsController,
                        controllerAs: 'vm',
                        size: 'lg',
                        resolve: {
                            isBoy: function () { return isBoy; }
                        }
                    });
                };
                // Pop-up the specific name stats page
                this.$scope.showSpecificNameStats = function (name, isBoy) {
                    _this.$uibModal.open({
                        animation: true,
                        templateUrl: "/scripts/main/names/stats/specific/specificstats.view.html",
                        controller: main.names.stats.specific.SpecificStatsController,
                        controllerAs: 'vm',
                        size: 'lg',
                        resolve: {
                            name: function () { return name; },
                            isBoy: function () { return isBoy; }
                        }
                    });
                };
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
            NamesController.prototype.addCurrentNameGirl = function (name) {
                var _this = this;
                name = name.trim();
                var suggestedForUserId = this.selectedFriend ? this.selectedFriend.userId : this.user.userId;
                this.frontEndService.putName(name, false, suggestedForUserId)
                    .error(function (error) { return _this.errorService.raiseError("Failed to add girl's name", error); })
                    .success(function (response) {
                    _this.$scope.girlsNames.push(response);
                    _this.user.babyNames.push(response);
                    _this.$scope.currentNameGirl = "";
                });
            };
            NamesController.prototype.addCurrentNameBoy = function (name) {
                var _this = this;
                name = name.trim();
                var suggestedForUserId = this.selectedFriend ? this.selectedFriend.userId : this.user.userId;
                this.frontEndService.putName(name, true, suggestedForUserId)
                    .error(function (error) { return _this.errorService.raiseError("Failed to add boy's name", error); })
                    .success(function (response) {
                    _this.$scope.boysNames.push(response);
                    _this.user.babyNames.push(response);
                    _this.$scope.currentNameBoy = "";
                });
            };
            NamesController.prototype.deleteName = function (entry) {
                var _this = this;
                this.frontEndService.deleteName(entry.nameId)
                    .success(function (response) {
                    _this.user.babyNames = _this.user.babyNames.filter(function (e) { return e != entry; });
                    if (entry.isBoy) {
                        _this.$scope.boysNames = _this.$scope.boysNames.filter(function (e) { return e != entry; });
                    }
                    else {
                        _this.$scope.girlsNames = _this.$scope.girlsNames.filter(function (e) { return e != entry; });
                    }
                });
            };
            return NamesController;
        }());
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
        var NavController = /** @class */ (function () {
            function NavController($scope, $uibModal, $locale, frontEndService, userService, stateService, errorService) {
                var _this = this;
                this.$scope = $scope;
                this.frontEndService = frontEndService;
                this.userService = userService;
                this.errorService = errorService;
                this.userService.userSetEvent(function (user) { return _this.$scope.user = user; });
                this.$scope.locale = $locale.id;
                this.$scope.confirmFriendRequest = function (friend) { return _this.confirmFriendRequest(friend); };
                this.$scope.ignoreFriendRequest = function (friend) { return _this.ignoreFriendRequest(friend); };
                this.$scope.logout = function () {
                    _this.frontEndService.logout();
                };
                this.$scope.viewFriend = function (friend) {
                    console.log("Changing to friend", friend);
                    stateService.changeState(function (s) { s.selectedUserId = friend.userId; return s; });
                };
                this.$scope.viewUser = function () {
                    console.log("Changing to user");
                    stateService.changeState(function (s) { s.selectedUserId = _this.userService.User.userId; return s; });
                };
                stateService.stateChangedEvent(function (state) {
                    console.log("State changed", state);
                    if (_this.userService.User) {
                        if (state.selectedUserId == _this.userService.User.userId) {
                            _this.userService.Friend = null;
                        }
                        else {
                            for (var i = 0; i < _this.userService.User.friends.length; i++) {
                                if (_this.userService.User.friends[i].userId == state.selectedUserId) {
                                    _this.userService.Friend = _this.userService.User.friends[i];
                                }
                            }
                        }
                    }
                });
                this.$scope.isUserSelected = function () { return _this.userService.Friend == null; };
                this.$scope.isFriendSelected = function (friend) { return _this.userService.Friend == friend; };
                this.$scope.addFriend = function () {
                    $uibModal.open({
                        animation: true,
                        templateUrl: '/scripts/main/share/share.view.html',
                        controller: main.share.ShareController,
                        controllerAs: 'vm',
                        size: 'lg',
                    });
                };
                // Pop-up the account settings screen
                this.$scope.updateAccountSettings = function () {
                    $uibModal.open({
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
                    .error(function (error) { return _this.errorService.raiseError('Failed to confirm friend', error); })
                    .success(function () {
                    _this.frontEndService.getUser()
                        .error(function (error) { return _this.errorService.raiseError('Failed to fetch user after friend confirmation', error); })
                        .success(function (user) { return _this.userService.User = user; });
                });
            };
            NavController.prototype.ignoreFriendRequest = function (friend) {
                var _this = this;
                this.frontEndService.deleteFriend(friend.userId)
                    .error(function (error) { return _this.errorService.raiseError('Failed to ignore friend', error); })
                    .success(function () {
                    _this.frontEndService.getUser()
                        .error(function (error) { return _this.errorService.raiseError('Failed to fetch user after ignoring friend', error); })
                        .success(function (user) { return _this.userService.User = user; });
                });
            };
            return NavController;
        }());
        nav.NavController = NavController;
    })(nav = main.nav || (main.nav = {}));
})(main || (main = {}));
var main;
(function (main) {
    'use strict';
})(main || (main = {}));
/// <reference path="../references.ts" />
var main;
(function (main) {
    'use strict';
    var MainController = /** @class */ (function () {
        function MainController($scope, userService, errorService) {
            $scope.isLoggedIn = false;
            $scope.isError = false;
            // Successfully fetching a user implies the logged-in state is true
            userService.userSetEvent(function (user) { return $scope.isLoggedIn = (user != null); });
            // Set and clear the error message
            errorService.errorEvent({
                onErrorSet: function (url, description, error) { return $scope.isError = true; },
                onErrorClear: function () { return $scope.isError = false; }
            });
        }
        // Sub-components of the "Main" view
        MainController.navDirective = {
            name: 'navBar',
            controller: main.nav.NavController,
            scope: {},
            restrict: 'E',
            replace: true,
            templateUrl: '/scripts/main/nav/nav.view.html'
        };
        MainController.progressDirective = {
            name: 'pregnancyProgress',
            controller: main.progress.ProgressController,
            scope: {},
            restrict: 'E',
            replace: true,
            templateUrl: '/scripts/main/progress/progress.view.html'
        };
        MainController.namesDirective = {
            name: 'names',
            controller: main.names.NamesController,
            scope: {},
            restrict: 'E',
            replace: true,
            templateUrl: '/scripts/main/names/names.view.html'
        };
        // Login
        MainController.loginDirective = {
            name: 'login',
            controller: login.LoginController,
            scope: {},
            restrict: 'E',
            replace: true,
            templateUrl: '/scripts/login/login.view.html'
        };
        // Error
        MainController.errorDirective = {
            name: 'error',
            controller: error.ErrorController,
            scope: {},
            restrict: 'E',
            replace: true,
            templateUrl: '/scripts/error/error.view.html'
        };
        MainController.directives = [
            MainController.navDirective,
            MainController.progressDirective,
            MainController.namesDirective,
            MainController.loginDirective,
            MainController.errorDirective
        ];
        return MainController;
    }());
    main.MainController = MainController;
})(main || (main = {}));
/// <reference path="typings/tsd.d.ts" />
/// <reference path="models/local-date.ts" />
/// <reference path="models/wrapped-baby-name.ts" />
/// <reference path="models/wrapped-friend.ts" />
/// <reference path="models/wrapped-user.ts" />
/// <reference path="models/name-stat-models.ts" />
/// <reference path="services/error-service.ts" />
/// <reference path="services/front-end-service.ts" />
/// <reference path="services/user-service.ts" />
/// <reference path="services/state-service.ts" />
/// <reference path="error/error.model.ts" />
/// <reference path="error/error.controller.ts" />
/// <reference path="login/login.model.ts" />
/// <reference path="login/login.controller.ts" />
/// <reference path="main/share/share.model.ts" />
/// <reference path="main/share/share.controller.ts" />
/// <reference path="main/account/account.model.ts" />
/// <reference path="main/account/account.controller.ts" />
/// <reference path="main/progress/progress.model.ts" />
/// <reference path="main/progress/progress.controller.ts" />
/// <reference path="main/names/stats/general/generalstats.model.ts" />
/// <reference path="main/names/stats/general/generalstats.controller.ts" />
/// <reference path="main/names/stats/specific/specificstats.model.ts" />
/// <reference path="main/names/stats/specific/specificstats.controller.ts" />
/// <reference path="main/names/names.model.ts" />
/// <reference path="main/names/names.controller.ts" />
/// <reference path="main/nav/nav.model.ts" />
/// <reference path="main/nav/nav.controller.ts" />
/// <reference path="main/main.model.ts" />
/// <reference path="main/main.controller.ts" />
/// <reference path="app.ts" />
/// <reference path="references.ts" />
var App;
(function (App) {
    var app = angular.module('pregnaware', ['ngCookies', 'ngAnimate', 'ui.bootstrap']);
    app.service('errorService', services.ErrorService);
    app.service('frontEndService', services.FrontEndService);
    app.service('userService', services.UserService);
    app.service('stateService', services.StateService);
    // The root directive to launch the app
    app.directive('root', function () { return ({
        controller: main.MainController,
        restrict: 'E',
        replace: true,
        templateUrl: '/scripts/main/main.view.html'
    }); });
    // Add the directives required by the main view
    main.MainController.directives.forEach(function (d) { return app.directive(d.name, function () { return d; }); });
})(App || (App = {}));
//# sourceMappingURL=app.js.map