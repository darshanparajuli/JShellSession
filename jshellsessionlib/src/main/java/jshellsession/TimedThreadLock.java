/*
 * Copyright (c) 2017 Darshan Parajuli
 */

package jshellsession;

class TimedThreadLock {

    private final Object mLock = new Object();
    private volatile boolean mLockReleased;

    TimedThreadLock() {
        mLockReleased = false;
    }

    void lock() {
        synchronized (mLock) {
            while (!mLockReleased) {
                try {
                    mLock.wait();
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    void lock(long duration /* in millis */) {
        if (duration < 0) {
            throw new IllegalStateException("duration is less than 0");
        } else if (duration == 0) {
            lock();
        } else {
            synchronized (mLock) {
                final long startTime = System.currentTimeMillis();
                while (duration > 0) {
                    try {
                        mLock.wait(duration);
                    } catch (InterruptedException ignored) {
                    }

                    // handle interruptions
                    if (mLockReleased) {
                        break;
                    } else {
                        final long elapsedTime = System.currentTimeMillis() - startTime;
                        duration = Math.max(duration - elapsedTime, 0);
                    }
                }
                mLockReleased = false;
            }
        }
    }

    void unlock() {
        synchronized (mLock) {
            mLockReleased = true;
            mLock.notify();
        }
    }

}
