/// <reference path="../references.ts" />

module Utils {
    export class WebRoot {

        /** The cookie key used to store the current session */
        public static SessionIdKey: string = "sessionId";

        private static devUrlRoot = 'http://localhost:8601';
        private static prodUrlRoot = '';

        public static Url(path: string, sessionId: string = null) {
            return this.devUrlRoot + path + (sessionId ? "?" + WebRoot.SessionIdKey + "=" + sessionId : "");
        }
    }
}