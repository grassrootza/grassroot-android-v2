package za.org.grassroot.android.services.auth;

import android.accounts.Account;
import android.accounts.AccountManager;

import za.org.grassroot.android.ApplicationLoader;

/**
 * Created by luke on 2017/07/26.
 */
public final class GrassrootAuthUtils {

    public static String getToken() {
        AccountManager manager = AccountManager.get(ApplicationLoader.applicationContext);
        Account account = getAccount(manager);
        return account == null ? null : manager.peekAuthToken(account, AuthConstants.AUTH_TOKENTYPE);
    }

    private static Account getAccount(AccountManager manager) {
        Account[] accounts = manager.getAccountsByType(AuthConstants.ACCOUNT_TYPE);
        return accounts.length == 0 ? null : accounts[0];
    }

}
