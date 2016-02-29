/// <reference path="../references.ts" />
module service {
    import EditUserRequest = entities.EditUserRequest;
    'use strict';

    import User = entities.User;
    import LocalDate = entities.LocalDate;
    import LoginRequest = entities.LoginRequest;
    import AddUserRequest = entities.AddUserRequest;
    import CookieKeys = utils.CookieKeys;

    class FrontEndUrl {
        public static getUrl(path: string, sessionId: string = null) {
            var sessionPart = sessionId ? "sessionId=" + sessionId : "";
            return "/FrontEndSvc/" + path + (sessionId ? "?" + sessionPart : "");
        }
    }

    export class FrontEndSvc {
        private $http : angular.IHttpService;
        private $cookies : angular.cookies.ICookiesService;

        constructor($http: angular.IHttpService, $cookies:angular.cookies.ICookiesService) {
            this.$http = $http;
            this.$cookies = $cookies;
        }

        private getSessionId() {
            return this.$cookies.get(CookieKeys.SessionIdKey);
        }

        /* ---- Login / logout ---- */

        public login(loginRequest : LoginRequest) {
            return this.$http.post(FrontEndUrl.getUrl('login'), loginRequest);
        }

        /* ---- User ---- */

        public newUser(addUserRequest: AddUserRequest) {
            return this.$http.post(FrontEndUrl.getUrl('user'), addUserRequest);
        }

        public getUser(userId: number = null) : angular.IPromise<User> {
            return this.$http.get(FrontEndUrl.getUrl('user', this.getSessionId(), userId))
                .error((errorMsg, errorCode) => )
                .success((user: User, ))
        }

        public editUser(editUserRequest: EditUserRequest) {
            return this.$http.put(FrontEndUrl.getUrl('user', this.getSessionId()), editUserRequest);
        }

        public addFriend(friendEmail: String) {
            return this.$http.put(FrontEndUrl.getUrl('user/friend', this.getSessionId()), {email: friendEmail});
        }

        public deleteFriend(friendEmail: String) {
            return this.$http.put(FrontEndUrl.getUrl('user/friend', this.getSessionId()), {email: friendEmail});
        }

        /** Creates a link to the session created for the new friend */
        public getCreateFriendLink(urlRoot: string, sessionId: string) {
            return urlRoot + '/newfriend' + '?sessionId=' + sessionId;
        }

        /* ---- Progress ---- */

        public putDueDate(dueDate: LocalDate) {
            return this.$http.put(FrontEndUrl.getUrl('progress', this.getSessionId()), dueDate)
        }

        public deleteDueDate(userId: number) {
            return this.$http.delete(FrontEndUrl.getUrl('progress', this.getSessionId()));
        }

        /* ---- Names ---- */

        public putName(name: string, gender: string, userId: number) {
            return this.$http.put(
                FrontEndUrl.getUrl(
                    'NamingSvc/name', this.getSessionId(), userId),
                    { nameId: -1, name: name, gender: gender, suggestedByUserId: userId });
        }

        public deleteName(nameId: number, userId: number) {
            return this.$http.delete(
                FrontEndUrl.getUrl('NamingSvc/name/' + nameId, this.getSessionId(), userId));
        }
    }
}
