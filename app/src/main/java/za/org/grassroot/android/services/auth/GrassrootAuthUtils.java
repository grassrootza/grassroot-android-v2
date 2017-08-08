package za.org.grassroot.android.services.auth;

import android.accounts.Account;
import android.accounts.AccountManager;

import za.org.grassroot.android.GrassrootApplication;

public final class GrassrootAuthUtils {

    public static final String AUTH_CLIENT_TYPE = "ANDROID";

    public static boolean isLoggedIn() {
        return getAccount(AccountManager.get(GrassrootApplication.applicationContext)) != null;
    }

    public static String getToken() {
        AccountManager manager = AccountManager.get(GrassrootApplication.applicationContext);
        Account account = getAccount(manager);
        return account == null ? null : manager.peekAuthToken(account, AuthConstants.AUTH_TOKENTYPE);
    }

    private static Account getAccount(AccountManager manager) {
        Account[] accounts = manager.getAccountsByType(AuthConstants.ACCOUNT_TYPE);
        return accounts.length == 0 ? null : accounts[0];
    }
}
