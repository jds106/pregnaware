/// <reference path="../references.ts" />
module service {
    'use strict';

    import LocalDate = entities.LocalDate;
    import LoginRequest = entities.LoginRequest;
    import NewUserRequest = entities.NewUserRequest;
    import CookieKeys = utils.CookieKeys;

    class FrontEndUrl {
        private static devUrlRoot = 'http://localhost:8601';
        private static prodUrlRoot = '';

        public static getUrl(path: string, sessionId: string = null, userId: number = null) {
            var sessionPart = sessionId ? "sessionId=" + sessionId : "";
            var userIdPart = userId ? "userId=" + userId : "";
            return FrontEndUrl.devUrlRoot
                + path + (sessionId ? "?" + sessionPart : "")
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

        public login(loginRequest : LoginRequest) {
            return this.$http.post(FrontEndUrl.getUrl('/FrontEndSvc/login'), loginRequest);
        }

        public logout() {
            return this.$http.post(FrontEndUrl.getUrl('/FrontEndSvc/logout', this.getSessionId()), {});
        }

        public newUser(newUserRequest: NewUserRequest) {
            return this.$http.put(FrontEndUrl.getUrl('/FrontEndSvc/user'), newUserRequest);
        }

        public getUser(userId: number = null) {
            return this.$http.get(FrontEndUrl.getUrl('/FrontEndSvc/user', this.getSessionId(), userId));
        }

        public getNames(userId: number = null) {
            return this.$http.get(FrontEndUrl.getUrl('/FrontEndSvc/NamingSvc/names', this.getSessionId(), userId));
        }

        public getDueDate(userId: number = null) {
            return this.$http.get(FrontEndUrl.getUrl('/FrontEndSvc/ProgressSvc/progress', this.getSessionId(), userId));
        }

        public putDueDate(dueDate: LocalDate, userId: number = null) {
            return this.$http.put(
                FrontEndUrl.getUrl('/FrontEndSvc/ProgressSvc/progress', this.getSessionId(), userId),
                dueDate)
        }

        public deleteDueDate(userId: number = null) {
            return this.$http.delete(
                FrontEndUrl.getUrl('/FrontEndSvc/ProgressSvc/progress', this.getSessionId(), userId));
        }
    }
}
