package com.dantann.backupexample

import com.google.android.gms.drive.events.ChangeEvent
import com.google.android.gms.drive.events.ChangeListener
import com.google.android.gms.drive.events.CompletionEvent
import com.google.android.gms.drive.events.CompletionListener

import java.util.concurrent.CopyOnWriteArrayList

class MyDriveEventService private constructor() {

    private val mEventListeners = CopyOnWriteArrayList<EventListener>()

    fun addEventListener(listener: EventListener?) {
        if (listener != null && !mEventListeners.contains(listener)) {
            mEventListeners.add(listener)
        }
    }

    fun removeEventListener(listener: EventListener?) {
        if (listener != null) {
            mEventListeners.remove(listener)
        }
    }

    private fun notifyOnChange(event: ChangeEvent) {
        for (listener in mEventListeners) {
            listener.onChange(event)
        }
    }

    private fun notifyOnCompletion(event: CompletionEvent) {
        for (listener in mEventListeners) {
            listener.onCompletion(event)
        }
    }

    class DriveEventService : com.google.android.gms.drive.events.DriveEventService() {

        override fun onChange(event: ChangeEvent) {
            super.onChange(event)
            instance?.notifyOnChange(event)
        }

        override fun onCompletion(event: CompletionEvent) {
            super.onCompletion(event)
            instance?.notifyOnCompletion(event)
        }

    }

    abstract class EventListener : ChangeListener, CompletionListener

    companion object {

        var instance: MyDriveEventService? = null
            get() {
                if (field == null) {
                    synchronized(MyDriveEventService::class.java) {
                        if (field == null) {
                            field = MyDriveEventService()
                        }
                    }
                }
                return field!!
            }
    }
}
