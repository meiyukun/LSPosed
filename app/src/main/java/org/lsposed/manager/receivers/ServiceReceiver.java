/*
 * This file is part of LSPosed.
 *
 * LSPosed is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LSPosed is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LSPosed.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2020 EdXposed Contributors
 * Copyright (C) 2021 LSPosed Contributors
 */

package org.lsposed.manager.receivers;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import org.lsposed.manager.ConfigManager;
import org.lsposed.manager.adapters.ScopeAdapter;
import org.lsposed.manager.util.ModuleUtil;
import org.lsposed.manager.util.NotificationUtil;

import java.util.List;
import java.util.stream.Collectors;

public class ServiceReceiver extends BroadcastReceiver {

    private static String getPackageName(Intent intent) {
        Uri uri = intent.getData();
        return (uri != null) ? uri.getSchemeSpecificPart() : null;
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        int userId = intent.getIntExtra(Intent.EXTRA_USER, 0);
        String packageName = getPackageName(intent);
        if (packageName == null) {
            return;
        }

        ModuleUtil.InstalledModule module = ModuleUtil.getInstance().reloadSingleModule(packageName, userId);
        if (module == null) {
            return;
        }
        if (intent.getAction().equals("org.lsposed.action.MODULE_NOT_ACTIVATAED")) {
            NotificationUtil.showNotification(context, packageName, module.getAppName(), userId, false);
        } else if (intent.getAction().equals("org.lsposed.action.MODULE_UPDATED")) {
            NotificationUtil.showNotification(context, packageName, module.getAppName(), userId, true);
            //重启最表面的应用
            try {
                List<ScopeAdapter.ApplicationWithEquals> moduleScope = ConfigManager.getModuleScope(module.packageName);
                List<ActivityManager.RecentTaskInfo> recentTasks = ConfigManager.getRecentTasks(1, 0, userId);
                if (!recentTasks.isEmpty()){
                    ActivityManager.RecentTaskInfo recentTaskInfo = recentTasks.get(0);
                    String topPackageName = recentTaskInfo.topActivity.getPackageName();
                    if (!topPackageName.equals("android")){
                        for (ScopeAdapter.ApplicationWithEquals app : moduleScope) {
                            if (app.packageName.equals(topPackageName) && app.userId == userId) {
                                ConfigManager.reLaunchApp(topPackageName, app.userId);
                                break;
                            }
                        }
                    }
                }
            } catch (Throwable ignored) {
            }

        }

        var enabled = "org.lsposed.action.MODULE_UPDATED".equals(intent.getAction());
        var systemModule = intent.getBooleanExtra("systemModule", false);
        NotificationUtil.showNotification(context, packageName, module.getAppName(), userId, enabled, systemModule);
    }
}
