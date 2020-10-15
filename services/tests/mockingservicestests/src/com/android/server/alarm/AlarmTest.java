/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.server.alarm;

import static android.app.AlarmManager.ELAPSED_REALTIME;

import static com.android.server.alarm.Alarm.APP_STANDBY_POLICY_INDEX;
import static com.android.server.alarm.Alarm.REQUESTER_POLICY_INDEX;
import static com.android.server.alarm.Constants.TEST_CALLING_PACKAGE;
import static com.android.server.alarm.Constants.TEST_CALLING_UID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import android.app.PendingIntent;
import android.platform.test.annotations.Presubmit;

import androidx.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@Presubmit
@RunWith(AndroidJUnit4.class)
public class AlarmTest {

    private Alarm createDefaultAlarm(long requestedElapsed, long windowLength) {
        return new Alarm(ELAPSED_REALTIME, 0, requestedElapsed, windowLength, 0,
                mock(PendingIntent.class), null, null, null, 0, null, TEST_CALLING_UID,
                TEST_CALLING_PACKAGE);
    }

    @Test
    public void initSetsOnlyRequesterPolicy() {
        final Alarm a = createDefaultAlarm(4567, 2);
        assertEquals(4567, a.getPolicyElapsed(REQUESTER_POLICY_INDEX));
        assertEquals(0, a.getPolicyElapsed(APP_STANDBY_POLICY_INDEX));
    }

    @Test
    public void whenElapsed() {
        final Alarm a = createDefaultAlarm(0, 0);

        a.setPolicyElapsed(REQUESTER_POLICY_INDEX, 4);
        a.setPolicyElapsed(APP_STANDBY_POLICY_INDEX, 10);
        assertEquals(10, a.getWhenElapsed());

        a.setPolicyElapsed(REQUESTER_POLICY_INDEX, 12);
        assertEquals(12, a.getWhenElapsed());

        a.setPolicyElapsed(REQUESTER_POLICY_INDEX, 7);
        assertEquals(10, a.getWhenElapsed());

        a.setPolicyElapsed(APP_STANDBY_POLICY_INDEX, 2);
        assertEquals(7, a.getWhenElapsed());

        a.setPolicyElapsed(APP_STANDBY_POLICY_INDEX, 7);
        assertEquals(7, a.getWhenElapsed());
    }

    @Test
    public void maxWhenElapsed() {
        final Alarm a = createDefaultAlarm(10, 12);
        assertEquals(22, a.getMaxWhenElapsed());

        a.setPolicyElapsed(REQUESTER_POLICY_INDEX, 15);
        assertEquals(27, a.getMaxWhenElapsed());

        a.setPolicyElapsed(REQUESTER_POLICY_INDEX, 2);
        assertEquals(14, a.getMaxWhenElapsed());

        a.setPolicyElapsed(APP_STANDBY_POLICY_INDEX, 5);
        assertEquals(14, a.getMaxWhenElapsed());

        a.setPolicyElapsed(APP_STANDBY_POLICY_INDEX, 16);
        assertEquals(16, a.getMaxWhenElapsed());

        a.setPolicyElapsed(APP_STANDBY_POLICY_INDEX, 12);
        assertEquals(14, a.getMaxWhenElapsed());
    }

    @Test
    public void setPolicyElapsed() {
        final Alarm exactAlarm = createDefaultAlarm(10, 0);

        assertTrue(exactAlarm.setPolicyElapsed(REQUESTER_POLICY_INDEX, 4));
        assertTrue(exactAlarm.setPolicyElapsed(APP_STANDBY_POLICY_INDEX, 10));

        assertFalse(exactAlarm.setPolicyElapsed(REQUESTER_POLICY_INDEX, 8));
        assertFalse(exactAlarm.setPolicyElapsed(REQUESTER_POLICY_INDEX, 10));
        assertFalse(exactAlarm.setPolicyElapsed(APP_STANDBY_POLICY_INDEX, 8));

        assertTrue(exactAlarm.setPolicyElapsed(REQUESTER_POLICY_INDEX, 7));

        final Alarm inexactAlarm = createDefaultAlarm(10, 5);

        assertTrue(inexactAlarm.setPolicyElapsed(REQUESTER_POLICY_INDEX, 4));
        assertTrue(inexactAlarm.setPolicyElapsed(APP_STANDBY_POLICY_INDEX, 10));

        // whenElapsed won't change, but maxWhenElapsed will.
        assertTrue(inexactAlarm.setPolicyElapsed(REQUESTER_POLICY_INDEX, 8));
        assertTrue(inexactAlarm.setPolicyElapsed(REQUESTER_POLICY_INDEX, 10));

        assertFalse(inexactAlarm.setPolicyElapsed(APP_STANDBY_POLICY_INDEX, 8));
    }
}
