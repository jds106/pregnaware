/// <reference path="../references.ts" />
module services {
    'use strict';

    import WrappedUser = models.WrappedUser;
    import WrappedFriend = models.WrappedFriend;
    import WrappedBabyName = models.WrappedBabyName;
    import LocalDate = models.LocalDate;

    // These are the ONLY status codes returned by the app
    export class StatusCodeHandler {
        public OK = 200;

        public BadRequest = 400;
        public NotFound = 404;
        public Conflict = 409;

        public ServiceUnavailable = 503;
    }

    export class FrontEndService {
        private $http : ng.IHttpService;
        private $cookies : ng.cookies.ICookiesService;
        private routeService: RouteService;
        private userService: UserService;

        private sessionId: string;

        private sessionIdKey = "sessionId";

        constructor(
            $http: ng.IHttpService,
            $cookies:ng.cookies.ICookiesService,
            routeService: RouteService,
            userService: UserService) {

            this.$http = $http;
            this.$cookies = $cookies;
            this.userService = userService;
            this.routeService = routeService;

            /** Optimistically fetch the user - tests to see if we are logged in */
            let headers =
                <ng.IRequestShortcutConfig>{ headers: { "X-SessionId" : this.$cookies.get(this.sessionIdKey) } };

            this.$http.get(this.getUrl('user'), headers)
                .error((e) => {
                    this.sessionId = null;
                    this.$cookies.remove(this.sessionIdKey);
                    this.userService.User = null;
                    this.routeService.loginPage();
                })
                .success((user: WrappedUser) => {
                    this.sessionId = this.$cookies.get(this.sessionIdKey);
                    this.userService.User = user;
                    this.routeService.mainPage();
                })
        }

        private getHeaders() : ng.IRequestShortcutConfig {
            var headers: ng.IHttpRequestConfigHeaders = { };

            if (this.sessionId)
                headers = { "X-SessionId" : this.sessionId };

            return { headers: headers };
        }

        private getUrl(path: string) {
            return "/FrontEndSvc/" + path;
        }

        /* ---- Login / Logout ---- */

        public login(email: string, password: string) {
            this.$http.post(this.getUrl('login'), { email: email, password: password }, { })
                .success((sessionId: string) => {
                    this.sessionId = sessionId;
                    this.$cookies.put(this.sessionIdKey, sessionId);
                    this.getUser()
                        .success((user: WrappedUser) => {
                            this.userService.User = user;
                            this.routeService.mainPage();
                        })
                });
        }

        public logout() {
            this.sessionId = null;
            this.$cookies.remove(this.sessionIdKey);
            this.routeService.loginPage();
        }

        /* ---- User ---- */

        public getUser() : ng.IHttpPromise<WrappedUser> {
            return this.$http.get(this.getUrl('user'), this.getHeaders());
        }

        public newUser(displayName: string, email: string, password: string) : ng.IHttpPromise<string> {
            var response = this.$http.post(
                this.getUrl('user'),
                { displayName: displayName, email: email, password: password},
                this.getHeaders());

            response.success((sessionId: string) => {
                this.sessionId = sessionId;
                this.$cookies.put(this.sessionIdKey, sessionId);
            });

            return response;
        }

        public editUser(displayName: string, email: string, password: string) : ng.IHttpPromise<any> {
            return this.$http.put(this.getUrl('user'),
                {displayName: displayName, email: email, password: password},
                this.getHeaders());
        }

        public addFriend(friendEmail: String) : ng.IHttpPromise<WrappedFriend> {
            return this.$http.put(this.getUrl('user/friend'), friendEmail, this.getHeaders());
        }

        public deleteFriend(friendId: number) : ng.IHttpPromise<any> {
            return this.$http.delete(this.getUrl('user/friend/' + friendId), this.getHeaders());
        }

        /* ---- Progress ---- */

        public putDueDate(dueDate: LocalDate) : ng.IHttpPromise<LocalDate> {
            return this.$http.put(this.getUrl('user/duedate'), dueDate, this.getHeaders())
        }

        public deleteDueDate() : ng.IHttpPromise<any> {
            return this.$http.delete(this.getUrl('user/duedate'), this.getHeaders());
        }

        /* ---- Names ---- */

        public putName(name: string, isBoy: boolean, suggestedForUserId: number) : ng.IHttpPromise<WrappedBabyName> {
            return this.$http.put(
                this.getUrl('names/' + suggestedForUserId),
                { name: name, isBoy: isBoy },
                this.getHeaders());
        }

        public deleteName(nameId: number) : ng.IHttpPromise<any> {
            return this.$http.delete(this.getUrl('names/' + nameId), this.getHeaders());
        }

        /* ---- State ---- */

        public getUserState() : ng.IHttpPromise<string> {
            return this.$http.get(this.getUrl('user/state'), this.getHeaders())
        }

        public putUserState(state: string) : ng.IHttpPromise<any> {
            return this.$http.put(this.getUrl('user/state'), state, this.getHeaders())
        }
    }
}
