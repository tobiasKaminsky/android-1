/*
 *
 * Nextcloud Android client application
 *
 * @author Tobias Kaminsky
 * Copyright (C) 2020 Tobias Kaminsky
 * Copyright (C) 2020 Nextcloud GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.owncloud.android.ui.activity;

import android.Manifest;

import com.owncloud.android.AbstractIT;
import com.owncloud.android.lib.resources.notifications.models.Notification;
import com.owncloud.android.lib.resources.notifications.models.RichObject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;

@RunWith(AndroidJUnit4.class)
public class NotificationsActivityIT extends AbstractIT {
    @Rule public IntentsTestRule<NotificationsActivity> activityRule = new IntentsTestRule<>(NotificationsActivity.class,
                                                                                             true,
                                                                                             false);

    @Rule
    public final GrantPermissionRule permissionRule = GrantPermissionRule.grant(
        Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Test
    public void openDrawer() throws InterruptedException {
        super.openDrawer(activityRule);
    }

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void open() throws Throwable {
        List<Notification> notifications = new ArrayList<>();
        notifications.add(new Notification(1,
                                           "files",
                                           "test",
                                           new Date(),
                                           "",
                                           "",
                                           "Test",
                                           "",
                                           new HashMap<String, RichObject>(),
                                           "",
                                           "",
                                           new HashMap<String, RichObject>(),
                                           "",
                                           "",
                                           null));

        NotificationsActivity sut = spy(activityRule.launchActivity(null));

        doNothing().when(sut).fetchAndSetData();

        // still GetNotificationsRemoteOperation is called (verify via logat)

        sut.populateList(notifications);

    }
}
