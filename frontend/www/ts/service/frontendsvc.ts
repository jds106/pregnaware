/// <reference path="../references.ts" />
module service {
    'use strict';

    import WrappedUser = entities.WrappedUser;
    import WrappedFriend = entities.WrappedFriend;
    import WrappedBabyName = entities.WrappedBabyName;

    import LoginRequest = entities.LoginRequest;
    import AddUserRequest = entities.AddUserRequest;
    import EditUserRequest = entities.EditUserRequest;

    import LocalDate = entities.LocalDate;

    import CookieKeys = utils.CookieKeys;

    class FrontEndUrl {
        public static getUrl(path: string, sessionId: string = null) {
            var sessionPart = sessionId ? "sessionId=" + sessionId : "";
            return "/FrontEndSvc/" + path + (sessionId ? "?" + sessionPart : "");
        }
    }

    // These are the ONLY status codes returned by the app
    class StatusCodeHandler {
        public OK = 200;

        public BadRequest = 400;
        public NotFound = 404;
        public Conflict = 409;

        public ServiceUnavailable = 503;
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

        /* ---- Login ---- */

        public login(loginRequest : LoginRequest) : angular.IHttpPromise<string> {
            return this.$http.post(FrontEndUrl.getUrl('login'), loginRequest);
        }

        /* ---- User ---- */

        public getUser() : angular.IHttpPromise<WrappedUser> {
            return this.$http.get(FrontEndUrl.getUrl('user'));
        }

        public newUser(addUserRequest: AddUserRequest) : angular.IHttpPromise<string> {
            return this.$http.post(FrontEndUrl.getUrl('user'), addUserRequest);
        }

        public editUser(editUserRequest: EditUserRequest) : angular.IHttpPromise<any> {
            return this.$http.put(FrontEndUrl.getUrl('user', this.getSessionId()), editUserRequest);
        }

        public addFriend(friendEmail: String) : angular.IHttpPromise<WrappedFriend> {
            return this.$http.put(FrontEndUrl.getUrl('user/friend', this.getSessionId()), {email: friendEmail});
        }

        public deleteFriend(friendEmail: String) : angular.IHttpPromise<any> {
            return this.$http.delete(FrontEndUrl.getUrl('user/friend', this.getSessionId()), {email: friendEmail});
        }

        /** Creates a link to the session created for the new friend */
        public getCreateFriendLink(urlRoot: string, sessionId: string) : string {
            return urlRoot + '/newfriend' + '?sessionId=' + sessionId;
        }

        /* ---- Progress ---- */

        public putDueDate(dueDate: LocalDate) : angular.IHttpPromise<LocalDate> {
            return this.$http.put(FrontEndUrl.getUrl('progress', this.getSessionId()), dueDate)
        }

        public deleteDueDate() : angular.IHttpPromise<any> {
            return this.$http.delete(FrontEndUrl.getUrl('progress', this.getSessionId()));
        }

        /* ---- Names ---- */

        public putName(name: string, isBoy: boolean, suggestedForUserId: number) : angular.IHttpPromise<WrappedBabyName> {
            return this.$http.put(
                FrontEndUrl.getUrl('NamingSvc/name/' + suggestedForUserId, this.getSessionId()),
                { name: name, isBoy: isBoy });
        }

        public deleteName(nameId: number) : angular.IHttpPromise<any> {
            return this.$http.delete(
                FrontEndUrl.getUrl('NamingSvc/name/' + nameId, this.getSessionId()));
        }
    }
}
