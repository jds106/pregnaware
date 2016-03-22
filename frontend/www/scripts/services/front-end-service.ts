/// <reference path="../references.ts" />
module services {

    'use strict';

    import WrappedUser = models.WrappedUser;
    import WrappedFriend = models.WrappedFriend;
    import WrappedBabyName = models.WrappedBabyName;
    import LocalDate = models.LocalDate;

    import NameStat = models.NameStat;
    import NameSummaryStat = models.NameSummaryStat;
    import NameStatByCountry = models.NameStatByCountry;
    import NameStatByMonth = models.NameStatByMonth;
    import NameStatByRegion = models.NameStatByRegion;

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
                .error(error => this.routeService.errorPage('Login failed', error))
                .success((sessionId: string) => {
                    this.sessionId = sessionId;
                    this.$cookies.put(this.sessionIdKey, sessionId);
                    this.getUser()
                        .error(error => this.routeService.errorPage('Login [fetch user] failed', error))
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

        public newUser(displayName: string, email: string, password: string) {
            this.$http.post(
                this.getUrl('user'), { displayName: displayName, email: email, password: password}, this.getHeaders())
                .error(error => this.routeService.errorPage('New user failed', error))
                .success((sessionId: string) => {
                    this.sessionId = sessionId;
                    this.$cookies.put(this.sessionIdKey, sessionId);
                    this.getUser()
                        .error(error => this.routeService.errorPage('New user [fetch user] failed', error))
                        .success((user: WrappedUser) => {
                            this.userService.User = user;
                            this.routeService.mainPage();
                        })
                });
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

        /* --- Name stats --- */

        private toGender(isBoy: boolean) { return isBoy ? 'boy' : 'girl'; }

        public getNameStatsYears() : ng.IHttpPromise<number[]> {
            return this.$http.get(this.getUrl('namestats/meta/years'), this.getHeaders())
        }

        public getNameStatsCount() : ng.IHttpPromise<NameSummaryStat[]> {
            return this.$http.get(this.getUrl('namestats/meta/count'), this.getHeaders())
        }

        public getNameStatsCompleteForName(name: string, isBoy: boolean) : ng.IHttpPromise<NameStat[]> {
            var gender = this.toGender(isBoy);
            return this.$http.get(this.getUrl(`namestats/data/${gender}/complete/name/${name}`), this.getHeaders())
        }

        public getNameStatsCompleteForYear(year: number, isBoy: boolean) : ng.IHttpPromise<NameStat[]> {
            var gender = this.toGender(isBoy);
            return this.$http.get(this.getUrl(`namestats/data/${gender}/complete/summary/${year}`), this.getHeaders())
        }

        public getNameStatsByCountryForName(name: string, isBoy: boolean) : ng.IHttpPromise<NameStatByCountry[]> {
            var gender = this.toGender(isBoy);
            return this.$http.get(this.getUrl(`namestats/data/${gender}/country/name/${name}`), this.getHeaders())
        }

        public getNameStatsByCountryForYear(year: number, isBoy: boolean) : ng.IHttpPromise<NameStatByCountry[]> {
            var gender = this.toGender(isBoy);
            return this.$http.get(this.getUrl(`namestats/data/${gender}/country/summary/${year}`), this.getHeaders())
        }

        public getNameStatsByMonthForName(name: string, isBoy: boolean) : ng.IHttpPromise<NameStatByMonth[]> {
            var gender = this.toGender(isBoy);
            return this.$http.get(this.getUrl(`namestats/data/${gender}/month/name/${name}`), this.getHeaders())
        }

        public getNameStatsByMonthForYear(year: number, isBoy: boolean) : ng.IHttpPromise<NameStatByMonth[]> {
            var gender = this.toGender(isBoy);
            return this.$http.get(this.getUrl(`namestats/data/${gender}/month/summary/${year}`), this.getHeaders())
        }

        public getNameStatsByRegionForName(name: string, isBoy: boolean) : ng.IHttpPromise<NameStatByRegion[]> {
            var gender = this.toGender(isBoy);
            return this.$http.get(this.getUrl(`namestats/data/${gender}/region/name/${name}`), this.getHeaders())
        }

        public getNameStatsByRegionForYear(year: number, isBoy: boolean) : ng.IHttpPromise<NameStatByRegion[]> {
            var gender = this.toGender(isBoy);
            return this.$http.get(this.getUrl(`namestats/data/${gender}/region/summary/${year}`), this.getHeaders())
        }
    }
}
