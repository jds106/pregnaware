/// <reference path="../references.ts" />
module service {
    import EditUserRequest = entities.EditUserRequest;
    'use strict';

    import User = entities.User;
    import LocalDate = entities.LocalDate;
    import LoginRequest = entities.LoginRequest;
    import NewUserRequest = entities.NewUserRequest;
    import CookieKeys = utils.CookieKeys;

    class FrontEndUrl {
        //private static urlRoot = 'http://localhost:8601';

        public static getUrl(path: string, sessionId: string = null, userId: number = null) {
            var sessionPart = sessionId ? "sessionId=" + sessionId : "";
            var userIdPart = userId ? "userId=" + userId : "";
            //return FrontEndUrl.urlRoot
            return "/FrontEndSvc/" + path + (sessionId ? "?" + sessionPart : "")
                + (userId ? "&" + userIdPart : "");
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

        public logout() {
            return this.$http.post(FrontEndUrl.getUrl('logout', this.getSessionId()), {});
        }

        /* ---- User ---- */

        public newUser(newUserRequest: NewUserRequest) {
            return this.$http.put(FrontEndUrl.getUrl('user'), newUserRequest);
        }

        public getUser(userId: number = null) {
            return this.$http.get(FrontEndUrl.getUrl('user', this.getSessionId(), userId));
        }

        public editUser(editUserRequest: EditUserRequest) {
            return this.$http.put(FrontEndUrl.getUrl('editUser', this.getSessionId()), editUserRequest);
        }

        public findUser(email: string) {
            return this.$http.get(FrontEndUrl.getUrl('UserSvc/findUser/' + email, this.getSessionId()));
        }

        public addFriend(friend: User) {
            return this.$http.put(FrontEndUrl.getUrl('friend', this.getSessionId()), friend);
        }

        public createFriend(friendEmail: string) {
            return this.$http.put(FrontEndUrl.getUrl('createFriend', this.getSessionId()), {email: friendEmail});
        }

        /** Creates a link to the session created for the new friend */
        public getCreateFriendLink(urlRoot: string, sessionId: string) {
            return urlRoot + '/newfriend' + '?sessionId=' + sessionId;
        }

        /* ---- Progress ---- */

        public getDueDate(userId: number) {
            return this.$http.get(FrontEndUrl.getUrl('ProgressSvc/progress', this.getSessionId(), userId));
        }

        public putDueDate(dueDate: LocalDate, userId: number) {
            return this.$http.put(
                FrontEndUrl.getUrl('ProgressSvc/progress', this.getSessionId(), userId),
                dueDate)
        }

        public deleteDueDate(userId: number) {
            return this.$http.delete(
                FrontEndUrl.getUrl('ProgressSvc/progress', this.getSessionId(), userId));
        }

        /* ---- Names ---- */

        public getNames(userId: number) {
            return this.$http.get(FrontEndUrl.getUrl('NamingSvc/names', this.getSessionId(), userId));
        }

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
